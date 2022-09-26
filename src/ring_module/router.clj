(ns ring-module.router
  (:require
   [iapetos.collector.jvm :as jvm]
   [ring.util.response :refer [bad-request resource-response]]
   [iapetos.collector.ring :as ring]
   [iapetos.core :as prometheus]
   [taoensso.timbre :as log]
   [clojure.string :refer [split join]]))

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

(defn not-only-digits? [s]
  (not (every? #(Character/isDigit %) s)))


;; TODO - this URI scheme to determine dispatch is very constrained.
;; Consider something more flexible. Perhaps multimethods are not the right 
;; choice for a routing API.
;;
(defn normalize-uri
  "Given a ring request, transform a URI to exclude a numeric component.
   For example, /v1/foo/bar/12848 becomes /v1/foo/bar"
  [request]
  (let [uri-elements (-> :uri request (split #"/") rest)]
    (->> uri-elements
         (take-while not-only-digits?)
         (join "/")
         (str "/"))))

;; (defmulti normalize-uri :uri)

;; (defmethod normalize-uri "/favicon.ico" [req]
;;   )



(defmulti router
  "An open-ended ring router. It is intended that applications will provide their own
   implmentations as needed. The dispatch function returns a vector of [uri :request-method]
   For example: 
   
   (defmethod router [\"/v1/lookup/\" :get] [request]
   ...)
   "
  (juxt normalize-uri :request-method))

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
  (router {:uri "/health" :request-method :get})
  (router {:uri "/ping" :request-method :get})


  (normalize-uri {:uri "/v1/a/b/c/3872"})

  (rest (split "/v1/v2/abc" #"/"))

  (->>
   (split "v1/foo/123" #"/")
   (take-while not-only-digits?)
   (join "/"))

  (resource-response "clojure.png" {:root "public"})

  (#'router {:uri "a/b/c"})
  (meta #'router)
  
  ;;
  )

