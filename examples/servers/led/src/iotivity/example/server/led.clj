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

(def res-prop (java.util.EnumSet/of ResourceProperty/DISCOVERABLE
                                    ResourceProperty/OBSERVABLE))

(def led-t-light "core.light")

(def led-uri-light "/a/light")
(def led-name-light "Geras Light")
(def light (led/make-led led-name-light led-uri-light led-t-light))
(def l-handle (OcPlatform/registerResource (:uri light)
                                           (:t light)
                                           (:interface light)
                                           light
                                           (EnumSet/of ResourceProperty/DISCOVERABLE
                                                       ResourceProperty/OBSERVABLE)))

(def led-uri-geras "/geras/led")
(def led-geras-name "Geras LED")
(def gled (led/make-led led-geras-name led-uri-geras led-t-light))
(def g-handle (OcPlatform/registerResource (:uri gled)
                                           (:t gled)
                                           (:interface gled)
                                           gled
                                           (EnumSet/of ResourceProperty/DISCOVERABLE
                                                       ResourceProperty/OBSERVABLE)))

(type gled)

;; "core.light"

(def led-name2 "Geras led 2")
(def led-uri-geras2 "/geras/led/2")

(def gled2 (led/make-led led-name2 led-uri-geras2 led-t-light))

(def gled2-handle (OcPlatform/registerResource (:uri gled2)
                                               (:t gled2)
                                               (:interface gled2)
                                               gled2
                                               res-prop))

(def led-uri-geras3 "/geras/led/3")
(def led-name3 "Geras led 3")

(def gled3 (led/make-led led-name3 led-uri-geras3 led-t-light))

(def gled3-handle (OcPlatform/registerResource (:uri gled3)
                                             (:t gled3)
                                             (:interface gled3)
                                             gled3
                                             res-prop))


(defn stop
  []
  (println "OIC server shutting down...")
  )

(println "led server loaded")
