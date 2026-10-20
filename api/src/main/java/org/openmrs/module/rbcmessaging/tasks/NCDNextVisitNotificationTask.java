/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.rbcmessaging.tasks;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.rbcmessaging.util.SendSMSToNCDPatientAction;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A scheduled task for sending laboratory test results notifications via SMS. Scheduled Tasks are
 * regularly timed tasks that can run every few seconds, every day, every week, etc. See
 * Admin-->Manager Scheduled Tasks for the administration of them.
 * 
 * @author Bailly RURANGIRWA
 */
public class NCDNextVisitNotificationTask extends AbstractTask {
	
	// Logger
	private static final Logger log = LoggerFactory.getLogger(NCDNextVisitNotificationTask.class);
	
	@Override
	public void execute() {
		/*
		 * try {
		 */
		/*
		 * SendSMSAction sendMessageAction = new SendSMSAction();
		 * sendMessageAction.sendSMS();
		 *//*
		     * SendNCDSMSAction sendNCDSMSAction = new SendNCDSMSAction();
		     * sendNCDSMSAction.sendSMS(); } catch (Exception e) {
		     * log.error("Failed to send SMS notifications", e); }
		     */
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String phoneNumber = "0788529201";
		String healthFacility = "Test HC";
		
		if (!isExecuting) {
			log.debug("Starting sending sms Task...");
			
			startExecuting();
			try {
				// SendSMSToNCDPatientAction sendNotificationAction = new
				// SendSMSToNCDPatientAction();
				// sendNotificationAction.sendSMS();
				
				String apiUsername = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpihncd.username");
				String apiPassword = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpihncd.password");
				String authorizationURL = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.authorizationURL");
				String postURL = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.postURL");
				String sender = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.sender");
				
				String jsonCredential = "{\"api_username\": \"" + apiUsername + "\",  \"api_password\": \"" + apiPassword + "\"}";
				HttpPost httpPostCredential = new HttpPost(authorizationURL);
				
				httpPostCredential.setEntity(new StringEntity(jsonCredential));
				
				httpPostCredential.setHeader("Content-type", "application/json");
				
				CloseableHttpClient clientCredential = HttpClients.createDefault();
				CloseableHttpResponse responseCredential = clientCredential.execute(httpPostCredential);
				String resultsCredential = EntityUtils.toString(responseCredential.getEntity());
				
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
				System.out.println("Token: " + access_token);
				clientCredential.close();
				String appointmentDateUUID = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.appointmentDateConceptUUIDs");
				
				String[] appointmentDateUUIDList = appointmentDateUUID.split(",");
				StringBuilder appointmentFound = new StringBuilder();
				
				List<Concept> returnVisitConcepts = new ArrayList<Concept>();
				
				for (String conceptUUID : appointmentDateUUIDList) {
					returnVisitConcepts.add(Context.getConceptService().getConceptByUuid(conceptUUID));
				}
				
				List<Obs> visitsInCommingWeek = new ArrayList<Obs>();
				
				Date currentDate = new Date();
				Calendar c = Calendar.getInstance();
				c.setTime(currentDate);
				c.add(Calendar.DATE, 5);
				Date currentDatePlusAddedDays = c.getTime();
				
				Calendar calendarrdvdate = Calendar.getInstance();
				calendarrdvdate.setTime(currentDatePlusAddedDays);
				calendarrdvdate.set(Calendar.HOUR_OF_DAY, 0);
				calendarrdvdate.set(Calendar.MINUTE, 0);
				calendarrdvdate.set(Calendar.SECOND, 0);
				calendarrdvdate.set(Calendar.MILLISECOND, 0);
				
				Date rdvdate = calendarrdvdate.getTime();
				
				visitsInCommingWeek = Context.getObsService().getObservations(null, null, returnVisitConcepts, null, null, null, null, null, null, null, null, false);
				int licationId = Context.getLocationService().getLocation(Integer.parseInt(Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.locationId"))).getLocationId();
				
				String serverLocation = Context.getLocationService().getLocation(Integer.parseInt(Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.locationId"))).getName();
				System.out.println("Obs Size: " + visitsInCommingWeek.size());
				if (visitsInCommingWeek.size() > 0) {
					int j = 0;
					for (Obs o : visitsInCommingWeek) {
						// System.out.println("Person ID: " + o.getPerson().getPersonId());
						
						if (o.getPerson() != null) {
							if (o.getPerson().getAttribute("Phone number") != null) {
								phoneNumber = o.getPerson().getAttribute("Phone number").getValue();
							}
							
							if (o.getPerson().getAttribute("Health Center") != null) {
								healthFacility = Context.getLocationService().getLocation(Integer.parseInt(o.getPerson().getAttribute("Health Center").getValue())).getName();
								
							}
						}
						phoneNumber = "0788529201";
						List<PatientProgram> NCDs = new ArrayList<PatientProgram>();
						
						Program hypertension = Context.getProgramWorkflowService().getProgramByUuid("a39cb21d-5f5e-4013-9b6b-139453ab93ec");
						NCDs.addAll(Context.getProgramWorkflowService().getPatientPrograms(Context.getPatientService().getPatient(o.getPerson().getPersonId()), hypertension, null, null, null, null, true));
						Program diabetes = Context.getProgramWorkflowService().getProgramByUuid("eff53c35-eaa8-4cae-9d68-78c2c21a5e6d");
						NCDs.addAll(Context.getProgramWorkflowService().getPatientPrograms(Context.getPatientService().getPatient(o.getPerson().getPersonId()), diabetes, null, null, null, null, true));
						Program ckd = Context.getProgramWorkflowService().getProgramByUuid("1b48afcc-347b-4419-b481-0a6a9db1e900");
						NCDs.addAll(Context.getProgramWorkflowService().getPatientPrograms(Context.getPatientService().getPatient(o.getPerson().getPersonId()), ckd, null, null, null, null, true));
						Program astima = Context.getProgramWorkflowService().getProgramByUuid("6b825913-ab44-4e49-9ea0-c9e5919f94af");
						NCDs.addAll(Context.getProgramWorkflowService().getPatientPrograms(Context.getPatientService().getPatient(o.getPerson().getPersonId()), astima, null, null, null, null, true));
						Program heartFailure = Context.getProgramWorkflowService().getProgramByUuid("e60a89ad-de11-421a-b5bf-b489e264ffa6");
						NCDs.addAll(Context.getProgramWorkflowService().getPatientPrograms(Context.getPatientService().getPatient(o.getPerson().getPersonId()), heartFailure, null, null, null, null, true));
						
						if (o != null && o.getValueDate() != null && o.getValueDate().getTime() == rdvdate.getTime() && phoneNumber != "" && NCDs.size() > 0 && serverLocation.equalsIgnoreCase(healthFacility)) {
							UUID uuid = UUID.randomUUID();
							String uuidAsString = uuid.toString();
							String appointment = formatter.format(o.getValueDate());
							String messageBody = "Kuri " + o.getPerson().getFamilyName() + " " + o.getPerson().getGivenName() + " ubu butumwa buturutse kuri " + healthFacility + " burakwibutsa ko itariki yo kujya kwivuza muri servisi y'indwara zitandura ari " + appointment + " Mugize ikibazo mwahamagara kuri 0788848156. Murakoze ";
							j++;
							String json = "{\"msisdn\": \"" + phoneNumber + "\",  \"message\": \"" + messageBody + "\",  \"msgRef\": \"" + uuidAsString + "\", \"sender_id\": \"" + sender + "\"}";
							
							HttpPost httpPost = new HttpPost(postURL);
							
							httpPost.setEntity(new StringEntity(json));
							httpPost.setHeader("Accept", "application/json");
							httpPost.setHeader("Content-type", "application/json");
							httpPost.setHeader("Authorization", access_token);
							
							CloseableHttpClient client = HttpClients.createDefault();
							CloseableHttpResponse response = client.execute(httpPost);
							System.out.println(response.getStatusLine());
							client.close();
						}
					}
				}
				
			}
			catch (Exception e) {
				log.error("Error executing sending sms Task", e);
			}
			finally {
				stopExecuting();
			}
		}
		
	}
	
}
