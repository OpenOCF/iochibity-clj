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
  (println "initializing bridge OIC stack")
  (let [pc (PlatformConfig. ServiceType/IN_PROC
                            ModeType/CLIENT
                            "0.0.0.0"             ;; bind to all available interfaces
                            0                     ;; use randomly available port
                            QualityOfService/LOW)]
    (OcPlatform/Configure pc)))

(defn register-platform
  []
  (let [^OcPlatformInfo platform-info
        (OcPlatformInfo.
         "bridgePlatformId"
         "bridgeManufactName"
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
        (do ;; Log.e(TAG, e.toString());
          (println "Failed to register platform info."))))))


(defn register-device
  []
  (let [device (OcDeviceInfo. "bridgeDevice1" "0.1.0" "sh.1.0")]
    (try
      (OcPlatform/registerDeviceInfo device)
      (catch Exception e
        (prn "caught: " e)))))
