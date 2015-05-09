package com.st.nicobot.api.domain.model.response;

import java.util.List;

import com.st.nicobot.api.domain.model.Message;
import com.st.nicobot.api.domain.model.Paging;

/**
 * @author Julien
 *
 */
public class MessageResponse implements UnmarshalledResponse {

	private List<Message> messages;
	
	private Paging paging;
	
	public MessageResponse() {	}

	public List<Message> getMessages() {
		return messages;
	}
	
	public Paging getPaging() {
		return paging;
	}
}
