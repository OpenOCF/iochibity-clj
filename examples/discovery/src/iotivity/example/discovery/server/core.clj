(ns iotivity.example.discovery.server.core
  (:require [iotivity.example.discovery.server.controller :as controller]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]])
  (:gen-class))
   ;; :implements [org.apache.commons.daemon.Daemon]))

;; (println (System/getProperty "java.library.path"))

(defn -main
  [& args]
  (let [c (chan)]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (>!! c "Shutting down ")
                               (println "Goodbye!"))))
  ;; (.start (Thread. (fn [] (controller/run))))
  (controller/run)
  (let [msg (<!! c)]
    (print (str msg (Thread/currentThread) "..."))))) ; this will block until the shutdown hook writes to channel c

