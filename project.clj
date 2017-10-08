(defproject kubera-clojure "0.1.0-SNAPSHOT"
  :description "Automatic PostgreSQL backups"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clojurewerkz/quartzite "2.0.0"]
                 [clj-http "3.7.0"]
                 [environ/environ.core "0.3.1"]
                 [org.clojure/tools.logging "0.4.0"]]
  :main ^:skip-aot kubera-clojure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
