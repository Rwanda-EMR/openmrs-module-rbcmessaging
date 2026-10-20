package org.openmrs.module.rbcmessaging.sms;

public class SmsResult {
	
	private final String provider;
	
	private final String providerMessageId;
	
	public SmsResult(String provider, String providerMessageId) {
		this.provider = provider;
		this.providerMessageId = providerMessageId;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public String getProviderMessageId() {
		return providerMessageId;
	}
}
