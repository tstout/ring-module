(ns ring-module.router-test
  (:require [clojure.test :refer [run-tests]]
            [expectations.clojure.test :refer [defexpect
                                               expect]]
            [ring-module.router :refer [register-uri-handler
                                        reset-registry!
                                        router]]
            [ring-module.ping]))

(defn get-status [resp]
  (select-keys resp [:status]))

(defexpect default-routes
  (expect {:status 200}
          (get-status
           (router {:uri "/ping" :request-method :get})))

  (expect {:status 200}
          (get-status
           (router {:uri "/favicon.ico" :request-method :get}))))


(defmethod router ["/foo-bar" :get] [_]
  {:status 200})

(defexpect add-route
  (reset-registry!)
  (register-uri-handler (fn [uri]
                          (when (= uri "/foo-bar")
                            uri)))
  (expect {:status 200}
          (get-status
           (router {:uri "/foo-bar" :request-method :get}))))

(comment
  *e
  (run-tests)
  "see https://github.com/clojure-expectations/clojure-test for examples")