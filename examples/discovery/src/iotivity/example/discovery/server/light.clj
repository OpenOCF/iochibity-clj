(ns iotivity.example.discovery.server.light
  (:import [org.iotivity.base
            EntityHandlerResult
            OcPlatform
            OcPlatform$EntityHandler
            OcResourceHandle
            OcResourceRequest
            RequestHandlerFlag
            ]))

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
  EntityHandlerResult/OK
  )

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
