(ns more-case.impl.pred
  #?(:clj
     (:import
      (clojure.lang
       PersistentArrayMap
       PersistentList
       PersistentVector
       Symbol)
      java.util.regex.Pattern)))

(declare generate-pred)

;;; ========== clause-type ==========

(def class-symbol-regexp
  #"^(([^.]+\.)*|js/)[A-Z].+$")

(defn- clause-symbol-type
  [sym]
  (if (re-seq class-symbol-regexp (name sym))
    ::class
    ::var))

(defmulti clause-type type)
(defmethod clause-type :default
  #?@(:clj [[_] ::object]
      :cljs [[v] (if (regexp? v) ::pattern ::object)]))
(defmethod clause-type Symbol [sym] (if (= '_ sym) ::any (clause-symbol-type sym)))
(defmethod clause-type PersistentArrayMap [_] ::map)
(defmethod clause-type #?(:clj PersistentList :cljs List) [_] ::list)
(defmethod clause-type PersistentVector [_] ::vector)
#?(:clj (defmethod clause-type Pattern [_] ::pattern))

;;; ========== generate-pred ==========

(defn- generate-map-pred
  [e [k v]]
  (generate-pred `(get ~e ~k) v))

(defn- generate-any-order-vector-pred
  [e p]
  (let [result (gensym)
          tmp (gensym)
          bindings (mapcat (fn [v]
                             `(~result (when-let [k# (and ~result
                                                          (some (fn [~tmp] (and ~(generate-pred tmp v) ~tmp))
                                                                ~result))]
                                         (disj ~result k#))))
                           p)]
      `(let [~result (set ~e)
             ~@bindings]
         (empty? ~result)))
  )

(defmulti generate-pred (fn [_ pred] (clause-type pred)))
(defmethod generate-pred ::any [e _] `(any? ~e))
(defmethod generate-pred ::class [e p] `(instance? ~p ~e))
(defmethod generate-pred ::var [e p] `(~p ~e))
(defmethod generate-pred ::pattern [e p] `(and (string? ~e) (some? (re-seq ~p ~e))))
(defmethod generate-pred ::list [e p] `(contains? ~(set p) ~e))
(defmethod generate-pred ::map [e p] `(and ~@(map #(generate-map-pred e %) p)))
(defmethod generate-pred ::vector [e p]
  (if (:any-order (meta p))
    (generate-any-order-vector-pred e p)
    `(and ~@(map-indexed #(generate-map-pred e %&) p))))
(defmethod generate-pred ::object [e p] `(= ~p ~e))
