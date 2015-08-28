package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.NicobotProperty;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.internal.services.MessagesImpl;
import com.st.nicobot.services.PropertiesService;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.joda.time.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Logs on 28-08-15.
 */
@RunWith(MockitoJUnitRunner.class)
public class WeekEndTest {

    private static DateTime MONDAY_15_00 = new DateTime(2015,8,24,15,00);
    private static DateTime THURSDAY_15_00 = new DateTime(2015,8,27,15,00);
    private static DateTime FRIDAY_16_30 = new DateTime(2015,8,28,16,30);
    private static DateTime FRIDAY_20_00 = new DateTime(2015,8,28,20,00);
    private static DateTime SATURDAY_12_00 = new DateTime(2015,8,29,12,00);
    private static DateTime SUNDAY_23_00 = new DateTime(2015,8,30,23,00);
    private static DateTime MONDAY_03_00 = new DateTime(2015,8,31,3,00);

    private static DateTime EXPECTED_WEEKEND_START = new DateTime(2015,8,28,17,00);
    private static DateTime EXPECTED_WEEKEND_END = new DateTime(2015,8,31,9,00);

    @InjectMocks
    private WeekEnd weekEnd = new WeekEnd();

    @InjectMocks
    private MessagesImpl messages = new MessagesImpl();

    @Mock
    PropertiesService props;

    @Mock
    private NicoBot nicobot;

    private Option option;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(weekEnd, "messages", messages);
        when(props.get(NicobotProperty.BOT_NAME)).thenReturn("nicobot");
        messages.start();
    }

    private void handle() {
        SlackMessagePosted message = mock(SlackMessagePosted.class);
        when(message.getMessageContent()).thenReturn("!weekend");
        when(message.getChannel()).thenReturn(mock(SlackChannel.class));
        ArrayList<SlackChannel> list = new ArrayList<>();
        list.add(message.getChannel());
        when(nicobot.getChannels()).thenReturn(list);

        option = new Option(message);

        weekEnd.doCommand("!weekend", null, option);
    }

    private void checkMethod(String messageToCheck) {
        System.out.println(messageToCheck);
        handle();

        verify(nicobot).sendMessage(any(SlackMessagePosted.class), messageCaptor.capture());

        String message = messageCaptor.getValue();

        assertNotNull(message);

        assertEquals(messageToCheck, message);
    }

    @Test
    public void WeekEnd_Test_MONDAY_15_00() {
        DateTimeUtils.setCurrentMillisFixed(MONDAY_15_00.getMillis());

        String expectedMsg = String.format(messages.getOtherMessage("weNoDays"), Days.daysBetween(MONDAY_15_00, EXPECTED_WEEKEND_START).getDays());

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_THURSDAY_15_00() {
        DateTimeUtils.setCurrentMillisFixed(THURSDAY_15_00.getMillis());

        String expectedMsg = String.format(messages.getOtherMessage("weNoHours"), Hours.hoursBetween(THURSDAY_15_00, EXPECTED_WEEKEND_START).getHours());

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_FRIDAY_16_30() {
        DateTimeUtils.setCurrentMillisFixed(FRIDAY_16_30.getMillis());

        String expectedMsg = String.format(messages.getOtherMessage("weNoMinutes"), Minutes.minutesBetween(FRIDAY_16_30, EXPECTED_WEEKEND_START).getMinutes());

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_FRIDAY_20_00() {
        DateTimeUtils.setCurrentMillisFixed(FRIDAY_20_00.getMillis());

        String expectedMsg = messages.getOtherMessage("weYesDays");

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_SATURDAY_12_00() {
        DateTimeUtils.setCurrentMillisFixed(SATURDAY_12_00.getMillis());

        String expectedMsg = String.format(messages.getOtherMessage("weYesHours"), Hours.hoursBetween(SATURDAY_12_00, EXPECTED_WEEKEND_END).getHours());

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_SUNDAY_23_00() {
        DateTimeUtils.setCurrentMillisFixed(SUNDAY_23_00.getMillis());

        String expectedMsg = String.format(messages.getOtherMessage("weYesHours"), Hours.hoursBetween(SUNDAY_23_00, EXPECTED_WEEKEND_END).getHours());

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_MONDAY_03_00() {
        DateTimeUtils.setCurrentMillisFixed(MONDAY_03_00.getMillis());

        String expectedMsg = messages.getOtherMessage("weYesMinutes");

        checkMethod(expectedMsg);
    }


}
