(ns coracle.test.marshaller
  (:require [midje.sweet :refer :all]
            [coracle.marshaller :as v]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

(def valid-timestamp (t/date-time 2015 6 7))
(def valid-timestamp-str (str valid-timestamp))
(def valid-timestamp-long (tc/to-long valid-timestamp))

(tabular
  (fact
    (v/activity-from-json  ?activity) => ?output)
  ?activity                             ?output
  {}                                    {:error {"@published" "not-present"}}
  {"@published" "blah"}                 {"@published" "blah"
                                         :error {"@published" "invalid"}}
  {"@published" valid-timestamp-str}    {"@published" valid-timestamp-long}
  )

(fact "can convert back to original format"
  (let [original {"@published" valid-timestamp-str}]
    (-> original v/activity-from-json v/activity-to-json) => original))

(tabular
  (fact
    (v/marshall-query-params ?params) => ?output)
  ?params                       ?output
  {}                            {}
  {:from valid-timestamp-str}   {:from valid-timestamp-long}
  {:from valid-timestamp-str
   :to valid-timestamp-str}     {:from valid-timestamp-long
                                  :to valid-timestamp-long}
  )
