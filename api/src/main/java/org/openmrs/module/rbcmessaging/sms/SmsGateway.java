package org.openmrs.module.rbcmessaging.sms;

import java.io.IOException;

public interface SmsGateway {
	
	SmsResult send(SmsMessage message) throws IOException;
}
