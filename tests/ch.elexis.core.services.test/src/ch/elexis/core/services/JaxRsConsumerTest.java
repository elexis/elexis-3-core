package ch.elexis.core.services;

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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;
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
	public static void beforeClass() throws IOException{
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/test", new MyHttpHandler());
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
		server.setExecutor(executor);
		server.start();
	}
	
	@AfterClass
	public static void afterClass(){
		server.stop(5);
		executor.shutdown();
	}
	
	@Test
	public void jaxrsFunctionality(){
		FakeResource resource = ConsumerFactory.createConsumer(
			"http://localhost:" + server.getAddress().getPort(), FakeResource.class);
		assertEquals("get", resource.getContent());
		assertEquals("post", resource.postContent());
		assertEquals("put", resource.putContent(""));
		assertEquals("delete", resource.deleteContent());
	}
	
	private static class MyHttpHandler implements HttpHandler {
		
		@Override
		public void handle(HttpExchange exchange) throws IOException{
			String requestMethod = exchange.getRequestMethod();
			handleResponse(exchange, requestMethod.toLowerCase());
		}
		
		private void handleResponse(HttpExchange httpExchange, String requestParamValue)
			throws IOException{
			try (OutputStream outputStream = httpExchange.getResponseBody()) {
				httpExchange.sendResponseHeaders(200, requestParamValue.length());
				outputStream.write(requestParamValue.getBytes());
				outputStream.flush();
			}
		}
		
	}
	
}
