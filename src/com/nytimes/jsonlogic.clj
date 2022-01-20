(ns com.nytimes.jsonlogic
  (:refer-clojure :rename {apply core-apply})
  (:require
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [clojure.walk :as walk])
  (:import
   (clojure.lang Symbol)
   (java.util Collection List Map)))

(defn dispatch-operate [op _env]
  (log/debug :op (pr-str op))
  (if (and (map? op) (= 1 (count op)))
    (-> op keys first symbol)
    (type op)))

(defmulti operate dispatch-operate)

(defn update-keys
  [m f]
  ;; TODO remove once clojure 1.11 is released
  (let [ret (persistent!
             (reduce-kv (fn [acc k v] (assoc! acc (f k) v))
                        (transient {})
                        m))]
                        (with-meta ret (meta m))))

(defn parse
  [rule]
  (walk/postwalk
   (fn [node]
     (if (map? node)
       (update-keys node symbol) node))
   rule))

(defn apply
  "Apply a jsonlogic rule to data. If no data is given, it defaults to an empty
  map."
  ([rule]
   (apply rule {}))
  ([rule env]
   (operate (parse rule) env)))

(defn seek [pred coll] (first (filter #(pred %) coll)))

;; ----------------------------------------------------------------------
;;  Coercion

(defmulti coerce
  "Coerce data types according to jsonlogic rules."
  (fn [val to-type]
    (if (= (type val) to-type)
      ::identity
      [(type val) to-type])))

(defmethod coerce ::identity           [x _] x)
(defmethod coerce [Boolean Double]     [x _] (if x 1.0 0.0))
(defmethod coerce [Boolean Float]      [x _] (float (if x 1.0 0.0)))
(defmethod coerce [Boolean Integer]    [x _] (int (if x 1 0)))
(defmethod coerce [Boolean Long]       [x _] (if x 1 0))
(defmethod coerce [Boolean String]     [x _] (str x))
(defmethod coerce [Collection Boolean] [x _] (not (empty? x)))
(defmethod coerce [Map Boolean]        [x _] true)
(defmethod coerce [Double Boolean]     [x _] (if (zero? x) false true))
(defmethod coerce [Double Float]       [x _] (float x))
(defmethod coerce [Double Integer]     [x _] (int x))
(defmethod coerce [Double Long]        [x _] (long x))
(defmethod coerce [Double String]      [x _] (str x))
(defmethod coerce [Float Boolean]      [x _] (if (zero? x) false true))
(defmethod coerce [Float Double]       [x _] (double x))
(defmethod coerce [Float Integer]      [x _] (int x))
(defmethod coerce [Float Long]         [x _] (long x))
(defmethod coerce [Float String]       [x _] (str x))
(defmethod coerce [Integer Boolean]    [x _] (if (zero? x) false true))
(defmethod coerce [Integer Double]     [x _] (double x))
(defmethod coerce [Integer Float]      [x _] (float x))
(defmethod coerce [Integer Long]       [x _] (long x))
(defmethod coerce [Integer String]     [x _] (str x))
(defmethod coerce [Long Boolean]       [x _] (if (zero? x) false true))
(defmethod coerce [Long Double]        [x _] (double x))
(defmethod coerce [Long Float]         [x _] (float x))
(defmethod coerce [Long Integer]       [x _] (int x))
(defmethod coerce [Long String]        [x _] (str x))
(defmethod coerce [Object String]      [x _] (String/valueOf x))
(defmethod coerce [String Boolean]     [x _] (not (str/blank? x)))
(defmethod coerce [String Double]      [x _] (Double/parseDouble x))
(defmethod coerce [String Float]       [x _] (Float/parseFloat x))
(defmethod coerce [String Integer]     [x _] (Integer/parseInt x))
(defmethod coerce [String Long]        [x _] (Long/parseLong x))
(defmethod coerce [nil Boolean]        [_ _] false)

(defn coerce-coll
  [coll]
  (when-let [[x & xs] coll]
    (let [target-type (type x)]
      (into [x] (map #(coerce % target-type) xs)))))

(defn coerce-double [x] (coerce x Double))

(defn coerce-numeric
  [xs]
  (when-let [target (seek number? xs)]
    (into [] (mapv #(coerce % (type target)) xs))))

(defn bool [x] (coerce x Boolean))
(defn falsy? [x]  (false? (bool x)))
(defn truthy? [x] (true? (bool x)))

;; ----------------------------------------------------------------------
;; Operators

(defmethod operate :default
  [rule _]
  (let [msg (str "Unrecognized operation "
                 (if (map? rule)
                   (first (keys rule))
                   rule))]
    (throw (IllegalArgumentException. msg))))

(derive Number ::primitive)
(derive Boolean ::primitive)
(derive String ::primitive)
(derive Symbol ::primitive)

(defmethod operate ::primitive [x _] x)
(defmethod operate nil         [x _] nil)
(defmethod operate List        [x env] (mapv #(operate % env) x))
(defmethod operate Map         [x env] (operate x env))

(def separator #"\.")

(defn path [s]
  (cond
    (= [] s)    []
    (nil? s)    []
    (string? s) (str/split s separator)
    (int? s)    [s]
    :else       [s]))

(defn get-path
  ([env ks]
   (get-path env ks nil))
  ([env ks not-found]
   (loop [sentinel (Object.)
          env      env
          ks       (seq ks)]
    (if ks
      (let [k   (cond-> (first ks)
                  (vector? env) (coerce Long))
            env (get env k sentinel)]
        (if (identical? sentinel env)
          not-found
          (recur sentinel env (next ks))))
      env))))

(defn has-path?
  [env ks]
  (let [sentinel (Object.)]
    (not= sentinel (get-path env ks sentinel))))

(defmethod operate 'var
  [{args 'var} env]
  (let [args         (operate args env)
        [ks default] (cond
                       (= "" args)    [args env]
                       (vector? args) (let [[ks default] args]
                                        [ks default])
                       :else          [args nil])]
    (get-path env (path ks) default)))

(defmethod operate 'missing
  [{args 'missing} env]
  (let [args (operate args env)
        args (if (string? args) [args] args)]
    (reduce (fn [accm arg]
              (if (has-path? env (path (operate arg env)))
                accm
                (conj accm arg)))
            []
            args)))

(defmethod operate 'missing_some
  [{args 'missing_some} env]
  (let [[threshold keyseq] (operate args env)
        missing            (operate {'missing keyseq} env)
        total-found        (- (count keyseq) (count missing))]
    (if (>= total-found threshold)
      []
      missing)))

(defmethod operate 'if
  [{args 'if} env]
  ;; split up the args into test-value pairs. if there is no final test, aka an
  ;; else clause, inject true so it always succeeds.
  ;;
  ;; In other words
  ;;    [test-1, value-1, test-2, value-2, value-3]
  ;; becomes
  ;;    [(test-1, value-1), (test-2, value-2), (true, value-3)]
  (let [branches (partition-all 2 (if (odd? (count args))
                                    (concat (butlast args) [true (last args)])
                                    args))
        result   (some (fn [[test value]]
                         (when (bool (operate test env))
                           (reduced value)))
                       branches)]
    (when (reduced? result)
      (operate @result env))))

(defmethod operate (symbol "?:")
  [rule env]
  (operate {'if (first (vals rule))} env))

(defmethod operate '==
  [{args '==} env]
  (core-apply = (coerce-coll (operate args env))))

(defmethod operate '===
  [{args '===} env]
  (core-apply = (operate args env)))

(defmethod operate '!=
  [{args '!=} env]
  (core-apply not= (coerce-coll (operate args env))))

(defmethod operate '!==
  [{args '!==} env]
  (core-apply not= (operate args env)))

(defmethod operate '!
  [{arg '!} env]
  (let [arg (operate arg env)]
    (not (bool (if (coll? arg) (first arg) arg)))))

(defmethod operate '!!
  [{arg '!!} env]
  (let [arg (if (coll? arg) (first arg) arg)]
    (bool (operate arg env))))

(defmethod operate 'or
  [{args 'or} env]
  (when-let [args (seq (operate args env))]
    (or (first (filter bool args))
        false)))

(defmethod operate 'and
  [{args 'and} env]
  (when-let [args (seq (operate args env))]
    (reduce (fn [accm val]
              (if (and (bool accm) (bool val))
                val
                (seek falsy? args)))
            args)))

(defmethod operate '>
  [{args '>} env]
  (core-apply > (coerce-numeric (operate args env))))

(defmethod operate '>=
  [{args '>=} env]
  (core-apply >= (coerce-numeric (operate args env))))

(defmethod operate '<
  [{args '<} env]
  (core-apply < (coerce-numeric (operate args env))))

(defmethod operate '<=
  [{args '<=} env]
  (core-apply <= (coerce-numeric (operate args env))))

(defmethod operate 'max
  [{args 'max} env]
  (core-apply max (operate args env)))

(defmethod operate 'min
  [{args 'min} env]
  (core-apply min (operate args env)))

(defmethod operate '+
  [{args '+} env]
  (if (string? args)
    (if (str/includes? args ".")
      (coerce args Double)
      (coerce args Long))
    (core-apply + (coerce-numeric (operate args env)))))

(defmethod operate '-
  [{args '-} env]
  (if (number? args)
    (- args)
    (core-apply - (take 2 (coerce-numeric (operate args env))))))

(defmethod operate '*
  [{args '*} env]
  (core-apply * (coerce-numeric (operate args env))))

(defmethod operate '/
  [{args '/} env]
  (let [result (core-apply / (take 2 (coerce-numeric (operate args env))))]
    (if (ratio? result)
      (double result)
      result)))

(defmethod operate '%
  [{args '%} env]
  (core-apply mod (take 2 (coerce-numeric (operate args env)))))

(defmethod operate 'map
  [{args 'map} env]
  (let [[v f] args]
    (mapv (partial apply f)
          (operate v env))))

(defmethod operate 'reduce
  [{args 'reduce} env]
  (let [current     "current"
        accumulator "accumulator"
        array       (operate (nth args 0) env)
        function    (nth args 1)
        init        (operate (nth args 2) env)
        env         (reduce (fn [ctx val]
                              (let [ctx' (assoc ctx current val)]
                                (assoc ctx' accumulator (operate function (merge env ctx')))))
                            {accumulator init, current nil}
                            array)]
    (get env accumulator)))

(defmethod operate 'filter
  [{args 'filter} env]
  (assert (= 2 (count args)) "filter requires exactly 2 args")
  (let [array     (operate (first args) env)
        predicate (second args)]
    (filter (fn [env] (bool (operate predicate env)))
            array)))

(defmethod operate 'all
  [{args 'all} env]
  (assert (= 2 (count args)) "all requires exactly 2 args")
  (let [array     (operate (first args) env)
        predicate (second args)]
    (and (not (empty? array))
         (every? (partial operate predicate) array))))

(defmethod operate 'none
  [{args 'none} env]
  (assert (= 2 (count args)) "none requires exactly 2 args")
  (let [none?     (fn [pred coll]
                    (cond
                      (nil? (seq coll))         true
                      (not (pred (first coll))) (recur pred (next coll))
                      :else                     false))
        array     (operate (first args) env)
        predicate (second args)]
    (none? (partial operate predicate) array)))

(defmethod operate 'some
  [{args 'some} env]
  (assert (= 2 (count args)) "some requires exactly 2 args")
  (let [array     (operate (first args) env)
        predicate (second args)]
    (bool (some (partial operate predicate) array))))

(defmethod operate 'merge
  [{args 'merge} env]
  (reduce (fn [accm x]
            (if (coll? x)
                (into accm x)
                (conj accm x)))
          []
          (operate (if (coll? args) args [args]) env)))

(defmethod operate 'in
  [{args 'in} env]
  (let [[needle haystack] (operate args env)]
    (if (string? haystack)
      (str/includes? haystack needle)
      (contains? (set haystack) needle))))

(defmethod operate 'cat
  [{args 'cat} env]
  (str/join "" (operate args env)))

(defmethod operate 'substr
  [{args 'substr} env]
  (let [[string start end] (operate args env)
        len                (count string)
        direction          (fn [x] (cond
                                     (nil? x) nil
                                     (>= x 0) :+
                                     :else    :-))]
    (case [(direction start) (direction end)]
      [:+ nil] (subs string start len)
      [:- nil] (subs string (+ len start) len)
      [:+ :+]  (subs string start (+ start end))
      [:+ :-]  (subs string start (+ len end))
      [:- :+]  (subs string (+ len start) (+ len start end))
      [:- :-]  (subs string (+ len start)  (+ len end)))))

  ;; Miscellaneous

(defmethod operate 'log
  [{msg 'log} _env]
  (log/info msg))
