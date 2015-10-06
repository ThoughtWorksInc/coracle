(ns coracle.jws
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c])
  (:import [org.jose4j.jwk RsaJwkGenerator JsonWebKeySet]))

(defn generate-key-pair [key-id]
  (doto (RsaJwkGenerator/generateJwk 2048)
    (.setKeyId key-id)))

(defn in-millis [time]
  (-> time c/to-long str))

(defn generate-key-id []
  (str "key-" (in-millis (t/now))))

(defn json-web-key->json-web-key-set [json-web-key]
  (.toJson (JsonWebKeySet. [json-web-key])))

(defn jws-response-generator [key-pair]
  identity
  )
