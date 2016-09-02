(set-env! :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [org.clojure/tools.namespace "0.3.0-alpha3"]
                            [org.clojure/core.async "0.2.385"]])

(task-options! repl {:port 8088})
