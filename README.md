# slackbot-router
[![Clojars Project](https://img.shields.io/clojars/v/jcorrado/slackbot-router.svg)](https://clojars.org/jcorrado/slackbot-router)

slackbot-router is a small routing library for Slack bots, organizing incoming message tests and reply-generators into tables.  Incoming messages are handled asynchronously.

## Installation
Add the following dependency to your `project.clj` file:

	[jcorrado/slackbot-router "0.1.1"]

## Usage

### Bot Setup
A Slack bot is a web application designed to handle events surfaced by a configured Slack workspace.  Slack offers two patterns: the [Real Time Messaging API](https://api.slack.com/rtm) and the simpler [Events API](https://api.slack.com/events-api); I have only used the latter.

Here is a fragment of a Compojure app that shows the basic idea.  Our message routing rules are in `message-table`, detailed below.

You will need to configure Ring, via `project.clj` to use your `init` and `handler` fns.

```clojure
(defproject mybot "0.1.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [jcorrado/slackbot-router "0.1.1"]
                 ;; ...]
  :ring {:init mybot.handler/init
         :handler mybot.handler/app})
```

A simple `mybot.handler` fragment with the basic idea.

```clojure
(def events-c (chan 100))

(defn slack-event-handler
  [req]
  (let [event (:body req)]
    (try
      (response (slackbot-router.core/route-event event events-c))
      (catch java.lang.Exception e
        (println "Unknown Slack message type:" (.getMessage e))
        ;; We always reply 200 to incoming Slack events
        (response "OK")))))

(defroutes app
  (POST "/slack-event" [body] slack-event-handler))

(defn init
  []
  (go-loop []
    (let [msg (<! events-c)]
      (if-let [reply (slackbot-router.core/route-message message-table msg)]
       	;;
        ;; The incoming message passed a test in our routinge table
        ;; and the associated reply generator returned non-nil - do
        ;; something with that here.
        ;;
        ))
    (recur)))
```

### Routing Table Design
A table consists of two-element vectors.  The first element is a test function, to which the message is passed.  If that test returns anything truthy, that returned value is passed to the  second element: the reply-generating function.  A typical design would likely have that reply, in turn, posted to Slack, but that's outside of the domain of `slackbot-router`.

```clojure
(def basic-table
  [(fn [msg] (= msg "!ping"))
   (fn [_] "!pong")])
```

This simple _test-text/reply-text_ pattern is common enough to warrant a macro, which also support regular expressions.

```clojure
(def basic-table
  [(text-match "!ping" "!pong")
   (text-match #"(?i)^.*deploy(ing)? (to )?prod(uction)?"
               "*leeeroy jeeenkins!!1* :hotdog:")])
```

The return of a successful test is passed to the associated reply generating function.
```clojure
(def boromir-table
  [(text-match "Who is Boromir?" "High Warden of the White Tower")

   [(fn [msg]
      (if-let [[_ deed] (re-matches #"(?i)!boromir\s+(.+)\s*" (:text msg))]
        deed))
    (fn [deed]
      (format "*One does not simply %s*" deed))]])]
```

You may nest tables, breaking your routes apart.
```clojure
(def message-table
  [basic-table
   boromir-table]))
```

`slackbot-router` flattens the nested tables and tests, in sequence,
from the top.


## License

Copyright © 2018 Jereme Corrado

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
