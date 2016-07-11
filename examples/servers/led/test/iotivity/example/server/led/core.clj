(ns iotivity.minimal.server.core
  (:import [org.iotivity.base
            EntityHandlerResult
            ErrorCode
            ModeType
            ObservationInfo
            OcDeviceInfo
            OcException
            OcPlatform OcPlatform$EntityHandler
            OcPlatformInfo
            OcRepresentation
            OcResource
            OcResourceHandle
            OcResourceRequest
            OcResourceResponse
            PlatformConfig
            QualityOfService
            RequestHandlerFlag
            RequestType
            ResourceProperty
            ServiceType]
           [java.util ArrayList EnumSet LinkedList List Map]
           [java.io Serializable]
           ))

(println (System/getProperty "java.library.path"))

(clojure.lang.RT/loadLibrary "iotivity-jni")

(def pc (PlatformConfig. ServiceType/IN_PROC
                         ModeType/SERVER
                         "0.0.0.0"             ;; bind to all available interfaces
                         0                     ;; use randomly available port
                         QualityOfService/LOW))

(OcPlatform/Configure pc)

(def dev1 (OcDeviceInfo. "myDevice1" "0.1.0" "sh.1.0"))
(def dev2 (OcDeviceInfo. "myDevice2" "0.1.0" "sh.1.0"))
(def dev3 (OcDeviceInfo. "myDevice3" "0.1.0" "sh.1.0"))

(try
  (OcPlatform/registerDeviceInfo dev1)
  (catch Exception e
    (prn "caught: " e)))


;; OcDeviceInfo is Java SDK only
    ;; private String mDeviceName;
    ;; private List<String> mDeviceTypes;

;; octypes.h:
;; typedef struct
;; {
;;     /** Pointer to the device name.*/
;;     char *deviceName;
;;     /** Pointer to the types.*/
;;     OCStringLL *types;
;;     /** Pointer to the device specification version.*/
;;     char *specVersion;
;;     /** Pointer to the device data model version.*/
;;     char *dataModelVersion;
;; } OCDeviceInfo;
;; ocstack.c: OCSetDeviceInfo

;; OcDeviceInfo (note case) is the Java SDK version

(def ^OcPlatformInfo platform-info
  (OcPlatformInfo.
   "myPlatformId"
   "myManufactName"
   "www.myurl.com"
   "myModelNumber"
   "myDateOfManufacture"
   "myPlatformVersion"
   "Manufacturer OS version"
   "myHardwareVersion"
   "myFirmwareVersion"
   "www.mysupporturl.com"
   (str (System/currentTimeMillis))))

(try
  (OcPlatform/registerPlatformInfo platform-info)
  (catch OcException e
    (do ;; Log.e(TAG, e.toString());
      (println "Failed to register platform info."))))

(deftype OICServer []
  )

(defn examine-oc-resource-request
  [request]
  ;; resource/include/OCResourceRequest.h:
    ;; private:
    ;;     std::string m_requestType;
    ;;     std::string m_resourceUri;
    ;;     QueryParamsMap m_queryParameters;
    ;;     int m_requestHandlerFlag;
    ;;     OCRepresentation m_representation;
    ;;     ObservationInfo m_observationInfo;
    ;;     HeaderOptions m_headerOptions;
    ;;     OCRequestHandle m_requestHandle;
    ;;     OCResourceHandle m_resourceHandle;
  (println "resource-request summary:")
  (println (str "request-type: " (.getRequestType request)))
  (println (str "resource-uri: " (.getResourceUri request)))
  (println (str "request-handle: " (.getRequestHandle request)))
  (println (str "resource-handle: " (.getResourceHandle request)))
  ;; etc.
  )

(defn service-create-request
  [request]
  (println "Servicing create request")
  )

(defn service-retrieve-request
  [request]
  (println "Servicing retrieve request")
  )

(defn service-update-request
  [request]
  (println "Servicing update request")
  )

(defn service-delete-request
  [request]
  (println "Servicing delete request")
  )

(defn service-notify-request
  [request]
  (println "Servicing notify request")
  )

