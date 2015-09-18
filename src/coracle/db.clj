(ns coracle.db
  (:require [monger.collection :as mc]
            [monger.core :as m]
            [clojure.walk :refer [stringify-keys]]))

(def coll "activities")

(defn connect-to-db [mongo-uri]
  (-> (m/connect-via-uri mongo-uri)
      :db))

(defn add-activity [db activity]
  (mc/insert db coll activity))

(defn fetch-activities [db]
  (->>
    (mc/find-maps db coll)
    (map #(dissoc % :_id))
    stringify-keys))
