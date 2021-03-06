(defproject coracle "0.1.0-SNAPSHOT"
  :description "FIXME: describe your microservice"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot coracle.core
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring "1.3.2"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [scenic "0.2.5"]
                 [cheshire "5.5.0"]
                 [com.novemberain/monger "3.0.0-rc2"]
                 [environ "1.0.1"]
                 [clj-time "0.11.0"]
                 [prismatic/schema "1.0.1"]
                 [org.bitbucket.b_c/jose4j "0.4.4"]
                 [org.slf4j/slf4j-simple "1.7.12"]
                 [ragtime "0.4.2"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[midje "1.6.3"]
                                      [ring-mock "0.1.5"]
                                      [kerodon "0.7.0"]]
                       :plugins      [[lein-midje "3.1.1"]
                                      [lein-environ "1.0.1"]]
                       :env          {:host   "0.0.0.0"
                                      :port   "8000"
                                      :external-jwk-set-url "http://localhost:8000/jwk-set"
                                      :mongodb-host "localhost"
                                      :mongodb-port "27017"
                                      :mongodb-db   "coracle"}}
             :docker  {:env {:host   "0.0.0.0"
                             :port   "7000"
                             :mongodb-port "27017"
                             :mongodb-db   "coracle"}}})
