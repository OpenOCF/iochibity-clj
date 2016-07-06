(ns iotivity.example.gateway.bridge.server.controller
  (:require [iotivity.example.gateway.bridge.server.config :as config]
            [iotivity.example.gateway.bridge.server.light  :as light])
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

(def light (light/make-light "/a/light" "John's light"))

(def es (java.util.EnumSet/of ResourceProperty/DISCOVERABLE
                              ResourceProperty/OBSERVABLE))
(defn run
  []
  (clojure.lang.RT/loadLibrary "iotivity-jni")
  (config/initialize)
  (config/register-platform)
  (config/register-device)

  (OcPlatform/registerResource (:uri  light)
                               (:type light)
                               (:interface light)
                               light
                               (EnumSet/of ResourceProperty/DISCOVERABLE
                                           ResourceProperty/OBSERVABLE)))
