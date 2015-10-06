(ns coracle.marshaller
  (:require [clj-time.coerce :as tc]))

(defn- coerce-time [m key-path optional?]
  (let [time-value (get-in m key-path)]
    (cond
      time-value (if-let [time-as-long (-> time-value tc/from-string tc/to-long)]
                   (assoc-in m key-path time-as-long)
                   (assoc-in m (concat [:error] key-path) "invalid"))
      (not optional?) (assoc-in m (concat [:error] key-path) "not-present")
      :else m)))

(defn validate-type [activity]
  (let [type (get activity "@type")]
    (cond
      (nil? type) (assoc-in activity [:error "@type"] "not-present")
      (not (string? type)) (assoc-in activity [:error "@type"] "invalid")
      (empty? type) (assoc-in activity [:error "@type"] "invalid")
      :else activity)))

(defn validate-and-parse-activity [activity]
  (if (map? activity)
    (-> activity
        (coerce-time ["published"] false)
        validate-type)
    {:error "invalid-json"}))

(defn stringify-activity-timestamp [activity]
  (-> activity
      (update-in ["published"] (comp str tc/from-long))))

(defn remove-nil-values [m]
  (->> m
       (remove (fn [[k v]] (nil? v)))
       (into {})))

(defn marshall-query-params [query-params]
  (-> query-params
      (coerce-time [:from] true)
      (coerce-time [:to] true)
      remove-nil-values))



