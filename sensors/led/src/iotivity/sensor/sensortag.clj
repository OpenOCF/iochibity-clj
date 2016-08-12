(ns iotivity.sensor.sensortag
  (:import [tinyb BluetoothManager])
  #_(:import [org.iotivity.base
            EntityHandlerResult
            OcPlatform
            OcPlatform$EntityHandler
            OcResourceHandle
            OcResourceRequest
            RequestHandlerFlag
            RequestType
            ]))

(def red-uuid   "68:C9:0B:05:C8:87")
(def green-uuid "68:C9:0B:05:BE:02")

(def ^:dynamic *running* (atom false))

(def manager (BluetoothManager/getBluetoothManager))

manager

(defn print-device
  [^BluetoothDevice device]
  (println "Address = " (.getAddress device))
  (println " Name = " (.getName device))
  (println " Connected = " (.getConnected device)))

(defn ->celsius
  [raw]
  (/ raw 128.0))


(defn get-device
  [address]
  (loop [i 0
         devices (.getDevices manager)]
    (doseq [dev devices] (print-device dev))
    (let [dev (filter #(= address (.getAddress %)) devices)]
      (if (not (nil? dev))
        dev
        (if (> i 15)
          (do (println "device not found"))
          (do (Thread/sleep 4000)
              (recur (inc i) (.getDevices manager))))))))

(defn start
  []
  (reset! *running* true)
  (let [started (.manager startDiscovery)
        sensor (getDevice red-uuid)]
    (println "The discovery started: " (str started))


