package butvinm.mercury.bot;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

import butvinm.mercury.bot.exceptions.ShareDirMissedException;
import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.gitlab.models.PipelineEvent;
import butvinm.mercury.bot.storage.Mongo;
import butvinm.mercury.bot.storage.Redis;
import butvinm.mercury.bot.telegram.BotRouter;
import butvinm.mercury.bot.telegram.models.BotUser;

@SpringBootApplication
@RestController
public class Application {
    private final File shareDir;

    private final GLClient glClient;

    private final BotRouter router;

    public Application(
        @Value("${share}") Path shareDir,
        @Value("${users.db}") Path usersDb,
        @Value("${gitlab.host}") String glHost,
        @Value("${gitlab.access.token}") String glAccessToken,
        @Value("${bot.token}") String botToken,
        @Value("${chat.id}") String chatId
    ) throws ShareDirMissedException {
        this.shareDir = shareDir.toFile();
        if (!this.shareDir.exists()) {
            throw new ShareDirMissedException(this.shareDir);
        }

        this.glClient = initGitLabClient(glHost, glAccessToken);

        this.router = initBotRouter(
            botToken,
            chatId,
            glClient,
            shareDir.resolve(usersDb).toFile()
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostMapping("/pipelines")
    public SendResponse pipelineHandler(@RequestBody PipelineEvent event) {
        return router.handlePipelineEvent(event);
    }

    @PostMapping("/pipelines/{pipelineId}/messages")
    public void pipelineMessageHandler(
        @PathVariable Long pipelineId,
        @RequestBody String message
    ) {
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

    private GLClient initGitLabClient(
        String host,
        String accessToken
    ) {
        return new GLClient(host, accessToken);
    }

    private BotRouter initBotRouter(
        String botToken,
        String chatId,
        GLClient glClient,
        File usersDb
    ) {
        var bot = new TelegramBot(botToken);
        var pipelineMessagesStore = new Redis<Long, List<String>>();
        var usersStore = new Mongo<BotUser>(usersDb, BotUser.class);
        return new BotRouter(
            bot,
            chatId,
            glClient,
            pipelineMessagesStore,
            usersStore
        );
    }
}
