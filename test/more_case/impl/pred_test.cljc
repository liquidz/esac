(ns more-case.impl.pred-test
  (:require #?@(:clj  [[clojure.test :as t]
                       [more-case.impl.pred :as sut]]
                :cljs [[cljs.test :as t :include-macros true]
                       [more-case.impl.pred :as sut]])))

(t/deftest class-symbol-regexp-test
  (t/are [in] (some? (re-seq sut/class-symbol-regexp in))
    "Hello"
    "com.example.Hello"
    "com.Hello"
    "js/Error"))
