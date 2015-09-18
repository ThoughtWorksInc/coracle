(ns coracle.marshaller
  (:require [clj-time.coerce :as tc]))

(defn coerce-time [m key-path]
  (let [v (get-in m key-path)]
    (if v
      (if-let [l (-> v tc/from-string tc/to-long)]
        (assoc-in m key-path l)
        (assoc-in m (concat [:errors] key-path) "invalid"))
      (assoc-in m (concat [:errors] key-path) "not-present"))))

(defn activity-from-json [activity]
  (if (map? activity)
    (-> activity
        (coerce-time ["@published"]))
    {:error "invalid-json"}
    ))

(defn activity-to-json [activity]
  (-> activity
      (update-in ["@published"] (comp str tc/from-long))))

(defn remove-nil-values [m]
  (->> m
       (remove (fn [[k v]] (nil? v)))
       (into {})))

(defn marshall-query-params [query-params]
  (-> query-params
      (update-in [:from] (comp tc/to-long tc/from-string))
      (update-in [:to] (comp tc/to-long tc/from-string))
      remove-nil-values))


