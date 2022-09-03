package be.zqsd.slack.client;

public class SlackClientException extends RuntimeException {

    public SlackClientException(Exception e) {
        super(e);
    }
}
