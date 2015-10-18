package com.st.nicobot.bot.cmd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Logs on 18-10-15.
 */
@RunWith(MockitoJUnitRunner.class)
public class NiCommandTest {


    @Test
    public void test_getArgs_No_Quotes() throws Exception {
        String testString = "String with no quotes";

        String[] output = NiCommand.getArgs(testString);

        assertNotNull(output);
        assertEquals(4, output.length);
        assertEquals("String", output[0]);
        assertEquals("with", output[1]);
        assertEquals("no", output[2]);
        assertEquals("quotes", output[3]);

    }

    @Test
    public void test_getArgs_No_Quotes_2s() throws Exception {
        String testString = "String    with     no      quotes";

        String[] output = NiCommand.getArgs(testString);

        assertNotNull(output);
        assertEquals(4, output.length);
        assertEquals("String", output[0]);
        assertEquals("with", output[1]);
        assertEquals("no", output[2]);
        assertEquals("quotes", output[3]);

    }

    @Test
    public void test_getArgs_1_Quoted_arg() throws Exception {
        String testString = "String with \"one quoted arg\"";

        String[] output = NiCommand.getArgs(testString);

        assertNotNull(output);
        assertEquals(3, output.length);
        assertEquals("String", output[0]);
        assertEquals("with", output[1]);
        assertEquals("one quoted arg", output[2]);

    }

    @Test
    public void test_getArgs_2_Quoted_args() throws Exception {
        String testString = "String \"with two\" \"quoted args\"";

        String[] output = NiCommand.getArgs(testString);

        assertNotNull(output);
        assertEquals(3, output.length);
        assertEquals("String", output[0]);
        assertEquals("with two", output[1]);
        assertEquals("quoted args", output[2]);
    }

    @Test
    public void test_getArgs_2_Quoted_args_2() throws Exception {
        String testString = "String \"with two\" awesome \"quoted args\"";

        String[] output = NiCommand.getArgs(testString);

        assertNotNull(output);
        assertEquals(4, output.length);
        assertEquals("String", output[0]);
        assertEquals("with two", output[1]);
        assertEquals("awesome", output[2]);
        assertEquals("quoted args", output[3]);
    }

    @Test
    public void test_getArgs_2_Quoted_args_3() throws Exception {
        String testString = "String \"with two\"\"quoted args\"";

        String[] output = NiCommand.getArgs(testString);

        assertNotNull(output);
        assertEquals(3, output.length);
        assertEquals("String", output[0]);
        assertEquals("with two", output[1]);
        assertEquals("quoted args", output[2]);
    }

    @Test
    public void test_getArgs_2_Quoted_args_4() throws Exception {
        String testString = "\"c'est bon laaa ?\" \"oui.\"";

        String[] output = NiCommand.getArgs(testString);

        assertNotNull(output);
        assertEquals(2, output.length);
        assertEquals("c'est bon laaa ?", output[0]);
        assertEquals("oui.", output[1]);
    }

}
