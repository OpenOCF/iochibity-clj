(ns iotivity.example.server.oic.client
  (:require [clojure.pprint :as pp]
            [clojure.core.async :as a]
            [iotivity.example.server.oic.config :as config])
  (:import [org.iotivity.base
            OcPlatform$OnResourceFoundListener
            OcConnectivityType
            OcPlatform
            OcPlatform$OnPlatformFoundListener
            OcPlatform$OnDeviceFoundListener
            OcRepresentation
            OcResource
            OcResource$OnGetListener
            OcResource$OnPutListener
            OcResource$OnPostListener
            OcResource$OnObserveListener
            ModeType
            PlatformConfig
            QualityOfService
            ServiceType]
           [java.util EnumSet]))

;; client: discovery request routines

(def plock (Object.))

(defrecord Platform
    [result]
  OcPlatform$OnPlatformFoundListener
  (onPlatformFound [this, representation] ;; ^OcRepresentation
    (println "Platform/onPlatformFound")
    (dump-response representation)
    (a/go (a/>! result (oicrep->edn representation)))))

(defn discover-platforms
  [host client]
  (println "oic/discover-platforms")
  (OcPlatform/getPlatformInfo nil                                  ;; multicast
                              OcPlatform/WELL_KNOWN_PLATFORM_QUERY ;; uri
                              ;; (EnumSet/of (OcConnectivityType/CT_DEFAULT))
                              ;; (EnumSet/of (OcConnectivityType/CT_ADAPTER_IP)) ;; both IPv4, IPv6
                              (EnumSet/of (OcConnectivityType/CT_IP_USE_V4))
                              ;; (EnumSet/of (OcConnectivityType/CT_IP_USE_V6))
                              client)) ;; protocol: OnPlatformFoundlistener

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defrecord Device
    [result] ;; channel
  OcPlatform$OnDeviceFoundListener
  (onDeviceFound [this, representation] ;; ^OcRepresentation
    (println "onDeviceFound")
    (dump-response representation)
    (a/go (a/>! result (oicrep->edn representation)))))

(defn discover-devices
  [host client]
  (println "discovering devices")
  (OcPlatform/getDeviceInfo ""
                              OcPlatform/WELL_KNOWN_DEVICE_QUERY ;; uri
                              ;; (EnumSet/of (OcConnectivityType/CT_DEFAULT))
                              (EnumSet/of (OcConnectivityType/CT_IP_USE_V4))
                              client)) ;; protocol: OnPlatformFoundlistener

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def rlock (Object.))

(defrecord Client
    [channel]
  OcPlatform$OnResourceFoundListener
  (onResourceFound [this, resource]  ;; OcResource
    (if (nil? resource)
      (println "Found resource is invalid")
      (do
        ;;(dump-resource resource)
        (locking rlock
          (a/>!! channel (oicresource->edn resource))
          #_(if (a/>!! channel (oicresource->edn resource))
              (println "PUT OK" (.getId (Thread/currentThread)))
              (println "PUT FAIL" (.getId (Thread/currentThread))))))))
        ;; (a/close! channel))))
        ;;(a/go (a/>! channel (oicresource->edn resource))))))
  )

(defrecord Resource
    [channel]
  OcResource$OnGetListener
  (onGetCompleted [this ;;List<OcHeaderOption>
                   headerOptionList
                   ocRepresentation]
    (println "onGetCompleted"))
  (onGetFailed [this ex]
        (println "onGetFailed"))

  OcResource$OnPutListener
  (onPutCompleted [this ;;List<OcHeaderOption>
                   headerOptionList
                   ocRepresentation]
    (println "onPutCompleted"))
  (onPutFailed [this  ex]
    (println "onPutFailed"))

  OcResource$OnPostListener
  (onPostCompleted [this ;;List<OcHeaderOption>
                   headerOptionList
                    ocRepresentation]
    (println "onPostCompleted"))
  (onPostFailed [this ex]
    (println "onPostFailed"))

  OcResource$OnObserveListener
  (onObserveCompleted [this ;;List<OcHeaderOption>
                       headerOptionList
                       ocRepresentation
                       sequence-number]
    (println "onObserveCompleted"))
  (onObserveFailed [this ex]
    (println "onObserveFailed")))

(defn discover-resources
  [client & timeout]
  (println (str "discover-resources (thread " (.getId (Thread/currentThread)) ")"))
  (OcPlatform/findResource ""
                           OcPlatform/WELL_KNOWN_QUERY ;; "/oic/res"
                           (EnumSet/of(OcConnectivityType/CT_DEFAULT)),
                           client)
  (a/go
    (Thread/sleep (or timeout 1000))
    (a/close! (:channel client))))
