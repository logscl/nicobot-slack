package com.st.nicobot.bot.utils;

public enum NicobotProperty {

	BOT_NAME("bot.name", "nicobot"),

	SLACK_API_KEY("slack.api.key", ""),

	API_URI("api.uri", "http://api.nicobot.cloudbees.net"),
	API_PERSISTENCE_STRATEGY("api.persistence.stragegy", "com.st.nicobot.internal.api.services.NoPersistenceStrategy");

	private String key;
	private String defaultValue;

	NicobotProperty(String key, String defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
