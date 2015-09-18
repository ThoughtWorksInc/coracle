(ns coracle.test.core
  (:require [midje.sweet :refer :all]
            [coracle.core :refer [handler]]
            [ring.mock.request :refer [request]]
            [cheshire.core :as json]
            [coracle.test.helpers :as h]
            [coracle.db :as db]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [monger.collection :as mc]))

(defn post-json [url params]
  (->
    (request :post url)
    (assoc :body params)))

(defn activity-json [actor published]
  {"@actor"     actor
   "@published" (str published)})

(defn db-activity [actor published]
  {"@actor"     actor
   "@published" (tc/to-long published)})

(def timestamp (t/now))

(facts "Can store json activity"
       (h/with-db-do
         (fn [test-db]
           (let [test-handler (handler test-db)
                 request (post-json "/activities" (activity-json "dave" timestamp))]
             (-> (test-handler request) :status) => 201
             (first (db/fetch-activities test-db {})) => (contains {"@actor" "dave"})))))

(facts "Can load json activity"
       (h/with-db-do
         (fn [test-db]
           (let [test-handler (handler test-db)
                 request (request :get "/activities")]
             (db/add-activity test-db (activity-json "dave" timestamp))
             (let [response (test-handler request)]
               (fact
                 (-> response :body json/parse-string) => [(activity-json "dave" timestamp)]))))))

(fact "Can load json activitites with query"
      (h/with-db-do
        (fn [test-db]
          (let [test-handler (handler test-db)
                d1 (t/now)
                d2 (t/plus (t/now) (t/weeks 1))
                d3 (t/plus (t/now) (t/weeks 2))
                request (request :get (format "/activities?from=%s&to=%s" d1 d3))]
            (db/add-activity test-db (db-activity "tofu" d1))
            (db/add-activity test-db (db-activity "bloob" d2))
            (db/add-activity test-db (db-activity "roy" d3))
            (let [response (test-handler request)]
              (fact
                (-> response :body json/parse-string) => [(activity-json "bloob" d2)]))))))


