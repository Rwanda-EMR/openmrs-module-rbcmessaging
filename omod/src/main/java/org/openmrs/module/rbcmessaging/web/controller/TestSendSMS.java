/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.rbcmessaging.web.controller;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.UUID;

public class TestSendSMS {
	
	public static void main(String[] args) throws IOException {
		
		System.out.println("Hello, World!");
		
		String jsonCredential = "{\"api_username\": \"20D13C54-CC1B-4D5A-ADCE-40189C57D728\",  \"api_password\": \"49931A9A-DB96-499C-878C-98D13C358D2B\"}";
		HttpPost httpPostCredential = new HttpPost("https://messaging.fdibiz.com/api/v1/auth/");
		httpPostCredential.setEntity(new StringEntity(jsonCredential));
		httpPostCredential.setHeader("Content-type", "application/json");
		
		CloseableHttpClient clientCredential = HttpClients.createDefault();
		CloseableHttpResponse responseCredential = clientCredential.execute(httpPostCredential);
		String resultsCredential = EntityUtils.toString(responseCredential.getEntity());
		// System.out.println(resultsCredential);
		
		String access_token = "";
		String[] resultsStringArray = resultsCredential.split(",");
		int i = 0;
		for (String st : resultsStringArray) {
			
			int j = st.indexOf("access_token");
			if (j > 0) {
				System.out.println(st.split("\"")[3]);
				access_token = "Bearer " + st.split("\"")[3];
			} else {
				continue;
			}
		}
		clientCredential.close();
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();
		
		String phoneNumber = "0788529201";
		String messageBody = "Hi This is a message from code";
		
		String json = "{\"msisdn\": \"" + phoneNumber + "\",  \"message\": \"" + messageBody + "\",  \"msgRef\": \"" + uuidAsString + "\", \"sender_id\": \"RBC\"}";
		HttpPost httpPost = new HttpPost("https://messaging.fdibiz.com/api/v1/mt/single");
		httpPost.setEntity(new StringEntity(json));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("Authorization", access_token);
		
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(httpPost);
		System.out.println(response.getStatusLine());
		System.out.println("access_token: " + access_token);
		client.close();
		
	}
}
