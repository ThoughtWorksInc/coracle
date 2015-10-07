(ns coracle.config
  (:require [environ.core :as e]))

(def environment e/env)

(defn get-required
  ([env-key default]
   (if-let [v (get environment env-key default)]
     v
     (throw (Exception. (format "No value supplied for key [%s] and no default provided" env-key)))))
  ([env-key]
    (get-required env-key nil)))

(defn app-port [] (Integer. (get-required :port)))
(defn app-host [] (get-required :host))

(defn mongo-port [] (get-required :mongodb-port))

(defn mongo-container-tcp [port]
  (let [k (-> (format "mongo-port-%s-tcp-addr" port) keyword)]
    (k environment)))

(defn mongo-host []
  (if-let [host (:mongodb-host environment)]
    host
    (if-let [h (mongo-container-tcp (mongo-port))]
      h
      (throw (Exception. "Host not specified, and environment variable with linked container host cannot be found.")))))

(defn mongo-db []
  (get-required :mongodb-db))

(defn mongo-uri []
  (format "mongodb://%s:%s/%s" (mongo-host) (mongo-port) (mongo-db)))

(defn bearer-token []
  (get-required :bearer-token))

(defn secure? []
  (= "true" (get-required :secure "false")))
