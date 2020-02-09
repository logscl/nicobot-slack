package be.zqsd.nicobot.bot.handler;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Emoji;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.bot.behavior.BackwardsSpeaking;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Lorsque le message recu est un palindrome, il y a une chance que nicobot troll et le renvoi sans modif (simulation de 
 * {@link BackwardsSpeaking}.
 * 
 * @author Julien
 *
 */
@Service
public class TrollBackwardsSpeaking extends ConditionalMessageEvent {

	@Autowired
	private NicoBot nicobot;
	
    @Override
    public int getChance() {
        return 150;
    }

    @Override
    public void onMessage(SlackMessagePosted message) {
    	Option opt = new Option(message);
    	
    	if (testCondition(opt)) {
            nicobot.sendMessage(message, message.getMessageContent(), Emoji.TROLL, true);
        }
    }
    
    @Override
    public boolean testImpl(Option o) {
    	return o.message.getMessageContent().length() > 1 && StringUtils.reverse(o.message.getMessageContent()).equals(o.message.getMessageContent());
    }
}
