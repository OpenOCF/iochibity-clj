(ns iotivity.example.gateway.bridge.http
  (:require
   [iotivity.example.gateway.bridge.oic  :as oic]
   [compojure.core :as compojure :refer [GET]]
   [ring.middleware.params :as params]
   [compojure.route :as route]
   [compojure.response :refer [Renderable]]
   [aleph.http :as http]
   [byte-streams :as bs]
   [manifold.stream :as s]
   [manifold.deferred :as d]
   [clojure.core.async :as a]))

(defn oic-platform-handler
  "platform discovery"
  [req]
  (println "http/oic-platform-handler")
  (let [result (a/chan)
        p (oic/->Platform result)]
    (oic/discover-platforms nil p)
    {:status 200
     :headers {"content-type" "text/plain"}
     :body (pr-str (a/<!! (a/go (a/<! result))))}))

(defn oic-device-handler
  "device discovery"
  [req]
  (println "http/oic-device-handler")
  (let [result (a/chan)
        p (oic/->Device result)]
    (oic/discover-devices nil p)
    {:status 200
     :headers {"content-type" "text/plain"}
     :body (pr-str (a/<!! (a/go (a/<! result))))}))

(defn oic-resource-handler
  "resource discovery"
  [req]
  (println "http/oic-resource-handler")
  (let [result (a/chan 10)
        r (oic/->Resource result)
        out (a/chan)]
    (oic/discover-resources r)
    (a/go-loop []
      (if-let [r (a/<! result)]
        (do
          ;; (println "TOOK")
          (a/>! out (pr-str r))
          (recur))
        (do ;; (println "CLOSING")
            (a/close! result)
            (a/close! out))))
    {:status 200
     :headers {"content-type" "text/plain"}
     :body (s/->source out)}))

(def handler
  (params/wrap-params
    (compojure/routes
      (GET "/oic/platforms" [] oic-platform-handler)
      (GET "/oic/devices"   [] oic-device-handler)
      (GET "/oic/resources" [] oic-resource-handler)
      (route/not-found "No such page."))))

;; fixme: defonce global var s
(defn start
  [port]
  (def s (http/start-server handler {:port port})))

(defn stop
  []
  (.close s))
