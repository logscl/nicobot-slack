package com.st.nicobot.api.domain.model.request;

import java.util.List;

import com.st.nicobot.api.domain.model.Message;

/**
 * @author Julien
 *
 */
public class MessageRequest {

	private List<Message> messages;
	
	public MessageRequest() {	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
}
