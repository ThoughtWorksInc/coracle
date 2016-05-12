(ns coracle.test.migration
  (:require [midje.sweet :refer :all]
            [monger.collection :as monger-c]
            [coracle.migration :as m]
            [coracle.test.helpers :as h]))

(defn test-migration-1 [db]
  (monger-c/insert db "fruit" {:name "orange"}))
(defn test-migration-2 [db]
  (monger-c/insert db "fruit" {:name "lemon"}))
(defn test-migration-3 [db]
  (monger-c/insert db "fruit" {:name "apple"}))

(defn count-fruit [db fruit-type]
  (monger-c/count db "fruit" {:name fruit-type}))

(def migrations [{:id "migration1"
                  :up test-migration-1}
                 {:id "migration2"
                  :up test-migration-2}])

(facts "About running migrations"
       (fact "each migration is only run once"
             (h/with-db-do
               (fn [db]
                 (do
                   (m/run-migrations db migrations)
                   (m/run-migrations db migrations))
                 (count-fruit db "orange") => 1
                 (count-fruit db "lemon") => 1
                 (count-fruit db "apple") => 0)))

       (fact "can run a new migration"
             (h/with-db-do
               (fn [db]
                 (let [new-migration {:id "migration3" :up test-migration-3}
                       updated-migrations (conj migrations new-migration)]
                   (m/run-migrations db updated-migrations)
                   (count-fruit db "orange") => 1
                   (count-fruit db "lemon") => 1
                   (count-fruit db "apple") => 1)))))

(tabular
  (facts "about changing @type to type"
         (h/with-db-do
           (fn [db]
             (monger-c/insert db "activities" ?activity-1)
             (monger-c/insert db "activities" ?activity-2)
             (apply ?migration-function [db])
             (monger-c/count db "activities") => 2
             (get-in (monger-c/find-map-by-id db "activities" "123") ?new-keys) => "Create"
             (get-in (monger-c/find-map-by-id db "activities" "123") ?old-keys) => nil
             (get-in (monger-c/find-map-by-id db "activities" "456") ?new-keys) => "Objective"
             (get-in (monger-c/find-map-by-id db "activities" "456") ?old-keys) => nil)))
  ?activity-1                                        ?activity-2                                           ?migration-function   ?new-keys        ?old-keys
  {:_id "123" (keyword "@type") "Create"}            {:_id "456" (keyword "@type") "Objective"}            m/rename-base-type    [:type]          [(keyword "@type")]
  {:_id "123" :actor {(keyword "@type") "Create"}}   {:_id "456" :actor {(keyword "@type") "Objective"}}   m/rename-actor-type   [:actor :type]   [:actor (keyword "@type")]
  {:_id "123" :target {(keyword "@type") "Create"}}  {:_id "456" :target {(keyword "@type") "Objective"}}  m/rename-target-type  [:target :type]  [:target (keyword "@type")]
  {:_id "123" :object {(keyword "@type") "Create"}}  {:_id "456" :object {(keyword "@type") "Objective"}}  m/rename-object-type  [:object :type]  [:object (keyword "@type")])

(tabular
  (facts "about changing @id to id"
         (h/with-db-do
           (fn [db]
             (monger-c/insert db "activities" ?activity-1)
             (monger-c/insert db "activities" ?activity-2)
             (apply ?migration-function [db])
             (monger-c/count db "activities") => 2
             (get-in (monger-c/find-map-by-id db "activities" "123") ?new-keys) => "onetwothree"
             (get-in (monger-c/find-map-by-id db "activities" "123") ?old-keys) => nil
             (get-in (monger-c/find-map-by-id db "activities" "456") ?new-keys) => "fourfivesix"
             (get-in (monger-c/find-map-by-id db "activities" "456") ?old-keys) => nil)))
  ?activity-1                                           ?activity-2                                           ?migration-function  ?new-keys      ?old-keys
  {:_id "123" :actor {(keyword "@id") "onetwothree"}}   {:_id "456" :actor {(keyword "@id") "fourfivesix"}}   m/rename-actor-id    [:actor :id]   [:actor (keyword "@id")]
  {:_id "123" :target {(keyword "@id") "onetwothree"}}  {:_id "456" :target {(keyword "@id") "fourfivesix"}}  m/rename-target-id   [:target :id]  [:target (keyword "@id")]
  {:_id "123" :object {(keyword "@id") "onetwothree"}}  {:_id "456" :object {(keyword "@id") "fourfivesix"}}  m/rename-object-id   [:object :id]  [:object (keyword "@id")])

(tabular
  (facts "about changing displayName to name"
         (h/with-db-do
           (fn [db]
             (monger-c/insert db "activities" ?activity-1)
             (monger-c/insert db "activities" ?activity-2)
             (apply ?migration-function [db])
             (monger-c/count db "activities") => 2
             (get-in (monger-c/find-map-by-id db "activities" "123") ?new-keys) => "Name"
             (get-in (monger-c/find-map-by-id db "activities" "123") ?old-keys) => nil
             (get-in (monger-c/find-map-by-id db "activities" "456") ?new-keys) => "Person"
             (get-in (monger-c/find-map-by-id db "activities" "456") ?old-keys) => nil)))
  ?activity-1                                 ?activity-2                                   ?migration-function           ?new-keys        ?old-keys
  {:_id "123" :actor {:displayName "Name"}}   {:_id "456" :actor {:displayName "Person"}}   m/rename-actor-displayname    [:actor :name]   [:actor :displayName]
  {:_id "123" :target {:displayName "Name"}}  {:_id "456" :target {:displayName "Person"}}  m/rename-target-displayname   [:target :name]  [:target :displayName]
  {:_id "123" :object {:displayName "Name"}}  {:_id "456" :object {:displayName "Person"}}  m/rename-object-displayname   [:object :name]  [:object :displayName])
