package com.st.nicobot.bot.behavior;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Julien
 *
 */
@Service
public class BackwardsSpeaking implements NiConduct {

	@Autowired
	private NicoBot nicobot;

	/**
	 * Nombre maximum de char que la phrase doit contenir pour être inversée
	 */
	private static int MAX_LENGTH_TO_REVERSE = 50;
	private static Pattern pattern = Pattern.compile("(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?");
	
	@Override
	public int getChance() {
		return 30;
	}

	@Override
	public void behave(Option opts) {
		if(!nicobot.isSelfMessage(opts.message)) {
			final String reversedMessage = reverseString(opts.message.getMessageContent());
			if (reversedMessage != null) {
				nicobot.sendMessage(opts.message, reversedMessage);
			}
		}
	}
	
	/**
	 * Retourne {@code string} renversé. <br/>
	 * Si une majuscule est présente sur le 1er caractere de {@code string}, 
	 * alors le 1er char de la chaine renversée sera en maj aussi.
	 * Conditions d'annulation :
	 * - La phrase contient ou est une url;
	 * - La phrase dépasse {@link MAX_LENGTH_TO_REVERSE} caractères
	 * @param string
	 * @return
	 */
	protected String reverseString(String string) {
		
		// 1. Si c'est une url, on passe 
		if(pattern.matcher(string).find()) {
			return null;
		}
		
		// 2. Si la phrase est trop longue, on passe aussi
		if(string.length() > MAX_LENGTH_TO_REVERSE) {
			return null;
		}
		
		boolean isFirstLetterUpperCase = CharUtils.isAsciiAlphaUpper(string.charAt(0));

		StringBuilder builder = new StringBuilder(string).reverse();
		
		if (isFirstLetterUpperCase) {
			String firstCharSequence = builder.substring(0, 1);
			String lastCharSequence = builder.substring(builder.length()-1);
			
			builder.replace(0, 1, StringUtils.upperCase(firstCharSequence));
			builder.replace(builder.length()-1, builder.length(), StringUtils.lowerCase(lastCharSequence));
		}

		return fixTags(builder.toString());
	}

	private String fixTags(String string) {
		Pattern pattern = Pattern.compile(":[^:]*:");

		Matcher match = pattern.matcher(string);

		while(match.find()) {
			String text = match.group();
			string = string.replace(text, StringUtils.reverse(text));
		}

		pattern = Pattern.compile(">[^<]*(@|#)<");

		match = pattern.matcher(string);

		while(match.find()) {
			String text = match.group();
			string = string.replace(text, StringUtils.reverse(text));
		}

		return string;

	}

}