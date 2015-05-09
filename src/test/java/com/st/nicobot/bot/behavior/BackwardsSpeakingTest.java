package com.st.nicobot.bot.behavior;

import static org.junit.Assert.*;
import org.junit.Test;

import com.st.nicobot.bot.behavior.BackwardsSpeaking;

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
	
	public void testMatchUrl() throws Exception {
		String message = "http://www.test.com";
		String resp = backwardsSpeaking.reverseString(message);
		assertNull(resp);
		
		message = "Cliques ici : http://lol.fnu/ :D";
		resp = backwardsSpeaking.reverseString(message);
		assertNull(resp);
		
		message = "Sa nana snob porte de trop bons ananas";
		resp = backwardsSpeaking.reverseString(message);
		assertEquals("sanana snob port ed etrop bons anan aS", resp);
	}
	
	public void testTooLongMessage() throws Exception {
		String message = "Une phrase un peu trop longue que Nicobot ne doit pas répèter !";
		String resp = backwardsSpeaking.reverseString(message);
		assertNull(resp);
	}
}
