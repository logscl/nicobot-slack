package be.zqsd.thirdparty;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class GithubService {

    private static final Logger LOG = getLogger(GithubService.class);

    private final String userName;
    private final String repositoryName;
    private final IssueService issueService;

    @Inject
    public GithubService(@ConfigProperty(name = "github.api.key") String apiKey,
                         @ConfigProperty(name = "github.repository.username") String userName,
                         @ConfigProperty(name = "github.repository.name") String repositoryName) {
        this.userName = userName;
        this.repositoryName = repositoryName;
        var client = new GitHubClient();
        client.setOAuth2Token(apiKey);
        this.issueService = new IssueService(client);
    }

    public Optional<Issue> createIssue(String title, String content) {
        try {
            var issue = new Issue()
                    .setTitle(title)
                    .setBody(content);

            return of(issueService.createIssue(userName, repositoryName, issue));
        } catch (IOException e) {
            LOG.error("Unable to create Github issue", e);
            return empty();
        }
    }
}
