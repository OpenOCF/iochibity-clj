(ns iotivity.example.discovery.server.config
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
         "myPlatformId"
         "myManufactName"
         "www.myurl.com"
         "myModelNumber"
         "myDateOfManufacture"
         "myPlatformVersion"
         "Manufacturer OS version"
         "myHardwareVersion"
         "myFirmwareVersion"
         "www.mysupporturl.com"
         (str (System/currentTimeMillis)))]
    (try
      (OcPlatform/registerPlatformInfo platform-info)
      (catch OcException e
        (do ;; Log.e(TAG, e.toString());
          (println "Failed to register platform info."))))))


(defn register-device
  []
  (let [device (OcDeviceInfo. "myDevice1" "0.1.0" "sh.1.0")]
    (try
      (OcPlatform/registerDeviceInfo device)
      (catch Exception e
        (prn "caught: " e)))))
