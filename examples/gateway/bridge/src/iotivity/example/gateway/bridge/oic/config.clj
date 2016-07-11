(ns iotivity.example.gateway.bridge.oic.config
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
  (println "initializing bridge OIC stack in mode " (.getValue (ModeType/SERVER)))
  (let [pc (PlatformConfig. ServiceType/IN_PROC
                            ModeType/SERVER
                            "0.0.0.0"             ;; bind to all available interfaces
                            0                     ;; use randomly available port
                            QualityOfService/LOW)]
    (OcPlatform/Configure pc)))

(defn register-platform
  []
  (let [^OcPlatformInfo platform-info
        (OcPlatformInfo.
         "bPlatformId"
         "bManufactName"
         "www.bridgeurl.com"
         "bridgeModelNumber"
         "bridgeDateOfManufacture"
         "bridgePlatformVersion"
         "Manufacturer OS version"
         "bridgeHardwareVersion"
         "bridgeFirmwareVersion"
         "www.bridgesupporturl.com"
         (str (System/currentTimeMillis)))]
    (try
      (OcPlatform/registerPlatformInfo platform-info)
      (catch OcException e
        (do
          (println "Failed to register platform info.")
          (println (.toString e)))))))

(defn register-device
  []
  (let [device (OcDeviceInfo. "bridgeDevice1" "0.1.0" "sh.1.0")]
    (try
      (OcPlatform/registerDeviceInfo device)
      (catch Exception e
        (prn "caught: " e)))))
