(ns coracle.test.marshaller
  (:require [midje.sweet :refer :all]
            [coracle.marshaller :as v]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

(def valid-timestamp (t/date-time 2015 6 7))
(def valid-timestamp-str (str valid-timestamp))
(def valid-timestamp-long (tc/to-long valid-timestamp))

(tabular
  (fact "validate-and-parse-activity validates published timestamp"
    (v/validate-and-parse-activity  ?activity) => ?output)
  ?activity                             ?output
  {}                                    {:error {"published" "not-present"
                                                 "@type" "not-present"}}
  {"published" "blah"
   "@type" ""}                          {"published" "blah"
                                         "@type" ""
                                         :error {"published" "invalid"
                                                 "@type" "invalid"}}
  {"published" valid-timestamp-str
   "@type" 123}                         {"published" valid-timestamp-long
                                         "@type" 123
                                         :error {"@type" "invalid"}}
  {"published" valid-timestamp-str
   "@type" "Add"}                       {"published" valid-timestamp-long
                                         "@type" "Add"})

(fact "can convert back and forth between activity formats"
  (let [original {"published" valid-timestamp-str
                  "@type" "something"}]
    (-> original v/validate-and-parse-activity v/stringify-activity-timestamp) => original))

(tabular
  (fact "marshall-query-params converts parameter timestamp strings into longs when calling a range"
    (v/marshall-query-params ?params) => ?output)
  ?params                       ?output
  {}                            {}
  {:from valid-timestamp-str}   {:from valid-timestamp-long}
  {:from valid-timestamp-str
   :to valid-timestamp-str}     {:from valid-timestamp-long
                                  :to valid-timestamp-long})
