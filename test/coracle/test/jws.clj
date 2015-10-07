(ns coracle.test.jws
  (:require [midje.sweet :refer :all]
            [cheshire.core :as json]
            [coracle.jws :as jws])
  (:import [org.jose4j.jwk RsaJsonWebKey]
           [org.jose4j.jws JsonWebSignature]
           [org.jose4j.lang IntegrityException]))

;; Using defs as generating json web key multiple times slows down tests
(def test-json-web-key-id "some-key-id")
(def test-json-web-key (jws/generate-json-web-key test-json-web-key-id))
(def another-json-web-key (jws/generate-json-web-key "another-key-id"))

(fact "can generate a json-web-key"
      test-json-web-key =not=> nil?
      (fact "it is an rsa-json-web-key"
            (class test-json-web-key) => RsaJsonWebKey)
      (fact "it includes a key id"
            (.getKeyId test-json-web-key) => "some-key-id")
      (fact "it includes an algorithm value of RS256"
            (.getAlgorithm test-json-web-key) => "RS256"))

(fact "can generate a key ID"
      (jws/generate-key-id) => "key-1234"
      (provided
        (jws/in-millis anything) => "1234"))

(fact "in-millis returns unix epoch milliseconds as a string"
      (jws/in-millis "2015-10-06") => "1444089600000")

(fact "json-web-key->json-web-key-set generates a json-web-key-set as a json string"
      (let [json-web-key-set (jws/json-web-key->json-web-key-set test-json-web-key)]
        (-> (json/parse-string json-web-key-set) (get "keys") first (get "kid")) => test-json-web-key-id))

(defn verify-signature-and-decode [jws-signed-and-encoded-payload json-web-key]
  (.getPayload (doto (JsonWebSignature.)
                 (.setCompactSerialization jws-signed-and-encoded-payload)
                 (.setKey (.getKey json-web-key)))))

(fact "jws-generator generates signed and encoded payloads"
      (let [json-web-key-for-signing test-json-web-key
            another-json-web-key-not-used-for-signing another-json-web-key
            jws-generator (jws/jws-generator json-web-key-for-signing)
            json-payload "{\"hello\": \"barry\"}"
            jws-signed-and-encoded-payload (jws-generator json-payload)]
        (fact "signed and encoded payload is a string"
              (class jws-signed-and-encoded-payload) => java.lang.String)
        (fact "generated payload can be decoded with the correct json-web-key"
              (verify-signature-and-decode jws-signed-and-encoded-payload json-web-key-for-signing) => json-payload)
        (fact "verification fails when the wrong json-web-key is used"
              (verify-signature-and-decode jws-signed-and-encoded-payload another-json-web-key-not-used-for-signing) => (throws IntegrityException))))
