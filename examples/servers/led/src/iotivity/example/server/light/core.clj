(ns iotivity.example.gateway.bridge.core
  (:require [iotivity.example.gateway.bridge.http :as http]
            [iotivity.example.gateway.bridge.oic :as oic]
            [clojure.core.async :as a :refer [>!! <!!]])
  (:gen-class))

(defn -main
  [& args]
  (let [c (a/chan)]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                 ;; (controller/stop)
                                 (oic/stop)
                                 (http/stop)
                                 (>!! c "Shutting down ")
                                 (println "Goodbye!"))))
    ;; (controller/start)
    (oic/start)
    (http/start 8088)
    (let [msg (<!! c)] ; this will block until the shutdown hook writes to channel c
      (print (str msg (Thread/currentThread) "...")))))

