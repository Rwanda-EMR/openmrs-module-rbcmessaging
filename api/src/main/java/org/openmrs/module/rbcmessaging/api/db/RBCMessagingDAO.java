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
package org.openmrs.module.rbcmessaging.api.db;

import java.util.List;

import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.module.rbcmessaging.api.RBCMessagingService;
import org.openmrs.module.rbcmessaging.domain.RBCMessagingMessage;

/**
 * Database methods for {@link RBCMessagingService}.
 */
public interface RBCMessagingDAO {
	
	/**
	 * @see MessageService#getAllMessages()
	 */
	public List<RBCMessagingMessage> getAllMessages();
	
	/**
	 * @see MessageService#getMessage(Integer)
	 */
	public RBCMessagingMessage getMessage(Integer messageId);
	
	/**
	 * @see MessageService#findMessages(MessagingGateway, Person, Person, String, Integer)
	 */
	public List<RBCMessagingMessage> findMessagesWithPeople(Person sender, Person recipient, String content, Integer status);
	
	/**
	 * @see MessageService#saveMessage(RBCMessagingMessage)
	 */
	public void saveMessage(RBCMessagingMessage message) throws APIException;
	
	/**
	 * @see MessageService#deleteMessage(RBCMessagingMessage)
	 */
	public void deleteMessage(RBCMessagingMessage message) throws APIException;
	
	public List<RBCMessagingMessage> getOutboxMessages();
}
