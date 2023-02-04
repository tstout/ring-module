(ns ring-module.core
  (:require
   #_[clojure.tools.logging :as log]
   [iapetos.collector.ring :as ring]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.params :refer [wrap-params]]
   [ring-module.ping]
   [ring-module.router :refer [handler registry]]))

(defn init [_]
  ;;(log/info "Starting jetty server on port 8080")
  (run-jetty (-> #'handler
                 wrap-params
                 #_(wrap-env env)
                 (ring/wrap-metrics registry)
                 #_(wrap-stacktrace-log))
             {:join? false
              :port 8080}))

(defn -main [& args]
  (init nil))