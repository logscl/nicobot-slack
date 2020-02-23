package be.zqsd.nicobot.utils;

public enum NicobotProperty {

	SLACK_API_KEY("slack.api.key"),

	API_URI("api.uri", "http://api.nicobot.cloudbees.net"),
	API_KEY("nicobot.persitence.api.key"),

	SEARCH_CX_KEY("search.cx.key"),
	SEARCH_API_KEY("search.api.key"),

	YOUTUBE_VIDEO_URI("youtube.video.uri"),

	GITHUB_API_KEY("github.api.key"),
	GITHUB_REPOSITORY_USERNAME("github.repository.username"),
	GITHUB_REPOSITORY_NAME("github.repository.name"),

	FEATURED_CHANNEL("nicobot.featured.channel"),

	ALGORITHMIA_API_KEY("algorithmia.api.key"),
	ALGORITHMIA_NUDITY_ALGORITHM("algorithmia.nudity.algorithm");

	private String key;
	private String defaultValue;

	NicobotProperty(String key, String defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	NicobotProperty(String key) {
		this.key = key;
		this.defaultValue = "";
	}

	public String getKey() {
		return key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
