(ns ring-module.router
  (:require
   [iapetos.collector.jvm :as jvm]
   [iapetos.collector.ring :as ring]
   [iapetos.core :as prometheus]
   [taoensso.timbre :as log]
   [clojure.data.json :as json]
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

(defn read-body
  "In Ring, the http body is actualy a java InputStream.
   This function consumes the InputStream and converts the resulting 
   string into a clojure persistent data structure."
  [body]
  (-> body slurp))

(defn mk-response
  "Create a ring response map"
  ([body]
   (mk-response body 200))
  ([body status]
   (mk-response body status {"Content-Type" "application/json"}))
  ([body status headers]
   {:status  status
    :headers headers
    :body    body}))

(defn not-only-digits? [s]
  (not (every? #(Character/isDigit %) s)))

(defn normalize-uri
  "Given a ring request, transform a uri to exclude a numeric component.
   For example, /v1/foo/bar/12848 becomes /v1/foo/bar"
  [request]
  (let [uri-elements (-> :uri request (split #"/") rest)]
    (->> uri-elements
         (take-while not-only-digits?)
         (join "/")
         (str "/"))))

(defmulti router
  "An open-ended ring router. It is intended that applications will provide their own
   implmentations as needed. The dispatch function returns a vector of [:uri :request-method]
   For example: 
   
   (defmethod router [\"/v1/lookup/\" :get] [request]
   ...)   
   "
  (juxt normalize-uri :request-method))

(defmethod router ["/health" :get] [_]
  (mk-response
   (->
    {:health
     {:status "NORMAL"
      :healthAssessments []}
     :_links {:self {:href "/health"}}}
    (json/write-str :escape-slash false))))

(defmethod router :default [request]
  (let [{:keys [uri]} request]
    (log/debugf "Unrecognized URI %s" uri)
    (log/debugf "Complete Response: %s" request)
    (mk-response (str "Unknown URI " uri) 400)))

(defn handler [request]
  (let [{:keys [uri]} request]
    (log/debugf "Processing URI %s" uri)
    (router request)))

(comment
  ;; REPL experiments
  (router {:uri "/health" :request-method :get})

  (normalize-uri-for-route {:uri "/v1/a/b/c/3872"})

  (rest (split "/v1/v2/abc" #"/"))

  (->>
   (split "v1/foo/123" #"/")
   (take-while not-only-digits?)
   (join "/"))
  ;;
  )

