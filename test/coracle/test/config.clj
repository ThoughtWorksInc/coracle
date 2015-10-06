(ns coracle.test.config
  (:require [midje.sweet :refer :all]
            [coracle.config :as config]))

(facts "about docker mongo host"
       (fact "use mongodb host if present"
             (with-redefs [config/environment {:mongodb-host "mongohost"
                                               :mongodb-port 1234
                                               :mongodb-db   "the-db"}]
               (config/mongo-uri) => "mongodb://mongohost:1234/the-db"))
       (fact "use docker mongo var if host isn't present"
             (with-redefs [config/environment {:mongodb-port 2345
                                               :mongodb-db   "the-db"
                                               :mongo-port-2345-tcp-addr "docker-addr"}]
               (config/mongo-uri) => "mongodb://docker-addr:2345/the-db"))
       (fact "if neither host or docker mongo var is available then exception is thrown"
             (with-redefs [config/environment {:mongodb-port 1234
                                               :mongodb-db "coracle"}]
               (config/mongo-uri) => (throws Exception))))
