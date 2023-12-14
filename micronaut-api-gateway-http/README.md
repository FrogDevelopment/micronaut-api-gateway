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
    my-service:
      service-id: my-service-id
      mapping:
        - route: foo-bar
          context: /foo/bar
        - route: my-endpoint
    some-remote-application:
       uri: https://example.com
       mapping:
          - route: example
```

- `{host}/api/foo-bar` will be proxied to `{service-endpoint}/foo/bar`
- `{host}/api/my-endpoint` will be proxied to `{service-endpoint}/my-endpoint`
- `{host}/api/example` will be proxied to `https://example.com`
