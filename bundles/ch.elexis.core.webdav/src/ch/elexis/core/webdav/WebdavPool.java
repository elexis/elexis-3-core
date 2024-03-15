package ch.elexis.core.webdav;

import java.net.ProxySelector;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.VersionInfo;
import org.slf4j.LoggerFactory;

import com.github.sardine.Sardine;
import com.github.sardine.Version;
import com.github.sardine.impl.SardineImpl;
import com.github.sardine.impl.SardineRedirectStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public enum WebdavPool {

	INSTANCE;
	
	private Cache<String, Sardine> sardines;

	private Registry<ConnectionSocketFactory> defaultSchemeRegistry;
	private PoolingHttpClientConnectionManager defaultConnectionManager;
	private DefaultSchemePortResolver defaultSchemePortResolver;
	private SardineRedirectStrategy defaultRedirectStrategy;
	private HttpRoutePlanner defaultRoutePlanner;

	private WebdavPool() {
		sardines = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
	}

	public synchronized Sardine getSardine(URL url) {
		try {
			Sardine sardine = sardines.get(getSardineKey(url), () -> {
				HttpClientBuilder builder = getHttpClientBuilder(url);
				Sardine sardineImpl = new SardineImpl(builder);
				sardineImpl.enablePreemptiveAuthentication(url);
				sardineImpl.enableCompression();
				return sardineImpl;
			});
			if (sardine == null) {
				sardines.put(getSardineKey(url), sardine);
			}
			return sardine;
		} catch (ExecutionException e) {
			LoggerFactory.getLogger(getClass()).error("Error getting webdav", e);
			return null;
		}
	}

	private HttpClientBuilder getHttpClientBuilder(URL url) {
		Registry<ConnectionSocketFactory> schemeRegistry = this.createDefaultSchemeRegistry();
		HttpClientConnectionManager cm = this.createDefaultConnectionManager(schemeRegistry);
		String version = Version.getSpecification();
		if (version == null) {
			version = VersionInfo.UNAVAILABLE;
		}
		HttpClientBuilder ret = HttpClients.custom().setUserAgent("Sardine/" + version)
				.setRedirectStrategy(this.createDefaultRedirectStrategy())
				.setDefaultRequestConfig(RequestConfig.custom()
						// Only selectively enable this for PUT but not all entity enclosing methods
						.setExpectContinueEnabled(false).setCookieSpec(CookieSpecs.STANDARD).build())
				.setConnectionManager(cm)
				.setRoutePlanner(this.createDefaultRoutePlanner(createDefaultSchemePortResolver(), null));

		if (StringUtils.isNotBlank(url.getUserInfo())) {
			if (url.getUserInfo().contains(":")) {
				// username:password
				String[] userInfo = url.getUserInfo().split(":");
				ret.setDefaultCredentialsProvider(
						createDefaultCredentialsProvider(userInfo[0], userInfo[1], null, null));
			} else {
				// bearer token
				Header bearerHeader = new BasicHeader("Authorization", "Bearer " + url.getUserInfo());
				ret.setDefaultHeaders(Collections.singletonList(bearerHeader));
			}
		}
		return ret;
	}

	HttpClientConnectionManager createDefaultConnectionManager(Registry<ConnectionSocketFactory> schemeRegistry) {
		if (defaultConnectionManager == null) {
			defaultConnectionManager = new PoolingHttpClientConnectionManager(schemeRegistry);
		}
		return defaultConnectionManager;
	}

	private Registry<ConnectionSocketFactory> createDefaultSchemeRegistry() {
		if (defaultSchemeRegistry == null) {
			defaultSchemeRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
		}
		return defaultSchemeRegistry;
	}

	private DefaultSchemePortResolver createDefaultSchemePortResolver() {
		if (defaultSchemePortResolver == null) {
			defaultSchemePortResolver =  new DefaultSchemePortResolver();
		}
		return defaultSchemePortResolver;
	}

	private SardineRedirectStrategy createDefaultRedirectStrategy() {
		if(defaultRedirectStrategy == null) {
			defaultRedirectStrategy = new SardineRedirectStrategy();
		}
		return defaultRedirectStrategy;
	}

	private HttpRoutePlanner createDefaultRoutePlanner(SchemePortResolver resolver, ProxySelector selector) {
		if (defaultRoutePlanner == null) {
			defaultRoutePlanner = new SystemDefaultRoutePlanner(resolver, selector);
		}
		return defaultRoutePlanner;
	}

	private CredentialsProvider createDefaultCredentialsProvider(String username, String password, String domain,
			String workstation) {
		CredentialsProvider provider = new BasicCredentialsProvider();
		if (username != null) {
			provider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.NTLM),
					new NTCredentials(username, password, workstation, domain));
			provider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.BASIC),
					new UsernamePasswordCredentials(username, password));
			provider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.DIGEST),
					new UsernamePasswordCredentials(username, password));
			provider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.SPNEGO),
					new NTCredentials(username, password, workstation, domain));
			provider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.KERBEROS),
					new UsernamePasswordCredentials(username, password));
		}
		return provider;
	}

	String getSardineKey(URL url) {
		return url.getUserInfo() + "@" + url.getHost();
	}
}
