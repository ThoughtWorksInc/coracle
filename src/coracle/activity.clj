(ns coracle.activity
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as r]
            [cheshire.core :as json]
            [coracle.db :as db]
            [coracle.marshaller :as m]))

(defn- unsigned-activity-response [activities]
  (-> (r/response activities)
      (r/content-type "application/activity+json")))

(defn- signed-activity-response [jws-generator activities]
  (let [activities-signed-and-encoded-payload (-> activities
                                                  json/generate-string
                                                  jws-generator)]
    (-> (r/response {:jws-signed-payload activities-signed-and-encoded-payload})
        (r/content-type "application/jose+json"))))

(defn- bad-request-response [body]
  (-> (r/response body) (r/status 400)))

(defn- activity-from-request [req]
  (->> req :body m/validate-and-parse-activity))

(defn add-activity [db req]
  (log/info "adding activity with request: " req)
  (let [data (activity-from-request req)]
    (if (empty? (:error data))
      (do (db/add-activity db data)
          (-> (r/response {:status :success}) (r/status 201)))
      (bad-request-response (:error data)))))

(defn- descending [a b]
  (compare b a))

(defn- published [activity-json]
  (get activity-json "published"))

(defn- generate-activity-response [db jws-generator query-params]
  (let [activities (->> (db/fetch-activities db query-params)
                        (sort-by published descending)
                        (map m/stringify-activity-timestamp))]
    (if (= "true" (:signed query-params))
      (signed-activity-response jws-generator activities)
      (unsigned-activity-response activities))))

(defn get-activities [db jws-generator req]
  (let [query-params (-> req :params m/marshall-query-params)
        error-m (:error query-params)]
    (if (empty? error-m)
      (generate-activity-response db jws-generator query-params)
      (bad-request-response error-m))))

(defn latest-published-timestamp [db _]
  (let [latest-published-activity (db/fetch-latest-published-activity db)
        jsonified-activity (m/stringify-activity-timestamp latest-published-activity)
        response-body (if latest-published-activity
                        {:latest-published-timestamp (published jsonified-activity)}
                        {})]
    (-> (r/response response-body)
        (r/content-type "application/json"))))

