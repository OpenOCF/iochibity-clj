(ns iotivity.minimal.client.core
  (:import [org.iotivity.base
            ErrorCode
            ModeType
            PlatformConfig
            ObserveType
            OcConnectivityType
            OcException
            OcHeaderOption
            OcPlatform OcPlatform$OnResourceFoundListener
            OcRepresentation
            OcResource
            OcResource$OnGetListener
            OcResource$OnPutListener
            OcResource$OnPostListener
            OcResource$OnObserveListener
            OcResourceIdentifier
            QualityOfService
            ServiceType]
           [java.util EnumSet HashMap List Map]
           ))

(clojure.lang.RT/loadLibrary "ocstack-jni")

(deftype Client []
  OcPlatform$OnResourceFoundListener
  (onResourceFound [this, resource]
    (prn "FOUND RESOURCE:")
    (println (str "\tURI:       " (.getUri resource)))
    (println (str "\tHost addr: " (.getHost resource)))
    (println (str "\tTypes: "))
    (doseq [rt (.getResourceTypes resource)]
      (println "\t\t" rt))
    (println (str "\tInterfaces: "))
    (doseq [if (.getResourceInterfaces resource)]
      (println "\t\t" if))
    (println (str "\tConnTypes: "))
    (doseq [ct (.getConnectivityTypeSet resource)]
      (let [ct-val (.getValue ct)
            ct-lbl (if (= ct-val (.getValue OcConnectivityType/CT_ADAPTER_IP)) "IPv4, IPv6"
                       (if (= ct-val OcConnectivityType/CT_ADAPTER_TCP) "TCP"
                       (if (== ct-val (.getValue OcConnectivityType/CT_IP_USE_V6)) "IPv6 only"
                           (if (= ct-val OcConnectivityType/CT_SCOPE_SITE) "IPv6 Site-Local Scope"
                               (if (== ct-val (.getValue OcConnectivityType/CT_SCOPE_ORG)) "IPv6 Organization-Local Scope"
                                   (if (= ct-val OcConnectivityType/CT_FLAG_SECURE) "SECURE"
                                       (str ct-val)))))))]
      (println "\t\t" ct-lbl))))

  OcResource$OnGetListener
  (onGetCompleted [this ;;List<OcHeaderOption>
                   headerOptionList
                   ocRepresentation])
  (onGetFailed [this ex])

  OcResource$OnPutListener
  (onPutCompleted [this ;;List<OcHeaderOption>
                   headerOptionList
                   ocRepresentation])
  (onPutFailed [this  ex])

  OcResource$OnPostListener
  (onPostCompleted [this ;;List<OcHeaderOption>
                   headerOptionList
                    ocRepresentation])
  (onPostFailed [this ex])

  OcResource$OnObserveListener
  (onObserveCompleted [this ;;List<OcHeaderOption>
                       headerOptionList
                       ocRepresentation
                       sequence-number])
  (onObserveFailed [this ex]))

(def client (Client.))

(defn find-resources
  [uri]
  (println "finding " uri)
  (OcPlatform/findResource ""
                           uri
                           (EnumSet/of(OcConnectivityType/CT_DEFAULT)),
                           client))

(def pc (PlatformConfig. ServiceType/IN_PROC
                         ModeType/CLIENT
                         "0.0.0.0" ;; bind to all available interfaces
                         0         ;; use randomly available port
                         QualityOfService/LOW))

(OcPlatform/Configure pc)

;; ;; OcPlatform/WELL_KNOWN_QUERY

(def light-uri
  (str OcPlatform/WELL_KNOWN_QUERY "?rt=core.light"))

(find-resources light-uri)

;; (find-resources (str OcPlatform/WELL_KNOWN_QUERY "?rt=intel.fridge"))

(defn start-simple-client []
  (println "start-simple-client")
  (let [pc (PlatformConfig.
            ServiceType/IN_PROC
            ModeType/CLIENT
            "0.0.0.0" ;; By setting to "0.0.0.0", it binds to all available interfaces
            0         ;; Uses randomly available port
            QualityOfService/LOW)
        request-uri (str OcPlatform/WELL_KNOWN_QUERY "?rt=core.light")]
    (OcPlatform/Configure pc)
    (find-resources (str OcPlatform/WELL_KNOWN_QUERY "?rt=intel.fridge"))
    (find-resources request-uri)
    ))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (clojure.lang.RT/loadLibrary "ocstack-jni")
  (start-simple-client)
  (Thread/sleep 4000))
