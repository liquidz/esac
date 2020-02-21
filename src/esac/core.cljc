(ns esac.core
  (:refer-clojure :exclude [case])
  (:require
   [esac.impl.pred :as i.pred]))

(defmacro match?
  "Return true if `e` matches `pred`.

  => (match?
  =>  [1 2 {:foo \"bar\" :hello {:value \"world\"}}]
  =>  [odd? _ {:hello {:value #\"^wo\"}}])
  true

  => (match? [3 1 2] [1 2 3])
  false

  => (match? [3 1 2] ^:in-any-order [1 2 3])
  true"
  [e pred]
  (i.pred/generate-pred e pred))

(defmacro case
  "Takes an expression, and a set of clauses.
   Each clause can take the form of either:
     test-constant               result-expr
     test-function               result-expr
     test-class                  result-expr
     test-regex                  result-expr
     test-vector                 result-expr
     test-map                    result-expr
     (test-expr1 ... test-exprN) result-expr

  => (case 10
  =>   12            ::concrete-value
  =>   number?       ::function
  =>   (3 neg?)      ::one-of-them
  =>   Exception      ::class
  =>   #\"re\"         ::regexp
  =>   [5 _ [7]]     ::vector
  =>   {:a {:b :c}}  ::map
  =>   ::default)
  ::function"
  [e & clauses]
  (let [e' (gensym)
        [default clauses] (if (odd? (count clauses))
                            [(last clauses) (drop-last clauses)]
                            [nil clauses])
        cond-clauses (->> (partition 2 clauses)
                          (mapcat (fn [[pred body]]
                                    `((match? ~e' ~pred) ~body))))]
    `(let [~e' ~e]
       (cond ~@cond-clauses
             :else ~default))))
