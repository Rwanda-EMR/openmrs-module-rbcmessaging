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
package org.openmrs.module.rbcmessaging.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.openmrs.api.context.Context;
import org.openmrs.module.rbcmessaging.api.RBCMessagingService;
import org.openmrs.module.rbcmessaging.domain.RBCMessagingMessage;
import org.openmrs.module.rbcmessaging.util.RBCMessagingUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * The main controller.
 */
@Controller
public class RBCMessagingModuleManageController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/rbcmessaging/manage", method = RequestMethod.GET)
	public void manage(ModelMap model) {
		model.addAttribute("messages", Context.getService(RBCMessagingService.class).getAllMessages());
	}
	
	@RequestMapping("/module/rbcmessaging/resend")
	public ModelAndView exportModulesToFile(@RequestParam(required = false, value = "messageId") String messageId, @RequestParam(required = false, value = "resendType") String resendType, HttpServletRequest request, HttpServletResponse response) throws Exception {
		RBCMessagingMessage message = Context.getService(RBCMessagingService.class).getMessage(Integer.valueOf(messageId));
		CloseableHttpResponse responseFromPost = RBCMessagingUtil.postMessage(message.getDestination(), message.getContent());
		if(responseFromPost.getStatusLine().getStatusCode() == 0) {
			
		}
		Context.getService(RBCMessagingService.class).saveMessage(message);
		return new ModelAndView(new RedirectView("manage.form"));
	}
}
