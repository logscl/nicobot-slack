package com.st.nicobot.api.services;

import com.st.nicobot.api.domain.model.Message;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Julien
 *
 */
public interface APIMessageService {

	List<Message> getLastMessages(Integer maxMessages, DateTime startDate);
	
	void saveMessages(List<Message> messages);
	
}
