(ns esac.core
  (:refer-clojure :exclude [case])
  (:require
   [clojure.set :as set]
   [esac.impl.pred :as i.pred]))

(defmacro match?
  "FIXME

  => (match? 1 number?)
  true"
  [e pred]
  (i.pred/generate-pred e pred))

(defmacro case
  "FIXME

  => (case 10
  =>   odd? :odd-number
  =>   :even-number)
  :even-number"
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
