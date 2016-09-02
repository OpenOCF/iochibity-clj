(ns iotivity.mraa.server.core
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

;; (clojure.lang.RT/loadLibrary "iotivity-jni")

(def pc (PlatformConfig. ServiceType/IN_PROC
                         ModeType/SERVER
                         "0.0.0.0"             ;; bind to all available interfaces
                         0                     ;; use randomly available port
                         QualityOfService/LOW))

(OcPlatform/Configure pc)

;; (def dev1 (OcDeviceInfo. "myDevice1" "0.1.0" "sh.1.0"))
(def dev1 (OcDeviceInfo. "myDevice1" (java.util.ArrayList. ["core.light"])))

;; (.getDeviceName dev1)
;; (.getDeviceTypes dev1)

;; (def dev2 (OcDeviceInfo. "myDevice2" "0.1.0" "sh.1.0"))
;; (def dev3 (OcDeviceInfo. "myDevice3" "0.1.0" "sh.1.0"))

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
   "Geras-01"                           ; platform id
   "Geras"                              ; mfg name
   "www.geras.com"                      ; url
   "Geras Model 01"                     ; model number
   "2016-06-01"                         ; date of mfg
   "0.1.0"                              ; platform version
   "0.2.0"                              ; mfg os version
   "0.3.0"                              ; hw version
   "0.4.0"                              ; sw version
   "support.geras.com"               ; support url
   (str (System/currentTimeMillis))))

(try
  (OcPlatform/registerPlatformInfo platform-info)
  (catch OcException e
    (do ;; Log.e(TAG, e.toString());
      (println "Failed to register platform info."))))

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

(def SUCCESS 200)
(defonce NAME_KEY "name")
(defonce STATE_KEY "state")
(defonce POWER_KEY "power")

(defn getOcRepresentation
  []
  (let [rep (OcRepresentation.)]
    (try
      (.setValue rep NAME_KEY "mName")
      (.setValue rep STATE_KEY true)
      (.setValue rep POWER_KEY 30)
      (catch OcException e
        (prn "Failed to set representation values")
        (prn e)))
    rep))

(defn sendResponse
  [^OcResourceResponse response]
  (try
    (OcPlatform/sendResponse response)
    EntityHandlerResult/OK
    (catch OcException e
      (prn "Failed to send response")
      (prn (.toString e))
      EntityHandlerResult/ERROR)))

(defn service-retrieve-request
  [request]
  (println "Servicing retrieve request")
  (let [response (OcResourceResponse.)]
    (.setRequestHandle response (.getRequestHandle request))
    (.setResourceHandle response (.getResourceHandle request))

        ;; if (mIsSlowResponse) { // Slow response case
        ;;     new Thread(new Runnable() {
        ;;         public void run() {
        ;;             handleSlowResponse(request);
        ;;         }
        ;;     }).start();
        ;;     ehResult = EntityHandlerResult.SLOW;
        ;; } else { // normal response case.
    (.setErrorCode response SUCCESS)
    (.setResponseResult response EntityHandlerResult/OK)
    (.setResourceRepresentation response (getOcRepresentation))
    (let [ehResult (sendResponse response)]
      (println "retrieve result: " ehResult)
      ehResult)))

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

(defn dispatch-light-request
  [request]
  (println "Dispatching light request")
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
  (condp = (.getRequestType request)
    RequestType/PUT (service-create-request request)
    RequestType/GET (service-retrieve-request request)
    RequestType/POST (service-update-request request)
    RequestType/DELETE (service-delete-request request)))

(defrecord OICLight
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

    (examine-oc-resource-request request)

    (let [flag-set (.getRequestHandlerFlagSet request)]
      ;; (println (str "flag-set: " flag-set " " (type flag-set)))
      (cond
        (.contains flag-set RequestHandlerFlag/INIT)
        (do (println "\tRequest Flag: INIT")
            EntityHandlerResult/OK)

        (.contains flag-set RequestHandlerFlag/REQUEST)
        (do (println "\tRequest Flag: REQUEST")
            (let [result (dispatch-light-request request)]
              (println "DONE")
              result))

        (.contains flag-set RequestHandlerFlag/OBSERVER)
        (do (println "\tRequest Flag: OBSERVER")
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

(def light-uri "/a/light")
(def light-name "John's a light")
(def light (make-light light-uri light-name))

(def es (java.util.EnumSet/of ResourceProperty/DISCOVERABLE
                              ResourceProperty/OBSERVABLE))

(def light-handle (OcPlatform/registerResource (:uri light)
                                               (:type light)
                                               (:interface light)
                                               light
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
