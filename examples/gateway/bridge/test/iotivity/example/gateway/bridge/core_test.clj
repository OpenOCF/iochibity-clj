(ns iotivity.example.gateway.bridge.core-test
  (:require [clojure.test :refer :all]
            ;; [iotivity.example.gateway.bridge.core :as core]
            ;; [iotivity.example.gateway.bridge.http :as http]
            [iotivity.example.gateway.bridge.oic  :as oic])
  (:import [org.iotivity.base
            OcPlatform
            OcPlatform$OnResourceFoundListener
            OcConnectivityType
            OcPlatform
            OcPlatform$OnPlatformFoundListener
            OcPlatform$OnDeviceFoundListener]))

(oic/start)

(def c (oic/->Client))

(oic/discover-platforms nil c)

(oic/discover-devices nil c)

(def light-uri
  (str OcPlatform/WELL_KNOWN_QUERY)) ;; "?rt=core.light"))

(oic/find-resources light-uri c)

(oic/find-resources (str OcPlatform/WELL_KNOWN_QUERY "?rt=intel.fridge") c)

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
