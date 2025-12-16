package ch.elexis.core.httpclient;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class HttpClientUtil {

	public static String getOrThrowAcceptJson(CloseableHttpClient httpClient, String url) throws IOException {
		HttpGet request = new HttpGet(url);
		request.addHeader("accept", "application/json");

		return httpClient.execute(request, response -> {
			return EntityUtils.toString(response.getEntity());
		});
	}

}
