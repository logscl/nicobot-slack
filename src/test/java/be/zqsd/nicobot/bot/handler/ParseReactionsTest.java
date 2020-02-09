package be.zqsd.nicobot.bot.handler;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Reaction;
import be.zqsd.nicobot.services.Commands;
import be.zqsd.nicobot.services.LeetGreetingService;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.PropertiesService;
import be.zqsd.nicobot.utils.NicobotProperty;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.Mockito.*;

/**
 * Created by Logs on 16-05-15.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParseReactionsTest {

    @InjectMocks
    private ParseReactions reactions = new ParseReactions();

    @Mock
    private Messages messages;

    @Mock
    private NicoBot nicobot;

    @Mock
    private LeetGreetingService greetingService;

    @Mock
    private Commands commands;

    @Mock
    private PropertiesService properties;

    @Before
    public void setup() {
        when(greetingService.isLeetHourActive()).thenReturn(false);
        when(nicobot.sendMessage(any(SlackMessagePosted.class), anyString())).thenReturn(null);
        when(properties.get(NicobotProperty.FEATURED_CHANNEL)).thenReturn("general");
    }

    /**
     * Certaines réactions ne doivent pas être déclanchées à chaque message
     */
    @Test
    public void testReactionWithDelay() {
        SlackMessagePosted message = mock(SlackMessagePosted.class);
        when(message.getMessageContent()).thenReturn("bla bla julie bla bla");
        when(message.getChannel()).thenReturn(mock(SlackChannel.class));
        when(message.getChannel().getName()).thenReturn("general");
        ArrayList<SlackChannel> list = new ArrayList<>();
        list.add(message.getChannel());

        HashSet<Reaction> set = new HashSet<>();
        set.add(new Reaction(".*(julie|hercot) .*",			true, 30, "On en reparle quand elle aura arrêté avec son équipe de meeeerde celle là.  Iiiimmmmbécile."));
        when(messages.getSentences()).thenReturn(set);


        reactions.onMessage(message);

        reactions.onMessage(message);

        verify(nicobot).sendMessage(any(SlackMessagePosted.class), anyString());

    }
}
