package org.openmrs.module.rbcmessaging.domain;

import java.util.Date;

import javax.xml.soap.MessageFactory;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Person;

/**
 * A class that represents a text based message sent within the messaging framework. This class
 * should be extended to create different message types, but could be used as-is in rare situations.
 * In general, you should not call the message constructor directly but should instead create them
 * via a class that implements the {@link MessageFactory} interface. Messages created outside of
 * message factories are not guaranteed to have properly formatted.
 * 
 * @see MessageFactory
 */
public class RBCMessagingMessage extends BaseOpenmrsObject {
	
	protected RBCMessagingMessage() {
	}
	
	private Integer messageId;
	
	/**
	 * The number of times that the system has tried to send the message. Once this number reaches the
	 * max_retries global property value, the message will be marked as 'failed' and the system will not
	 * continue to attempt to send it
	 */
	private Integer sendAttempts = 0;
	
	// content
	/**
	 * The string content of the message.
	 */
	protected String content;
	
	/**
	 * The address that the message was sent/is being sent to
	 */
	protected String destination;
	
	/**
	 * The person who received this message, can be null
	 */
	protected Person recipient;
	
	/**
	 * The date that the message was sent
	 */
	protected Date date;
	
	/**
	 * The status of this message
	 */
	private Integer status;
	
	/**
	 * Creates a message with only a destination and content. The date and origin are all filled in
	 * automatically at the time of sending.
	 * 
	 * @param destination the destination of the message
	 * @param content the content of the message
	 */
	public RBCMessagingMessage(String destination, String content) {
		this.destination = destination;
		this.content = content;
	}
	
	/**
	 * @return the text content of the message
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * @return
	 */
	public String getDestination() {
		return destination;
	}
	
	/**
	 * @param destination
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	/**
	 * @return
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * @param dateSent
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	/**
	 * @return
	 */
	public RBCMessagingMessageStatus getMessageStatus() {
		return RBCMessagingMessageStatus.getStatusByNumber(this.getStatus());
	}
	
	/**
	 * @param status
	 */
	public void setMessageStatus(RBCMessagingMessageStatus status) {
		this.setStatus(status.getNumber());
	}
	
	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}
	
	public Integer getMessageId() {
		return messageId;
	}
	
	public void setId(Integer messageId) {
		this.messageId = messageId;
	}
	
	public Integer getId() {
		return messageId;
	}
	
	public void setRecipient(Person recipient) {
		this.recipient = recipient;
	}
	
	public Person getRecipient() {
		return recipient;
	}
	
	/**
	 * @return The recipient's name if the recipient is set. Otherwise it returns the destination
	 *         address
	 */
	public String getDisplayDestination() {
		if (recipient != null) {
			return recipient.getPersonName().toString();
		} else {
			return destination;
		}
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}
	
	/**
	 * @param sendAttempts the sendAttempts to set
	 */
	public void setSendAttempts(Integer sendAttempts) {
		this.sendAttempts = sendAttempts;
	}
	
	/**
	 * @return the sendAttempts
	 */
	public Integer getSendAttempts() {
		return sendAttempts;
	}
	
}
