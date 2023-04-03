package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

import static java.lang.String.join;

@ApplicationScoped
public class Chat implements NiCommand {

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
        var completionRequest = CompletionRequest.builder()
                .prompt(question)
                .model("text-davinci-003")
                .build();
        var answer = openAiService.createCompletion(completionRequest).getChoices().get(0).getText().trim();
        nicobot.sendMessage(triggeringMessage, answer);
    }
}
