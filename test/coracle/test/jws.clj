(ns coracle.test.jws
  (:require [midje.sweet :refer :all]
            [clj-time.core :as t]
            [cheshire.core :as json]
            [coracle.jws :as jws])
  (:import [org.jose4j.jwk RsaJsonWebKey JsonWebKey$Factory]))

(fact "can generate a key pair"
      (let [key-pair (jws/generate-key-pair "some-key-id")]
        key-pair =not=> nil?
        (class key-pair) => RsaJsonWebKey
        (.getKeyId key-pair) => "some-key-id"))

(fact "can generate a key ID"
      (jws/generate-key-id) => "key-1234"
      (provided
        (jws/in-millis anything) => "1234"))

(fact "in-millis returns unix epoch milliseconds as a string"
      (jws/in-millis "2015-10-06") => "1444089600000")

(fact "json-web-key->json-web-key-set generates a json-web-key-set as a json string"
      (let [key-id "some-key-id"
            key-pair (jws/generate-key-pair key-id)
            jwks (jws/json-web-key->json-web-key-set key-pair)]
        (-> (json/parse-string jwks) (get "keys") first (get "kid")) => key-id))


(defn decode [response]
  )

(future-fact "jws response generator can sign responses"
      (let [key-pair (-> (slurp "./test-resources/test-key.json") JsonWebKey$Factory/newJwk)
            jws-response-generator (jws/jws-response-generator key-pair)
            json-payload "{\"hello\": \"rob\"}"
            jws-response (jws-response-generator json-payload)
            decoded-response (decode jws-response)]

        (get decoded-response "payload") => json-payload))