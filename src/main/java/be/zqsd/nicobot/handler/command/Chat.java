package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.slack.client.WebClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionCreateParams.WebSearchOptions;
import com.openai.models.chat.completions.ChatCompletionCreateParams.WebSearchOptions.UserLocation;
import com.openai.models.chat.completions.ChatCompletionCreateParams.WebSearchOptions.UserLocation.Approximate;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.slack.api.model.Message;
import com.slack.api.model.event.MessageEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static java.lang.String.join;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class Chat implements NiCommand {

    private static final Logger LOG = getLogger(Chat.class);
    public static final String CHAT_COMMAND_TRIGGER = "!chat";

    private final Nicobot nicobot;
    private final WebClient client;

    private final String gptModel;
    private final int maxTokens;

    private final OpenAIClientAsync openAIClient;

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
        this.openAIClient = OpenAIOkHttpClientAsync.builder()
                .apiKey(openAIApiKey)
                .timeout(Duration.ofMinutes(1))
                .build();
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
        var params = buildChatCompletionCreate(arguments, triggeringMessage);

        openAIClient.chat()
                .completions()
                .create(params)
                .thenAccept(completion -> completion.choices().getFirst().message().content().ifPresent(response -> {
                    LOG.debug("Sending response to users...");
                    nicobot.sendMessageInThread(triggeringMessage, response);
                }));
    }

    private ChatCompletionCreateParams buildChatCompletionCreate(Collection<String> arguments, MessageEvent triggeringMessage) {
        var builder = ChatCompletionCreateParams.builder()
                .model(gptModel)
                .maxCompletionTokens(maxTokens)
                .webSearchOptions(createWebSearchOptions());
        if (triggeringMessage.getThreadTs() != null) {
            var messagesOfThread = nicobot.getThreadMessages(triggeringMessage);
            LOG.debug("Building conversation from previous messages...");
            messagesOfThread.stream()
                    .map(this::buildMessageParam)
                    .flatMap(Optional::stream)
                    .forEach(builder::addMessage);
        } else {
            var question = join(" ", arguments);
            LOG.debug("Will Query for question '{}'", question);
            builder.addUserMessage(question);
        }
        return builder.build();
    }

    private WebSearchOptions createWebSearchOptions() {
        return WebSearchOptions.builder()
                .userLocation(UserLocation.builder()
                        .approximate(Approximate.builder()
                                .country("BE")
                                .build())
                        .build())
                .build();
    }

    private Optional<ChatCompletionMessageParam> buildMessageParam(Message message) {
        if (message.getText().startsWith(CHAT_COMMAND_TRIGGER)) {
            var userMessage = ChatCompletionUserMessageParam.builder()
                    .content(message.getText().substring(6))
                    .build();
            return of(ChatCompletionMessageParam.ofUser(userMessage));
        } else if (message.getUser().equals(client.botId())) {
            var systemMessage = ChatCompletionSystemMessageParam.builder()
                            .content(message.getText())
                                    .build();
            return of(ChatCompletionMessageParam.ofSystem(systemMessage));
        } else {
            return empty();
        }
    }
}
