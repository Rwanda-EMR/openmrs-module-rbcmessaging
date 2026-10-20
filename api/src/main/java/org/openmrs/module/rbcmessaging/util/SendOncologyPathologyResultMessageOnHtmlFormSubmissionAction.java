/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.rbcmessaging.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntrySession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class SendOncologyPathologyResultMessageOnHtmlFormSubmissionAction implements CustomFormSubmissionAction {
	
	@Override
	public void applyAction(FormEntrySession session) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Patient p = session.getPatient();
		String phoneNumber = null;
		if (p.getPerson().getAttribute("Phone number") != null) {
			phoneNumber = p.getPerson().getAttribute("Phone number").getValue();
		}
		
		try {
			
			String apiUsername = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpihncd.username");
			String apiPassword = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpihncd.password");
			String authorizationURL = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.authorizationURL");
			String postURL = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.postURL");
			String sender = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.sender");
			String contactPhoneNumber = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpihncd.contactPhoneNumber");
			
			String jsonCredential = "{\"api_username\": \"" + apiUsername + "\",  \"api_password\": \"" + apiPassword + "\"}";
			
			HttpPost httpPostCredential = new HttpPost(authorizationURL);
			
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
					// System.out.println(st.split("\"")[3]);
					access_token = "Bearer " + st.split("\"")[3];
				} else {
					continue;
				}
			}
			clientCredential.close();
			UUID uuid = UUID.randomUUID();
			String uuidAsString = uuid.toString();
			
			String messageBody = "Ibitaro bya butaro, birakumenyesha ko igisubizo cya Laboratwari(ikizamini) watanze cyo gusuzuma uburwayi cyabonetse. Igihe cya randevu nikigera, muzajya ku bitaro byabafatiye ikizamini bikumenyeshe igisubizo cyanyu, banakubwire igikurikiraho niba ari ngombwa.";
			
			if (messageBody != null && phoneNumber != null) {
				String json = "{\"msisdn\": \"" + phoneNumber + "\",  \"message\": \"" + messageBody + "\",  \"msgRef\": \"" + uuidAsString + "\", \"sender_id\": \"" + sender + "\"}";
				
				HttpPost httpPost = new HttpPost(postURL);
				
				httpPost.setEntity(new StringEntity(json));
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				httpPost.setHeader("Authorization", access_token);
				
				CloseableHttpClient client = HttpClients.createDefault();
				CloseableHttpResponse response = client.execute(httpPost);
				System.out.println(response.getStatusLine());
				// System.out.println("access_token: " + access_token);
				client.close();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		// =============== end SMS ====================
		
	}
}
