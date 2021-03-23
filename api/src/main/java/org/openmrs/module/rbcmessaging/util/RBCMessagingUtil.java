/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.rbcmessaging.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openmrs.api.context.Context;
import org.openmrs.module.rbcmessaging.domain.RBCMessagingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for rbcmessaging module. It includes methods for connecting to RapidPro for posting
 * JSON requests and methods for parsing rbcmessaging module global properties configurations.
 * 
 * @author Bailly RURANGIRWA
 */
public class RBCMessagingUtil {
	
	private static final Logger log = LoggerFactory.getLogger(RBCMessagingUtil.class);
	
	/**
	 * Creates a JSON post request to a configured URL from "rbcmessaging.postURL" global property.
	 * 
	 * @param phone The phone number to send the message to
	 * @param messageText The actual message to send
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public static CloseableHttpResponse postMessage(String phone, String messageText) throws ClientProtocolException, IOException, AuthenticationException {
		String countryCode = Context.getAdministrationService().getGlobalProperty("rbcmessaging.countryCode");
		
		String fixedPhoneNumber = phone.replaceFirst("0", countryCode);
		String json = "{ \"urns\": [ \"tel:" + fixedPhoneNumber + "\"], \"text\": \"" + new String(messageText.getBytes("UTF-8"), "ISO-8859-1") + "\" }";
		HttpPost httpPost = new HttpPost(Context.getAdministrationService().getGlobalProperty("rbcmessaging.postURL"));
		httpPost.setEntity(new StringEntity(json));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("Authorization", Context.getAdministrationService().getGlobalProperty("rbcmessaging.Authorization"));
		
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(httpPost);
		client.close();
		return response;
	}
	
	/**
	 * Verify that the provided response was successful, and then copy the response to the given
	 * output buffer.
	 *
	 * <p>If the response was not successful (e.g. not a "200 OK") status code, or the response
	 * payload was empty, an {@link IOException} will be thrown.
	 *
	 * @param response
	 *         A representation of the response from the server.
	 * @param message
	 *         The stream to which the response data will be written.
	 *
	 * @throws IOException
	 *         If the response was unsuccessful, did not contain any data, or could not be written
	 *         completely to the target output stream.
	 */
	private static void processKeyResponse(CloseableHttpResponse response, RBCMessagingMessage message)
	        throws IOException {
	    final StatusLine statusLine = response.getStatusLine();

	    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
	        final HttpEntity responseEntity = response.getEntity();

	        if (responseEntity == null) {
	            throw new IOException("No response body returned.");
	        } else {
	            try (InputStream inputStream = responseEntity.getContent()) {
	                ByteStreams.copy(inputStream, message);
	            }
	        }
	    } else {
	        throw new IOException("PGP server returned an error: " + statusLine);
	    }
	}
}
