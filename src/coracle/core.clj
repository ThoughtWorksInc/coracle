(ns coracle.core
  (:gen-class)
  (:require [scenic.routes :refer :all]
            [ring.util.response :as r]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def _handler (scenic-handler (load-routes-from-file "routes.txt")
                              {:status (fn [request] (r/response {:server "alive"}))}
                              (fn [request] (r/response {:error "not found"}))))

(def handler (-> _handler
                 wrap-json-response))

(def port 7000)
(def server (atom nil))

(defn start-server [port]
  (reset! server (run-jetty handler {:port port})))

(defn -main
  ""
  [& args]
  (prn "starting server...")
  (start-server port))
