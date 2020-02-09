package be.zqsd.nicobot.bot.services;

import be.zqsd.hgt.HgtScore;
import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.internal.services.HappyGeekTimeServiceImpl;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.UsernameService;
import be.zqsd.nicobot.services.PersistenceService;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Logs on 24-01-16.
 */
@RunWith(MockitoJUnitRunner.class)
public class HappyGeekTimeServiceTest {

    @InjectMocks
    private HappyGeekTimeServiceImpl hgtService = new HappyGeekTimeServiceImpl();

    @Mock
    private NicoBot nicobot;

    @Mock
    private UsernameService usernameService;

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private Messages messages;

    private static String ALL_TOP = "All top: ";
    private static String WEEK_TOP = "Week top: ";
    private static String NO_ONE = "No one";

    private static List<String> names = Arrays.asList("Michel", "Roger", "Marcel");

    @Before
    public void setUp() {
        when(messages.getMessage("allTopHGT", DateTime.now().getYear(), DateTime.now().getDayOfYear())).thenReturn(ALL_TOP);
        when(messages.getMessage("weekTopHGT")).thenReturn(WEEK_TOP);
        when(messages.getMessage("noOne")).thenReturn(NO_ONE);

        when(nicobot.getSession()).thenReturn(mock(SlackSession.class));
        when(nicobot.getSession().findUserById(anyString())).thenReturn(mock(SlackUser.class));
        when(usernameService.getNoHLName(any(SlackUser.class))).thenReturn(names.get(0), names.get(1), names.get(2));
    }

    @Test
    public void test_getAllTimeTopUsers() throws IOException {
        List<HgtScore> testData = new LinkedList<>();
        testData.add(new HgtScore("0", 3));
        testData.add(new HgtScore("1", 2));
        testData.add(new HgtScore("2", 1));

        when(persistenceService.getYearlyHgtScores(anyString())).thenReturn(unmodifiableList(testData));

        String output = hgtService.getAllTimeTopUsers("general");
        String expected = String.format("%s%s (%d), %s (%d), %s (%d)",
                ALL_TOP,
                names.get(0), testData.get(0).getScore(),
                names.get(1), testData.get(1).getScore(),
                names.get(2), testData.get(2).getScore());

        assertEquals(expected,output);

    }

    @Test
    public void test_getWeekTopUsers() throws IOException {
        List<HgtScore> testData = new LinkedList<>();
        testData.add(new HgtScore("0", 3));
        testData.add(new HgtScore("1", 2));
        testData.add(new HgtScore("2", 1));

        when(persistenceService.getWeeklyHgtScores(anyString())).thenReturn(unmodifiableList(testData));

        String output = hgtService.getWeekTopUsers("general");
        String expected = String.format("%s%s (%d), %s (%d), %s (%d)",
                WEEK_TOP,
                names.get(0), testData.get(0).getScore(),
                names.get(1), testData.get(1).getScore(),
                names.get(2), testData.get(2).getScore());

        assertEquals(expected,output);
    }

    @Test
    public void test_getWeekTopUsers_Nobody() throws IOException {
        when(persistenceService.getWeeklyHgtScores(anyString())).thenReturn(emptyList());

        String output = hgtService.getWeekTopUsers("general");

        assertEquals(WEEK_TOP + NO_ONE, output);
    }

}
