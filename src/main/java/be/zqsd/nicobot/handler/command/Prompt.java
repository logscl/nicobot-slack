package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.methods.response.files.FilesUploadV2Response;
import com.slack.api.model.event.MessageEvent;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.theokanning.openai.image.CreateImageRequest.builder;
import static java.lang.String.join;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class Prompt implements NiCommand {

    private static final Logger LOG = getLogger(Prompt.class);

    private final Nicobot nicobot;

    private final String imageModel;
    private final String imageQuality;
    private final String imageStyle;
    private final String imageSize;

    private final OpenAiService openAiService;

    @Inject
    public Prompt(Nicobot nicobot,
                  @ConfigProperty(name = "openai.api.key") String openAIApiKey,
                  @ConfigProperty(name = "openai.api.imageModel") String imageModel,
                  @ConfigProperty(name = "openai.api.imageQuality", defaultValue = "standard") String imageQuality,
                  @ConfigProperty(name = "openai.api.imageStyle", defaultValue = "vivid") String imageStyle,
                  @ConfigProperty(name = "openai.api.imageSize", defaultValue = "1024x1024") String imageSize) {
        this.nicobot = nicobot;
        this.imageModel = imageModel;
        this.imageQuality = imageQuality;
        this.imageStyle = imageStyle;
        this.imageSize = imageSize;
        this.openAiService = new OpenAiService(openAIApiKey, Duration.ofMinutes(1));
    }

    @Override
    public Collection<String> getCommandNames() {
        return Collections.singletonList("!prompt");
    }

    @Override
    public String getDescription() {
        return "Génère une image en utilisant Dall-e";
    }

    @Override
    public String getFormat() {
        return "!prompt description d'une image à générer";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        var question = join(" ", arguments);
        var request = buildRequest(question);

        supplyAsync(() -> queryOpenAI(request))
                .thenApplyAsync(imageUrl -> imageUrl.map(this::downloadFile).orElseThrow())
                .thenApply(file -> file.map(f -> this.uploadFileToSlack(triggeringMessage, f).orElseThrow()))
                .exceptionally(exception -> {
                    LOG.error("A problem occurred when querying openAI / downloading file / uploading to slack", exception);
                    return empty();
                });

        LOG.debug("Query for question '{}' done. Now waiting...", question);
    }

    private CreateImageRequest buildRequest(String prompt) {
        return builder()
                .model(imageModel)
                .quality(imageQuality)
                .style(imageStyle)
                .size(imageSize)
                .prompt(prompt)
                .build();
    }

    private Optional<String> queryOpenAI(CreateImageRequest request) {
        LOG.debug("Querying OpenAPI...");
        try {
            var result = openAiService.createImage(request);
            LOG.debug("Query Done, OpenAI returned a result: {}", result);
            return result.getData().stream()
                    .map(Image::getUrl)
                    .findFirst();
        } catch (OpenAiHttpException e) {
            LOG.error("Open AI Failed to return a response", e);
            return empty();
        }
    }

    private Optional<File> downloadFile(String fileUrl) {
        try (var inputStream = new BufferedInputStream(new URL(fileUrl).openStream())) {
            var outputFile = new File("/tmp/" + UUID.randomUUID() + ".png");
            Files.copy(inputStream, outputFile.toPath());
            return of(outputFile);
        } catch (Exception e) {
            LOG.error("Unable to download Image and create a file from it", e);
            return empty();
        }
    }

    private Optional<FilesUploadV2Response> uploadFileToSlack(MessageEvent triggeringMessage, File file) {
        LOG.debug("Sending response to users...");
        return nicobot.uploadFile(triggeringMessage, triggeringMessage.getTs(), file);
    }
}
