(ns coracle.marshaller
  (:require [clj-time.coerce :as tc]))

(defn coerce-time [m key-path optional?]
  (let [v (get-in m key-path)]
    (cond
      v
      (if-let [l (-> v tc/from-string tc/to-long)]
        (assoc-in m key-path l)
        (assoc-in m (concat [:errors] key-path) "invalid"))
      (not optional?)
      (assoc-in m (concat [:errors] key-path) "not-present")
      :else m)))

(defn activity-from-json [activity]
  (if (map? activity)
    (-> activity
        (coerce-time ["@published"] false))
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
      (coerce-time [:from] true)
      (coerce-time [:to] true)
      remove-nil-values))


