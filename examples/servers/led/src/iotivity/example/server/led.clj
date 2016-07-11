(ns iotivity.minimal.server.led
  (:require [iotivity.sensor.led :as led]
            [iotivity.example.server.led.config :as config])
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
           [mraa Dir Gpio Platform mraa]
           [java.util ArrayList EnumSet LinkedList List Map]
           [java.io Serializable]
           ))

(defn start
  []
  (println "OIC LED server starting...")
  (config/initialize)
  ;; (clojure.lang.RT/loadLibrary "mraajava")
  ;; (mraa/initialize)
  )

(start)

(config/register-platform)

(config/register-device)

(def led-uri-geras "/geras/led/")
(def led-geras-name "Geras LED")
(def gled (led/make-led led-uri-geras led-geras-name))
(def g-handle (OcPlatform/registerResource (:uri gled)
                                           (:type gled)
                                           (:interface gled)
                                           gled
                                           (EnumSet/of ResourceProperty/DISCOVERABLE
                                                       ResourceProperty/OBSERVABLE)))

(def led-uri-a "/a/led/")
(def led-name "a John's led")
(def aled (led/make-led led-uri-a led-name))

(def es (java.util.EnumSet/of ResourceProperty/DISCOVERABLE
                              ResourceProperty/OBSERVABLE))

(def led-handle (OcPlatform/registerResource (:uri aled)
                                             (:type aled)
                                             (:interface aled)
                                             aled
                                             (EnumSet/of ResourceProperty/DISCOVERABLE
                                                         ResourceProperty/OBSERVABLE)))

(def led-uri-b "/b/led/")
(def led-name "b John's led")
(def bled (led/make-led led-uri-b led-name))

(def led-handle (OcPlatform/registerResource (:uri bled)
                                             (:type bled)
                                             (:interface bled)
                                             bled
                                             (EnumSet/of ResourceProperty/DISCOVERABLE
                                                         ResourceProperty/OBSERVABLE)))

(defn stop
  []
  (println "OIC server shutting down...")
  )
