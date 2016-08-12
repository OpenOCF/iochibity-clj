(ns iotivity.example.gateway.bridge.oic
  (:require [clojure.pprint :as pp]
            [clojure.core.async :as a]
            [iotivity.example.gateway.bridge.oic.config :as config]
            [iotivity.example.gateway.bridge.utils :as util])
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
           [java.util EnumSet HashMap])
)
(defrecord PlatformDiscoveryService
    [result]
  OcPlatform$OnPlatformFoundListener
  (onPlatformFound [this, representation] ;; ^OcRepresentation
    (println "Platform/onPlatformFound")
    (util/dump-response representation)
    (a/go (a/>! result (util/oicrep->edn representation)))))

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
(defrecord DeviceDiscoveryService
    [result] ;; channel
  OcPlatform$OnDeviceFoundListener
  (onDeviceFound [this, representation] ;; ^OcRepresentation
    (println "onDeviceFound")
    (util/dump-response representation)
    (a/go (a/>! result (util/oicrep->edn representation)))))

(defn discover-devices
  [host client]
  (println "discovering devices")
  (OcPlatform/getDeviceInfo ""
                              OcPlatform/WELL_KNOWN_DEVICE_QUERY ;; uri
                              ;; (EnumSet/of (OcConnectivityType/CT_DEFAULT))
                              ;; (EnumSet/of (OcConnectivityType/CT_ADAPTER_IP)) ;; both IPv4, IPv6
                              (EnumSet/of (OcConnectivityType/CT_IP_USE_V4))
                              client))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defrecord ResourceProxy
    [resource channel]
  OcResource$OnGetListener
  (onGetCompleted [this ;;List<OcHeaderOption>
                   headerOptionList
                   ocRepresentation]
    (println "onGetCompleted")
    (println "Resource URI: " (.getUri ocRepresentation))
    (println "State: " (.getValue ocRepresentation "state"))
    (println "Power: " (.getValue ocRepresentation "power")))
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

;; Map<String, String> queryParams = new HashMap<>();
;; mFoundCollectionResource.get(
;;                              "",
;;                              OcPlatform.DEFAULT_INTERFACE,
;;                              queryParams,
;;                              this

(defn retrieve
  [rproxy]
  (println "retrieving " (.getUri (:resource rproxy)))
  (let [hm (HashMap.)]
    (println "Type hm: " (type hm))
    (.get (:resource rproxy) hm rproxy (QualityOfService/HIGH))))
    ;; (.get (:resource rproxy) "" OcPlatform/DEFAULT_INTERFACE hm rproxy)))

;;(HashMap. {"" ""}) rproxy))

(defn update
  [rproxy k v]  ;; ResourceRequestService value
  (let [rep (OcRepresentation.)]
    (.setValue rep k v)
    (println "putting to " (.getUri (:resource rproxy)) (:resource rproxy))
    (.post (:resource rproxy) rep (HashMap.) rproxy)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def o (Object.))

(defrecord ResourceDiscoveryService
    [channel]
  OcPlatform$OnResourceFoundListener
  (onResourceFound [this, resource]  ;; OcResource
    ;; (println (str "onResourceFound " (.getId (Thread/currentThread))))
    (if (nil? resource)
      (println "Found resource is invalid")
      (do
        ;; (util/dump-resource resource)
        (dosync  ;; locking o  ;; dosync?
         (if (= "/a/light" (.getUri resource))
           (let [rqsthndl (->ResourceProxy resource (a/chan))]
             (println "GETTING")
             (.get resource (HashMap.) rqsthndl)))
         (let [r  (util/oicresource->edn resource)]
           ;; (println (str "putting ("  (.getId (Thread/currentThread)) ") " r))
           (a/>!! channel resource)))))))
          ;; #_(if (a/>!! channel (oicresource->edn resource))
          ;;     (println "PUT OK" (.getId (Thread/currentThread)))
          ;;     (println "PUT FAIL" (.getId (Thread/currentThread)))))))))


(defn discover-resources
  [client & timeout]
  (println (str "discover-resources (thread " (.getId (Thread/currentThread)) ")"))
  (OcPlatform/findResource ""
                           OcPlatform/WELL_KNOWN_QUERY ;; "/oic/res"
                           ;; (EnumSet/of (OcConnectivityType/CT_DEFAULT))
                           ;;(EnumSet/of (OcConnectivityType/CT_ADAPTER_IP)) ;; both IPv4, IPv6
                           (EnumSet/of (OcConnectivityType/CT_IP_USE_V4))
                           client)
  (a/go
    (Thread/sleep (or timeout 1000))
    (println "closing channel  (thread " (.getId (Thread/currentThread)) ")")
    (a/close! (:channel client))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn start
  []
  (println "OIC Gateway server starting...")
  (clojure.lang.RT/loadLibrary "iotivity-jni")
  (config/initialize ModeType/CLIENT)
  ;; no registration for clients?
  #_(config/register-platform)
  #_(config/register-device))

(defn stop
  []
  (println "OIC Gateway bridge shutting down...")
  )

(println "loaded oic")
