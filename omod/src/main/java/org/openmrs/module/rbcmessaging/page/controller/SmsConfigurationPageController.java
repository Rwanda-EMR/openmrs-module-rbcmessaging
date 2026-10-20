package org.openmrs.module.rbcmessaging.page.controller;

import java.net.URI;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.rbcmessaging.api.RBCMessagingService;
import org.openmrs.module.rbcmessaging.sms.SmsGatewayFactory;
import org.openmrs.module.rbcmessaging.sms.SmsResult;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.web.bind.annotation.RequestParam;

public class SmsConfigurationPageController {
	
	private static final String PAGE = "smsConfiguration";
	
	public void get(PageModel model, @SpringBean("adminService") AdministrationService administrationService, @RequestParam(value = "status", required = false) String status, @RequestParam(value = "detail", required = false) String detail) {
		Context.requirePrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		model.addAttribute("activeGateway", administrationService.getGlobalProperty(SmsGatewayFactory.GATEWAY_PROPERTY, SmsGatewayFactory.DEFAULT_GATEWAY));
		model.addAttribute("fdibizUsername", value(administrationService, "rbcmessaging.mohpih.username"));
		model.addAttribute("fdibizPasswordConfigured", configured(administrationService, "rbcmessaging.mohpih.password"));
		model.addAttribute("fdibizSender", value(administrationService, "rbcmessaging.mohpih.sender"));
		model.addAttribute("fdibizAuthorizationUrl", value(administrationService, "rbcmessaging.mohpih.authorizationURL"));
		model.addAttribute("fdibizPostUrl", value(administrationService, "rbcmessaging.mohpih.postURL"));
		model.addAttribute("intouchUsername", value(administrationService, "rbcmessaging.intouch.username"));
		model.addAttribute("intouchPasswordConfigured", configured(administrationService, "rbcmessaging.intouch.password"));
		model.addAttribute("intouchSender", value(administrationService, "rbcmessaging.intouch.sender"));
		model.addAttribute("intouchPostUrl", value(administrationService, "rbcmessaging.intouch.postURL"));
		model.addAttribute("intouchCoding", administrationService.getGlobalProperty("rbcmessaging.intouch.coding", "0"));
		model.addAttribute("intouchDeliveryReportUrl", value(administrationService, "rbcmessaging.intouch.deliveryReportURL"));
		model.addAttribute("intouchDeliveryReportLevel", administrationService.getGlobalProperty("rbcmessaging.intouch.deliveryReportLevel", "2"));
		model.addAttribute("status", status);
		model.addAttribute("detail", detail);
	}
	
	public String post(UiUtils ui, @SpringBean("adminService") AdministrationService administrationService, @RequestParam("action") String action, HttpServletRequest request) {
		Context.requirePrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		try {
			if ("save".equals(action)) {
				saveConfiguration(administrationService, request);
				return redirect(ui, "saved", "Configuration saved");
			}
			if ("test".equals(action)) {
				SmsResult result = sendTest(administrationService, request);
				return redirect(ui, "tested", "Accepted by " + result.getProvider() + "; message ID: " + result.getProviderMessageId());
			}
			return redirect(ui, "error", "Unsupported action");
		}
		catch (Exception e) {
			return redirect(ui, "error", safeMessage(e));
		}
	}
	
