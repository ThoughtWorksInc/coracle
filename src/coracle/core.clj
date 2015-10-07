(ns coracle.core
  (:gen-class)
  (:require [scenic.routes :refer :all]
            [clojure.tools.logging :as log]
            [ring.util.response :as r]
            [ring.middleware.defaults :as ring-defaults]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [cheshire.core :as json]
            [coracle.config :as c]
            [coracle.db :as db]
            [coracle.marshaller :as m]
            [coracle.jws :as jws]))

(defn unsigned-activity-response [activities]
  (-> (r/response activities)
      (r/content-type "application/activity+json")))

(defn signed-activity-response [jws-generator activities]
  (let [activities-signed-and-encoded-payload (-> activities
                                                  json/generate-string
                                                  jws-generator)]
    (-> (r/response {:jws-signed-payload activities-signed-and-encoded-payload})
        (r/content-type "application/jose+json"))))

(defn bad-request-response [body]
  (-> (r/response body) (r/status 400)))

(defn not-found-handler [req]
  (-> (r/response {:error "not found"}) (r/status 404)))

(defn activity-from-request [req]
  (->> req :body m/validate-and-parse-activity))

(defn add-activity [db req]
  (log/info "adding activity with request: " req)
  (let [data (activity-from-request req)]
    (if (empty? (:error data))
      (do (db/add-activity db data)
          (-> (r/response {:status :success}) (r/status 201)))
      (bad-request-response (:error data)))))

(def descending #(compare %2 %1))

(def published #(get % "published"))


(defn generate-activity-response [db jws-generator query-params]
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

(defn ping [request]
  (-> (r/response "pong")
      (r/content-type "text/plain")))

(defn jwk-set [json-web-key-set request]
  (-> (r/response json-web-key-set)
      (r/content-type "application/json")))

(defn latest-published-timestamp [db request]
  (let [latest-published-activity (db/fetch-latest-published-activity db)
        jsonified-activity (m/stringify-activity-timestamp latest-published-activity)
        response-body (if latest-published-activity
                        {:latest-published-timestamp (published jsonified-activity)}
                        {})]
    (-> (r/response response-body)
        (r/content-type "application/json"))))

(defn handlers [db json-web-key-set jws-generator]
  {:add-activity               (partial add-activity db)
   :show-activities            (partial get-activities db jws-generator)
   :ping                       ping
   :latest-published-timestamp (partial latest-published-timestamp db)
   :jwk-set                    (partial jwk-set json-web-key-set)})

(def routes (load-routes-from-file "routes.txt"))

(defn wrap-bearer-token [handler bearer-token]
  (fn [request]
    (let [request-method (:request-method request)
          request-bearer-token (get-in request [:headers "bearer-token"])]
      (cond
        (= :get request-method) (handler request)
        (= bearer-token request-bearer-token) (handler request)
        :default (do (log/warn (format "Unauthorised request with bearer-token [%s]" request-bearer-token))
                     (-> (r/response {:error "unauthorised"})
                         (r/content-type "application/json")
                         (r/status 401)))))))

(defn handler [db bearer-token json-web-key-set jws-generator]
  (-> (scenic-handler routes (handlers db json-web-key-set jws-generator) not-found-handler)
      (wrap-bearer-token bearer-token)
      (wrap-json-response)
      (ring-defaults/wrap-defaults (if (c/secure?)
                                     (assoc ring-defaults/secure-api-defaults :proxy true)
                                     ring-defaults/api-defaults))
      (wrap-json-body :keywords? false)))

(defn start-server [db host port bearer-token json-web-key-set jws-generator]
  (run-jetty (handler db bearer-token json-web-key-set jws-generator) {:port port :host host}))

(defn -main [& args]
  (let [db (db/connect-to-db (c/mongo-uri))
        json-web-key (jws/generate-json-web-key (jws/generate-key-id))
        json-web-key-set (jws/json-web-key->json-web-key-set json-web-key)
        jws-generator (jws/jws-generator json-web-key)]
    (start-server db (c/app-host) (c/app-port) (c/bearer-token) json-web-key-set jws-generator)))
