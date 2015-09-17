(ns coracle.test.core
  (:require [midje.sweet :refer :all]
            [coracle.core :refer [handler]]
            [ring.mock.request :refer [request]]
            [monger.core :as m]
            [monger.collection :as mc]
            [cheshire.core :as json]))

(defn post-json [url params]
  (->
    (request :post url)
    (assoc :body params)))

(defn with-db-do [thing-to-do]
  (let [{:keys [db conn]} (m/connect-via-uri "mongodb://localhost:27017/coracle-test")]
    (thing-to-do db)
    (m/drop-db conn "coracle-test")))

(facts "Can store json activity"
       (with-db-do
         (fn [test-db]
           (let [test-handler (handler test-db)
                 request (post-json "/activities" {"actor" "dave"})]
             (-> (test-handler request) :status) => 201
             (first (mc/find-maps test-db "activities")) => (contains {:actor "dave"})))))

(facts "Can load json activity"
       (with-db-do
         (fn [test-db]
           (let [test-handler (handler test-db)
                 request (request :get "/activities")]
             (mc/insert test-db "activities" {"actor" "dave"})
             (let [response (test-handler request)]
               (fact
                 (-> response :body json/parse-string) => [{"actor" "dave"}]))))))


