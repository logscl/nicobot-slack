package com.st.nicobot.bot.handler;

import com.st.nicobot.api.domain.model.Message;
import com.st.nicobot.api.services.APIMessageService;
import com.st.nicobot.bot.NicoBot;
import com.ullink.slack.simpleslackapi.SlackMessage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author Julien
 */
@Service
public class SaveMessage extends AbstractMessageEvent {

	@Autowired
	private APIMessageService apiMessageService;
	
	@Autowired
	private NicoBot nicoBot;

	@Override
	public void onMessage(SlackMessage message) {
		if(message.getChannel().getName().equals("general")) {
			onEvent(message);
		}
	}

	@Override
	public void onEvent(final SlackMessage message) {
		Thread t = new Thread() {
			public void run() {
				Message msg = new Message(new DateTime(), message.getSender().getUserName(), message.getMessageContent());
				apiMessageService.saveMessages(Collections.singletonList(msg));
			}
		};
		
		t.start();
	}

}
