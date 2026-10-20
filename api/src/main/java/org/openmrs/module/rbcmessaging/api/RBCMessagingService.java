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
package org.openmrs.module.rbcmessaging.api;

import java.io.IOException;
import java.util.List;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.rbcmessaging.domain.RBCMessagingMessage;
import org.openmrs.module.rbcmessaging.sms.SmsResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured
 * in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(RBCMessagingService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
public interface RBCMessagingService extends OpenmrsService {
	
	/**
	 * Sends an SMS to the phone number stored on a patient. The active vendor and the patient phone
	 * attribute are selected through the module's global properties.
	 * 
	 * @param patient patient who should receive the message
	 * @param message message body
	 * @return the vendor's acceptance result
	 * @throws APIException if the request is invalid or the vendor rejects/cannot process it
	 */
	SmsResult sendSms(Patient patient, String message) throws APIException;
	
	/**
	 * Sends an SMS to an explicit phone number through the currently configured vendor.
	 * 
	 * @param phoneNumber destination number, including the country code
	 * @param message message body
	 * @return the vendor's acceptance result
	 * @throws APIException if the request is invalid or the vendor rejects/cannot process it
	 */
	SmsResult sendSms(String phoneNumber, String message) throws APIException;
	
	/**
	 * @return all messages
	 */
	@Transactional(readOnly = true)
	public List<RBCMessagingMessage> getAllMessages();
	
	/**
	 * @param messageId
	 * @return the message with the corresponding messageId
	 */
	@Transactional(readOnly = true)
	public RBCMessagingMessage getMessage(Integer messageId);
	
	/**
	 * @param sender
	 * @return all messages that sender has sent
	 */
	@Transactional(readOnly = true)
	public List<RBCMessagingMessage> getMessagesFromPerson(Person sender);
	
	/**
	 * @param recipient
	 * @return all messages that recipient has received
	 */
	@Transactional(readOnly = true)
	public List<RBCMessagingMessage> getMessagesToPerson(Person recipient);
	
	/**
	 * @param person
	 * @return all messages to or from person
	 */
	@Transactional(readOnly = true)
	public List<RBCMessagingMessage> getMessagesToOrFromPerson(Person person);
	
	/**
	 * Create or update message
	 */
	@Transactional
	public void saveMessage(RBCMessagingMessage message) throws APIException;
	
	/**
	 * Delete message
	 */
	@Transactional
	public void deleteMessage(RBCMessagingMessage message) throws APIException;
	
	@Transactional(readOnly = true)
	public List<RBCMessagingMessage> getOutboxMessages();
	
	void sendLabResultsNotifications() throws AuthenticationException, ClientProtocolException, IOException;
	
}
