(ns iotivity.example.gateway.bridge.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            [iotivity.example.gateway.bridge.utils  :as util]
            [iotivity.example.gateway.bridge.oic  :as oic]
            [iotivity.example.gateway.bridge.http :as http])
  (:import [org.iotivity.base
            OcPlatform
            OcPlatform$OnResourceFoundListener
            OcResource$OnGetListener
            OcConnectivityType
            OcPlatform
            OcPlatform$OnPlatformFoundListener
            OcPlatform$OnDeviceFoundListener]))

(oic/start)

(def p (oic/->PlatformDiscoveryService (a/chan)))

(oic/discover-platforms nil p)

(def d (oic/->DeviceDiscoveryService (a/chan)))

(oic/discover-devices nil d)

;; (def light-uri
;;   (str OcPlatform/WELL_KNOWN_QUERY)) ;; "?rt=core.light"))

(def rds-chan (a/chan))
(def rds (oic/->ResourceDiscoveryService rds-chan))

(def ^:dynamic *resources* (atom {}))

(a/go-loop []
  (if-let [r (a/<! rds-chan)]
    (do
      (print "Resource: ")
      (println (.getUri r))
      (println)
      (swap! *resources* assoc  (.getUri r) r)
      (recur))))

(oic/discover-resources rds)

(doseq [[k v] @*resources*]
  (println k v))

@*resources*

;;;;;;;;;;;;;;;;
;; now let's do something with a resource

(def led (get @*resources* "/a/light"))

(def led-state-key "state")

(def led-chan (a/chan))

(def led-proxy (oic/->ResourceProxy led led-chan))

(oic/retrieve led-proxy)

(oic/update led-proxy led-state-key 1)

;;;;
(def light (get @*resources* "/a/light"))

(util/dump-resource light)

(type light)

(def light-chan (a/chan))

(def light-proxy (oic/->ResourceProxy light light-chan))

(oic/retrieve light-proxy)

(oic/update light-proxy "power" 30)

(util/dump-resource (:resource light-proxy))

;;;;;;;;;;;;;;;;
(http/start 8089)

;;(http/stop)
