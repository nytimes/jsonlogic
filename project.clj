(defproject com.nytimes/jsonlogic "0.1.2-SNAPSHOT"
  :description "JsonLogic for Clojure"
  :url "https://github.com/nytimes/jsonlogic"
  :license {:name "Apache License"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.2.4"]]

  :repl-options {:init-ns com.nytimes.jsonlogic}

  :jvm-opts ["--add-opens" "java.base/java.lang=ALL-UNNAMED"]

  :profiles {:dev {:source-paths ["dev"]
                   :jvm-opts     ["-Dorg.slf4j.simpleLogger.defaultLogLevel=debug"]
                   :dependencies [[metosin/testit "0.4.1"]
                                  [org.slf4j/slf4j-api "1.7.30"]
                                  [org.slf4j/slf4j-simple "1.7.30"]]}}

  :repositories [["releases"  {:url           "https://clojars.org/repo"
                               :sign-releases false
                               :username      [:env/clojars_username]
                               :password      [:env/clojars_password]}]
                 ["snapshots" {:url           "https://clojars.org/repo"
                               :sign-releases false
                               :username      [:env/clojars_username]
                               :password      [:env/clojars_password]}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