	private void saveConfiguration(AdministrationService administrationService, HttpServletRequest request) {
		String gateway = trim(request.getParameter("gateway"));
		if (!"fdibiz".equals(gateway) && !"intouch".equals(gateway)) {
			throw new IllegalArgumentException("Select a supported SMS vendor");
		}
		
		validateHttpUrl(request.getParameter("fdibizAuthorizationUrl"), "FDI Biz authorization URL", "fdibiz".equals(gateway));
		validateHttpUrl(request.getParameter("fdibizPostUrl"), "FDI Biz send URL", "fdibiz".equals(gateway));
		validateHttpUrl(request.getParameter("intouchPostUrl"), "Intouch send URL", "intouch".equals(gateway));
		String deliveryReportUrl = trim(request.getParameter("intouchDeliveryReportUrl"));
		validateHttpUrl(deliveryReportUrl, "Intouch delivery report URL", false);
		String deliveryReportLevel = trim(request.getParameter("intouchDeliveryReportLevel"));
		if (!isBlank(deliveryReportUrl) && !"1".equals(deliveryReportLevel) && !"2".equals(deliveryReportLevel) && !"3".equals(deliveryReportLevel)) {
			throw new IllegalArgumentException("Delivery report level must be 1, 2, or 3");
		}
		
		set(administrationService, SmsGatewayFactory.GATEWAY_PROPERTY, gateway);
		set(administrationService, "rbcmessaging.mohpih.username", request.getParameter("fdibizUsername"));
		setPasswordIfProvided(administrationService, "rbcmessaging.mohpih.password", request.getParameter("fdibizPassword"));
		set(administrationService, "rbcmessaging.mohpih.sender", request.getParameter("fdibizSender"));
		set(administrationService, "rbcmessaging.mohpih.authorizationURL", request.getParameter("fdibizAuthorizationUrl"));
		set(administrationService, "rbcmessaging.mohpih.postURL", request.getParameter("fdibizPostUrl"));
		set(administrationService, "rbcmessaging.intouch.username", request.getParameter("intouchUsername"));
		setPasswordIfProvided(administrationService, "rbcmessaging.intouch.password", request.getParameter("intouchPassword"));
		set(administrationService, "rbcmessaging.intouch.sender", request.getParameter("intouchSender"));
		set(administrationService, "rbcmessaging.intouch.postURL", request.getParameter("intouchPostUrl"));
		set(administrationService, "rbcmessaging.intouch.coding", request.getParameter("intouchCoding"));
		set(administrationService, "rbcmessaging.intouch.deliveryReportURL", deliveryReportUrl);
		set(administrationService, "rbcmessaging.intouch.deliveryReportLevel", deliveryReportLevel);
	}
	
	private SmsResult sendTest(AdministrationService administrationService, HttpServletRequest request) throws Exception {
		String phoneNumber = trim(request.getParameter("testPhoneNumber"));
		String content = trim(request.getParameter("testMessage"));
		if (isBlank(phoneNumber) || isBlank(content)) {
			throw new IllegalArgumentException("Test phone number and message are required");
		}
		return Context.getService(RBCMessagingService.class).sendSms(phoneNumber, content);
	}
	
	private void validateHttpUrl(String url, String label, boolean required) {
		if (isBlank(url)) {
			if (required) {
				throw new IllegalArgumentException(label + " is required");
			}
			return;
		}
		try {
			URI uri = new URI(url.trim());
			if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) || isBlank(uri.getHost())) {
				throw new IllegalArgumentException(label + " must be a valid HTTP or HTTPS URL");
			}
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IllegalArgumentException(label + " must be a valid HTTP or HTTPS URL");
		}
	}
	
	private String redirect(UiUtils ui, String status, String detail) {
		try {
			return "redirect:" + ui.pageLink("rbcmessaging", PAGE) + "?status=" + URLEncoder.encode(status, "UTF-8") + "&detail=" + URLEncoder.encode(detail, "UTF-8");
		}
		catch (Exception e) {
			return "redirect:" + ui.pageLink("rbcmessaging", PAGE);
		}
	}
	
	private String safeMessage(Exception exception) {
		String message = exception.getMessage();
		return isBlank(message) ? "The operation failed. Check the server log for details." : message;
	}
	
	private boolean configured(AdministrationService administrationService, String property) {
		return !isBlank(administrationService.getGlobalProperty(property));
	}
	
	private String value(AdministrationService administrationService, String property) {
		String value = administrationService.getGlobalProperty(property);
		return value == null ? "" : value;
	}
	
	private void set(AdministrationService administrationService, String property, String value) {
		administrationService.setGlobalProperty(property, trim(value));
	}
	
	private void setPasswordIfProvided(AdministrationService administrationService, String property, String password) {
		if (!isBlank(password)) {
			administrationService.setGlobalProperty(property, password);
		}
	}
	
	private String trim(String value) {
		return value == null ? "" : value.trim();
	}
	
	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
