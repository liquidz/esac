(ns more-case.core
  (:refer-clojure :exclude [case])
  (:require
   [clojure.set :as set]
   [more-case.impl.pred :as i.pred]))

(defmacro match?
  [e pred]
  (i.pred/generate-pred e pred))

(defmacro case
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


(comment
(let [ls [3 1 2]]
  (match? ls
          ^:any-order
          [1 2 3 ])
  )
)


'(let [a (set ls)
       a (if-let [k (some #(and (match? % 1) %) a)]
           (disj a k))
       a (if-let [k (some #(and (match? % 2) %) a)]
           (disj a k))
       a (if-let [k (some #(and (match? % 3) %) a)]
           (disj a k))
       ]
   (empty? a)
   )

(comment
(let [e [2 1]
      p [1 2]
      es (set e)
      ps (set p)]

  (println "result"
           (reduce (fn [rest-es v]
                     (println "start ---" rest-es v)
                     (if-let [xxx (some #(let [_ (println "checking " % "<=>" v)
                                               o (match? % v)
                                               ]
                                             (and (match? % v) %))
                                        rest-es)]
                       (do (println "disj" rest-es xxx)
                           (disj rest-es xxx))
                       (reduced rest-es))
               ; (if-let [xxx (some #(and (match? % v) %) rest-es)]
               ;   (disj rest-es xxx)
               ;   (reduced rest-es))
                     )
                   es ps)))
)









