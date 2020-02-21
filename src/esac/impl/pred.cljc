(ns esac.impl.pred
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

(defn- generate-vector-pred-in-any-order
  [e pred]
  (let [res (gensym)
        tmp (gensym)
        bindings (mapcat (fn [item]
                           `(~res (some->> ~res
                                           (some (fn [~tmp] (and ~(generate-pred tmp item) ~tmp)))
                                           (disj ~res))))
                         (set pred))]
    `(let [~res (set ~e)
           ~@bindings]
       (= #{} ~res))))

(defmulti generate-pred (fn [_ pred] (clause-type pred)))
(defmethod generate-pred ::any [e _] `(any? ~e))
(defmethod generate-pred ::class [e pred] `(instance? ~pred ~e))
(defmethod generate-pred ::var [e pred] `(~pred ~e))
(defmethod generate-pred ::pattern [e pred] `(and (string? ~e) (some? (re-seq ~pred ~e))))
(defmethod generate-pred ::list [e pred] `(or ~@(map #(generate-pred e %) pred)))
(defmethod generate-pred ::map [e pred] `(and ~@(map #(generate-map-pred e %) pred)))
(defmethod generate-pred ::vector [e pred]
  (if (:in-any-order (meta pred))
    (generate-vector-pred-in-any-order e pred)
    `(and ~@(map-indexed #(generate-map-pred e %&) pred))))
(defmethod generate-pred ::object [e pred] `(= ~pred ~e))
