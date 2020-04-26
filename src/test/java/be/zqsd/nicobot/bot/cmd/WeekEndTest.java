package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.internal.services.MessagesImpl;
import be.zqsd.nicobot.services.PropertiesService;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import static java.time.Duration.between;
import static java.time.LocalDateTime.of;
import static java.time.temporal.ChronoUnit.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Logs on 28-08-15.
 */
@RunWith(MockitoJUnitRunner.class)
public class WeekEndTest {

    private static LocalDateTime MONDAY_15_00 = of(2015,8,24,15,0,1);
    private static LocalDateTime THURSDAY_15_00 = of(2015,8,27,15,0,2);
    private static LocalDateTime FRIDAY_16_30 = of(2015,8,28,16,30,3);
    private static LocalDateTime FRIDAY_20_00 = of(2015,8,28,20,0,4);
    private static LocalDateTime SATURDAY_12_00 = of(2015,8,29,12,0,5);
    private static LocalDateTime SUNDAY_23_00 = of(2015,8,30,23,0,6);
    private static LocalDateTime MONDAY_03_00 = of(2015,8,31,3,0,7);

    private static LocalDateTime EXPECTED_WEEKEND_START = of(2015,8,28,17,0);
    private static LocalDateTime EXPECTED_WEEKEND_END = of(2015,8,31,9,0);

    @InjectMocks
    private WeekEnd weekEnd = new WeekEnd();

    @InjectMocks
    private MessagesImpl messages = new MessagesImpl();

    @Mock
    PropertiesService props;

    @Mock
    private NicoBot nicobot;

    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Before
    public void setUp() {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        ReflectionTestUtils.setField(weekEnd, "messages", messages);
        messages.afterPropertiesSet();
    }

    private void handle() {
        SlackMessagePosted message = mock(SlackMessagePosted.class);
        ArrayList<SlackChannel> list = new ArrayList<>();
        list.add(message.getChannel());

        weekEnd.doCommand("!weekend", null, new Option(message));
    }

    private String getRemainingTimeStr(LocalDateTime d1, LocalDateTime d2) {
        return ReflectionTestUtils.invokeMethod(weekEnd, "getRemainingTimeStr", between(d1,d2));
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
        when(clock.instant()).thenReturn(MONDAY_15_00.toInstant(ZoneOffset.UTC));

        String expectedMsg = messages.getMessage("weNoDays", DAYS.between(MONDAY_15_00, EXPECTED_WEEKEND_START), getRemainingTimeStr(MONDAY_15_00, EXPECTED_WEEKEND_START));

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_THURSDAY_15_00() {
        when(clock.instant()).thenReturn(THURSDAY_15_00.toInstant(ZoneOffset.UTC));

        long hours = HOURS.between(THURSDAY_15_00, EXPECTED_WEEKEND_START);

        String expectedMsg = messages.getMessage("weNoHours", hours, hours > 1 ? "s" : "", getRemainingTimeStr(THURSDAY_15_00, EXPECTED_WEEKEND_START));

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_FRIDAY_16_30() {
        when(clock.instant()).thenReturn(FRIDAY_16_30.toInstant(ZoneOffset.UTC));


        long minutes = MINUTES.between(FRIDAY_16_30, EXPECTED_WEEKEND_START);

        String expectedMsg = messages.getMessage("weNoMinutes", minutes, minutes > 1 ? "s" : "", getRemainingTimeStr(FRIDAY_16_30, EXPECTED_WEEKEND_START));

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_FRIDAY_20_00() {
        when(clock.instant()).thenReturn(FRIDAY_20_00.toInstant(ZoneOffset.UTC));

        String expectedMsg = messages.getMessage("weYesDays");

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_SATURDAY_12_00() {
        when(clock.instant()).thenReturn(SATURDAY_12_00.toInstant(ZoneOffset.UTC));

        String expectedMsg = messages.getMessage("weYesHours", HOURS.between(SATURDAY_12_00, EXPECTED_WEEKEND_END));

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_SUNDAY_23_00() {
        when(clock.instant()).thenReturn(SUNDAY_23_00.toInstant(ZoneOffset.UTC));

        String expectedMsg = messages.getMessage("weYesHours", HOURS.between(SUNDAY_23_00, EXPECTED_WEEKEND_END));

        checkMethod(expectedMsg);
    }

    @Test
    public void WeekEnd_Test_MONDAY_03_00() {
        when(clock.instant()).thenReturn(MONDAY_03_00.toInstant(ZoneOffset.UTC));

        String expectedMsg = messages.getMessage("weYesMinutes");

        checkMethod(expectedMsg);
    }


}
