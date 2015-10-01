(ns coracle.config
  (:require [environ.core :as e]))

(defn app-port [] (Integer. (e/env :port)))
(defn app-host [] (e/env :host))

(defn mongo-port [] (e/env :mongodb-port))

(defn mongo-container-tcp [port]
  (let [k (-> (format "mongo-port-%s-tcp-addr" port) keyword)]
    (e/env k)))

(defn mongo-host []
  (if-let [host (e/env :mongodb-host)]
    host
    (if-let [h (mongo-container-tcp (mongo-port))]
      h
      (throw (Exception. "Host not specified, and environment variable with linked container host cannot be found.")))))

(defn mongo-db []
  (e/env :mongo-db))

(defn mongo-uri []
  (format "mongodb://%s:%s/%s" (mongo-host) (mongo-port) (mongo-db)))

(defn bearer-token []
  (e/env :bearer-token))

(defn secure? []
  (= "true" (e/env :secure)))
