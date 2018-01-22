(ns slackbot-router.core-test
  (:require [clojure.test :refer :all]
            [slackbot-router.core :refer :all]
            [slackbot-router.util :refer :all]))

(def test-table-0
  [[(fn [m]
      (if (= (:text m) "table-0-test-0")
        m))
    (fn [m] m)]

   [(fn [m]
      (if (= (:text m) "table-0-test-1")
        m))
    (fn [m] m)]

   (text-match "table-0-test-2" "table-0-test-2")

   (text-match #"table-0-test-3" "table-0-test-3")])

(deftest test-root-table-matches
  (testing "A match in the root table"
    (let [msg {:text "table-0-test-0"}]
      (is (= (route-message test-table-0 msg) msg))))

  (testing "A miss in the root table"
    (let [msg {:text "should-not-match"}]
      (is (nil? (route-message test-table-0 msg)))))

  (testing "A match in the root table, skipping a prior miss"
    (let [msg {:text "table-0-test-1"}]
      (is (= (route-message test-table-0 msg) msg)))))

(deftest test-text-match-macro
  (testing "text-match macro with string"
    (let [msg {:text "table-0-test-2"}]
      (is (= (route-message test-table-0 msg) msg))))

  (testing "text-match macro with regex"
    (let [msg {:text "table-0-test-3"}]
      (is (= (route-message test-table-0 msg) msg)))))
