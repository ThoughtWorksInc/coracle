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

(defn activity-from-request [req]
  (->> req :body m/activity-from-json))

(defn add-activity [db req]
  (let [data (activity-from-request req)]
    (if (empty? (:error data))
      (do (db/add-activity db data)
          (-> (r/response {:status :success}) (r/status 201)))
      (-> (r/response (:error data)) (r/status 400)))))

(defn get-activities [db req]
  (let [query-params (-> req :params m/marshall-query-params)]
    (prn query-params)
    (if (empty? (:error query-params))
      (->> (db/fetch-activities db query-params)
           (map m/activity-to-json)
           (r/response))
      (-> (r/response (:error query-params)) (r/status 400)))))

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