(defn dispatch-request
  [request]
  (println "dispatch-request")
  ;; Check for query params (if any)
  ;; Map<String, String> queries = request.getQueryParameters();
  (let [query-params (.getQueryParameters request)]
    (println (str "query params: " query-params " type:" (type query-params)))
    (if (empty? query-params)
      (println "No query parameters in this request")
      (println "Query processing is up to entityHandler"))
    (doseq [entry (.entrySet query-params)]
      (println (str "Query key: "  (.getKey entry)  " value: " (.getValue entry)))))

  ;; CRUDN dispatch
  ;; CRUDN to CoAP mapping: both CREATE and UPDATE may map to either
  ;; POST or PUT, depending on whether the target URI already exists
  ;; on the server:

  ;; POST with exisiting URI, with complete resource rep in payload
  ;;                         -> CREATE new resource with new (server-generated) URI
  ;; POST with exisiting URI, with partial resource rep in payload
  ;;                         -> UPDATE (i.e. MODIFY) existing resource
  ;; POST with new URI       -> "Resource not found" error

  ;; PUT  with new URI       -> CREATE
  ;; PUT  with exisiting URI -> UPDATE - i.e. REPLACE in toto
  ;; In summary, PUT means create or replace, POST means create or modivy

  ;; NOTE: as per the above this dispatch logic is incorrect, but
  ;; we'll stick with it for now since it matches the original Java
  ;; example code
  #_(condp = (.getRequestType request)
    RequestType/PUT (service-create-request request)
    RequestType/GET (service-retrieve-request request)
    RequestType/POST (service-update-request request)
    RequestType/DELETE (service-delete-request request))
  )

(deftype OICLight
    [^String uri, ^String type, ^String interface, ^OcResourceHandle handle,
     ^String name, ^Boolean state, ^Integer power]
  OcPlatform$EntityHandler
  (^EntityHandlerResult
    handleEntity [this, ^OcResourceRequest request]
    (println "Received resource request")
    (if (nil? request)
      (do
        (println "Server request is invalid")
        EntityHandlerResult/ERROR))

    (let [flag-set (.getRequestHandlerFlagSet request)]
      ;; (println "flag-set:")
      ;; (println flag-set)
      ;; (println (str "flag-set: " flag-set " " (type flag-set)))
      (cond
        (.contains flag-set RequestHandlerFlag/INIT)
        (do (println "\t\tRequest Flag: INIT")
            EntityHandlerResult/OK)

        (.contains flag-set RequestHandlerFlag/REQUEST)
        (do (println "\t\tRequest Flag: REQUEST")
            (dispatch-request request))

        (.contains flag-set RequestHandlerFlag/OBSERVER)
        (do (println "\t\tRequest Flag: OBSERVER")
            (service-notify-request request))))))

(defn make-light
  [uri name & {:keys [state power type interface handle]
             :or {state false
                  power 0
                  type "core.light"
                  interface OcPlatform/DEFAULT_INTERFACE
                  handle nil}}]
  (println "uri: " uri)
  (OICLight. uri type interface handle name state power))

(def light-uri-a "/a/light/")
(def light-uri-c "/c/light/")

(def light-name "a John's light")
(def alight (make-light light-uri-a light-name))

(def light-uri-b "/b/light/")
(def light-name "b John's light")
(def blight (make-light light-uri-b light-name))

(def es (java.util.EnumSet/of ResourceProperty/DISCOVERABLE
                              ResourceProperty/OBSERVABLE))

(def light-handle (OcPlatform/registerResource (.uri blight)
                                               (.type blight)
                                               (.interface blight)
                                               blight
                                               (EnumSet/of ResourceProperty/DISCOVERABLE
                                                           ResourceProperty/OBSERVABLE)))

;; (find-resources (str OcPlatform/WELL_KNOWN_QUERY "?rt=intel.fridge"))

;; (defn start-simple-client []
;;   (println "start-simple-client")
;;   (let [pc (PlatformConfig.
;;             ServiceType/IN_PROC
;;             ModeType/CLIENT
;;             "0.0.0.0" ;; By setting to "0.0.0.0", it binds to all available interfaces
;;             0         ;; Uses randomly available port
;;             QualityOfService/LOW)
;;         request-uri (str OcPlatform/WELL_KNOWN_QUERY "?rt=core.light")]
;;     (OcPlatform/Configure pc)
;;     (find-resources (str OcPlatform/WELL_KNOWN_QUERY "?rt=intel.fridge"))
;;     (find-resources request-uri)
;;     ))

;; (defn -main
;;   "I don't do a whole lot ... yet."
;;   [& args]
;;   (clojure.lang.RT/loadLibrary "ocstack-jni")
;;   (start-simple-client)
;;   (Thread/sleep 4000))
