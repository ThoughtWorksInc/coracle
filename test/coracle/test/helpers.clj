(ns coracle.test.helpers
  (:require [monger.core :as m]))

(defn finalise-db-interaction [conn]
  (m/drop-db conn "coracle-test")
  (m/disconnect conn))

(defn with-db-do [thing-to-do]
  (let [{:keys [db conn]} (m/connect-via-uri "mongodb://localhost:27017/coracle-test")]
    (try
      (thing-to-do db)
      (finally
        (finalise-db-interaction conn)))))