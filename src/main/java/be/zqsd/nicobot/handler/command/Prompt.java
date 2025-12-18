package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.errors.BadRequestException;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageGenerateParams.Quality;
import com.openai.models.images.ImageGenerateParams.Size;
import com.slack.api.methods.response.files.FilesUploadV2Response;
import com.slack.api.model.event.MessageEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.util.*;

import static com.openai.models.images.ImageGenerateParams.Background.AUTO;
import static com.openai.models.images.ImageGenerateParams.Moderation.LOW;
import static com.openai.models.images.ImageGenerateParams.OutputFormat.PNG;
import static java.lang.String.join;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class Prompt implements NiCommand {

    private static final Logger LOG = getLogger(Prompt.class);

    private final Nicobot nicobot;

    private final String imageModel;
    private final Quality imageQuality;
    private final Size imageSize;

    private final OpenAIClientAsync openAIClient;

    @Inject
    public Prompt(Nicobot nicobot,
                  @ConfigProperty(name = "openai.api.key") String openAIApiKey,
                  @ConfigProperty(name = "openai.api.imageModel") String imageModel,
                  @ConfigProperty(name = "openai.api.imageQuality", defaultValue = "auto") String imageQuality,
                  @ConfigProperty(name = "openai.api.imageSize", defaultValue = "auto") String imageSize) {
        this.nicobot = nicobot;
        this.imageModel = imageModel;
        this.imageQuality = Quality.of(imageQuality);
        this.imageSize = Size.of(imageSize);
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
                .thenApplyAsync(imagesResponse -> imagesResponse.data().orElseThrow())
                .thenApplyAsync(imageList -> imageList.stream().findFirst().orElseThrow())
                .thenApplyAsync(image -> convertToFile(image.b64Json().orElseThrow()))
                .thenApply(file -> file.map(f -> this.uploadFileToSlack(triggeringMessage, f).orElseThrow()))
                .exceptionally(exception -> handleError(triggeringMessage, exception));

        LOG.debug("Query for question '{}' done. Now waiting...", question);
    }

    private Optional<FilesUploadV2Response> handleError(MessageEvent triggeringMessage, Throwable exception) {
        if (exception.getCause() instanceof BadRequestException cause) {
            nicobot.sendMessageInThread(triggeringMessage, cause.getMessage());
        } else {
            LOG.debug("There was an unknown issue processing this prompt", exception);
        }
        return empty();
    }

    private ImageGenerateParams buildRequest(String prompt) {
        return ImageGenerateParams.builder()
                .model(imageModel)
                .quality(imageQuality)
                .size(imageSize)
                .outputFormat(PNG)
                .background(AUTO)
                .moderation(LOW)
                .prompt(prompt)
                .build();
    }

    private Optional<File> convertToFile(String payload) {
        byte[] imageBytes = Base64.getDecoder().decode(payload);

        try (var inputStream = new ByteArrayInputStream(imageBytes)) {
            var outputFile = new File("/tmp/" + UUID.randomUUID() + ".png");
            var image = ImageIO.read(inputStream);
            ImageIO.write(image, "png", outputFile);
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
