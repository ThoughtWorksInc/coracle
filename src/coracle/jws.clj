(ns coracle.jws
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c])
  (:import [org.jose4j.jwk RsaJwkGenerator JsonWebKeySet]
           [org.jose4j.jws JsonWebSignature AlgorithmIdentifiers]))

(defn generate-json-web-key [key-id]
  (doto (RsaJwkGenerator/generateJwk 2048)
    (.setKeyId key-id)
    (.setAlgorithm AlgorithmIdentifiers/RSA_USING_SHA256)))

(defn in-millis [time]
  (-> time c/to-long str))

(defn generate-key-id []
  (str "key-" (in-millis (t/now))))

(defn json-web-key->json-web-key-set [json-web-key]
  (.toJson (JsonWebKeySet. [json-web-key])))

(defn jws-generator [json-web-key]
  (let [jws (doto (JsonWebSignature.)
              (.setAlgorithmHeaderValue (.getAlgorithm json-web-key))
              (.setKey (.getPrivateKey json-web-key)))]
    (fn [payload]
      (.setPayload jws payload)
      (.getCompactSerialization jws))))
