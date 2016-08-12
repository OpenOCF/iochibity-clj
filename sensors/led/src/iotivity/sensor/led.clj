(ns iotivity.sensor.led
  (:import [org.iotivity.base
            EntityHandlerResult
            OcPlatform
            OcPlatform$EntityHandler
            OcResourceHandle
            OcResourceRequest
            RequestHandlerFlag
            RequestType
            ]))

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
  (println "dispatch-request: " (.getResourceUri request))
  ;; Check for query params (if any)
  ;; Map<String, String> queries = request.getQueryParameters();
  (let [query-params (.getQueryParameters request)]
    (println (str "query params: " query-params " type:" (type query-params)))
    (if (empty? query-params)
      (println "No query parameters in this request")
      (println "Query processing is up to entityHandler"))
    (doseq [entry (.entrySet query-params)]
      (println (str "Query key: "  (.getKey entry)  " value: " (.getValue entry)))))
  (condp = (.getRequestType request)
    RequestType/PUT (service-create-request request)
    RequestType/GET (service-retrieve-request request)
    RequestType/POST (service-update-request request)
    RequestType/DELETE (service-delete-request request))
  EntityHandlerResult/OK
  )

(defrecord OICLed
    [^String uri, ^String t, ^String interface, ^OcResourceHandle handle,
     ^String name, ^Boolean state, ^Integer power
     ^Boolean discoverable?, ^Boolean observable?]
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

(defn make-led
  [name uri t & {:keys [interface handle
                      state power discoverable? observable?]
             :or {state false
                  power 0
                  t t
                  interface OcPlatform/DEFAULT_INTERFACE
                  discoverable? true
                  observerable? true
                  handle nil}}]
  (println (format "making led name %s, uri %s, type %s" name uri t))
  (OICLed. uri t interface handle name state power discoverable? observable?))

(println "led sensor loaded")
