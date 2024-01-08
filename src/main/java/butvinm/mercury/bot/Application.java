package butvinm.mercury.bot;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
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
        new File("./users.db"),
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

    @PostMapping("/pipelines/{pipelineId}/messages")
    public void pipelineMessageHandler(
        @PathVariable Long pipelineId,
        @RequestBody String message
    ) {
        logger.info("Pipeline %s Message: %s".formatted(pipelineId, message));
        router.handlePipelineMessage(pipelineId, message);
    }

    @PostMapping("/bot")
    @JsonDeserialize()
    public Object botWebhook(@RequestBody String updateString) {
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
        File usersDb,
        Logger logger
    ) {
        var bot = new TelegramBot(botToken);
        var pipelineMessagesStore = new Redis<Long, List<String>>();
        var usersStore = new Mongo<BotUser>(usersDb, BotUser.class);
        return new BotRouter(
            bot,
            chatId,
            glClient,
            pipelineMessagesStore,
            usersStore,
            logger
        );
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
