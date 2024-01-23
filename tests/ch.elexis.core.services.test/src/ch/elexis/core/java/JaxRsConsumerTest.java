package ch.elexis.core.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class JaxRsConsumerTest {

	private static HttpServer server;
	private static ThreadPoolExecutor executor;

	@Path("/test")
	private interface FakeResource {
		@GET
		String getContent();

		@POST
		String postContent();

		@POST
		String postContent(String content);

		@PUT
		String putContent(String content);

		@DELETE
		String deleteContent();
	}

	@BeforeClass
	public static void beforeClass() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/test", new MyHttpHandler());
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
		server.setExecutor(executor);
		server.start();
	}

	@AfterClass
	public static void afterClass() {
		server.stop(5);
		executor.shutdown();
	}

	@Test
	public void jaxrsFunctionality() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:" + server.getAddress().getPort());
		FakeResource resource = WebResourceFactory.newResource(FakeResource.class, target);
		assertEquals("get", resource.getContent());
		assertEquals("post", resource.postContent());
		assertEquals("put", resource.putContent(""));
		assertEquals("delete", resource.deleteContent());

	}

	private static class MyHttpHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			handleResponse(exchange, requestMethod.toLowerCase());
		}

		private void handleResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {
			try (OutputStream outputStream = httpExchange.getResponseBody()) {
				httpExchange.sendResponseHeaders(200, requestParamValue.length());
				outputStream.write(requestParamValue.getBytes());
				outputStream.flush();
			}
		}

	}

}
