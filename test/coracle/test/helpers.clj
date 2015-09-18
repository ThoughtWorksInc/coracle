(ns coracle.test.helpers
  (:require [monger.core :as m]))

(defn with-db-do [thing-to-do]
  (let [{:keys [db conn]} (m/connect-via-uri "mongodb://localhost:27017/coracle-test")]
    (thing-to-do db)
    (m/drop-db conn "coracle-test")))
