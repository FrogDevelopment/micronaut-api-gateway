# HTTP API GATEWAY

## Direct Gateway

Requests will be directly redirect to the Service set in the URI.   
For example, given the configuration

```yaml
gateway:
  direct: true
```

- `{host}/api/my-service/foo/bar` will be proxied to `{my-service-endpoint}/foo/bar`
- `{host}/api/my-service/my-endpoint` will be proxied to `{my-service-endpoint}/my-endpoint`

## Routed Gateway

Requests will be re-routed based on configured routes and context.  
For example, given the configuration

```yaml
gateway:
  direct: false
  routes:
    my-service-id:
      mapping:
        - route: foo-bar
          context: /foo/bar
        - route: my-endpoint
```

- `{host}/api/foo-bar` will be proxied to `{service-endpoint}/foo/bar`
- `{host}/api/my-endpoint` will be proxied to `{service-endpoint}/my-endpoint`

## What is going on

1. Parse the URI
    - when `gateway.direct=true` then the `service-id` is retrieved from path
    - when `gateway.direct=false` then the `service-id` is retrieved by looking for 1st matching configured routes
      against the path
2. Retrieve the 1st healthy *
   *[Kubernetes Endpoints](https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/)** bound
   to the **[Kubernetes Service](https://kubernetes.io/docs/concepts/services-networking/service/)** using Round Robin
   policy (see Micronaut LoadBalancer)
3. Mutate the request
    - replace the host and port by those from Endpoint
    - Replace path depending on the configuration
