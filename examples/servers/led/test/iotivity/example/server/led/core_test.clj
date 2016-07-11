(ns iotivity.example.server.led
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            ;; [iotivity.example.gateway.bridge.core :as core]
            ;; [iotivity.example.gateway.bridge.http :as http]
            ;;[iotivity.example.gateway.bridge.oic  :as oic]
            #_[iotivity.example.gateway.bridge.http :as http])
  (:import [org.iotivity.base
            OcPlatform
            OcPlatform$OnResourceFoundListener
            OcConnectivityType
            OcPlatform
            OcPlatform$OnPlatformFoundListener
            OcPlatform$OnDeviceFoundListener]))

(oic/start)

(def p (oic/->Platform (a/chan)))

(oic/discover-platforms nil p)

(def d (oic/->Device (a/chan)))

(oic/discover-devices nil d)

(def r (oic/->Resource (a/chan)))

(def light-uri
  (str OcPlatform/WELL_KNOWN_QUERY)) ;; "?rt=core.light"))

(oic/find-resources light-uri r)

(oic/find-resources (str OcPlatform/WELL_KNOWN_QUERY "?rt=intel.fridge") c)

(http/start 8089)

(http/stop)

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
