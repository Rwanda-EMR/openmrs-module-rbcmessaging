package org.openmrs.module.rbcmessaging.sms;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class IntouchSmsGateway implements SmsGateway {
	
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
	
	private final String postUrl;
	
	private final String username;
	
	private final String password;
	
	private final String sender;
	
	private final String coding;
	
	private final String deliveryReportUrl;
	
	private final String deliveryReportLevel;
	
	public IntouchSmsGateway(String postUrl, String username, String password, String sender, String coding, String deliveryReportUrl, String deliveryReportLevel) {
		this.postUrl = postUrl;
		this.username = username;
		this.password = password;
		this.sender = sender;
		this.coding = isBlank(coding) ? "0" : coding;
		this.deliveryReportUrl = deliveryReportUrl;
		this.deliveryReportLevel = isBlank(deliveryReportLevel) ? "2" : deliveryReportLevel;
	}
	
	@Override
	public SmsResult send(SmsMessage message) throws IOException {
		HttpPost request = new HttpPost(require(postUrl, "Intouch post URL"));
		List<NameValuePair> form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("username", require(username, "Intouch username")));
		form.add(new BasicNameValuePair("password", require(password, "Intouch password")));
		form.add(new BasicNameValuePair("to", require(message.getPhoneNumber(), "SMS phone number")));
		form.add(new BasicNameValuePair("from", require(sender, "Intouch sender")));
		form.add(new BasicNameValuePair("content", require(message.getContent(), "SMS content")));
		form.add(new BasicNameValuePair("coding", coding));
		if (!isBlank(deliveryReportUrl)) {
			form.add(new BasicNameValuePair("dlr-url", deliveryReportUrl));
			form.add(new BasicNameValuePair("dlr-level", deliveryReportLevel));
		}
		request.setEntity(new UrlEncodedFormEntity(form, UTF_8));
		
		try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(request)) {
			HttpEntity entity = response.getEntity();
			String body = entity == null ? "" : EntityUtils.toString(entity, UTF_8).trim();
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				throw new IOException("Intouch returned HTTP " + status + ": " + body);
			}
			Matcher messageId = MESSAGE_ID_PATTERN.matcher(body);
			if (!messageId.find()) {
				throw new IOException("Intouch response did not contain a message ID: " + body);
			}
			return new SmsResult("intouch", messageId.group());
		}
	}
	
	private String require(String value, String name) {
		if (isBlank(value)) {
			throw new IllegalStateException(name + " is not configured");
		}
		return value;
	}
	
	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
