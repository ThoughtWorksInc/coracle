(ns coracle.migration
  (:require [monger.ragtime]                                ;; monger.ragtime required for ragtime migrations to work
            [monger.collection :as coll]
            [ragtime.core :as ragtime]
            [clojure.tools.logging :as log]))

(def collection-name "activities")

(defn update-records [db coll f]
  (let [records (coll/find-maps db coll)]
    (doseq [record records]
      (coll/update-by-id db coll (:_id record) (f record)))))

(defn change-type [record]
  (if-let [type (get record (keyword "@type"))]
    (-> (assoc record :type type)
        (dissoc (keyword "@type")))
    record))

(defn rename-base-type [db]
  (log/info "Running migration rename-object-type")
  (update-records db "activities" change-type)
  (log/info "Finished running migration rename-object-type"))

(defn change-field [record class old-field new-field]
  (if-let [v (get-in record [class old-field])]
    (-> (assoc-in record [class new-field] v)
        (update-in [class] dissoc old-field))
    record))

(defn change-field-type [record class]
  (change-field record class (keyword "@type") :type))

(defn change-field-id [record class]
  (change-field record class (keyword "@id") :id))

(defn change-field-displayname [record class]
  (change-field record class :displayName :name))

(defn rename-actor-type [db]
  (log/info "Running migration rename-actor-type")
  (update-records db collection-name #(change-field-type % :actor))
  (log/info "Finished running migration rename-actor-type"))

(defn rename-target-type [db]
  (log/info "Running migration rename-target-type")
  (update-records db collection-name #(change-field-type % :target))
  (log/info "Finished running migration rename-target-type"))

(defn rename-object-type [db]
  (log/info "Running migration rename-object-type")
  (update-records db collection-name #(change-field-type % :object))
  (log/info "Finished running migration rename-object-type"))

(defn rename-actor-id [db]
  (log/info "Running migration rename-actor-id")
  (update-records db collection-name #(change-field-id % :actor))
  (log/info "Finished running migration rename-actor-id"))

(defn rename-target-id [db]
  (log/info "Running migration rename-target-id")
  (update-records db collection-name #(change-field-id % :target))
  (log/info "Finished running migration rename-target-id"))

(defn rename-object-id [db]
  (log/info "Running migration rename-object-id")
  (update-records db collection-name #(change-field-id % :object))
  (log/info "Finished running migration rename-object-id"))

(defn rename-actor-displayname [db]
  (log/info "Running migration rename-actor-displayname")
  (update-records db collection-name #(change-field-displayname % :actor))
  (log/info "Finished running migration rename-actor-displayname"))

(defn rename-target-displayname [db]
  (log/info "Running migration rename-target-displayname")
  (update-records db collection-name #(change-field-displayname % :target))
  (log/info "Finished running migration rename-target-displayname"))

(defn rename-object-displayname [db]
  (log/info "Running migration rename-object-displayname")
  (update-records db collection-name #(change-field-displayname % :object))
  (log/info "Finished running migration rename-object-displayname"))

;; IMPORTANT DO *NOT* MODIFY THE EXISTING MIGRATION IDS IN THIS LIST
(def migrations
  [{:id "rename-base-type" :up rename-base-type}
   {:id "rename-actor-type" :up rename-actor-type}
   {:id "rename-target-type" :up rename-target-type}
   {:id "rename-object-type" :up rename-object-type}
   {:id "rename-actor-id" :up rename-actor-id}
   {:id "rename-target-id" :up rename-target-id}
   {:id "rename-object-id" :up rename-object-id}
   {:id "rename-actor-displayname" :up rename-actor-displayname}
   {:id "rename-target-displayname" :up rename-target-displayname}
   {:id "rename-object-displayname" :up rename-object-displayname}])

(defn run-migrations
  ([db]
   (run-migrations db migrations))
  ([db migrations]
   (let [index (ragtime/into-index migrations)]
     (ragtime/migrate-all db index migrations))))
