(ns ring-module.core
  (:require
   [taoensso.timbre :as log]
   [iapetos.collector.ring :as ring]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring-module.ping]
   [ring-module.router :refer [handler registry]]))

(defn init [_]
  (log/info "Starting server on port 8080")
  (run-jetty (-> #'handler
                 #_(wrap-env env)
                 (ring/wrap-metrics registry)
                 #_(wrap-stacktrace-log))
             {:join? false
              :port 8080}))

(defn -main [& args]
  (init nil))

