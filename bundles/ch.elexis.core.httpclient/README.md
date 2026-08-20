# Injectable HTTP Client

This bundle provides an injectable, caching HTTP client. It can be injected via

```java
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

@Inject
CloseableHttpClient httpClient;
```

Cache can be cleared by sending `info/elexis/system/clear-cache` event.