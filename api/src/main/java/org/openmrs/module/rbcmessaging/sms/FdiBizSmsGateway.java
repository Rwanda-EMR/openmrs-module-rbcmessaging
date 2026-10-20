package org.openmrs.module.rbcmessaging.sms;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class FdiBizSmsGateway implements SmsGateway {
	
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\\\"access_token\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
	
	private final String username;
	
	private final String password;
	
	private final String authorizationUrl;
	
	private final String postUrl;
	
	private final String sender;
	
	public FdiBizSmsGateway(String username, String password, String authorizationUrl, String postUrl, String sender) {
		this.username = username;
		this.password = password;
		this.authorizationUrl = authorizationUrl;
		this.postUrl = postUrl;
		this.sender = sender;
	}
	
	@Override
	public SmsResult send(SmsMessage message) throws IOException {
		String accessToken = authenticate();
		String payload = "{\"msisdn\":\"" + escapeJson(message.getPhoneNumber()) + "\",\"message\":\"" + escapeJson(message.getContent()) + "\",\"msgRef\":\"" + escapeJson(message.getReference()) + "\",\"sender_id\":\"" + escapeJson(sender) + "\"}";
		String response = postJson(postUrl, payload, accessToken);
		return new SmsResult("fdibiz", response);
	}
	
	private String authenticate() throws IOException {
		String payload = "{\"api_username\":\"" + escapeJson(username) + "\",\"api_password\":\"" + escapeJson(password) + "\"}";
		Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(postJson(authorizationUrl, payload, null));
		if (!matcher.find()) {
			throw new IOException("FDI Biz authentication response did not contain an access token");
		}
		return "Bearer " + matcher.group(1);
	}
	
	private String postJson(String url, String payload, String authorization) throws IOException {
		HttpPost request = new HttpPost(require(url, "FDI Biz URL"));
		request.setEntity(new StringEntity(payload, UTF_8));
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-Type", "application/json; charset=UTF-8");
		if (authorization != null) {
			request.setHeader("Authorization", authorization);
		}
		try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(request)) {
			HttpEntity entity = response.getEntity();
			String body = entity == null ? "" : EntityUtils.toString(entity, UTF_8);
			int status = response.getStatusLine().getStatusCode();
			if (status < 200 || status >= 300) {
				throw new IOException("FDI Biz returned HTTP " + status + ": " + body);
			}
			return body;
		}
	}
	
	private String escapeJson(String value) {
		return require(value, "FDI Biz configuration value").replace("\\", "\\\\").replace("\"", "\\\"").replace("\b", "\\b").replace("\f", "\\f").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}
	
	private String require(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalStateException(name + " is not configured");
		}
		return value;
	}
}
