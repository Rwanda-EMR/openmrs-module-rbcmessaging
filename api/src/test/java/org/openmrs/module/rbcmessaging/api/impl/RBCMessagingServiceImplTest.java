package org.openmrs.module.rbcmessaging.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.module.rbcmessaging.sms.SmsGateway;
import org.openmrs.module.rbcmessaging.sms.SmsMessage;
import org.openmrs.module.rbcmessaging.sms.SmsResult;

public class RBCMessagingServiceImplTest {
	
	@Test
	public void sendSmsShouldDelegateToConfiguredGateway() {
		RecordingGateway gateway = new RecordingGateway();
		TestService service = new TestService(gateway);
		
		SmsResult result = service.sendSms(" 250788123456 ", "Patient message");
		
		assertEquals("250788123456", gateway.message.getPhoneNumber());
		assertEquals("Patient message", gateway.message.getContent());
		assertNotNull(gateway.message.getReference());
		assertEquals("test", result.getProvider());
		assertEquals("vendor-id", result.getProviderMessageId());
	}
	
	@Test
	public void sendSmsShouldResolvePhoneNumberFromPatient() {
		RecordingGateway gateway = new RecordingGateway();
		TestService service = new TestService(gateway);
		Patient patient = new Patient(7);
		PersonAttributeType phoneType = new PersonAttributeType();
		phoneType.setName("Mobile phone");
		patient.addAttribute(new PersonAttribute(phoneType, "250788000007"));
		
		service.sendSms(patient, "Follow-up message");
		
		assertEquals("250788000007", gateway.message.getPhoneNumber());
		assertEquals("Follow-up message", gateway.message.getContent());
	}
	
	@Test(expected = APIException.class)
	public void sendSmsShouldRejectBlankPhoneNumber() {
		new TestService(new RecordingGateway()).sendSms(" ", "Message");
	}
	
	@Test(expected = APIException.class)
	public void sendSmsShouldConvertGatewayFailureToApiException() {
		new TestService(new FailingGateway()).sendSms("250788123456", "Message");
	}
	
	private static class TestService extends RBCMessagingServiceImpl {
		
		private final SmsGateway gateway;
		
		private TestService(SmsGateway gateway) {
			this.gateway = gateway;
		}
		
		@Override
		protected SmsGateway createSmsGateway() {
			return gateway;
		}
		
		@Override
		protected String getPhoneAttributeName() {
			return "Mobile phone";
		}
	}
	
	private static class RecordingGateway implements SmsGateway {
		
		private SmsMessage message;
		
		@Override
		public SmsResult send(SmsMessage message) {
			this.message = message;
			return new SmsResult("test", "vendor-id");
		}
	}
	
	private static class FailingGateway implements SmsGateway {
		
		@Override
		public SmsResult send(SmsMessage message) throws IOException {
			throw new IOException("vendor unavailable");
		}
	}
}
