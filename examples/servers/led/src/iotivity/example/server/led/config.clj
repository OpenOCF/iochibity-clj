(ns iotivity.example.server.led.config
  (:import [org.iotivity.base
            ModeType
            OcDeviceInfo
            OcException
            OcPlatform
            OcPlatformInfo
            PlatformConfig
            QualityOfService
            ServiceType]))

(defn initialize
  []
  (println "loading iotivity-jni...")
  (clojure.lang.RT/loadLibrary "iotivity-jni")
  (println "configuring platform...")
  (let [pc (PlatformConfig. ServiceType/IN_PROC
                            ModeType/SERVER
                            "0.0.0.0"             ;; bind to all available interfaces
                            0                     ;; use randomly available port
                            QualityOfService/HIGH)]
    (OcPlatform/Configure pc)))

(defn register-platform
  []
  (let [platform-info
        (OcPlatformInfo.
         "bPlatformId"
         "bMfgName"
         "www.ledurl.com"
         "ledModelNumber"
         "ledDateOfManufacture"
         "ledPlatformVersion"
         "Manufacturer OS version"
         "ledHardwareVersion"
         "ledFirmwareVersion"
         "www.ledsupporturl.com"
         (str (System/currentTimeMillis)))]
    (try
      (OcPlatform/registerPlatformInfo platform-info)
      (println "registered platform")
      (catch OcException e
        (do
          (println "Failed to register platform info.")
          (println "EXC: " (.toString e)))))))

(defn register-device
  []
  (let [device (OcDeviceInfo. "ledDevice1" "0.1.0" "sh.1.0")]
    (try
      (OcPlatform/registerDeviceInfo device)
      (catch Exception e
        (prn "caught: " e)))
    (println "registered device")))

(println "loaded led config")
