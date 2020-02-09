package com.st.nicobot.bot.utils;

import com.ullink.slack.simpleslackapi.SlackChannel;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Reaction implements Serializable {

	private static final long serialVersionUID = -9167874891022108827L;
	private static final int DEFAULT_COOLDOWN_TIMER = 60;
	
	private Pattern pattern;
	private List<String> response;
	private int cooldownTimer;
	private Map<SlackChannel, DateTime> lastSpokenTimeByChan = new HashMap<>();
	
	/**
	 * Crée une réaction a un message sur le chan
	 * @param regex La regex à envoyer à Pattern.regex
	 * @param response la réponse à retourner en cas de succès au message
	 * @param caseInsensitive précise si le message est Case Insensitive ou non
	 * @param cooldownTimer Temps minimum pour ne pas répéter la réaction (en secondes)
	 */
	public Reaction(String regex, boolean caseInsensitive,	int cooldownTimer, String... response) {
		pattern = Pattern.compile(regex, (caseInsensitive) ? Pattern.CASE_INSENSITIVE : 0);
		this.response = Arrays.asList(response);
		this.cooldownTimer = cooldownTimer;
	}
	
	/**
	 * Crée une réaction Case insensitive et sans cooldown
	 * @param regex La regex à envoyer à Pattern.regex
	 * @param response la réponse à retourner en cas de succès au message
	 * @param response
	 */
	public Reaction(String regex, String... response) {
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		this.response = Arrays.asList(response);
		this.cooldownTimer = DEFAULT_COOLDOWN_TIMER;
	}
	
	/**
	 * Vérifie la regex par rapport au message donné.
	 * Si elle est valide, retoune <code>true</code>
	 * @param message le message à vérifier
	 * @return <code>true</code> si la regex valide le message.
	 */
	public boolean match(String message) {
		return pattern.matcher(message).matches();
	}
	
	public boolean canSaySentence(SlackChannel chan) {
		if(cooldownTimer > 0) {
			if(getSpokenTime(chan) != null) {
				if(getSpokenTime(chan).plusSeconds(cooldownTimer).isAfterNow()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public DateTime getSpokenTime(SlackChannel chan) {
		return lastSpokenTimeByChan.get(chan);
	}
	
	public void addSpokenTime(SlackChannel chan, DateTime date) {
		lastSpokenTimeByChan.put(chan, date);
	}

	public void addSpokenTime(SlackChannel chan) {
		addSpokenTime(chan, DateTime.now());
	}

	public Pattern getPattern() {
		return pattern;
	}

	public String getResponse() {
		if(response.size() == 1) {
			return response.get(0);
		} else {
			return response.get(RandomUtils.nextInt(0, response.size()));
		}
	}

	public int getCooldownTimer() {
		return cooldownTimer;
	}
}
