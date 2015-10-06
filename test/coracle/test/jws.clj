(ns coracle.test.jws
  (:require [midje.sweet :refer :all]
            [clj-time.core :as t]
            [cheshire.core :as json]
            [coracle.jws :as jws])
  (:import [org.jose4j.jwk RsaJsonWebKey]))

(fact "can generate a key pair"
      (let [key-pair (jws/generate-key-pair "some-key-id")]
        key-pair =not=> nil?
        (class key-pair) => RsaJsonWebKey
        (.getKeyId key-pair) => "some-key-id"))

(fact "can generate a key ID"
      (jws/generate-key-id) => "key-1234"
      (provided
        (jws/now-in-millis anything) => "1234"))

(fact "now-in-millis returns unix epoch milliseconds as a string"
      (jws/now-in-millis "2015-10-06") => "1444089600000")

(fact "json-web-key->json-web-key-set generates a json-web-key-set as a json string"
      (let [key-id "some-key-id"
            key-pair (jws/generate-key-pair key-id)
            jwks (jws/json-web-key->json-web-key-set key-pair)]
        (-> (json/parse-string jwks) (get "keys") first (get "kid")) => key-id))


