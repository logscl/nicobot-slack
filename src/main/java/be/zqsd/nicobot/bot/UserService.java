package be.zqsd.nicobot.bot;

import be.zqsd.slack.client.WebClient;
import com.slack.api.model.User;
import com.slack.api.model.User.Profile;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.shuffle;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class UserService {

    //not working any more chars: 200B. remaining untested: 200D/200E/200F/202F
    private static final String ZERO_WIDTH_SPACE = "\u200C";
    private static final String HIGHLIGHT_USER = "<@%s>";

    private Map<String, User> usersPerId;
    private final WebClient client;

    @Inject
    UserService(WebClient client) {
        this.client = client;
        loadUsers();
    }

    private void loadUsers() {
        usersPerId = client.fetchUsers()
                .stream()
                .collect(toMap(User::getId, identity()));
    }

    public void updateUser(User user) {
        usersPerId.replace(user.getId(), user);
    }

    public Optional<String> findUserName(String userId) {
        return ofNullable(usersPerId.get(userId))
                .map(User::getProfile)
                .map(Profile::getDisplayName);
    }

    public Optional<String> randomUserNameWithoutHighlight() {
        var activeUsers = usersPerId.values()
                .stream()
                .filter(User::isAppUser)
                .filter(not(User::isDeleted))
                .toList();
        shuffle(activeUsers);
        return activeUsers
                .stream()
                .findFirst()
                .map(User::getProfile)
                .map(Profile::getDisplayName)
                .map(this::withoutHighlight);
    }

    public Optional<String> userNameWithoutHighlight(String userId) {
        return findUserName(userId)
                .map(this::withoutHighlight);
    }

    public Optional<String> userNameWithHighlight(String userId) {
        return of(String.format(HIGHLIGHT_USER, userId));
    }

    public boolean isAdmin(String userId) {
        return ofNullable(usersPerId.get(userId))
                .map(User::isAdmin)
                .orElse(false);
    }

    private String withoutHighlight(String name) {
        return name.charAt(0) + ZERO_WIDTH_SPACE + name.substring(1);
    }
}
