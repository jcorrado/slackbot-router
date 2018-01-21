(ns slackbot-router.core-test
  (:require [clojure.test :refer :all]
            [slackbot-router.core :refer :all]))

(def test-table-0
  [[(fn [m]
      (if (= m "match-table-0-test-0")
        m))
    (fn [m] m)]

   [(fn [m]
      (if (= m "match-table-0-test-1")
        m))
    (fn [m] m)]])

(deftest test-root-table-matches
  (testing "A match in the root table"
    (is (= (route-message test-table-0 "match-table-0-test-0")
           "match-table-0-test-0")))

  (testing "A miss in the root table"
    (is (nil? (route-message test-table-0 "should-not-match"))))

  (testing "A match in the root table, skipping a prior miss"
    (is (= (route-message test-table-0 "match-table-0-test-1")
           "match-table-0-test-1"))))
