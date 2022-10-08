# ring-module
A [sys-loader](https://github.com/tstout/sys-loader) module for adding base http server functionality. This module spins up a jetty web server, with a couple of endpoints.

## Usage - deps.edn coordinates
```clojure
  com.github.tstout/ring-module
    {:git/url "https://github.com/tstout/ring-module"
     :git/tag "v1.0.0"
     :git/sha "e7a240e"}
```
## Endpoints
```
/ping    - responds with jvm process uptime in a human-friendly format.
/metrics - prometheus metrics.
```

## Extension
The namespace ring-module.router contains a multimethod for apps to define their routes.
The dispatch function of router returns a vector of [(registered-fn) :request-method] The request method is extracted from the  
ring request map. The request URI is passed to the registered-fn.
The purpose of the registered uri handler fn is to look at the URI and decide what value should be used as the first argument of the mutlimethod dispatch function. This might be a readable subset of the URI or it could be a keyword. These are just suggestions. You decide. For example: 
   ```clojure
   (ns an.example
     (:require [ring-module.router :refer [router register-uri-handler]]
               [clojure.string :refer [starts-with?]]))
   
   (defn lookup-handler [uri]
    (when (starts-with? uri "/v1/service/lookup")
      "/v1/service/lookup"))

   (register-uri-handler lookup-handler)

   (defmethod router ["/v1/service/lookup" :get] [request]
   ;; process ring request and respond with a ring response
   )
   ```
   
