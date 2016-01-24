package com.st.nicobot.api.domain.model;

import org.joda.time.DateTime;

/**
 * @author Julien
 *
 */
public class Message {

	private DateTime postedDate;
	
	private String username;
	
	private String message;
	
	public Message() {	}
	
	public Message(DateTime pDate, String uName, String msg) {
		this.postedDate = pDate;
		this.message = msg;
		this.username = uName;
	}

	public DateTime getPostedDate() {
		return postedDate;
	}

	public String getUsername() {
		return username;
	}

	public String getMessage() {
		return message;
	}

}
