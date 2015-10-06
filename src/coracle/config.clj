(ns coracle.config
  (:require [environ.core :as e]))

(defn get-env
  ([env-key default]
   (if-let [v (get e/env env-key default)]
     v
     (throw (Exception. "No value supplied for key [%s] and no default provided"))))
  ([env-key]
    (get-env env-key nil)))

(defn app-port [] (Integer. (get-env :port)))
(defn app-host [] (get-env :host))

(defn mongo-port [] (get-env :mongodb-port))

(defn mongo-container-tcp [port]
  (let [k (-> (format "mongo-port-%s-tcp-addr" port) keyword)]
    (get-env k)))

(defn mongo-host []
  (if-let [host (get-env :mongodb-host)]
    host
    (if-let [h (mongo-container-tcp (mongo-port))]
      h
      (throw (Exception. "Host not specified, and environment variable with linked container host cannot be found.")))))

(defn mongo-db []
  (get-env :mongo-db))

(defn mongo-uri []
  (format "mongodb://%s:%s/%s" (mongo-host) (mongo-port) (mongo-db)))

(defn bearer-token []
  (get-env :bearer-token ""))

(defn secure? []
  (= "true" (get-env :secure "false")))
