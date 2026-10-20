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

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessageOnHtmlFormSubmissionAction implements CustomFormSubmissionAction {
	
	private static final Logger log = LoggerFactory.getLogger(SendMessageOnHtmlFormSubmissionAction.class);
	
	private static final String PHONE_NUMBER_ATTRIBUTE = "Phone number";
	
	private static final String USERNAME_PROPERTY = "rbcmessaging.mohpih.username";
	
	private static final String PASSWORD_PROPERTY = "rbcmessaging.mohpih.password";
	
	private static final String AUTHORIZATION_URL_PROPERTY = "rbcmessaging.mohpih.authorizationURL";
	
	private static final String POST_URL_PROPERTY = "rbcmessaging.mohpih.postURL";
	
	private static final String SENDER_PROPERTY = "rbcmessaging.mohpih.sender";
	
	private static final String APPOINTMENT_CONCEPTS_PROPERTY = "rbcmessaging.mohpih.appointmentDateConceptUUIDs";
	
	private static final String LOCATION_ID_PROPERTY = "rbcmessaging.mohpih.locationId";
	
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\\\"access_token\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
	
	@Override
	public void applyAction(FormEntrySession session) {
		Patient patient = session.getPatient();
		String phoneNumber = getPhoneNumber(patient);
		if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
			log.warn("SMS was not sent because patient " + patient.getPatientId() + " has no phone number");
			return;
		}
		
		AdministrationService administrationService = Context.getAdministrationService();
		Set<String> appointmentConceptUuids = getAppointmentConceptUuids(administrationService);
		int locationId = Integer.parseInt(administrationService.getGlobalProperty(LOCATION_ID_PROPERTY));
		List<String> appointments = findAppointments(session, appointmentConceptUuids, locationId);
		if (appointments.isEmpty()) {
			return;
		}
		
		String message = buildMessage(patient, appointments);
		try {
			String accessToken = authenticate(administrationService);
			sendMessage(administrationService, phoneNumber, message, accessToken);
		}
		catch (IOException e) {
			throw new IllegalStateException("Unable to send appointment SMS", e);
		}
	}
	
	private String getPhoneNumber(Patient patient) {
		PersonAttribute phoneNumber = patient.getPerson().getAttribute(PHONE_NUMBER_ATTRIBUTE);
		return phoneNumber == null ? null : phoneNumber.getValue();
	}
	
	private Set<String> getAppointmentConceptUuids(AdministrationService administrationService) {
		String configuredUuids = administrationService.getGlobalProperty(APPOINTMENT_CONCEPTS_PROPERTY);
		Set<String> conceptUuids = new HashSet<String>();
		if (configuredUuids != null) {
			for (String uuid : Arrays.asList(configuredUuids.split(","))) {
				conceptUuids.add(uuid.trim().toLowerCase());
			}
		}
		return conceptUuids;
	}
	
	private List<String> findAppointments(FormEntrySession session, Set<String> conceptUuids, int locationId) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		List<String> appointments = new ArrayList<String>();
		for (Obs observation : session.getSubmissionActions().getObsToCreate()) {
			if (isAppointmentObservation(observation, conceptUuids, locationId)) {
				appointments.add(dateFormat.format(observation.getValueDate()) + "(" + observation.getConcept().getName().getName() + ")");
			}
		}
		return appointments;
	}
	
	private boolean isAppointmentObservation(Obs observation, Set<String> conceptUuids, int locationId) {
		return observation.getConcept() != null && observation.getConcept().getUuid() != null && conceptUuids.contains(observation.getConcept().getUuid().toLowerCase()) && observation.getValueDate() != null && observation.getEncounter() != null && observation.getEncounter().getLocation() != null && observation.getEncounter().getLocation().getLocationId() == locationId;
	}
	
	private String buildMessage(Patient patient, List<String> appointments) {
		return patient.getFamilyName() + " " + patient.getGivenName() + ". Uhawe itariki ya " + join(appointments) + " yo kugusubira ku ivuriro wisuzumishirijeho kanseri.";
	}
	
	private String authenticate(AdministrationService administrationService) throws IOException {
		String credentials = "{\"api_username\":\"" + escapeJson(administrationService.getGlobalProperty(USERNAME_PROPERTY)) + "\",\"api_password\":\"" + escapeJson(administrationService.getGlobalProperty(PASSWORD_PROPERTY)) + "\"}";
		String responseBody = postJson(administrationService.getGlobalProperty(AUTHORIZATION_URL_PROPERTY), credentials, null);
		Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(responseBody);
		if (!matcher.find()) {
			throw new IOException("Authentication response did not contain an access token");
		}
		return "Bearer " + matcher.group(1);
	}
	
	private void sendMessage(AdministrationService administrationService, String phoneNumber, String message, String accessToken) throws IOException {
		String json = "{\"msisdn\":\"" + escapeJson(phoneNumber) + "\",\"message\":\"" + escapeJson(message) + "\",\"msgRef\":\"" + UUID.randomUUID().toString() + "\",\"sender_id\":\"" + escapeJson(administrationService.getGlobalProperty(SENDER_PROPERTY)) + "\"}";
		postJson(administrationService.getGlobalProperty(POST_URL_PROPERTY), json, accessToken);
	}
	
	private String postJson(String url, String json, String authorization) throws IOException {
		HttpPost request = new HttpPost(url);
		request.setEntity(new StringEntity(json, UTF_8));
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-Type", "application/json; charset=UTF-8");
		if (authorization != null) {
			request.setHeader("Authorization", authorization);
		}
		
		try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(request)) {
			HttpEntity responseEntity = response.getEntity();
			String responseBody = responseEntity == null ? "" : EntityUtils.toString(responseEntity, UTF_8);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode < 200 || statusCode >= 300) {
				throw new IOException("Messaging service returned HTTP " + statusCode);
			}
			return responseBody;
		}
	}
	
	private String join(List<String> values) {
		StringBuilder result = new StringBuilder();
		for (String value : values) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(value);
		}
		return result.toString();
	}
	
	private String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\b", "\\b").replace("\f", "\\f").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}
}
