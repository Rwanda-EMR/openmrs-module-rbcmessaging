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

import org.openmrs.Cohort;

/**
 * Representation of the message configuration object. It indicates the cohort
 * for which to send a message and the actual message to send.
 * 
 * @author Bailly RURANGIRWA
 */
public class MessagingConfig {

	private Cohort cohort;

	private String messageText;

	public MessagingConfig() {
		super();
	}

	public MessagingConfig(Cohort cohort, String messageText) {
		super();
		this.cohort = cohort;
		this.messageText = messageText;
	}

	public Cohort getCohort() {
		return cohort;
	}

	public void setCohort(Cohort cohort) {
		this.cohort = cohort;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

}
