package org.openmrs.module.rbcmessaging.sms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class IntouchSmsGatewayTest {
	
	private HttpServer server;
	
	private RecordingHandler handler;
	
	@Before
	public void setUp() throws IOException {
		handler = new RecordingHandler();
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/send", handler);
		server.start();
	}
	
	@After
	public void tearDown() {
		server.stop(0);
	}
	
	@Test
	public void sendShouldPostIntouchFormAndReturnMessageId() throws Exception {
		IntouchSmsGateway gateway = gateway();
		
		SmsResult result = gateway.send(new SmsMessage("250788000000", "Isuzuma ryawe", "local-reference"));
		
		assertEquals("intouch", result.getProvider());
		assertEquals(RecordingHandler.MESSAGE_ID, result.getProviderMessageId());
		assertEquals("user", handler.parameters.get("username"));
		assertEquals("secret", handler.parameters.get("password"));
		assertEquals("250788000000", handler.parameters.get("to"));
		assertEquals("RBC", handler.parameters.get("from"));
		assertEquals("Isuzuma ryawe", handler.parameters.get("content"));
		assertEquals("0", handler.parameters.get("coding"));
		assertEquals("https://example.org/dlr", handler.parameters.get("dlr-url"));
		assertEquals("2", handler.parameters.get("dlr-level"));
	}
	
	@Test
	public void sendShouldRejectVendorErrorResponse() throws Exception {
		handler.status = 403;
		handler.response = "Error Authentication failure";
		
		try {
			gateway().send(new SmsMessage("250788000000", "Message", "reference"));
		}
		catch (IOException e) {
			assertTrue(e.getMessage().contains("HTTP 403"));
			assertTrue(e.getMessage().contains("Authentication failure"));
			return;
		}
		throw new AssertionError("Expected an IOException");
	}
	
	private IntouchSmsGateway gateway() {
		return new IntouchSmsGateway("http://localhost:" + server.getAddress().getPort() + "/send", "user", "secret", "RBC", "0", "https://example.org/dlr", "2");
	}
	
	private static class RecordingHandler implements HttpHandler {
		
		private static final String MESSAGE_ID = "07033084-5cfd-4812-90a4-e4d24ffb6e3d";
		
		private final Map<String, String> parameters = new HashMap<String, String>();
		
		private int status = 200;
		
		private String response = "Success \"" + MESSAGE_ID + "\"";
		
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String requestBody = read(exchange.getRequestBody());
			for (String pair : requestBody.split("&")) {
				String[] parts = pair.split("=", 2);
				parameters.put(URLDecoder.decode(parts[0], "UTF-8"), URLDecoder.decode(parts[1], "UTF-8"));
			}
			
			byte[] responseBytes = response.getBytes("UTF-8");
			exchange.sendResponseHeaders(status, responseBytes.length);
			OutputStream output = exchange.getResponseBody();
			output.write(responseBytes);
			output.close();
		}
		
		private String read(InputStream input) throws IOException {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int count;
			while ((count = input.read(buffer)) != -1) {
				output.write(buffer, 0, count);
			}
			return new String(output.toByteArray(), "UTF-8");
		}
	}
}
