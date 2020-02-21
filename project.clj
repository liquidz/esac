(defproject esac "0.1.0-SNAPSHOT"
  :description "Advanced \"case\" library for Clojure(Script)"
  :url "https;//github.com/liquidz/esac"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :plugins [[lein-cloverage "1.1.1"]]

  :profiles
  {:1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.0"]]}
   :1.10.1 {:dependencies [[org.clojure/clojure "1.10.1"]]}
   :provided [:1.10.1 {:dependencies [[org.clojure/clojurescript "1.10.597"]]}]
   :test {:dependencies [[lambdaisland/kaocha "0.0-590"]
                         [lambdaisland/kaocha-cljs "0.0-68"]
                         [testdoc "0.1.0"]]}
   :dev [:test {:dependencies [[org.clojure/clojure "1.10.1"]]}]}

  :aliases
  {"test-kaocha" ["with-profile" "+dev" "run" "-m" "kaocha.runner"]
   "test-clj" ["with-profile" "test,1.9:test,1.10:test,1.10.1" "test"]
   "test-all" ["do" ["test-clj"] ["test-kaocha"]]}

  :repl-options {:init-ns esac.core})
