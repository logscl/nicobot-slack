package com.st.nicobot.api.services;

import java.util.List;

import org.joda.time.DateTime;

import com.st.nicobot.api.domain.model.Message;

/**
 * @author Julien
 *
 */
public interface APIMessageService {

	List<Message> getLastMessages(Integer maxMessages, DateTime startDate);
	
	void saveMessages(List<Message> messages);
	
}
