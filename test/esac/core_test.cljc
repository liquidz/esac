(ns esac.core-test
  #?@(:clj
      [(:require [clojure.test :as t]
                 [esac.core :as sut]
                 [testdoc.core])]
      :cljs
      [(:require
         [cljs.test :as t :include-macros true]
         [esac.core :as sut :include-macros true])]))

#?(:clj
   (t/deftest docstring-test
     (t/is (testdoc #'sut/match?))
     (t/is (testdoc #'sut/case))))

(t/deftest match?-object-test
  (t/is (true? (sut/match? "hello" "hello")))
  (t/is (false? (sut/match? "hello" "world"))))

(t/deftest match?-var-test
  (t/is (true? (sut/match? "hello" string?)))
  (t/is (false? (sut/match? "hello" number?))))

#?(:clj
   (t/deftest match?-class-test
     (t/is (true? (sut/match? (ex-info "" {})
                              clojure.lang.ExceptionInfo)))
     (t/is (false? (sut/match? (IllegalAccessError. "")
                               clojure.lang.ExceptionInfo))))

   :cljs
   (t/deftest match?-class-test
     (t/is (true? (sut/match? (js/Error. "") js/Error)))))

(t/deftest match?-ant-test
  (doseq [v ["foo" #"foo" 123 (ex-info "" {}) '(1 2 3) [1 2 3] {:foo "bar"}]]
    (t/is (true? (sut/match? v _)))))

(t/deftest match?-pattern-test
  (t/is (true? (sut/match? "hello" #"he.+")))
  (t/is (false? (sut/match? "foo" #"he.+"))))

(t/deftest match?-list-test
  (t/is (true? (sut/match? 1 (1 2))))
  (t/is (true? (sut/match? 2 (1 2))))
  (t/is (false? (sut/match? 3 (1 2)))))

(t/deftest match?-vector-test
  (t/is (true? (sut/match? [1 2] [1 2])))
  (t/is (true? (sut/match? [1 2] [1 _])))
  (t/is (true? (sut/match? [1 2] [1])))
  (t/is (true? (sut/match? [1 2] [])))

  (t/is (true? (sut/match? [1 [2 3]] [1 [2 3]])))
  (t/is (true? (sut/match? [1 [2 3]] [1 [2 _]])))
  (t/is (true? (sut/match? [1 [2 3]] [1 [_ 3]])))
  (t/is (true? (sut/match? [1 [2 3]] [1 [2]])))
  (t/is (true? (sut/match? [1 [2 3]] [1 _])))
  (t/is (true? (sut/match? [1 [2 3]] [1])))

  (t/is (false? (sut/match? [1 2] [1 8])))
  (t/is (false? (sut/match? [1 2] [9 2])))
  (t/is (false? (sut/match? [1 2] [9 8])))
  (t/is (false? (sut/match? [1 2] [9]))))

(t/deftest match?-vector-any-order-test
  (t/is (true? (sut/match? [1 2 3] ^:in-any-order [1 2 3])))
  (t/is (true? (sut/match? [2 3 1] ^:in-any-order [1 2 3])))
  (t/is (true? (sut/match? [3 1 2] ^:in-any-order [1 2 3])))

  (t/is (false? (sut/match? [2 3 1] [1 2 3])))
  (t/is (false? (sut/match? [3 1 2] [1 2 3])))

  (t/is (true? (sut/match? [{:a "x" :c "y"} {:a "b" :c "d"}]
                           ^:in-any-order [{:a "b"} {:a "x"}])))
  (t/is (false? (sut/match? [{:a "x" :c "y"} {:a "b" :c "d"}]
                            [{:a "b"} {:a "x"}]))))

(t/deftest match?-map-test
  (t/is (true? (sut/match? {:a :b :c :d} {:a :b :c :d})))
  (t/is (true? (sut/match? {:a :b :c :d} {:a :b :c _})))
  (t/is (true? (sut/match? {:a :b :c :d} {:a :b})))
  (t/is (true? (sut/match? {:a :b :c :d} {:a _})))
  (t/is (true? (sut/match? {:a :b :c :d} {:c :d})))
  (t/is (true? (sut/match? {:a :b :c :d} {:c _})))
  (t/is (true? (sut/match? {:a :b :c :d} {})))

  (t/is (true? (sut/match? {:a {:b :c :d :e}} {:a {:b :c :d :e}})))
  (t/is (true? (sut/match? {:a {:b :c :d :e}} {:a {:b :c :d _}})))
  (t/is (true? (sut/match? {:a {:b :c :d :e}} {:a {:b :c}})))
  (t/is (true? (sut/match? {:a {:b :c :d :e}} {:a {:b _}})))
  (t/is (true? (sut/match? {:a {:b :c :d :e}} {:a _})))
  (t/is (true? (sut/match? {:a {:b :c :d :e}} {})))

  (t/is (false? (sut/match? {:a :b :c :d} {:a :b :c :y})))
  (t/is (false? (sut/match? {:a :b :c :d} {:a :x :c :d})))
  (t/is (false? (sut/match? {:a :b :c :d} {:a :x :c :y})))
  (t/is (false? (sut/match? {:a :b :c :d} {:a :x})))
  (t/is (false? (sut/match? {:a :b :c :d} {:c :y}))))

(t/deftest case-default-test
  (t/is (nil? (sut/case true
                false "foo")))
  (t/is (= "bar" (sut/case true
                   false "foo"
                   "bar"))))

(defn- test-case
  [v]
  (sut/case v
    "foo" ::ok1
    #"f+" ::ok2
    ("hello" "world") ::ok3
    string? ::ok4
    ::default))

(t/deftest case-test
  (t/are [expected in] (= expected (test-case in))
    ::ok1 "foo"
    ::ok2 "foooo"
    ::ok3 "hello"
    ::ok3 "world"
    ::ok4 "bar"
    ::default (ex-info "" {})))
