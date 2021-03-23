/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.rbcmessaging.scheduler.tasks;

import org.openmrs.api.context.Context;
import org.openmrs.module.rbcmessaging.api.RBCMessagingService;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scheduled task for sending laboratory test results notifications via SMS. Scheduled Tasks are
 * regularly timed tasks that can run every few seconds, every day, every week, etc. See
 * Admin-->Manager Scheduled Tasks for the administration of them.
 * 
 * @author Bailly RURANGIRWA
 */
public class LabTestResultsNotificationTask extends AbstractTask {
	
	// Logger
	private static final Logger log = LoggerFactory.getLogger(LabTestResultsNotificationTask.class);
	
	@Override
	public void execute() {
		try {
			Context.getService(RBCMessagingService.class).sendLabResultsNotifications();
			
		}
		catch (Exception e) {
			log.error("Failed to send lab results notifications", e);
		}
	}
	
}
