(ns coracle.config
  (:require [environ.core :as e]))

(defn app-port [] (Integer. (e/env :app-port)))
(defn app-host [] (e/env :app-host))
(defn mongo-uri [] (e/env :mongo-uri))


