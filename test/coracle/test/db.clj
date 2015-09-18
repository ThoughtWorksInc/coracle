(ns coracle.test.db
  (:require [midje.sweet :refer :all]
            [coracle.test.helpers :as h]
            [coracle.db :as db]
            [clj-time.core :as t]))

(facts "Can fetch and retrieve activities"
       (h/with-db-do
         (fn [db]
           (db/fetch-activities db) => []
           (db/add-activity db {"@actor" "Jerome Flynn"})
           (db/fetch-activities db) => [{"@actor" "Jerome Flynn"}])))

(defn build-activity [timestamp actor]
  {"@actor" actor
   "@published" timestamp})

(facts "Can retrieve all activities in between dates"
       (h/with-db-do
         (fn [db]
           (let [date1 (t/date-time 2015 11 10 5 4 3 0)
                 date2 (t/plus date1 (t/weeks 1))
                 date3 (t/plus date2 (t/weeks 1))]
             (db/add-activity db (build-activity date1 1))
             (db/add-activity db (build-activity date2 2))
             (db/add-activity db (build-activity date3 3))
             (tabular
               (fact
                 (let [result (db/fetch-activities db :from ?from :to ?to)]
                   (->> result (map #(get % "@actor")) set) => ?result))
               ?from       ?to        ?result
               nil         nil        #{1 2 3}
               date1       nil        #{2 3}
               date2       nil        #{3}
               date3       nil        #{}
               nil         date3      #{1 2}
               nil         date2      #{1}
               nil         date1      #{}
               date1       date3      #{2}
)))))
