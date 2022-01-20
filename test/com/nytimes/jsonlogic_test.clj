(ns com.nytimes.jsonlogic-test
  (:require
   [clojure.test :as test :refer [deftest testing]]
   [nytimes.jsonlogic :as jsonlogic]
   [testit.core :refer [=> =throws=> fact facts]]))

(deftest jsonlogic-tests
  (testing "# Non-rules get passed through"
    (facts
        (jsonlogic/apply true)      => true
        (jsonlogic/apply false)     => false
        (jsonlogic/apply 17)        => 17
        (jsonlogic/apply 3.14)      => 3.14
        (jsonlogic/apply "apple")   => "apple"
        (jsonlogic/apply nil)       => nil
        (jsonlogic/apply ["a" "b"]) => ["a", "b"]))

  (testing "# Single operator tests"
    (facts
        (jsonlogic/apply {"==" [1 1]})                                                 => true
        (jsonlogic/apply {"==" [1 "1"]})                                               => true
        (jsonlogic/apply {"==" [1 2]})                                                 => false
        (jsonlogic/apply {"===" [1 1]})                                                => true
        (jsonlogic/apply {"===" [1 "1"]})                                              => false
        (jsonlogic/apply {"===" [1 2]})                                                => false
        (jsonlogic/apply {"!=" [1 2]})                                                 => true
        (jsonlogic/apply {"!=" [1 1]})                                                 => false
        (jsonlogic/apply {"!=" [1 "1"]})                                               => false
        (jsonlogic/apply {"!==" [1 2]})                                                => true
        (jsonlogic/apply {"!==" [1 1]})                                                => false
        (jsonlogic/apply {"!==" [1 "1"]})                                              => true
        (jsonlogic/apply {">" [2 1]})                                                  => true
        (jsonlogic/apply {">" [1 1]})                                                  => false
        (jsonlogic/apply {">" [1 2]})                                                  => false
        (jsonlogic/apply {">" ["2" 1]})                                                => true
        (jsonlogic/apply {">=" [2 1]})                                                 => true
        (jsonlogic/apply {">=" [1 1]})                                                 => true
        (jsonlogic/apply {">=" [1 2]})                                                 => false
        (jsonlogic/apply {">=" ["2" 1]})                                               => true
        (jsonlogic/apply {"<" [2 1]})                                                  => false
        (jsonlogic/apply {"<" [1 1]})                                                  => false
        (jsonlogic/apply {"<" [1 2]})                                                  => true
        (jsonlogic/apply {"<" ["1" 2]})                                                => true
        (jsonlogic/apply {"<" [1 2 3]})                                                => true
        (jsonlogic/apply {"<" [1 1 3]})                                                => false
        (jsonlogic/apply {"<" [1 4 3]})                                                => false
        (jsonlogic/apply {"<=" [2 1]})                                                 => false
        (jsonlogic/apply {"<=" [1 1]})                                                 => true
        (jsonlogic/apply {"<=" [1 2]})                                                 => true
        (jsonlogic/apply {"<=" ["1" 2]})                                               => true
        (jsonlogic/apply {"<=" [1 2 3]})                                               => true
        (jsonlogic/apply {"<=" [1 4 3]})                                               => false
        (jsonlogic/apply {"!" [false]})                                                => true
        (jsonlogic/apply {"!" false})                                                  => true
        (jsonlogic/apply {"!" [true]})                                                 => false
        (jsonlogic/apply {"!" true})                                                   => false
        (jsonlogic/apply {"!" 0})                                                      => true
        (jsonlogic/apply {"!" 1})                                                      => false
        (jsonlogic/apply {"or" [true true]})                                           => true
        (jsonlogic/apply {"or" [false true]})                                          => true
        (jsonlogic/apply {"or" [true false]})                                          => true
        (jsonlogic/apply {"or" [false false]})                                         => false
        (jsonlogic/apply {"or" [false false true]})                                    => true
        (jsonlogic/apply {"or" [false false false]})                                   => false
        (jsonlogic/apply {"or" [false]})                                               => false
        (jsonlogic/apply {"or" [true]})                                                => true
        (jsonlogic/apply {"or" [1 3]})                                                 => 1
        (jsonlogic/apply {"or" [3 false]})                                             => 3
        (jsonlogic/apply {"or" [false 3]})                                             => 3
        (jsonlogic/apply {"or" [{"var"  "A"}, false]}, {"A" {"x" 1}})                  => {"x" 1}
        (jsonlogic/apply {"and" [true true]})                                          => true
        (jsonlogic/apply {"and" [false true]})                                         => false
        (jsonlogic/apply {"and" [true false]})                                         => false
        (jsonlogic/apply {"and" [false false]})                                        => false
        (jsonlogic/apply {"and" [true true true]})                                     => true
        (jsonlogic/apply {"and" [true true false]})                                    => false
        (jsonlogic/apply {"and" [false]})                                              => false
        (jsonlogic/apply {"and" [true]})                                               => true
        (jsonlogic/apply {"and" [1 3]})                                                => 3
        (jsonlogic/apply {"and" [3 false]})                                            => false
        (jsonlogic/apply {"and" [false 3]})                                            => false
        (jsonlogic/apply {"?:" [true 1 2]})                                            => 1
        (jsonlogic/apply {"?:" [false 1 2]})                                           => 2
        (jsonlogic/apply {"in" ["Bart" ["Bart" "Homer" "Lisa" "Marge" "Maggie"]]})     => true
        (jsonlogic/apply {"in" ["Milhouse" ["Bart" "Homer" "Lisa" "Marge" "Maggie"]]}) => false
        (jsonlogic/apply {"in" ["Spring" "Springfield"]})                              => true
        (jsonlogic/apply {"in" ["i" "team"]})                                          => false
        (jsonlogic/apply {"cat" "ice"})                                                => "ice"
        (jsonlogic/apply {"cat" ["ice"]})                                              => "ice"
        (jsonlogic/apply {"cat" ["ice" "cream"]})                                      => "icecream"
        (jsonlogic/apply {"cat" [1 2]})                                                => "12"
        (jsonlogic/apply {"cat" ["Robocop" 2]})                                        => "Robocop2"
        (jsonlogic/apply {"cat" ["we all scream for " "ice" "cream"]})                 => "we all scream for icecream"
        (jsonlogic/apply {"%" [1 2]})                                                  => 1
        (jsonlogic/apply {"%" [2 2]})                                                  => 0
        (jsonlogic/apply {"%" [3 2]})                                                  => 1
        (jsonlogic/apply {"max" [1 2 3]})                                              => 3
        (jsonlogic/apply {"max" [1 3 3]})                                              => 3
        (jsonlogic/apply {"max" [3 2 1]})                                              => 3
        (jsonlogic/apply {"max" [1]})                                                  => 1
        (jsonlogic/apply {"min" [1 2 3]})                                              => 1
        (jsonlogic/apply {"min" [1 1 3]})                                              => 1
        (jsonlogic/apply {"min" [3 2 1]})                                              => 1
        (jsonlogic/apply {"min" [1]})                                                  => 1
        (jsonlogic/apply {"+" [1 2]})                                                  => 3
        (jsonlogic/apply {"+" [2 2 2]})                                                => 6
        (jsonlogic/apply {"+" [1]})                                                    => 1
        (jsonlogic/apply {"+" ["1" 1]})                                                => 2
        (jsonlogic/apply {"*" [3 2]})                                                  => 6
        (jsonlogic/apply {"*" [2 2 2]})                                                => 8
        (jsonlogic/apply {"*" [1]})                                                    => 1
        (jsonlogic/apply {"*" ["1" 1]})                                                => 1
        (jsonlogic/apply {"-" [2 3]})                                                  => -1
        (jsonlogic/apply {"-" [3 2]})                                                  => 1
        (jsonlogic/apply {"-" [3]})                                                    => -3
        (jsonlogic/apply {"-" ["1" 1]})                                                => 0
        (jsonlogic/apply {"/" [4 2]})                                                  => 2
        (jsonlogic/apply {"/" [2 4]})                                                  => 0.5
        (jsonlogic/apply {"/" ["1" 1]})                                                => 1)

    (facts "Substring"
      (jsonlogic/apply {"substr" ["jsonlogic" 4]})     => "logic"
      (jsonlogic/apply {"substr" ["jsonlogic" -5]})    => "logic"
      (jsonlogic/apply {"substr" ["jsonlogic" 0 1]})   => "j"
      (jsonlogic/apply {"substr" ["jsonlogic" -1 1]})  => "c"
      (jsonlogic/apply {"substr" ["jsonlogic" 4 5]})   => "logic"
      (jsonlogic/apply {"substr" ["jsonlogic" -5 5]})  => "logic"
      (jsonlogic/apply {"substr" ["jsonlogic" -5 -2]}) => "log"
      (jsonlogic/apply {"substr" ["jsonlogic" 1 -5]})  => "son")

    (facts "Merge arrays"
      (jsonlogic/apply {"merge" []})            => []
      (jsonlogic/apply {"merge" [[1]]})         => [1]
      (jsonlogic/apply {"merge" [[1] []]})      => [1]
      (jsonlogic/apply {"merge" [[1] [2]]})     => [1 2]
      (jsonlogic/apply {"merge" [[1] [2] [3]]}) => [1 2 3]
      (jsonlogic/apply {"merge" [[1 2] [3]]})   => [1 2 3]
      (jsonlogic/apply {"merge" [[1] [2 3]]})   => [1 2 3])

    (facts "Given non-array arguments, merge converts them to arrays"
      (jsonlogic/apply {"merge" 1})       => [1]
      (jsonlogic/apply {"merge" [1 2]})   => [1 2]
      (jsonlogic/apply {"merge" [1 [2]]}) => [1 2])

    (facts "Too few args"
      (jsonlogic/apply {"if" []})        => nil
      (jsonlogic/apply {"if" [true]})    => true
      (jsonlogic/apply {"if" [false]})   => false
      (jsonlogic/apply {"if" ["apple"]}) => "apple")

    (facts "Simple if/then/else cases"
      (jsonlogic/apply {"if" [true "apple"]})           => "apple"
      (jsonlogic/apply {"if" [false "apple"]})          => nil
      (jsonlogic/apply {"if" [true "apple" "banana"]})  => "apple"
      (jsonlogic/apply {"if" [false "apple" "banana"]}) => "banana")

    (facts "Empty arrays are falsey"
      (jsonlogic/apply {"if" [[] "apple" "banana"]})        => "banana"
      (jsonlogic/apply {"if" [[1] "apple" "banana"]})       => "apple"
      (jsonlogic/apply {"if" [[1 2 3 4] "apple" "banana"]}) => "apple")

    (facts "Empty strings are falsey, all other strings are truthy"
      (jsonlogic/apply {"if" ["" "apple" "banana"]})         => "banana"
      (jsonlogic/apply {"if" ["zucchini" "apple" "banana"]}) => "apple"
      (jsonlogic/apply {"if" ["0" "apple" "banana"]})        => "apple")

    (facts "You can cast a string to numeric with a unary + "
      (jsonlogic/apply {"===" [0 "0"]})                     => false
      (jsonlogic/apply {"===" [0 {"+" "0"}]})               => true
      (jsonlogic/apply {"if" [{"+" "0"} "apple" "banana"]}) => "banana"
      (jsonlogic/apply {"if" [{"+" "1"} "apple" "banana"]}) => "apple")

    (facts "Zero is falsy, all other numbers are truthy"
      (jsonlogic/apply {"if" [0 "apple" "banana"]})      => "banana"
      (jsonlogic/apply {"if" [1 "apple" "banana"]})      => "apple"
      (jsonlogic/apply {"if" [3.1416 "apple" "banana"]}) => "apple"
      (jsonlogic/apply {"if" [-1 "apple" "banana"]})     => "apple")

    (facts "Truthy and falsy definitions matter in Boolean operations"
      (jsonlogic/apply {"!" [[]]})         => true
      (jsonlogic/apply {"!!" [[]]})        => false
      (jsonlogic/apply {"and" [[] true]})  => []
      (jsonlogic/apply {"or" [[] true]})   => true
      (jsonlogic/apply {"!" [0]})          => true
      (jsonlogic/apply {"!!" [0]})         => false
      (jsonlogic/apply {"and" [0 true]})   => 0
      (jsonlogic/apply {"or" [0 true]})    => true
      (jsonlogic/apply {"!" [""]})         => true
      (jsonlogic/apply {"!!" [""]})        => false
      (jsonlogic/apply {"and" ["" true]})  => ""
      (jsonlogic/apply {"or" ["" true]})   => true
      (jsonlogic/apply {"!" ["0"]})        => false
      (jsonlogic/apply {"!!" ["0"]})       => true
      (jsonlogic/apply {"and" ["0" true]}) => true
      (jsonlogic/apply {"or" ["0" true]})  => "0")

    (facts "If the conditional is logic, it gets evaluated"
      (jsonlogic/apply {"if" [{">" [2 1]} "apple" "banana"]}) => "apple"
      (jsonlogic/apply {"if" [{">" [1 2]} "apple" "banana"]}) => "banana")

    (facts "If the consequents are logic, they get evaluated"
      (jsonlogic/apply {"if" [true {"cat" ["ap" "ple"]} {"cat" ["ba" "na" "na"]}]})  => "apple"
      (jsonlogic/apply {"if" [false {"cat" ["ap" "ple"]} {"cat" ["ba" "na" "na"]}]}) => "banana")

    (facts "If/then/elseif/then cases"
      (jsonlogic/apply {"if" [true "apple" true "banana"]})                         => "apple"
      (jsonlogic/apply {"if" [true "apple" false "banana"]})                        => "apple"
      (jsonlogic/apply {"if" [false "apple" true "banana"]})                        => "banana"
      (jsonlogic/apply {"if" [false "apple" false "banana"]})                       => nil
      (jsonlogic/apply {"if" [true "apple" true "banana" "carrot"]})                => "apple"
      (jsonlogic/apply {"if" [true "apple" false "banana" "carrot"]})               => "apple"
      (jsonlogic/apply {"if" [false "apple" true "banana" "carrot"]})               => "banana"
      (jsonlogic/apply {"if" [false "apple" false "banana" "carrot"]})              => "carrot"
      (jsonlogic/apply {"if" [false "apple" false "banana" false "carrot"]})        => nil
      (jsonlogic/apply {"if" [false "apple" false "banana" false "carrot" "date"]}) => "date"
      (jsonlogic/apply {"if" [false "apple" false "banana" true "carrot" "date"]})  => "carrot"
      (jsonlogic/apply {"if" [false "apple" true "banana" false "carrot" "date"]})  => "banana"
      (jsonlogic/apply {"if" [false "apple" true "banana" true "carrot" "date"]})   => "banana"
      (jsonlogic/apply {"if" [true "apple" false "banana" false "carrot" "date"]})  => "apple"
      (jsonlogic/apply {"if" [true "apple" false "banana" true "carrot" "date"]})   => "apple"
      (jsonlogic/apply {"if" [true "apple" true "banana" false "carrot" "date"]})   => "apple"
      (jsonlogic/apply {"if" [true "apple" true "banana" true "carrot" "date"]})    => "apple")

    (facts "Arrays with logic"
      (jsonlogic/apply [1 {"var" "x"} 3] {"x" 2})                                => [1 2 3]
      (jsonlogic/apply {"if" [{"var" "x"} [{"var" "y"}] 99]} {"x" true, "y" 42}) => [42]))

  (facts "# Compound Tests"
    (jsonlogic/apply {"and" [{">" [3 1]} true]})              => true
    (jsonlogic/apply {"and" [{">" [3 1]} false]})             => false
    (jsonlogic/apply {"and" [{">" [3 1]} {"!" true}]})        => false
    (jsonlogic/apply {"and" [{">" [3 1]} {"<" [1 3]}]})       => true
    (jsonlogic/apply {"?:" [{">" [3 1]} "visible" "hidden"]}) => "visible")

  (testing "# Data-Driven"
    (facts
        (jsonlogic/apply {"var" ["a"]} {"a" 1})                     => 1
        (jsonlogic/apply {"var" ["b"]} {"a" 1})                     => nil
        (jsonlogic/apply {"var" ["a"]})                             => nil
        (jsonlogic/apply {"var" "a"} {"a" 1})                       => 1
        (jsonlogic/apply {"var" "b"} {"a" 1})                       => nil
        (jsonlogic/apply {"var" "a"})                               => nil
        (jsonlogic/apply {"var" ["a" 1]})                           => 1
        (jsonlogic/apply {"var" ["b" 2]} {"a" 1})                   => 2
        (jsonlogic/apply {"var" "a.b"} {"a" {"b" "c"}})             => "c"
        (jsonlogic/apply {"var" "a.q"} {"a" {"b" "c"}})             => nil
        (jsonlogic/apply {"var" ["a.q" 9]} {"a" {"b" "c"}})         => 9
        (jsonlogic/apply {"var" 1} ["apple" "banana"])              => "banana"
        (jsonlogic/apply {"var" "1"} ["apple" "banana"])            => "banana"
        (jsonlogic/apply {"var" "1.1"} ["apple" ["banana" "beer"]]) => "beer"
        (jsonlogic/apply {"var" "a.b.c"})                           => nil
        (jsonlogic/apply {"var" "a.b.c"} {"a" nil})                 => nil
        (jsonlogic/apply {"var" "a.b.c"} {"a" {"b" nil}})           => nil
        (jsonlogic/apply {"var" "0.a.1"} [{"a" [1 2]}])             => 2
        (jsonlogic/apply {"var" ""} 1)                              => 1
        (jsonlogic/apply {"var" nil} 1)                             => 1
        (jsonlogic/apply {"var" []} 1)                              => 1
        (jsonlogic/apply {"and" [{"<" [{"var" "temp"} 110]}
                                 {"==" [{"var" "pie.filling"} "apple"]}]}
                         {"temp" 100, "pie" {"filling" "apple"}})
        => true

        (jsonlogic/apply {"var" [{"?:" [{"<" [{"var" "temp"} 110]}
                                        "pie.filling"
                                        "pie.eta"]}]}
                         {"temp" 100, "pie" {"filling" "apple", "eta" "60s"}})
        => "apple"

        (jsonlogic/apply {"in" [{"var" "filling"} ["apple" "cherry"]]}
                         {"filling" "apple"})
        => true)

    (facts "Missing"
      (jsonlogic/apply {"missing" []})                                        => []
      (jsonlogic/apply {"missing" ["a"]})                                     => ["a"]
      (jsonlogic/apply {"missing" "a"})                                       => ["a"]
      (jsonlogic/apply {"missing" "a"} {"a" "apple"})                         => []
      (jsonlogic/apply {"missing" ["a"]} {"a" "apple"})                       => []
      (jsonlogic/apply {"missing" ["a" "b"]} {"a" "apple"})                   => ["b"]
      (jsonlogic/apply {"missing" ["a" "b"]} {"b" "banana"})                  => ["a"]
      (jsonlogic/apply {"missing" ["a" "b"]} {"a" "apple", "b" "banana"})     => []
      (jsonlogic/apply {"missing" ["a" "b"]})                                 => ["a" "b"]
      (jsonlogic/apply {"missing" ["a" "b"]})                                 => ["a" "b"]
      (jsonlogic/apply {"missing" ["a.b"]})                                   => ["a.b"]
      (jsonlogic/apply {"missing" ["a.b"]} {"a" "apple"})                     => ["a.b"]
      (jsonlogic/apply {"missing" ["a.b"]} {"a" {"c" "apple cake"}})          => ["a.b"]
      (jsonlogic/apply {"missing" ["a.b"]} {"a" {"b" "apple brownie"}})       => []
      (jsonlogic/apply {"missing" ["0.a.1"]} [{"a" [1 2]}])                   => []
      (jsonlogic/apply {"missing" ["a.b" "a.c"]} {"a" {"b" "apple brownie"}}) => ["a.c"])

    (facts "Missing some"
      (jsonlogic/apply {"missing_some" [1 ["a" "b"]]} {"a" "apple"})                                 => []
      (jsonlogic/apply {"missing_some" [1 ["a" "b"]]} {"b" "banana"})                                => []
      (jsonlogic/apply {"missing_some" [1 ["a" "b"]]} {"a" "apple", "b" "banana"})                   => []
      (jsonlogic/apply {"missing_some" [1 ["a" "b"]]} {"c" "carrot"})                                => ["a" "b"]
      (jsonlogic/apply {"missing_some" [2 ["a" "b" "c"]]} {"a" "apple", "b" "banana"})               => []
      (jsonlogic/apply {"missing_some" [2 ["a" "b" "c"]]} {"a" "apple", "c" "carrot"})               => []
      (jsonlogic/apply {"missing_some" [2 ["a" "b" "c"]]} {"a" "apple", "b" "banana", "c" "carrot"}) => []
      (jsonlogic/apply {"missing_some" [2 ["a" "b" "c"]]} {"a" "apple", "d" "durian"})               => ["b" "c"]
      (jsonlogic/apply {"missing_some" [2 ["a" "b" "c"]]} {"d" "durian", "e" "eggplant"})            => ["a" "b" "c"])

    (facts "Missing and If are friends, because empty arrays are falsey in JsonLogic"
      (jsonlogic/apply {"if" [{"missing" "a"} "missed it" "found it"]} {"a" "apple"})  => "found it"
      (jsonlogic/apply {"if" [{"missing" "a"} "missed it" "found it"]} {"b" "banana"}) => "missed it")

    (facts "Missing, Merge, and If are friends. VIN is always required, APR is only required if financing is true."
      (jsonlogic/apply {"missing" {"merge" ["vin" {"if" [{"var" "financing"} ["apr"] []]}]}}
                       {"financing" true})
      => ["vin" "apr"]

      (jsonlogic/apply {"missing" {"merge" ["vin" {"if" [{"var" "financing"} ["apr"] []]}]}}
                       {"financing" false})
      => ["vin"])

    (facts "Filter, map, all, none, and some"
      (jsonlogic/apply {"filter" [{"var" "integers"} true]}
                       {"integers" [1 2 3]})
      => [1 2 3]

      (jsonlogic/apply {"filter" [{"var" "integers"} false]}
                       {"integers" [1 2 3]})
      => []

      (jsonlogic/apply {"filter" [{"var" "integers"}
                                  {">=" [{"var" ""} 2]}]}
                       {"integers" [1 2 3]})
      => [2 3]

      (jsonlogic/apply {"filter" [{"var" "integers"}
                                  {"%" [{"var" ""} 2]}]}
                       {"integers" [1 2 3]})
      => [1 3]
      (jsonlogic/apply {"map" [{"var" "integers"}
                               {"*" [{"var" ""} 2]}]}
                       {"integers" [1 2 3]})
      => [2 4 6]

      (jsonlogic/apply {"map" [{"var" "integers"}
                               {"*" [{"var" ""} 2]}]})
      => []

      (jsonlogic/apply {"map" [{"var" "desserts"}
                               {"var" "qty"}]}
                       {"desserts" [{"name" "apple", "qty" 1}
                                    {"name" "brownie", "qty" 2}
                                    {"name" "cupcake", "qty" 3}]})
      => [1 2 3]

      (jsonlogic/apply {"reduce" [{"var" "integers"} {"+" [{"var" "current"} {"var" "accumulator"}]} 0]}
                       {"integers" [1 2 3 4]})
      => 10

      (jsonlogic/apply {"reduce" [{"var" "integers"}
                                  {"+" [{"var" "current"} {"var" "accumulator"}]}
                                  0]})
      => 0

      (jsonlogic/apply {"reduce" [{"var" "integers"}
                                  {"*" [{"var" "current"} {"var" "accumulator"}]}
                                  1]}
                       {"integers" [1 2 3 4]})
      => 24

      (jsonlogic/apply {"reduce" [{"var" "integers"}
                                  {"*" [{"var" "current"} {"var" "accumulator"}]}
                                  0]}
                       {"integers" [1 2 3 4]})
      => 0

      (jsonlogic/apply {"reduce" [{"var" "desserts"}
                                  {"+" [{"var" "accumulator"} {"var" "current.qty"}]}
                                  0]}
                       {"desserts" [{"name" "apple", "qty" 1}
                                    {"name" "brownie", "qty" 2}
                                    {"name" "cupcake", "qty" 3}]})
      => 6

      (jsonlogic/apply {"all" [{"var" "integers"}
                               {">=" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => true

      (jsonlogic/apply {"all" [{"var" "integers"}
                               {"==" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => false

      (jsonlogic/apply {"all" [{"var" "integers"}
                               {"<" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => false

      (jsonlogic/apply {"all" [{"var" "integers"}
                               {"<" [{"var" ""} 1]}]}
                       {"integers" []})
      => false

      (jsonlogic/apply {"all" [{"var" "items"}
                               {">=" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => true

      (jsonlogic/apply {"all" [{"var" "items"}
                               {">" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => false

      (jsonlogic/apply {"all" [{"var" "items"}
                               {"<" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => false

      (jsonlogic/apply {"all" [{"var" "items"}
                               {">=" [{"var" "qty"} 1]}]}
                       {"items" []})
      => false

      (jsonlogic/apply {"none" [{"var" "integers"}
                                {">=" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => false

      (jsonlogic/apply {"none" [{"var" "integers"}
                                {"==" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => false

      (jsonlogic/apply {"none" [{"var" "integers"}
                                {"<" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => true

      (jsonlogic/apply {"none" [{"var" "integers"}
                                {"<" [{"var" ""} 1]}]}
                       {"integers" []})
      => true

      (jsonlogic/apply {"none" [{"var" "items"}
                                {">=" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => false

      (jsonlogic/apply {"none" [{"var" "items"}
                                {">" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => false

      (jsonlogic/apply {"none" [{"var" "items"}
                                {"<" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => true

      (jsonlogic/apply {"none" [{"var" "items"}
                                {">=" [{"var" "qty"} 1]}]}
                       {"items" []})
      => true

      (jsonlogic/apply {"some" [{"var" "integers"}
                                {">=" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => true

      (jsonlogic/apply {"some" [{"var" "integers"}
                                {"==" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => true

      (jsonlogic/apply {"some" [{"var" "integers"}
                                {"<" [{"var" ""} 1]}]}
                       {"integers" [1 2 3]})
      => false

      (jsonlogic/apply {"some" [{"var" "integers"}
                                {"<" [{"var" ""} 1]}]}
                       {"integers" []})
      => false

      (jsonlogic/apply {"some" [{"var" "items"}
                                {">=" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => true

      (jsonlogic/apply {"some" [{"var" "items"}
                                {">" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => true

      (jsonlogic/apply {"some" [{"var" "items"}
                                {"<" [{"var" "qty"} 1]}]}
                       {"items" [{"qty" 1, "sku" "apple"}
                                 {"qty" 2, "sku" "banana"}]})
      => false

      (jsonlogic/apply {"some" [{"var" "items"}
                                {">=" [{"var" "qty"} 1]}]}
                       {"items" []})
      => false)))

(deftest bad-operator
  (fact "unknown operators throw exceptions"
    (jsonlogic/apply {"fubar" []}) =throws=> IllegalArgumentException))

(defmethod jsonlogic/operate 'cool
  [{string 'cool} _]
  (str "That's cool " string "!"))

(deftest custom-operator
  (testing "# Custom Operator"
    (fact (jsonlogic/apply {"cool" "dude"}) => "That's cool dude!")))

(deftest edge-cases
  (testing "# Edge Cases"
    (facts"Equality operators do not work with objects"
      (jsonlogic/apply {"==" [{"a" 1}, {"a" 1}]})  =throws=> (IllegalArgumentException. "Unrecognized operation a")
      (jsonlogic/apply {"!=" [{"a" 1}, {"a" 1}]})  =throws=> (IllegalArgumentException. "Unrecognized operation a")
      (jsonlogic/apply {"===" [{"a" 1}, {"a" 1}]}) =throws=> (IllegalArgumentException. "Unrecognized operation a")
      (jsonlogic/apply {"!==" [{"a" 1}, {"a" 1}]}) =throws=> (IllegalArgumentException. "Unrecognized operation a"))
    (facts "Var cannot access certain valid JSON key names"
      (jsonlogic/apply {"var" "a.b.c"} {"a.b.c" 1}) => nil)))
