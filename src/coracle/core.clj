(ns coracle.core
  (:gen-class)
  (:require [scenic.routes :refer :all]
            [ring.util.response :as r]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]]
            [monger.core :as m]
            [monger.collection :as mc]
            [coracle.config :as c]))

(defn not-found-handler [req]
  (-> (r/response {:error "not found"}) (r/status 404)))

(defn add-activity [db req]
  (mc/insert db "activities" (:body req))
  (-> (r/response {}) (r/status 201)))

(defn get-activities [db req]
  (->>
    (mc/find-maps db "activities")
    (map #(dissoc % :_id))
    (r/response)))

(defn _handler [db]
  (scenic-handler (load-routes-from-file "routes.txt")
                  {:add-activity    (partial add-activity db)
                   :show-activities (partial get-activities db)}
                   not-found-handler))

(defn handler [db]
  (-> (_handler db)
      (wrap-json-body :keywords? false)
      (wrap-json-response)))

(def server (atom nil))

(defn start-server [db host port]
  (reset! server (run-jetty (handler db) {:port port :host host})))

(defn -main [& args]
  (prn "starting server...")
  (let [db (-> (m/connect-via-uri (c/mongo-uri)) :db)]
    (start-server db (c/app-host) (c/app-port))))
