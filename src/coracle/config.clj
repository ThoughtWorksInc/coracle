(ns coracle.config
  (:require [environ.core :as e]))

(defn app-port [] (Integer. (e/env :port)))
(defn app-host [] (e/env :host))

(defn mongo-port [] (e/env :mongo-port))

(defn mongo-container-tcp [port]
  (let [k (-> (format "mongo-port-%s-tcp-addr" port) keyword)]
    (e/env k)))

(defn mongo-host []
  (if-let [host (e/env :mongo-host)]
    host
    (mongo-container-tcp (mongo-port))))

(defn mongo-db []
  (e/env :mongo-db))

(defn mongo-uri []
  (format "mongodb://%s:%s/%s" (mongo-host) (mongo-port) (mongo-db)))

(defn bearer-token []
  (e/env :bearer-token))
