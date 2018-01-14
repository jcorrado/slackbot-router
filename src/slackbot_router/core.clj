(ns slackbot-router.core
  (:require [clojure.core.async :refer [put!]]
            [clojure.spec.alpha :as s]))

(s/check-asserts true)

;; This spec is imperfect but effective
(s/def ::type string?)
(s/def ::challenge (s/and string? #(not (empty? %))))
(s/def ::channel (s/and string? #(not (empty? %))))
(s/def ::user (s/and string? #(not (empty? %))))
(s/def ::text string?)

(s/def ::url_verification (s/keys :req-un [::type ::challenge]))
(s/def ::message (s/keys :req-un [::type ::channel ::user ::text]))

;; Now that we're using spec, these feel redundant
(defn- verification-event?
  "Is event a Slack verification challenge?"
  [ev]
  (if (= (:type ev) "url_verification")
    ev))

(defn- message-event?
  "Is event a Slack message?  We filter out bot messages."
  [ev]
  (if (and (= (:type ev) "event_callback")
           (= (get-in ev [:event :type]) "message")
           (not= (get-in ev [:event :subtype]) "bot_message"))
    ev))

(def event-table
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
            event-table)
      "OK"))

(defn route-message
  "Route incoming Slack Message, returning a reply message string or
  nil."
  [table msg]
  (some (fn [[test-fn reply-fn]]
          (if-let [matching-msg (test-fn msg)]
            (reply-fn matching-msg)))
        table))
