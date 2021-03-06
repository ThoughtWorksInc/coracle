(ns coracle.test.db
  (:require [midje.sweet :refer :all]
            [coracle.test.helpers :as h]
            [coracle.db :as db]))

(facts "Can fetch and retrieve activities"
       (h/with-db-do
         (fn [db]
           (db/fetch-activities db {}) => []
           (db/add-activity db {"actor" "Jerome Flynn"})
           (db/fetch-activities db {}) => [{"actor" "Jerome Flynn"}])))

(fact "cannot store duplicate activities"
      (h/with-db-do
        (fn [db]
          (db/fetch-activities db {}) => []
          (db/add-activity db {"actor" "Jerome Flynn"})
          (db/add-activity db {"actor" "Jerome Flynn"})
          (db/fetch-activities db {}) => [{"actor" "Jerome Flynn"}])))


(defn build-activity [timestamp actor]
  {"actor" actor
   "published" timestamp})

(facts "Can retrieve all activities in between dates"
       (h/with-db-do
         (fn [db]
           (let [date1 100
                 date2 200
                 date3 300]
             (db/add-activity db (build-activity date1 1))
             (db/add-activity db (build-activity date2 2))
             (db/add-activity db (build-activity date3 3))
             (tabular
               (fact
                 (let [result (db/fetch-activities db {:from ?from :to ?to})]
                   (->> result (map #(get % "actor")) set) => ?result))
               ?from       ?to        ?result
               nil         nil        #{1 2 3}
               date1       nil        #{2 3}
               date2       nil        #{3}
               date3       nil        #{}
               nil         date3      #{1 2}
               nil         date2      #{1}
               nil         date1      #{}
               date1       date3      #{2})))))

(fact "Can retrieve the most recently published activity"
      (h/with-db-do
        (fn [db]
          (let [old-activity (build-activity 100 1)
                newest-activity (build-activity 200 2)]
            (db/add-activity db old-activity)
            (db/add-activity db newest-activity)
            (db/fetch-latest-published-activity db) => (just newest-activity)))))