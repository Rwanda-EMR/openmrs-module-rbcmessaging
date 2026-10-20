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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.PersonAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.rbcmessaging.api.RBCMessagingService;
import org.openmrs.module.rbcmessaging.sms.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendSMSAction {
	
	private static final Logger log = LoggerFactory.getLogger(SendSMSAction.class);
	
	private static final String APPOINTMENT_CONCEPTS_PROPERTY = "rbcmessaging.mohpih.appointmentDateConceptUUIDs";
	
	private static final String PHONE_NUMBER_ATTRIBUTE = "Phone number";
	
	public void sendSMS() {
		AdministrationService administrationService = Context.getAdministrationService();
		RBCMessagingService messagingService = Context.getService(RBCMessagingService.class);
		List<Concept> appointmentConcepts = getAppointmentConcepts(administrationService);
		Date reminderDate = getReminderDate(7);
		
		List<Obs> observations = Context.getObsService().getObservations(null, null, appointmentConcepts, null, null, null, null, null, null, null, null, false);
		for (Obs observation : observations) {
			if (isAppointmentOn(observation, reminderDate)) {
				sendReminder(messagingService, observation);
			}
		}
	}
	
	private List<Concept> getAppointmentConcepts(AdministrationService administrationService) {
		String configuredUuids = administrationService.getGlobalProperty(APPOINTMENT_CONCEPTS_PROPERTY);
		if (configuredUuids == null || configuredUuids.trim().isEmpty()) {
			throw new IllegalStateException(APPOINTMENT_CONCEPTS_PROPERTY + " is not configured");
		}
		
		List<Concept> concepts = new ArrayList<Concept>();
		for (String uuid : configuredUuids.split(",")) {
			Concept concept = Context.getConceptService().getConceptByUuid(uuid.trim());
			if (concept == null) {
				log.warn("Ignoring unknown appointment concept UUID " + uuid.trim());
			} else {
				concepts.add(concept);
			}
		}
		return concepts;
	}
	
	private Date getReminderDate(int daysFromToday) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, daysFromToday);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	private boolean isAppointmentOn(Obs observation, Date reminderDate) {
		return observation != null && observation.getValueDate() != null && observation.getValueDate().getTime() == reminderDate.getTime();
	}
	
	private void sendReminder(RBCMessagingService messagingService, Obs observation) {
		PersonAttribute phoneAttribute = observation.getPerson().getAttribute(PHONE_NUMBER_ATTRIBUTE);
		if (phoneAttribute == null || phoneAttribute.getValue() == null || phoneAttribute.getValue().trim().isEmpty()) {
			log.warn("Appointment SMS was not sent because person " + observation.getPerson().getPersonId() + " has no phone number");
			return;
		}
		
		String appointmentDate = new SimpleDateFormat("dd/MM/yyyy").format(observation.getValueDate());
		String messageBody = observation.getPerson().getFamilyName() + " " + observation.getPerson().getGivenName() + ". Itariki wahawe ya " + appointmentDate + " yo kugusubira ku ivuriro wisuzumishirijeho kanseri iregereje. Usabwe kujya ku ivuriro wisuzimishirijeho.";
		try {
			SmsResult result = messagingService.sendSms(phoneAttribute.getValue(), messageBody);
			log.info("Appointment SMS accepted by " + result.getProvider() + " with message ID " + result.getProviderMessageId());
		}
		catch (RuntimeException e) {
			log.error("Unable to send appointment SMS for person " + observation.getPerson().getPersonId(), e);
		}
	}
}
