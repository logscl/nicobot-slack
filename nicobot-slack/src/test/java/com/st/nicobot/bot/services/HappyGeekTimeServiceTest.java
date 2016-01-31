package com.st.nicobot.bot.services;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.internal.services.HappyGeekTimeServiceImpl;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.UsernameService;
import com.st.nicobot.services.memory.GreetersRepositoryManager;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

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
    private GreetersRepositoryManager greeters;

    @Mock
    private Messages messages;

    private static String ALL_TOP = "All top: ";
    private static String WEEK_TOP = "Week top: ";
    private static String NO_ONE = "No one";

    private static List<String> names = Arrays.asList("Michel", "Roger", "Marcel");

    @Before
    public void setUp() {
        when(messages.getMessage("allTopHGT")).thenReturn(ALL_TOP);
        when(messages.getMessage("weekTopHGT")).thenReturn(WEEK_TOP);
        when(messages.getMessage("noOne")).thenReturn(NO_ONE);

        when(nicobot.findUserById(anyString())).thenReturn(mock(SlackUser.class));
        when(usernameService.getNoHLName(any(SlackUser.class))).thenReturn(names.get(0), names.get(1), names.get(2));
    }

    @Test
    public void test_getAllTimeTopUsers() {
        Map<String, Integer> testData = new LinkedHashMap<>();
        testData.put(names.get(0), 3);
        testData.put(names.get(1), 2);
        testData.put(names.get(2), 1);

        when(greeters.getAllTimeGreeters(anyString())).thenReturn(testData);

        String output = hgtService.getAllTimeTopUsers("general");
        String expected = ALL_TOP + names.get(0) + " ("+testData.get(names.get(0))+"), "+ names.get(1) + " ("+testData.get(names.get(1))+"), "+ names.get(2) + " ("+testData.get(names.get(2))+")";

        assertEquals(expected,output);

    }

    @Test
    public void test_getWeekTopUsers() {
        Map<String, Integer> testData = new LinkedHashMap<>();
        testData.put(names.get(0), 3);
        testData.put(names.get(1), 2);
        testData.put(names.get(2), 1);

        when(greeters.getWeeklyGreeters(anyString())).thenReturn(testData);

        String output = hgtService.getWeekTopUsers("general");
        String expected = WEEK_TOP + names.get(0) + " ("+testData.get(names.get(0))+"), "+ names.get(1) + " ("+testData.get(names.get(1))+"), "+ names.get(2) + " ("+testData.get(names.get(2))+")";

        assertEquals(expected,output);
    }

    @Test
    public void test_getWeekTopUsers_Nobody() {
        when(greeters.getWeeklyGreeters(anyString())).thenReturn(Collections.emptyMap());

        String output = hgtService.getWeekTopUsers("general");

        assertEquals(WEEK_TOP + NO_ONE, output);
    }

}
