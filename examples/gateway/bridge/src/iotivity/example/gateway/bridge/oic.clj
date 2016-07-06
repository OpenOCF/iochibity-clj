(ns iotivity.example.gateway.bridge.oic
  (:require [iotivity.example.gateway.bridge.oic.config :as config])
  (:import [org.iotivity.base
            OcPlatform$OnResourceFoundListener
            OcConnectivityType
            OcPlatform
            OcPlatform$OnPlatformFoundListener
            OcPlatform$OnDeviceFoundListener
            OcRepresentation
            OcResource$OnGetListener
            OcResource$OnPutListener
            OcResource$OnPostListener
            OcResource$OnObserveListener
            ModeType
            PlatformConfig
            QualityOfService
            ServiceType]
           [java.util EnumSet])) ;; HashMap List Map]
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

(def o (Object.))

(defrecord Client []
  OcPlatform$OnPlatformFoundListener
  (onPlatformFound [this, representation] ;; ^OcRepresentation
    (println "onPlatformFound")
    (println (str "Host: " (.getHost representation)))
    )

  OcPlatform$OnDeviceFoundListener
  (onDeviceFound [this, representation] ;; ^OcRepresentation
    (println "onDeviceFound")
    )

  OcPlatform$OnResourceFoundListener
  (onResourceFound [this, resource]
    ;; callbacks are run on separate threads, so for logging we need
    ;; to lock a critical section to avoid msg interleaving
    (locking o
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

(defn discover-platforms
  [host client]
  (println "discovering platforms")
  (OcPlatform/getPlatformInfo ""
                              OcPlatform/WELL_KNOWN_PLATFORM_QUERY ;; uri
                              (EnumSet/of (OcConnectivityType/CT_DEFAULT))
                              client)) ;; protocol: OnPlatformFoundlistener

(defn discover-devices
  [host client]
  (println "discovering devices")
  (OcPlatform/getDeviceInfo ""
                              OcPlatform/WELL_KNOWN_DEVICE_QUERY ;; uri
                              (EnumSet/of (OcConnectivityType/CT_DEFAULT))
                              client)) ;; protocol: OnPlatformFoundlistener

        ;; OCStackResult getPlatformInfo(const std::string& host,
        ;;                          const std::string& platformURI,
        ;;                          OCConnectivityType connectivityType,
        ;;                          FindPlatformCallback platformInfoHandler)
        ;; OCStackResult getPlatformInfo(const std::string& host,
        ;;                          const std::string& platformURI,
        ;;                          OCConnectivityType connectivityType,
        ;;                          FindPlatformCallback platformInfoHandler,
        ;;                          QualityOfService QoS)

(defn find-resources
  [uri client]
  (println (str "finding " uri " (thread " (.getId (Thread/currentThread)) ")"))
  (OcPlatform/findResource ""
                           uri
                           (EnumSet/of(OcConnectivityType/CT_DEFAULT)),
                           client))

(defn start
  []
  (println "OIC server starting...")
  (clojure.lang.RT/loadLibrary "iotivity-jni")
  (config/initialize))

(defn stop
  []
  (println "OIC server shutting down...")
  )
