package be.zqsd.nicobot.bot.event;


import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public interface MessageEvent extends SlackMessagePostedListener {

   void onMessage(SlackMessagePosted message);
}
