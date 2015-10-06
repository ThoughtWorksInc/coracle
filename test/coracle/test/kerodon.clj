(ns coracle.test.kerodon
  (:require [midje.sweet :refer :all]
            [cheshire.core :as json]
            [kerodon.core :as k]
            [coracle.core :as c]))

(defn response-status-is [state status]
  (fact {:midje/name (str "Checking response status is " status)}
        (-> state :response :status) => status)
  state)

(defn response-type-is [state response-type]
  (fact {:midje/name (str "Checking that response type is " response-type)}
        (get-in state [:response :headers "Content-Type"]) => response-type)
  state)

(defn response-body-is [state response-body]
  (fact {:midje/name (str "Checking response body is " response-body)}
        (-> state :response :body) => response-body)
  state)

(fact "can get json-web-key-set"
      (let [json-web-key-set "{\"keys\": \"anything\"}"]
        (-> (k/session (c/handler nil nil json-web-key-set))
            (k/visit "/jwk-set")
            (response-status-is 200)
            (response-type-is "application/json")
            (response-body-is json-web-key-set))))