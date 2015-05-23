package com.st.nicobot.bot.event;


import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackMessageListener;

public interface MessageEvent extends SlackMessageListener {

   void onEvent(SlackMessage message);
}
