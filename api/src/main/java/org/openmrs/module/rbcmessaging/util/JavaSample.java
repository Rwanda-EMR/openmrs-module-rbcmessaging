package org.openmrs.module.rbcmessaging.util;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class JavaSample {
	
	public static void main(String[] args) {
		
		HttpClient httpclient = HttpClients.createDefault();
		
		try {
			URIBuilder builder = new URIBuilder("https://mtndeveloperapi.portal.mtn.co.rw/collection/token/");
			
			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);
			request.setHeader("Authorization", "Basic NThhNzViZTAtMzVjZi00MTQwLWJkY2UtZmFiY2U2Njk3NmU2OmYwMTE2ZWVhYTM3MzRkNDJiMjlhOTM5ZDdlNDIwNjQz");
			request.setHeader("Ocp-Apim-Subscription-Key", "89e548d531494da4b3b4e35040386eea");
			
			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();
			String resultsCredential = EntityUtils.toString(entity);
			/*
			 * if (entity != null) { System.out.println(EntityUtils.toString(entity)); }
			 */
			
			String access_token = "";
			String[] resultsStringArray = resultsCredential.split(",");
			int i = 0;
			for (String st : resultsStringArray) {
				
				int j = st.indexOf("access_token");
				if (j > 0) {
					// System.out.println(st.split("\"")[3]);
					access_token = "Bearer " + st.split("\"")[3];
					break;
				} else {
					continue;
				}
			}
			System.out.println(access_token);
			
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		/*
		 * 
		 * HttpClient httpclient = HttpClients.createDefault();
		 * 
		 * try { URIBuilder builder = new URIBuilder(
		 * "https://mtndeveloperapi.portal.mtn.co.rw/collection/v1_0/requesttopay");
		 * 
		 * 
		 * URI uri = builder.build(); HttpPost request = new HttpPost(uri);
		 * request.setHeader("Authorization", ""); request.setHeader("X-Callback-Url",
		 * ""); request.setHeader("X-Reference-Id", "");
		 * request.setHeader("X-Target-Environment", "");
		 * request.setHeader("Content-Type", "application/json");
		 * request.setHeader("Ocp-Apim-Subscription-Key", "{subscription key}");
		 * 
		 * 
		 * // Request body StringEntity reqEntity = new StringEntity("{body}");
		 * request.setEntity(reqEntity);
		 * 
		 * HttpResponse response = httpclient.execute(request); HttpEntity entity =
		 * response.getEntity();
		 * 
		 * if (entity != null) { System.out.println(EntityUtils.toString(entity)); } }
		 * catch (Exception e) { System.out.println(e.getMessage()); }
		 */
	}
}
