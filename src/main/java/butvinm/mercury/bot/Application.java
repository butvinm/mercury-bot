package butvinm.mercury.bot;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pengrad.telegrambot.model.Update;

import butvinm.mercury.bot.models.PipelineEvent;
import butvinm.mercury.bot.models.PipelineStatus;

@SpringBootApplication
@RestController
public class Application {
    private final Logger logger = initLogger();

    private final Bot bot = initBot(System.getenv("BOT_TOKEN"),
        System.getenv("CHAT_ID"));

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostMapping("/pipelines")
    public String mergeRequestHandler(
        @RequestBody PipelineEvent event
    ) {
        logger.info(event.toString());
        var eventStatus = event.getObjectAttributes().getStatus();
        if (eventStatus.equals(PipelineStatus.SUCCESS)) {
            return handleSuccessPipeline(event);
        }
        return "none";
    }

    @PostMapping("/bot")
    public String botWebhook(Update update) {
        var response = this.bot.handleUpdate(update);
        if (response.isPresent()) {
            return response.toString();
        }
        return null;
    }

    private String handleSuccessPipeline(PipelineEvent event) {
        var projectId = event.getProject().getId();
        var pipelineId = event.getObjectAttributes().getId();
        var pipelineName = event.getObjectAttributes().getName();
        var pipelineTime = event.getObjectAttributes().getFinishedAt();
        var report = "Pipeline \"%s\" finished successfully at %s"
            .formatted(pipelineName, pipelineTime);

        return this.bot.sendBuildReport(report, projectId, pipelineId)
            .toString();
    }

    private Bot initBot(String token, String chatId) {
        return new Bot(token, chatId);
    }

    private Logger initLogger() {
        var logger = Logger.getLogger("main");
        try {
            var fileHandler = new FileHandler("/tmp/logs/logs.log");
            logger.addHandler(fileHandler);
            var formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            return logger;
        } catch (SecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
