(ns coracle.core
  (:gen-class)
  (:require [scenic.routes :refer :all]
            [ring.util.response :as r]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [coracle.config :as c]
            [coracle.db :as db]
            [coracle.marshaller :as m]))

(defn not-found-handler [req]
  (-> (r/response {:error "not found"}) (r/status 404)))

(defn add-activity [db req]
  (->> req
       :body
       m/activity-from-json
      (db/add-activity db))
  (-> (r/response {}) (r/status 201)))

(defn get-activities [db req]
  (let [query-params (-> req :params m/marshall-query-params)]
    (->>
      (db/fetch-activities db query-params)
      (map m/activity-to-json)
      (r/response))))

(defn handlers [db]
  {:add-activity    (partial add-activity db)
   :show-activities (partial get-activities db)})

(def routes (load-routes-from-file "routes.txt"))

(defn handler [db]
  (-> (scenic-handler routes (handlers db) not-found-handler)
      wrap-keyword-params
      wrap-params
      (wrap-json-body :keywords? false)
      (wrap-json-response)))

(defn start-server [db host port]
  (run-jetty (handler db) {:port port :host host}))

(defn -main [& args]
  (prn "starting server...")
  (let [db (db/connect-to-db (c/mongo-uri))]
    (start-server db (c/app-host) (c/app-port))))
