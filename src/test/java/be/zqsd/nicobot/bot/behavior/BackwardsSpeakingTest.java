package be.zqsd.nicobot.bot.behavior;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Julien
 *
 */
public class BackwardsSpeakingTest {

	private BackwardsSpeaking backwardsSpeaking = new BackwardsSpeaking();
	
	@Test
	public void testReverseMessage() throws Exception {
		String message = "hello World";
		String resp = backwardsSpeaking.reverseString(message);
		assertEquals("dlroW olleh", resp);

		message = "Hello World";
		resp = backwardsSpeaking.reverseString(message);
		assertEquals("DlroW olleh", resp);
		
		message = "Hello World !";
		resp = backwardsSpeaking.reverseString(message);
		assertEquals("! dlroW olleh", resp);
	}

	@Test
	public void testMatchUrl() throws Exception {
		String message = "http://www.test.com";
		String resp = backwardsSpeaking.reverseString(message);
		assertNull(resp);
		
		message = "Clique ici : http://lol.fnu/ :D";
		resp = backwardsSpeaking.reverseString(message);
		assertNull(resp);
		
		message = "Sa nana snob porte de trop bons ananas";
		resp = backwardsSpeaking.reverseString(message);
		assertEquals("Sanana snob port ed etrop bons anan as", resp);
	}

	@Test
	public void testTooLongMessage() throws Exception {
		String message = "Une phrase un peu trop longue que Nicobot ne doit pas répéter !";
		String resp = backwardsSpeaking.reverseString(message);
		assertNull(resp);
	}
}
