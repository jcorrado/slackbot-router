(ns slackbot-router.core-test
  (:require [clojure.test :refer :all]
            [slackbot-router.core :refer :all]
            [slackbot-router.util :refer :all]))

(def table-0-0-0
  [(text-match "table-0-0-0-test-0" "table-0-0-0-test-0")])

(def table-0-0
  [(text-match "table-0-0-test-0" "table-0-0-test-0")
   table-0-0-0
   (text-match "table-0-0-test-1" "table-0-0-test-1")])

(def table-0-1
  [(text-match "table-0-1-test-0" "table-0-1-test-0")])

(def table-0
  [[(fn [m]
      (if (= (:text m) "table-0-test-0")
        m))
    (fn [m] m)]

   [(fn [m]
      (if (= (:text m) "table-0-test-1")
        m))
    (fn [m] m)]

   table-0-0

   table-0-1

   (text-match "table-0-test-2" "table-0-test-2")

   (text-match #"table-0-test-3" "table-0-test-3")])

(def root-table table-0)

;;
;; tests
;;
(deftest test-flatten-routing-table
  (testing "flatten-routing-table fn"
    (let [table           [[:a] [[:b] [:c]] [[:d] [[:e] [:f]]] [[:g]]]
          flattened-table [[:a]  [:b] [:c]   [:d]  [:e] [:f]    [:g]]]
      (is (= (flatten-routing-table table) flattened-table)))))

;; root table
(deftest test-root-table-matches
  (testing "A match in the root table"
    (let [msg {:text "table-0-test-0"}]
      (is (= (route-message root-table msg) msg))))

  (testing "A miss in the root table"
    (let [msg {:text "should-not-match"}]
      (is (nil? (route-message root-table msg)))))

  (testing "A match in the root table, skipping a prior miss"
    (let [msg {:text "table-0-test-1"}]
      (is (= (route-message root-table msg) msg)))))

;; nested subtables
(deftest test-subtable-matches
  (testing "A match in a nested subtable"
    (let [msg {:text "table-0-0-test-0"}]
      (is (= (route-message root-table msg) msg))))

  (testing "A match in a nested subtable"
    (let [msg {:text "table-0-0-test-1"}]
      (is (= (route-message root-table msg) msg))))

  (testing "A match in a nested subtable, two deep"
    (let [msg {:text "table-0-0-0-test-0"}]
      (is (= (route-message root-table msg) msg)))))

;; macros
(deftest test-text-match-macro
  (testing "text-match macro with string"
    (let [msg {:text "table-0-test-2"}]
      (is (= (route-message root-table msg) msg))))

  (testing "text-match macro with regex"
    (let [msg {:text "table-0-test-3"}]
      (is (= (route-message root-table msg) msg)))))
