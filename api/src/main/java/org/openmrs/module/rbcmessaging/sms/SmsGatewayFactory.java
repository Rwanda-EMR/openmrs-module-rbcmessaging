package org.openmrs.module.rbcmessaging.sms;

import org.openmrs.api.AdministrationService;

public final class SmsGatewayFactory {
	
	public static final String GATEWAY_PROPERTY = "rbcmessaging.gateway";
	
	public static final String DEFAULT_GATEWAY = "fdibiz";
	
	private SmsGatewayFactory() {
	}
	
	public static SmsGateway create(AdministrationService administrationService) {
		String gateway = administrationService.getGlobalProperty(GATEWAY_PROPERTY, DEFAULT_GATEWAY);
		if ("intouch".equalsIgnoreCase(gateway)) {
			return new IntouchSmsGateway(administrationService.getGlobalProperty("rbcmessaging.intouch.postURL"), administrationService.getGlobalProperty("rbcmessaging.intouch.username"), administrationService.getGlobalProperty("rbcmessaging.intouch.password"), administrationService.getGlobalProperty("rbcmessaging.intouch.sender"), administrationService.getGlobalProperty("rbcmessaging.intouch.coding", "0"), administrationService.getGlobalProperty("rbcmessaging.intouch.deliveryReportURL"), administrationService.getGlobalProperty("rbcmessaging.intouch.deliveryReportLevel", "2"));
		}
		if (DEFAULT_GATEWAY.equalsIgnoreCase(gateway)) {
			return new FdiBizSmsGateway(administrationService.getGlobalProperty("rbcmessaging.mohpih.username"), administrationService.getGlobalProperty("rbcmessaging.mohpih.password"), administrationService.getGlobalProperty("rbcmessaging.mohpih.authorizationURL"), administrationService.getGlobalProperty("rbcmessaging.mohpih.postURL"), administrationService.getGlobalProperty("rbcmessaging.mohpih.sender"));
		}
		throw new IllegalStateException("Unsupported SMS gateway: " + gateway);
	}
}
