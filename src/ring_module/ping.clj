(ns ring-module.ping
  (:require [ring.util.response :refer [response]]
            [ring-module.router :refer [router]]
            [clojure.data.json :as json])
  (:import [java.lang.management ManagementFactory]
           [java.util.concurrent TimeUnit]))

(defn uptime-ms []
  (.. ManagementFactory getRuntimeMXBean getUptime))

(defn uptime
  ([millis]
   (let [days (.toDays TimeUnit/MILLISECONDS millis)
         daysMillis (.toMillis TimeUnit/DAYS days)
         hours (.toHours TimeUnit/MILLISECONDS (- millis daysMillis))
         hoursMillis (.toMillis TimeUnit/HOURS hours)
         minutes (.toMinutes TimeUnit/MILLISECONDS (- millis daysMillis hoursMillis))
         minutesMillis (.toMillis TimeUnit/MINUTES minutes)
         seconds (.toSeconds TimeUnit/MILLISECONDS (- millis daysMillis hoursMillis minutesMillis))]
     (format "%d days %d hours %d minutes %d seconds"
             days
             hours
             minutes
             seconds)))
  ([] (uptime (uptime-ms))))

(defmethod router ["/ping" :get] [_req]
  (response (let [millis (uptime-ms)]
              (-> {:uptime (uptime millis)
                   :uptime-ms millis}
                  (json/write-str :escape-slash false)))))

(comment
  *e
  (uptime-ms)
  (uptime)
  (time (uptime))
  ;;
  )