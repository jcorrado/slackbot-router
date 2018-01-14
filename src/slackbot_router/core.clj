(ns slackbot-router.core
  (:require [clojure.core.async :refer [put!]]
            [clojure.spec.alpha :as s]))

(s/check-asserts true)

;; My first use of Spec; imperfect but effective.
(s/def ::type string?)
(s/def ::challenge (s/and string? #(not (empty? %))))
(s/def ::channel (s/and string? #(not (empty? %))))
(s/def ::user (s/and string? #(not (empty? %))))
(s/def ::text string?)

(s/def ::url_verification (s/keys :req-un [::type ::challenge]))
(s/def ::message (s/keys :req-un [::type ::channel ::user ::text]))

(def event-routing-table
  [;; Verification challenge from Slack
   [(fn [ev] (verification-event? ev))
    (fn [ev _]
      {:pre [(s/assert ::url_verification ev)]}
      {:challenge (:challenge ev)})]

   ;; Regular message, handled asynchronously
   [(fn [ev] (if (message-event? ev) (:event ev)))
    (fn [msg c]
      {:pre [(s/assert ::message msg)]}
      (put! c msg) "OK")]])

(defn route-event
  "Route incoming Slack Event to appropriate handler"
  [event reply-c]
  (or (some (fn [[test-fn reply-fn]]
              (if-let [matched (test-fn event)]
                (reply-fn matched reply-c)))
            event-routing-table)
      "OK"))

(def message-routing-table)

(defn route-message
  "Route incoming Slack Message, returning a reply message string or
  nil."
  ([msg] (route-message message-routing-table msg))
  ([table msg]
   (some (fn [[test-fn reply-fn]]
           (if-let [matching-msg (test-fn msg)]
             (reply-fn matching-msg)))
         table)))
