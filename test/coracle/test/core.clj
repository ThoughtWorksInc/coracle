(ns coracle.test.core
  (:require [midje.sweet :refer :all]
            [ring.mock.request :as r]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [coracle.test.helpers :as h]
            [coracle.test.jws :as jt]
            [coracle.db :as db]
            [coracle.core :refer [handler wrap-bearer-token]]
            [coracle.jws :as jws]))

(defn post-json [url params]
  (->
    (r/request :post url)
    (assoc :body params)))

(defn activity-json [actor published]
  {"actor"     actor
   "published" (str published)
   "@type"     "create"})

(defn db-activity [actor published]
  {"actor"     actor
   "published" (tc/to-long published)
   "@type"     "create"})

(def timestamp (t/now))

(defn generate-test-handler [parameters]
  (handler (:db parameters) (:bearer-token parameters) (:json-web-key-set parameters) (:jws-generator parameters)))


(facts "Can store json activity"
       (h/with-db-do
         (fn [test-db]
           (let [test-handler (generate-test-handler {:db test-db})
                 request (post-json "/activities" (activity-json "dave" timestamp))]
             (-> (test-handler request) :status) => 201
             (first (db/fetch-activities test-db {})) => (contains {"actor" "dave"})))))

(fact "cannot store duplicate activities"
      (h/with-db-do
        (fn [test-db]
          (let [test-handler (generate-test-handler {:db test-db})
                request (post-json "/activities" (activity-json "dave" timestamp))]
            (-> (test-handler request) :status) => 201
            (-> (test-handler request) :status) => 201
            (count (db/fetch-activities test-db {})) => 1))))

(facts "about wrap bearer token"
       (let [handler (fn [req] :some-handler-response)
             wrapped-handler (wrap-bearer-token handler "secret")]
         (fact "GET requests don't require a bearer token"
               (wrapped-handler (r/request :get "/activities")) => :some-handler-response)

         (fact "POST requests without a bearer token return a 401 response"
               (wrapped-handler (post-json "/activities" nil)) => (contains {:status 401}))

         (fact "POST requests with a valid bearer token calls handler on request"
               (let [request (-> (post-json "/activities" nil)
                                 (assoc :headers {"bearer-token" "secret"}))]
                 (wrapped-handler request) => :some-handler-response))

         (fact "POST requests with a invalid bearer token return a 401 response"
               (let [request (-> (post-json "/activities" nil)
                                 (assoc :headers {"bearer-token" "WRONG"}))]
                 (wrapped-handler request) => (contains {:status 401})))))

(fact "Get 401 if bearer token is invalid"
      (let [test-handler (generate-test-handler {})
            request (-> (post-json "/activities" nil)
                        (assoc :headers {"bearer-token" "invalid"}))
            response (test-handler request)]
        (-> response :status) => 401))

(fact "Get 400 error if json is invalid"
      (h/with-db-do
        (fn [test-db]
          (let [test-handler (generate-test-handler {:db test-db})
                request (post-json "/activities" "asdfas")
                response (test-handler request)]
            (-> response :status) => 400))))

(facts "Can load json activity"
       (h/with-db-do
         (fn [test-db]
           (let [test-handler (generate-test-handler {:db            test-db
                                                      :jws-generator (jws/jws-generator jt/test-json-web-key)})
                 request (r/request :get "/activities")]
             (db/add-activity test-db (activity-json "dave" timestamp))
             (let [response (test-handler request)]
               (fact
                 (-> response :body json/parse-string) => [(activity-json "dave" timestamp)]
                 (get-in response [:headers "Content-Type"]) => "application/activity+json"))))))

(facts "Can load json activitites"
       (h/with-db-do
         (fn [test-db]
           (let [test-handler (generate-test-handler {:db            test-db
                                                      :jws-generator (jws/jws-generator jt/test-json-web-key)})
                 d1 (t/now)
                 d2 (t/plus (t/now) (t/weeks 1))
                 d3 (t/plus (t/now) (t/weeks 2))
                 _ (db/add-activity test-db (db-activity "tofu" d1))
                 _ (db/add-activity test-db (db-activity "bloob" d2))
                 _ (db/add-activity test-db (db-activity "roy" d3))
                 expected-ordered-activities [(activity-json "roy" d3)
                                              (activity-json "bloob" d2)
                                              (activity-json "tofu" d1)]]
             (fact "Can load all (and are sorted in desc time order"
                   (let [request (r/request :get "/activities")]
                     (->> request test-handler :body json/parse-string) => expected-ordered-activities))
             (fact "Can load using time query"
                   (let [request (r/request :get (format "/activities?from=%s&to=%s" d1 d3))]
                     (-> request test-handler :body json/parse-string) => [(activity-json "bloob" d2)]))

             (fact "Can load signed activities using signed query"
                   (let [request (r/request :get "/activities?signed=true")
                         response (test-handler request)]
                     (fact "the correct content-type is used"
                           (-> response :headers (get "Content-Type")) => "application/jose+json")

                     (let [jws-signed-payload (-> response :body json/parse-string (get "jws-signed-payload"))]
                       (fact "the body contains the jws-signed-payload"
                             jws-signed-payload =not=> nil?)
                       (fact "the jws-signed-payload can be decoded and contains the activities"
                             (-> (jt/verify-signature-and-decode jws-signed-payload jt/test-json-web-key)
                                 json/parse-string) => expected-ordered-activities))))))))

(facts "Invalid query parameters return 400"
       (h/with-db-do
         (fn [test-db]
           (let [test-handler (generate-test-handler {:db test-db})
                 request (r/request :get (format "/activities?from=blah&to=blah"))]
             (db/add-activity test-db (db-activity "blah" (t/now)))
             (let [response (test-handler request)]
               (:status response) => 400
               (get-in response [:headers "Content-Type"]) => "application/json; charset=utf-8")))))

(facts "latest published time-stamp"
       (fact "can get last published activity time-stamp"
             (h/with-db-do
               (fn [test-db]
                 (let [test-handler (generate-test-handler {:db test-db})
                       d1 (t/now)
                       d2 (t/plus (t/now) (t/weeks 1))]
                   (db/add-activity test-db (db-activity "tofu" d1))
                   (db/add-activity test-db (db-activity "bloob" d2))
                   (let [response (test-handler (r/request :get "/latest-published-timestamp"))]
                     (:status response) => 200
                     (->> response :body json/parse-string) => {"latest-published-timestamp" (.toString d2)})))))

       (fact "when there are no activities return empty map"
             (h/with-db-do
               (fn [test-db]
                 (let [test-handler (generate-test-handler {:db test-db})]
                   (let [response (test-handler (r/request :get "/latest-published-timestamp"))]
                     (:status response) => 200
                     (->> response :body json/parse-string) => {}))))))

(fact "can get json-web-key-set"
      (let [test-json-web-key-set "some-json-web-key-set-as-a-json-string"
            test-handler (generate-test-handler {:json-web-key-set test-json-web-key-set})
            request (r/request :get "/jwk-set")
            response (test-handler request)]
        (-> response :headers (get "Content-Type")) => "application/json"
        (-> response :body) => test-json-web-key-set))

