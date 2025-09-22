package ch.elexis.core.services.oauth2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.status.ObjectStatus;
import jakarta.ws.rs.core.UriBuilder;

public class AuthorizationCodeFlowWithPKCE {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private AtomicReference<String> authorizationCode = new AtomicReference<>();

	private Thread redirectServerThread;

	private String clientId;
	private String redirectUri;
	private String pkceChallenge;
	private String pkceVerifier;

	public AuthorizationCodeFlowWithPKCE(String clientId) {
		this.clientId = clientId;
	}

	// Initiate the flow. Opens a server endpoint to handle the redirect and
	// provides the URI to hand over to the browser
	public URI initiateFlowGetBrowserUrl(URI authorizationUri, String clientSecret)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		int port = startClientRedirectUriServer();
		redirectUri = "http://localhost:" + port + "/callback";

		PKCEUtil pkceUtil = new PKCEUtil();
		pkceVerifier = pkceUtil.generateCodeVerifier();
		pkceChallenge = pkceUtil.generateCodeChallenge(pkceVerifier);

		UriBuilder requestUriBuilder = UriBuilder.fromUri(authorizationUri);
		requestUriBuilder.queryParam("response_type", "code");
		requestUriBuilder.queryParam("redirect_uri", redirectUri);
		requestUriBuilder.queryParam("code_challenge", pkceChallenge);
		requestUriBuilder.queryParam("code_challenge_method", "S256");
		requestUriBuilder.queryParam("client_id", clientId);
		requestUriBuilder.queryParam("client_secret", clientSecret);

		return requestUriBuilder.build();
	}

	/**
	 * Blocking operation. Waits for the retrieval of the authorization code, and
	 * exchanges it for the access token.
	 * 
	 * @return
	 */
	public ObjectStatus<AccessToken> fetchAccessTokenAfterAuthorizationCode(Gson gson, String tokenEndpoint,
			String clientSecret) {
		while (authorizationCode.get() == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error("Error wating for authorization code", e);
			}
		}

		shutdownServerThread();

		final HttpPost httpPost = new HttpPost(tokenEndpoint);
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
		params.add(new BasicNameValuePair("code", authorizationCode.get()));
		params.add(new BasicNameValuePair("code_verifier", pkceVerifier));
		params.add(new BasicNameValuePair("state", "state"));
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		params.add(new BasicNameValuePair("redirect_uri", redirectUri));
		httpPost.setEntity(new UrlEncodedFormEntity(params));

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			return client.execute(httpPost, res -> {
				if (res.getCode() == 200) {
					HttpEntity entity = res.getEntity();
					String accessTokenResponse = EntityUtils.toString(entity, "UTF-8");
					KeycloakAccessTokenResponse kcAccessTokenResponse = gson.fromJson(accessTokenResponse,
							KeycloakAccessTokenResponse.class);
					AccessToken accessToken = AccessTokenUtil.load(kcAccessTokenResponse, tokenEndpoint.toString(),
							clientId);
					return ObjectStatus.OK(accessToken);
				}
				return ObjectStatus.ERROR(res.getCode() + " " + res.getReasonPhrase());
			});

		} catch (IOException e) {
			return ObjectStatus.ERROR(e.getMessage());
		}
	}

	/**
	 * Will start an http server on a free port to handle the redirect call,
	 * automatically closes after receiving or a max of 1 minute (user interaction)
	 * 
	 * @return
	 */
	int startClientRedirectUriServer() {
		AtomicReference<Integer> localPort = new AtomicReference<>();

		redirectServerThread = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(0)) {
				serverSocket.setSoTimeout(500);
				localPort.set(serverSocket.getLocalPort());
				logger.debug("Listening on port " + localPort.get());
				while (!Thread.currentThread().isInterrupted()) {
					try (Socket accept = serverSocket.accept()) {
						boolean done = false;
						try (BufferedReader in = new BufferedReader(new InputStreamReader(accept.getInputStream()))) {
							try (BufferedWriter out = new BufferedWriter(
									new OutputStreamWriter(accept.getOutputStream()))) {

								String line = in.readLine();
								logger.debug("REQ " + line);
								if (line.startsWith("GET /callback")) {
									// We retrieve the authorizationcode and have to use
									// it to get the access token
									// GET /callback?code=23423 HTTP/1.1
									String callbackUri = line.split(" ")[1];
									Map<String, String> queryParameters = getQueryParameters(callbackUri);
									String _authorizationCode = queryParameters.get("code");
									if (StringUtils.isNotBlank(_authorizationCode)) {
										logger.debug("Authorization code is [" + _authorizationCode + "]");
										authorizationCode.set(_authorizationCode);
										done = true; // TODO ?
									}
									// TODO is state equal to the challenge?

								}

								int code = done ? 200 : 500;
								String body = "<HTML>" + code + "</HTML>";
								out.write("HTTP/1.0 " + code + "\r\n");
								out.write("Date: " + LocalDateTime.now() + "\r\n");
								out.write("Server: Custom Server\r\n");
								out.write("Content-Type: text/html\r\n");
								out.write("Content-Length: " + body.length() + "\r\n");
								out.write("\r\n");
								out.write(body);
								out.close();

							}
						}

						if (done) {
							continue;
						}

					} catch (URISyntaxException e) {
						logger.error("Error in redirect call uri", e);
					} catch (SocketTimeoutException sote) {
						// do nothing
					}
				}
				logger.debug("Stopped listening on port " + localPort.get());
			} catch (ClosedByInterruptException e) {
				logger.debug("Closing connection due to interrupt");
			} catch (SocketException e) {
				if (Thread.interrupted()) {
					logger.debug("Closing connection due to interrupt");
				} else {
					logger.warn("Socket exception", e);
				}
			} catch (IOException e) {
				logger.error("Error in redirect server handler", e);
			}

		}, "oauth2-redirect-uri-server");
		redirectServerThread.start();

		while (localPort.get() == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error("Error wating for server startup", e);
			}
		}
		return localPort.get();
	}

	private static Map<String, String> getQueryParameters(final String url) throws URISyntaxException {
		return new URIBuilder(new URI(url), StandardCharsets.UTF_8).getQueryParams().stream()
				.collect(Collectors.toMap(NameValuePair::getName,
						nameValuePair -> URLDecoder.decode(nameValuePair.getValue(), StandardCharsets.UTF_8)));
	}

	private void shutdownServerThread() {
		if (redirectServerThread != null) {
			redirectServerThread.interrupt();
		}
		redirectServerThread = null;
	}

	public void abort() {
		shutdownServerThread();
	}

}
