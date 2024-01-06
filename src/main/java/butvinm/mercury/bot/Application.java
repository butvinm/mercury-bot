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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.models.PipelineEvent;

@SpringBootApplication
@RestController
public class Application {
    private final Logger logger = initLogger();

    private final GitLabClient glClient = initGitLabClient(
        System.getenv("GITLAB_HOST"),
        System.getenv("GITLAB_ACCESS_TOKEN"),
        logger
    );

    private final BotRouter router = initBotRouter(
        System.getenv("BOT_TOKEN"),
        System.getenv("CHAT_ID"),
        glClient,
        logger
    );

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostMapping("/pipelines")
    public SendResponse pipelineHandler(@RequestBody PipelineEvent event) {
        logger.info(event.toString());
        return router.handlePipelineEvent(event);
    }

    @PostMapping("/bot")
    @JsonDeserialize()
    public SendResponse botWebhook(@RequestBody String updateString) {
        // Two wide-spread JSON serialization libraries,
        // what could went wrong...?
        // I don't wanna configure Spring to use Gson for that specific handler,
        // so just manually call Gson for telegrambot.Update
        var update = new Gson().fromJson(updateString, Update.class);
        return router.handleUpdate(update);
    }

    private GitLabClient initGitLabClient(
        String gitlabHost,
        String accessToken,
        Logger logger
    ) {
        return new GitLabClient(gitlabHost, accessToken, logger);
    }

    private BotRouter initBotRouter(
        String botToken,
        String chatId,
        GitLabClient glClient,
        Logger logger
    ) {
        return new BotRouter(new TelegramBot(botToken), chatId, glClient,
            logger);
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
