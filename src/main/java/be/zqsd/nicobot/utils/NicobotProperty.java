package be.zqsd.nicobot.utils;

public enum NicobotProperty {

	SLACK_API_KEY("slack.api.key"),

	API_URI("api.uri", "http://api.nicobot.cloudbees.net"),
	API_PERSISTENCE_STRATEGY("api.persistence.stragegy", "NoPersistenceStrategy"),
	API_KEY("nicobot.persitence.api.key"),

	SEARCH_URI("search.uri"),
	SEARCH_CX_KEY("search.cx.key"),
	SEARCH_API_KEY("search.api.key"),

	YOUTUBE_QUERY_URI("youtube.query.uri"),
	YOUTUBE_VIDEO_URI("youtube.video.uri"),

	GITHUB_ISSUE_ADD_URL("github.issue.add.url"),
	GITHUB_API_KEY("github.api.key"),

	GOOGLE_MAPS_URI("google.maps.uri"),
	GOOGLE_MAPS_PLACE_URI("google.maps.place.uri"),

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
