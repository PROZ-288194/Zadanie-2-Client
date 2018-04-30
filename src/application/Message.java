package application;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;
	private String textMessage;
	private String attachMessage;
	private Type messageType;
	
	public Message(Message.Type type_) {
		messageType = type_;
	}
	
	public void setUsername(String username_) {
		username = username_;
	}
	
	public void setMessage(String message_) {
		textMessage = message_;
	}
	
	public void setAttachMessage(String attachMessage_) {
		attachMessage = attachMessage_;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getTextMessage() {
		return textMessage;
	}
	
	public String getAttachMessage() {
		return attachMessage;
	}
	
	public String getType() {
		return messageType.toString();
	}
	public String toString() {
		return "username: " + username + "\n message: " + textMessage; 
	}
	
	public enum Type {
		TEXT, BINARY;
	}
}
