(ns iotivity.example.gateway.bridge.server.core
  (:require [iotivity.example.gateway.bridge.server.controller :as controller]
            [clojure.core.async :as a :refer [>!! <!!]])
  (:gen-class))

(defn -main
  [& args]
  (let [c (chan)]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                 (>!! c "Shutting down ")
                                 (println "Goodbye!"))))
    (controller/run)
    (let [msg (<!! c)] ; this will block until the shutdown hook writes to channel c
      (print (str msg (Thread/currentThread) "...")))))

