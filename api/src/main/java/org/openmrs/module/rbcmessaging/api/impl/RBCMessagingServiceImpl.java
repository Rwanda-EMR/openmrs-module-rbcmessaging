/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rbcmessaging.api.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.rbcmessaging.api.RBCMessagingService;
import org.openmrs.module.rbcmessaging.api.db.RBCMessagingDAO;
import org.openmrs.module.rbcmessaging.domain.RBCMessagingMessage;
import org.openmrs.module.rbcmessaging.util.RBCMessagingUtil;

/**
 * It is a default implementation of {@link RBCMessagingService}.
 */
public class RBCMessagingServiceImpl extends BaseOpenmrsService implements RBCMessagingService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private RBCMessagingDAO dao;
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(RBCMessagingDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @return the dao
	 */
	public RBCMessagingDAO getDao() {
		return dao;
	}
	
	public List<RBCMessagingMessage> getAllMessages() {
		return dao.getAllMessages();
	}
	
	public RBCMessagingMessage getMessage(Integer messageId) {
		return dao.getMessage(messageId);
	}
	
	public List<RBCMessagingMessage> getMessagesToPerson(Person recipient) {
		return dao.findMessagesWithPeople(recipient, null, null, null);
	}
	
	public List<RBCMessagingMessage> getMessagesFromPerson(Person sender) {
		return dao.findMessagesWithPeople(null, sender, null, null);
	}
	
	public List<RBCMessagingMessage> getMessagesToOrFromPerson(Person person) {
		return dao.findMessagesWithPeople(person, person, null, null);
	}
	
	public void deleteMessage(RBCMessagingMessage message) throws APIException {
		dao.deleteMessage(message);
	}
	
	public void saveMessage(RBCMessagingMessage message) throws APIException {
		dao.saveMessage(message);
	}
	
	public List<RBCMessagingMessage> getOutboxMessages() {
		return dao.getOutboxMessages();
	}
	
	public String getPersonName(Person person) {
		String patientName = "";
		if (person.getMiddleName() == null) {
			patientName = person.getFamilyName() + " " + person.getGivenName();
		} else {
			patientName = person.getFamilyName() + " " + person.getMiddleName() + " " + person.getGivenName();
		}
		return patientName;
	}
	
	@Override
	public void sendLabResultsNotifications() throws AuthenticationException, ClientProtocolException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		
		try {
			List<RBCMessagingMessage> messages = getAllMessages();
			for (RBCMessagingMessage message : messages) {
				String phone = message.getDestination();
				if (phone != null && phone.length() > 0) {
					Person person = message.getRecipient();
					String patientName = getPersonName(person);
					String messageAfterNameReplace = message.getContent().replace("patientName", patientName);
					CloseableHttpResponse response = RBCMessagingUtil.postMessage(phone, messageAfterNameReplace);
				}
				
			}
		}
		catch (Exception e) {
			log.error("There was an error sending appointment reminders" + e);
		}
		
	}
}
