package ch.elexis.core.java;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

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
