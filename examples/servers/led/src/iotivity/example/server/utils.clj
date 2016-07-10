(ns iotivity.example.server.utils
  (:require [clojure.pprint :as pp])
  (:import [org.iotivity.base
            OcConnectivityType
            OcRepresentation
            OcResource]
           [java.util EnumSet]))

(defn oicrep->edn
  ;; "Representations" communicate resource state from server to
  ;; client. Servers will create an OCRepresentation in
  ;; response to GET requests, populating its values map with state
  ;; data. (More likely, they will maintain such an OCRepresentation
  ;; to avoid mem allocs)

  ;; c++ API OCRepresentation fields
  ;; private:
  ;;     std::string m_host;
  ;;     std::string m_uri;
  ;;     std::vector<OCRepresentation> m_children;
  ;;     mutable std::map<std::string, AttributeValue> m_values;
  ;;     std::vector<std::string> m_resourceTypes;
  ;;     std::vector<std::string> m_interfaces;
  ;;     std::vector<std::string> m_dataModelVersions;
  ;;     InterfaceType m_interfaceType;
  ;; NB: the Java API as currently implemented does not expose all of
  ;; these fields. For example, the C++ API
  ;; exposes "getDataModelVersion", but the Java API does not. Ditto
  ;; for "getChildren", and several other operations.
  [^OcRepresentation rep]
  {:host (.getHost rep)
   :uri  (.getUri  rep)
   :sz   (.size    rep)
   :ts   (vec (.getResourceTypes rep))
   :ifs  (vec (.getResourceInterfaces rep))
   :props (.getValues rep)})

(defn oicresource->edn
  ;; "Resources" are used by clients as server resource proxies. They
  ;; do not capture resource state info (no "values"). Discovery
  ;; queries return Resource values; CRUDN operations go through
  ;; Resource objects, and return Representations.

  ;; So OCResource is really a kind of meta-representation; it
  ;; represents the resource in terms of its meta data (e.g. type) but
  ;; does not represent its state. Its main job is to support CRUDN
  ;; operations rather than data.

  ;; c++ API OcResource fields:
  ;; private:
  ;;     void setHost(const std::string& host);
  ;;     std::weak_ptr<IClientWrapper> m_clientWrapper;
  ;;     std::string m_uri;
  ;;     OCResourceIdentifier m_resourceId;
  ;;     OCDevAddr m_devAddr;
  ;;     bool m_useHostString;
  ;;     bool m_isObservable;
  ;;     bool m_isCollection;
  ;;     std::vector<std::string> m_resourceTypes;
  ;;     std::vector<std::string> m_interfaces;
  ;;     std::vector<std::string> m_children;
  ;;     OCDoHandle m_observeHandle;
  ;;     HeaderOptions m_headerOptions;
  ;; NOTICE: no "values" (i.e. properties)
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

(def device-fields
  {"di", "Device ID: "
   "n", "Device name: "
   "lcv", "Spec version url: "
   "dmv", "Data Model: "})

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

(def rlock (Object.))

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

(defn dump-device-response
  [^OcRepresentation representation]
  (locking dump-lock
    (println "fn: dump-device-response")
    (println (str "\tResource value map:"))
    (pp/pprint (.getValues representation))
    ))
