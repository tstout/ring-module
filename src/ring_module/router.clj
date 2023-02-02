(ns ring-module.router
  (:require
   [iapetos.collector.jvm :as jvm]
   [ring.util.response :refer [bad-request resource-response]]
   [iapetos.collector.ring :as ring]
   [iapetos.core :as prometheus]
   [clojure.tools.logging :as log]))

;; TODO - this probably belongs in core, not here
(defonce registry
  (->
   (prometheus/collector-registry)
   jvm/initialize
   ring/initialize))

(defn wrap-env
  "Add environment information into the ring request.
  The environment should be one of :dev :test or :prod"
  [handler env]
  (fn [request]
    (handler (assoc request :env env))))

(defn default-uri-handler [uri]
  (when (#{"/favicon.ico" "/ping"} uri)
    uri))

;; TODO - consider having a map here instead of a vector for 
;; easier removal of handlers. Leaving for now, as I don't 
;; feel removal is something useful.
(def uri-registry (atom [default-uri-handler]))

(defn reset-registry!
  "Reset the uri-registry to just the default-uri-handler.
   Intended for REPL use only."
  []
  (reset! uri-registry [default-uri-handler]))

(defn register-uri-handler
  "Register a fn which will be be composed with the router multimethod 
   dispatch function."
  [f]
  {:pre [(fn? f)]}
  ;;(log/debugf "Registering uri handler %s" (var f))
  (swap! uri-registry conj f))

;; TODO - might be a more idiomatic way to achieve this.
(defn uri-dispatch
  "Invoke each URI handler with a URI from a ring request. Returns the 
   first non nil result of the handlers registered via register-uri-handler.
   Returns nil if no handler returned a non nill value"
  [uri]
  (loop [handlers @uri-registry]
    (if (empty? handlers)
      nil
      (if-let [value ((first handlers) uri)]
        value
        (recur (rest handlers))))))

(defmulti router
  "An open-ended ring router. It is intended that applications will provide their own
   implmentations as needed. The dispatch function returns a vector of [uri :request-method]
   For example: 
   
   (defmethod router [\"/v1/lookup/\" :get] [request]
   ...)

   The first item of the vector returned by the dispatch fn is determined by
   functions registerd via register-uri-handler.
   "
  (juxt #(uri-dispatch (:uri %)) :request-method))

(defmethod router ["/favicon.ico" :get] [_]
  (resource-response "clojure.png" {:root "public"}))

(defmethod router :default [request]
  (let [{:keys [uri]} request]
    (log/debugf "Unrecognized URI %s" uri)
    ;;(log/debugf "Complete Response: %s" request)
    (bad-request (str "Unknown URI " uri))))

(defn handler [request]
  (let [{:keys [uri]} request]
    (log/debugf "Processing URI %s" uri)
    (router request)))


(comment
  *e
  (uri-dispatch "/favicon.ico")
  (uri-dispatch "/ping2")

  (reset-registry!)
  (register-uri-handler #(fn [_] nil))

  @uri-registry
  (router {:uri "/health" :request-method :get})
  (router {:uri "/ping" :request-method :get})

  (count @uri-registry)
  ;;
  )

