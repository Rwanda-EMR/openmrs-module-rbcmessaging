package org.openmrs.module.rbcmessaging.domain;

public enum RBCMessagingMessageStatus {
	SENT(0),
	RETRYING(1),
	FAILED(2),
	OUTBOX(3);
	
	private int number;
	
	private RBCMessagingMessageStatus(int number) {
		this.number = number;
	}
	
	public static RBCMessagingMessageStatus getStatusByNumber(int number) {
		for (RBCMessagingMessageStatus m : RBCMessagingMessageStatus.values()) {
			if (m.getNumber() == number) {
				return m;
			}
		}
		return null;
	}
	
	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}
}
