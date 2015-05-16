package com.st.nicobot.internal.api.services;

import com.st.nicobot.api.domain.model.Message;
import com.st.nicobot.api.domain.model.request.MessageRequest;
import com.st.nicobot.api.domain.model.response.MessageResponse;
import com.st.nicobot.api.services.APIMessageService;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.List;

/**
 * @author Julien
 *
 */
@Service
public class APIMessageServiceImpl extends APIBaseService<MessageResponse> implements APIMessageService {

	public final String SERVICE_NAME = "messages";

	private static final MessageResponse reponseInstance = new MessageResponse();

	@Override
	public MessageResponse getResponseInstance() {
		return reponseInstance;
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public List<Message> getLastMessages(Integer maxMessages, DateTime startDate) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		if (maxMessages != null) {
			queryParams.add("limit", maxMessages.toString());
		}

		if (startDate == null) {
			DateTime now = new DateTime();
			startDate = now.minusMinutes(5).toDateTime(DateTimeZone.UTC);
		}

		queryParams.add("start_date", startDate.toString());

		MessageResponse response = sendGetRequest(queryParams);
		List<Message> messages = Collections.emptyList();

		if (response != null) {
			messages = response.getMessages();
		}

		return messages;
	}

	@Override
	public void saveMessages(List<Message> messages) {
		MessageRequest request = new MessageRequest();
		request.setMessages(messages);

		sendPostRequest(request);
	}

}
