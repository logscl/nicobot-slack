package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

import static java.lang.String.join;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class Chat implements NiCommand {

    private static final Logger LOG = getLogger(Chat.class);

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
        nicobot.sendMessage(triggeringMessage, "That's all folks !");
//        var question = join(" ", arguments);
//        var completionRequest = CompletionRequest.builder()
//                .prompt(question)
//                .model("text-davinci-003")
//                .maxTokens(1500)
//                .build();
//        String answer;
//        try {
//            answer = openAiService.createCompletion(completionRequest).getChoices().get(0).getText().trim();
//        } catch(OpenAiHttpException ex) {
//            LOG.error("Error during request to OpenAiService", ex);
//            answer = "J'suis tout cassé";
//        }
//        nicobot.sendMessage(triggeringMessage, answer);
    }
}
