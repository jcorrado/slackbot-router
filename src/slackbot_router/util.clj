(ns slackbot-router.util)

(defn resolve-test-fn
  [test-arg]
  (if (= (type test-arg) java.util.regex.Pattern)
    (partial re-matches test-arg)
    (partial = test-arg)))

(defmacro text-match
  "Generating fns for simple text match route.  We also include some
  output formatting changes."
  [to-match reply]
  `[(fn [msg#]
      (if ((resolve-test-fn ~to-match) (:text msg#))
        true))
    (fn [_#]
      (if (map? ~reply)
        ~reply
        {:text ~reply}))])
