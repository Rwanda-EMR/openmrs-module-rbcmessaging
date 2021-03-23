/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.rbcmessaging;

/**
 * Representation of the message configuration object. It indicates the service for which to send
 * appointment reminders, the number of days before the appointment day and the actual message to
 * send.
 * 
 * @author Bailly RURANGIRWA
 */
public class RBCMessagingConfig {
	
	private String serviceUUID;
	
	private int daysBefore;
	
	private String messageText;
	
	public RBCMessagingConfig() {
		super();
	}
	
	public RBCMessagingConfig(String serviceUUID, int daysBefore, String messageText) {
		super();
		this.serviceUUID = serviceUUID;
		this.daysBefore = daysBefore;
		this.messageText = messageText;
	}
	
	public String getServiceUUID() {
		return serviceUUID;
	}
	
	public void setServiceUUID(String serviceUUID) {
		this.serviceUUID = serviceUUID;
	}
	
	public int getDaysBefore() {
		return daysBefore;
	}
	
	public void setDaysBefore(int daysBefore) {
		this.daysBefore = daysBefore;
	}
	
	public String getMessageText() {
		return messageText;
	}
	
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	
}
