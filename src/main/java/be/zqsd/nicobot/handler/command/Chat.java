package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.slack.client.WebClient;
import com.slack.api.model.Message;
import com.slack.api.model.event.MessageEvent;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.theokanning.openai.completion.chat.ChatCompletionRequest.builder;
import static java.lang.String.join;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class Chat implements NiCommand {

    private static final Logger LOG = getLogger(Chat.class);
    public static final String USER_ROLE = "user";
    public static final String ASSISTANT_ROLE = "assistant";
    public static final String CHAT_COMMAND_TRIGGER = "!chat";

    private final Nicobot nicobot;
    private final WebClient client;

    private final String gptModel;
    private final int maxTokens;

    private final OpenAiService openAiService;

    @Inject
    public Chat(Nicobot nicobot,
                WebClient client,
                @ConfigProperty(name = "openai.api.key") String openAIApiKey,
                @ConfigProperty(name = "openai.api.model") String gptModel,
                @ConfigProperty(name = "openai.api.maxTokens") int maxTokens) {
        this.nicobot = nicobot;
        this.client = client;
        this.gptModel = gptModel;
        this.maxTokens = maxTokens;
        this.openAiService = new OpenAiService(openAIApiKey, Duration.ofMinutes(1));
    }

    @Override
    public Collection<String> getCommandNames() {
        return Collections.singletonList(CHAT_COMMAND_TRIGGER);
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
        var request = buildChatCompletionRequest(arguments, triggeringMessage);

        supplyAsync(() -> queryOpenAI(request))
                .thenApply(response -> {
                    LOG.debug("Sending response to users...");
                    return nicobot.sendMessage(triggeringMessage.getChannel(), triggeringMessage.getTs(), response);
                });
    }

    private ChatCompletionRequest buildChatCompletionRequest(Collection<String> arguments, MessageEvent triggeringMessage) {
        if (triggeringMessage.getThreadTs() != null) {
            var messagesOfThread = nicobot.getThreadMessages(triggeringMessage);
            LOG.debug("Building conversation from previous messages...");
            return buildRequestFromThread(messagesOfThread);
        } else {
            var question = join(" ", arguments);
            LOG.debug("Will Query for question '{}'", question);
            return buildRequestFromSingleMessage(question);
        }
    }

    private ChatCompletionRequest buildRequestFromSingleMessage(String question) {
        return builder()
                .model(gptModel)
                .messages(singletonList(new ChatMessage(USER_ROLE, question)))
                .maxTokens(maxTokens)
                .build();
    }

    private ChatCompletionRequest buildRequestFromThread(Collection<Message> messages) {
        var requests = messages.stream()
                .map(this::convertMessageToChatGPTChatMessage)
                .flatMap(Optional::stream)
                .toList();

        return builder()
                .model(gptModel)
                .messages(requests)
                .maxTokens(maxTokens)
                .build();
    }

    private Optional<ChatMessage> convertMessageToChatGPTChatMessage(Message message) {
        if (message.getText().startsWith(CHAT_COMMAND_TRIGGER)) {
            return of(new ChatMessage(USER_ROLE, message.getText().substring(6)));
        } else if (message.getUser().equals(client.botId())) {
            return of(new ChatMessage(ASSISTANT_ROLE, message.getText()));
        } else {
            return empty();
        }
    }

    private String queryOpenAI(ChatCompletionRequest request) {
        LOG.debug("Querying OpenAPI...");
        try {
            var completion = openAiService.createChatCompletion(request);
            LOG.debug("Query Done, OpenAI returned a completion: {}", completion);
            return completion.getChoices().stream()
                    .map(ChatCompletionChoice::getMessage)
                    .map(ChatMessage::getContent)
                    .map(String::trim)
                    .findFirst()
                    .orElse("/shrug");
        } catch (OpenAiHttpException e) {
            LOG.error("Open AI Failed to return a response", e);
            return "Open AI failed: " + e.getMessage();
        }
    }
}
