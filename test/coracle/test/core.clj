(ns coracle.test.core
  (:require [midje.sweet :refer :all]
            [coracle.core :refer [handler]]
            [ring.mock.request :refer [request]]
            [cheshire.core :as json]
            [coracle.test.helpers :as h]
            [coracle.db :as db]))

(defn post-json [url params]
  (->
    (request :post url)
    (assoc :body params)))

(facts "Can store json activity"
       (h/with-db-do
         (fn [test-db]
           (let [test-handler (handler test-db)
                 request (post-json "/activities" {"actor" "dave"})]
             (-> (test-handler request) :status) => 201
             (first (db/fetch-activities test-db)) => (contains {"actor" "dave"})))))

(facts "Can load json activity"
       (h/with-db-do
         (fn [test-db]
           (let [test-handler (handler test-db)
                 request (request :get "/activities")]
             (db/add-activity test-db {"actor" "dave"})
             (let [response (test-handler request)]
               (fact
                 (-> response :body json/parse-string) => [{"actor" "dave"}]))))))


