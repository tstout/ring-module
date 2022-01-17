# ring-module
A [sys-loader](https://github.com/tstout/sys-loader) module for adding base http server functionality. This module spins up a jetty web server, with a couple of endpoints.

## Endpoints
```
/ping - responds with jvm process uptime in a human-friendly format.
/metrics - prometheus metrics.
```

## Extension
The namespace ring-module.router contains a multimethod for apps to define their routes.
The dispatch function of router returns a vector of [uri :request-method] extracted from the 
ring request map.
   For example: 
   
   (defmethod router ["/v1/lookup/" :get] [request]
   ...)
