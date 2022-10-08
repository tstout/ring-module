(ns ring-module.core-test
  (:require [clojure.test :refer [use-fixtures run-tests]]
            [expectations.clojure.test :refer [defexpect
                                               expect expecting]]))

(defn setup [f] (f))

(use-fixtures :once setup)

(defexpect example-test 
  (expect 1 1))

(comment
  *e
  (run-tests)
  "see https://github.com/clojure-expectations/clojure-test for examples")
