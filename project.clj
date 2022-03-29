(defproject com.nytimes/jsonlogic "0.1.0-SNAPSHOT"
  :description "JsonLogic for Clojure"
  :url "https://github.com/nytimes/jsonlogic"
  :license {:name "Apache License"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :plugins [[lein-codox "0.10.7"]]
  :dependencies [[org.clojure/clojure "1.11.0"]
                 [org.clojure/tools.logging "1.2.4"]]
  :repl-options {:init-ns nytimes.jsonlogic}
  :jvm-opts ["--add-opens" "java.base/java.lang=ALL-UNNAMED"]
  :profiles {:dev {:source-paths ["dev"]
                   :jvm-opts     ["-Dorg.slf4j.simpleLogger.defaultLogLevel=debug"]
                   :dependencies [[lambdaisland/kaocha "1.0.641"]
                                  [metosin/testit "0.4.1"]
                                  [org.slf4j/slf4j-api "1.7.30"]
                                  [org.slf4j/slf4j-simple "1.7.30"]]}}
  :aliases {"test" ["run" "-m" "kaocha.runner"]}
  :codox {:metadata   {:doc/format :markdown}
          :source-uri "https://github.com/nytimes/jsonlogic/blob/main/{filepath}#L{line}"})
