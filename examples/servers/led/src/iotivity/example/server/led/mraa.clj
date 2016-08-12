(ns iotivity.minimal.server.led.mraa
  (:require [clojure.core.async :as a])
  (:import [mraa Aio Dir Gpio mraa Platform Result Uart UartParity]))

(clojure.lang.RT/loadLibrary "mraajava")

(defn describe-platform
  []
  (let [pt (mraa/getPlatformType)]
    (println "mraa platform description:")
    (println (format "\tName:     %s" (mraa/getPlatformName)))
    (println (format "\tType:     %s" (str pt)))
    ;; getPlatformVersion should return "arduino" on the edison arduino shield
    ;; if it crashes, you need to cycle the power - DO NOT use reset button, pull the main
    (println (format "\tPlatform Version:  %s" (str (mraa/getPlatformVersion 0))))
    (println (format "\tVersion:  %s" (mraa/getVersion)))
    (println (format "\tPincount: %d" (mraa/getPinCount)))
    ))

(describe-platform)

(def gpio-led (Gpio. 13))

(println "gpio-led pin: " (str (.getPin gpio-led false)))

(println "gpio-led pin (raw): " (str (.getPin gpio-led true)))

(.dir gpio-led Dir/DIR_OUT)

(.write gpio-led 1)

(.write gpio-led 0)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OH090U Hall Effect Sensor

(def gpio-hall (Gpio. 8))

(println "gpio-hall pin: " (str (.getPin gpio-hall false)))

(println "gpio-hall pin (raw): " (str (.getPin gpio-hall true)))

(.dir gpio-hall Dir/DIR_IN)

(def ^:dynamic *running* (atom false))

(reset! *running* true)

(a/go
  (while @*running*
    (let [hall (.read gpio-hall)]
      (println "HALL: " hall)
      (Thread/sleep 1000))))

(reset! *running* false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; HC-SR501 PIR motion detector
(def motion (Aio. 0))

(dotimes [n 200]
  (let [i (.read motion)
        f (.readFloat motion)]
    (println (format "%d ADC Motion read %X - %d (%.5f)" n i i f))
    (Thread/sleep 100)))



;; ;;;;;;;;;;;;;;;;
;; (def uart-ftdi (Uart. 0))

;; (let [res  (.setBaudRate uart-ftdi 115200)]
;;   (if (not= res Result/SUCCESS)
;;     (println "Error setting baud rate")
;;     (println "baud rate set to 115200")))

;; (let [res (.setMode uart-ftdi 8 UartParity/UART_PARITY_NONE 1)]
;;   (if (not= res Result/SUCCESS)
;;     (println "Error setting mode")
;;     (println "mode set to 8 bits, parity none, 1 stopbit")))

;; (let [res (.setFlowcontrol uart-ftdi false false)]
;;   (if (not= res Result/SUCCESS)
;;     (println "Error setting flow control")
;;     (println "flow control set to false false")))

;; (.writeStr uart-ftdi "Hello monkeys")

;; (.flush uart-ftdi)
