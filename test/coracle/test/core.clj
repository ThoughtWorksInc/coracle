(ns coracle.test.core
  (:require [midje.sweet :refer :all]
            [coracle.core :refer [handler]]
            [ring.mock.request :refer [request]]
            [monger.core :as m]
            [monger.collection :as mc]))

(defn post-json [url params]
  (->
    (request :post url)
    (assoc :body params)))

(def test-db (:db (m/connect-via-uri "mongodb://localhost:27017/coracle-test")))
(def test-handler (handler test-db))

(facts "Can store json activity"
       (let [request (post-json "/activities" {"actor" "dave"})]
         (-> (test-handler request) :status) => 201
         (first (mc/find-maps test-db "activities")) => (contains {:actor "dave"})))

