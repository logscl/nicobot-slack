package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

import static com.theokanning.openai.completion.CompletionRequest.builder;
import static java.lang.String.join;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class Chat implements NiCommand {

    private static final Logger LOG = getLogger(Chat.class);
    private static final String GPT_MODEL = "text-davinci-003";
    private static final int MAX_TOKENS = 50;

    private final Nicobot nicobot;

    private final OpenAiService openAiService;

    @Inject
    public Chat(Nicobot nicobot,
                @ConfigProperty(name = "openai.api.key") String openAIApiKey) {
        this.nicobot = nicobot;
        this.openAiService = new OpenAiService(openAIApiKey);
    }

    @Override
    public Collection<String> getCommandNames() {
        return Collections.singletonList("!chat");
    }

    @Override
    public String getDescription() {
        return "Pose une question à Nicobot en utilisant ChatGPT";
    }

    @Override
    public String getFormat() {
        return "!chat une question";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        var question = join(" ", arguments);
        var request = buildRequest(question);

        supplyAsync(() -> queryOpenAI(request))
                .thenApply(response -> {
                    LOG.debug("Sending response to users...");
                    return nicobot.sendMessage(triggeringMessage, response);
                });

        LOG.debug("Query for question '{}' done. Now waiting...", question);
    }

    private CompletionRequest buildRequest(String question) {
        return builder()
                .prompt(question)
                .model(GPT_MODEL)
                .maxTokens(MAX_TOKENS)
                .build();
    }

    private String queryOpenAI(CompletionRequest request) {
        LOG.debug("Querying OpenAPI...");
        return openAiService.createCompletion(request)
                .getChoices().stream()
                .map(CompletionChoice::getText)
                .map(String::trim)
                .findFirst()
                .orElse("/shrug");
    }
}
