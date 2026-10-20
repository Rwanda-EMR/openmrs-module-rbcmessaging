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

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.rbcmessaging.sms.SmsGateway;
import org.openmrs.module.rbcmessaging.sms.SmsGatewayFactory;
import org.openmrs.module.rbcmessaging.sms.SmsMessage;
import org.openmrs.module.rbcmessaging.sms.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class SendSMSToNCDPatientAction {
	
	private static final Logger log = LoggerFactory.getLogger(SendSMSToNCDPatientAction.class);
	
	public void sendSMS() {
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		SmsGateway gateway = SmsGatewayFactory.create(Context.getAdministrationService());
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
		
		if (visitsInCommingWeek.size() > 0) {
			for (Obs o : visitsInCommingWeek) {
				String phoneNumber = "";
				String healthFacility = "";
				// System.out.println("Person ID: " + o.getPerson().getPersonId());
				
				if (o.getPerson() != null) {
					if (o.getPerson().getAttribute("Phone number") != null) {
						phoneNumber = o.getPerson().getAttribute("Phone number").getValue();
					}
					
					if (o.getPerson().getAttribute("Health Center") != null) {
						healthFacility = Context.getLocationService().getLocation(Integer.parseInt(o.getPerson().getAttribute("Health Center").getValue())).getName();
						
					}
				}
				
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
				
				if (o != null && o.getValueDate() != null && o.getValueDate().getTime() == rdvdate.getTime() && !phoneNumber.trim().isEmpty() && NCDs.size() > 0 && serverLocation.equalsIgnoreCase(healthFacility)) {
					String appointment = formatter.format(o.getValueDate());
					String messageBody = "Kuri " + o.getPerson().getFamilyName() + " " + o.getPerson().getGivenName() + " ubu butumwa buturutse kuri " + healthFacility + " burakwibutsa ko itariki yo kujya kwivuza muri servisi y'indwara zitandura ari " + appointment + " Mugize ikibazo mwahamagara kuri 0788848156. Murakoze ";
					sendReminder(gateway, o, phoneNumber, messageBody);
				}
			}
		}
	}
	
	private void sendReminder(SmsGateway gateway, Obs observation, String phoneNumber, String messageBody) {
		try {
			SmsResult result = gateway.send(new SmsMessage(phoneNumber, messageBody, UUID.randomUUID().toString()));
			log.info("NCD reminder SMS accepted by " + result.getProvider() + " with message ID " + result.getProviderMessageId());
		}
		catch (Exception e) {
			log.error("Unable to send NCD reminder SMS for person " + observation.getPerson().getPersonId(), e);
		}
	}
}
