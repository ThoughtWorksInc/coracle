(ns coracle.db
  (:require [monger.collection :as mc]
            [monger.core :as m]
            [monger.operators :as mop]
            [clojure.walk :refer [stringify-keys]]))

(def coll "activities")

(defn connect-to-db [mongo-uri]
  (-> (m/connect-via-uri mongo-uri)
      :db))

(defn add-activity [db activity]
  (when-not (mc/find-one db coll activity)
    (mc/insert db coll activity)))

(defn assoc-in-query [m map-path value]
  (if value
    (assoc-in m map-path value)
    m))

(defn construct-query [from to]
  (-> {}
      (assoc-in-query ["published" "$gt"] from)
      (assoc-in-query ["published" "$lt"] to)))

(defn fetch-activities [db {:keys [from to]}]
  (let [query (construct-query from to)]
    (->>
      (mc/find-maps db coll query)
      (map #(dissoc % :_id))
      stringify-keys)))

(defn fetch-latest-published-activity [db]
  (->> (mc/aggregate db coll [{mop/$sort {"published" -1}}])
      (map #(dissoc % :_id))
      stringify-keys
      first))