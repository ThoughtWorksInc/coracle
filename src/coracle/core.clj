(ns coracle.core
  (:gen-class)
  (:require [scenic.routes :refer :all]
            [ring.util.response :as r]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]]
            [coracle.config :as c]
            [coracle.db :as db]))

(defn not-found-handler [req]
  (-> (r/response {:error "not found"}) (r/status 404)))

(defn add-activity [db req]
  (db/add-activity db (:body req))
  (-> (r/response {}) (r/status 201)))

(defn get-activities [db req]
  (->>
    (db/fetch-activities db)
    (r/response)))

(defn handlers [db]
  {:add-activity    (partial add-activity db)
   :show-activities (partial get-activities db)})

(def routes (load-routes-from-file "routes.txt"))

(defn handler [db]
  (-> (scenic-handler routes (handlers db) not-found-handler)
      (wrap-json-body :keywords? false)
      (wrap-json-response)))

(defn start-server [db host port]
  (run-jetty (handler db) {:port port :host host}))

(defn -main [& args]
  (prn "starting server...")
  (let [db (db/connect-to-db (c/mongo-uri))]
    (start-server db (c/app-host) (c/app-port))))
