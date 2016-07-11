(ns iotivity.example.gateway.bridge.oic
  (:require [clojure.pprint :as pp]
            [clojure.core.async :as a]
            [iotivity.example.gateway.bridge.oic.config :as config])
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
;; HashMap List Map]
;; ErrorCode
;; ModeType
;; PlatformConfig
;;  ObserveType
;;  OcException
;;  OcHeaderOption
;;  OcResource
;;  OcResourceIdentifier
;;  QualityOfService
;;  ServiceType]

(println "reloading")

(defn oicrep->edn
  [^OcRepresentation rep]
  {:host (.getHost rep)
   :uri  (.getUri  rep)
   :sz   (.size    rep)
   :ts   (vec (.getResourceTypes rep))
   :ifs  (vec (.getResourceInterfaces rep))
   :props (.getValues rep)})

(def platform-fields
  {"pi",   "Platform ID                    ",
   "mnmn", "Manufacturer name              ",
   "mnml", "Manufacturer url               ",
   "mnmo", "Manufacturer Model No          ",
   "mndt", "Manufactured Date              ",
   "mnpv", "Manufacturer Platform Version  ",
   "mnos", "Manufacturer OS version        ",
   "mnhw", "Manufacturer hardware version  ",
   "mnfv", "Manufacturer firmware version  ",
   "mnsl", "Manufacturer support url       ",
   "st",   "Manufacturer system time       "})

(def dump-lock (Object.))

(defn dump-response
  [^OcRepresentation representation]
  (locking dump-lock
    (println "======= Dump of OcRepresentation response ========")
    (println (str "\tHost: " (.getHost representation)))
    (println (str "\tURI: " (.getUri representation)))
    ;; (println (str "\tNull? " (.isNull representation)))
    (println (str "\tEmpty? " (.isEmpty representation)))
    (println (str "\tSize: " (.size representation)))
    (println (str "\tResource types:"))
    (doseq [rt (.getResourceTypes representation)]
      (println (str "\t\t" rt)))
    (println (str "\tResource interfaces:"))
    (doseq [rif (.getResourceInterfaces representation)]
      (println (str "\t\t" rif)))
    (println (str "\tResource value map:"))
    (pp/pprint (.getValues representation))
    (println "======= end dump ========")
    ))

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

(def device-fields
  {"di", "Device ID: "
   "n", "Device name: "
   "lcv", "Spec version url: "
   "dmv", "Data Model: "})

(defn dump-device-response
  [^OcRepresentation representation]
  (locking dump-lock
    (println "fn: dump-device-response")
    (println (str "\tResource value map:"))
    (pp/pprint (.getValues representation))
    ))

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

(defn oicresource->edn
  [^OcResource r]
  {:thread (.getId (Thread/currentThread))
   :host (.getHost r)
   :uri  (.getUri  r)
   :unique-id (.getUniqueIdentifier r)
   :server-id (.getServerId r)
   :observable? (.isObservable r)
   :ts   (vec (.getResourceTypes r))
   :ifs  (vec (.getResourceInterfaces r))
   :cts  (.getConnectivityTypeSet r)})    ;;  EnumSet<OcConnectivityType>

(defn dump-resource
  [resource]
  (locking rlock
    (println (str "Thread: " (.getId (Thread/currentThread))))
    (println "FOUND RESOURCE:")
    (println (str "\tURI:       " (.getUri resource)))
    (println (str "\tHost addr: " (.getHost resource)))
    (println (str "\tTypes:"))
    (doseq [rt (.getResourceTypes resource)]
      (println (str "\t\t" rt)))
    (println (str "\tInterfaces:"))
    (doseq [if (.getResourceInterfaces resource)]
      (println (str "\t\t" if)))
    (println (str "\tConnTypes:"))
    (doseq [ct (.getConnectivityTypeSet resource)]
      (let [ct-val (.getValue ct)
            ct-lbl (if (= ct-val (.getValue OcConnectivityType/CT_ADAPTER_IP)) "IPv4, IPv6"
                       (if (= ct-val OcConnectivityType/CT_ADAPTER_TCP) "TCP"
                           (if (== ct-val (.getValue OcConnectivityType/CT_IP_USE_V6)) "IPv6 only"
                               (if (= ct-val OcConnectivityType/CT_SCOPE_SITE) "IPv6 Site-Local Scope"
                                   (if (== ct-val (.getValue OcConnectivityType/CT_SCOPE_ORG)) "IPv6 Organization-Local Scope"
                                       (if (= ct-val OcConnectivityType/CT_FLAG_SECURE) "SECURE"
                                           (str ct-val)))))))]
        (println (str "\t\t" ct-lbl))))))

(def o (Object.))
(defrecord Resource
    [channel]
  OcPlatform$OnResourceFoundListener
  (onResourceFound [this, resource]  ;; OcResource
    (if (nil? resource)
      (println "Found resource is invalid")
      (do
        ;;(dump-resource resource)
        (locking o
          (a/>!! channel (oicresource->edn resource))
          #_(if (a/>!! channel (oicresource->edn resource))
              (println "PUT OK" (.getId (Thread/currentThread)))
              (println "PUT FAIL" (.getId (Thread/currentThread))))))))
        ;; (a/close! channel))))
        ;;(a/go (a/>! channel (oicresource->edn resource))))))

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

(defn start
  []
  (println "OIC Gateway server starting...")
  (clojure.lang.RT/loadLibrary "iotivity-jni")
  (config/initialize)
  (config/register-platform)
  #_(config/register-device))

(defn stop
  []
  (println "OIC Gateway bridge shutting down...")
  )
