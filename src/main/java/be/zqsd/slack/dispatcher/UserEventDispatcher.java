package be.zqsd.slack.dispatcher;

import be.zqsd.nicobot.bot.UserService;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.model.event.UserChangeEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserEventDispatcher {

    private final UserService userService;

    @Inject
    UserEventDispatcher(UserService userService) {
        this.userService = userService;
    }

    public BoltEventHandler<UserChangeEvent> onUserChange() {
        return (event, context) -> {
            userService.updateUser(event.getEvent().getUser());
            return context.ack();
        };
    }
}
