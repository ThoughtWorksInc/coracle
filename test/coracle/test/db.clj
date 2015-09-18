(ns coracle.test.db
  (:require [midje.sweet :refer :all]
            [coracle.test.helpers :as h]
            [coracle.db :as db]))

(facts "Can fetch and retrieve activities"
       (h/with-db-do
         (fn [db]
           (db/fetch-activities db) => []
           (db/add-activity db {"@actor" "Jerome Flynn"})
           (db/fetch-activities db) => [{"@actor" "Jerome Flynn"}])))
