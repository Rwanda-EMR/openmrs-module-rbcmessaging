package org.openmrs.module.rbcmessaging.sms;

public class SmsMessage {
	
	private final String phoneNumber;
	
	private final String content;
	
	private final String reference;
	
	public SmsMessage(String phoneNumber, String content, String reference) {
		this.phoneNumber = phoneNumber;
		this.content = content;
		this.reference = reference;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public String getContent() {
		return content;
	}
	
	public String getReference() {
		return reference;
	}
}
