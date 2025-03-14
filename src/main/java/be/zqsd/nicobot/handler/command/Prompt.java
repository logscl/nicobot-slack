package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.core.JsonObject;
import com.openai.core.JsonValue;
import com.openai.errors.BadRequestException;
import com.openai.errors.OpenAIError;
import com.openai.errors.OpenAIServiceException;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageGenerateParams.Quality;
import com.openai.models.images.ImageGenerateParams.Size;
import com.openai.models.images.ImageGenerateParams.Style;
import com.slack.api.methods.response.files.FilesUploadV2Response;
import com.slack.api.model.event.MessageEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

import static java.lang.String.join;
import static java.util.Optional.*;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class Prompt implements NiCommand {

    private static final Logger LOG = getLogger(Prompt.class);

    private final Nicobot nicobot;

    private final String imageModel;
    private final String imageQuality;
    private final String imageStyle;
    private final String imageSize;

    private final OpenAIClientAsync openAIClient;

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
        this.openAIClient = OpenAIOkHttpClientAsync.builder()
                .apiKey(openAIApiKey)
                .timeout(Duration.ofMinutes(1))
                .build();
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

        openAIClient.images()
                .generate(request)
                .thenApplyAsync(imageResponse -> downloadFile(imageResponse.data().getFirst().url().orElseThrow()))
                .thenApply(file -> file.map(f -> this.uploadFileToSlack(triggeringMessage, f).orElseThrow()))
                .exceptionally(exception -> handleError(triggeringMessage, exception));

        LOG.debug("Query for question '{}' done. Now waiting...", question);
    }

    private Optional<FilesUploadV2Response> handleError(MessageEvent triggeringMessage, Throwable exception) {
        if (exception.getCause() instanceof BadRequestException cause) {
            var errorMessage = of(cause)
                    .map(OpenAIServiceException::error)
                    .map(OpenAIError::additionalProperties)
                    .map(properties -> properties.get("error"))
                    .map(JsonObject.class::cast)
                    .map(JsonObject::values)
                    .map(values -> values.get("message"))
                    .map(Objects::toString)
                    .orElse(":man-shrugging:");
            nicobot.sendMessage(triggeringMessage, errorMessage);

        } else {
            LOG.debug("There was an unknown issue processing this prompt", exception);
        }
        return empty();
    }

    private ImageGenerateParams buildRequest(String prompt) {
        return ImageGenerateParams.builder()
                .model(imageModel)
                .quality(Quality.of(imageQuality))
                .style(Style.of(imageStyle))
                .size(Size.of(imageSize))
                .prompt(prompt)
                .build();
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
