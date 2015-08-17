package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Reaction;
import com.st.nicobot.services.Commands;
import com.st.nicobot.services.LeetGreetingService;
import com.st.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Before
    public void setup() {
        when(nicobot.isSelfMessage(any(SlackMessagePosted.class))).thenReturn(false);
        when(greetingService.isLeetHourActive()).thenReturn(false);
        when(nicobot.sendMessage(any(SlackMessagePosted.class), anyString())).thenReturn(null);
        when(commands.isProbableCommand(anyString())).thenReturn(false);
    }

    /**
     * Certaines réactions ne doivent pas être déclanchées à chaque message
     */
    @Test
    public void testReactionWithDelay() {
        SlackMessagePosted message = mock(SlackMessagePosted.class);
        when(message.getMessageContent()).thenReturn("bla bla julie bla bla");
        when(message.getChannel()).thenReturn(mock(SlackChannel.class));
        ArrayList<SlackChannel> list = new ArrayList<>();
        list.add(message.getChannel());
        when(nicobot.getChannels()).thenReturn(list);

        HashSet<Reaction> set = new HashSet<>();
        set.add(new Reaction(".*(julie|hercot) .*",			true, 30, "On en reparle quand elle aura arrêté avec son équipe de meeeerde celle là.  Iiiimmmmbécile."));
        when(messages.getSentences()).thenReturn(set);


        reactions.onMessage(message);

        reactions.onMessage(message);

        verify(nicobot).sendMessage(any(SlackMessagePosted.class), anyString());

    }
}
