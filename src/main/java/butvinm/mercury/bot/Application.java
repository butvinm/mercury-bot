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

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.exceptions.ShareDirMissedException;
import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.gitlab.models.PipelineEvent;
import butvinm.mercury.bot.storage.Redis;
import butvinm.mercury.bot.telegram.BotRouter;
import butvinm.mercury.bot.telegram.ChatStore;
import butvinm.mercury.bot.telegram.UsersStore;
import butvinm.mercury.bot.telegram.handlers.AnyMessageHandler;
import butvinm.mercury.bot.telegram.handlers.BindChatHandler;
import butvinm.mercury.bot.telegram.handlers.RebuildHandler;
import butvinm.mercury.bot.telegram.handlers.StartHandler;
import butvinm.mercury.bot.telegram.models.BotUser;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Slf4j
public class Application {
    private final File shareDir;

    private final GLClient glClient;

    private final BotRouter router;

    public Application(
        @Value("${share}") Path shareDir,
        @Value("${users.db}") Path usersDb,
        @Value("${chat.db}") Path chatDb,
        @Value("${gitlab.host}") String glHost,
        @Value("${gitlab.access.token}") String glAccessToken,
        @Value("${bot.token}") String botToken
    ) throws ShareDirMissedException {
        this.shareDir = shareDir.toFile();
        if (!this.shareDir.exists()) {
            throw new ShareDirMissedException(this.shareDir);
        }

        this.glClient = initGitLabClient(glHost, glAccessToken);

        this.router = initBotRouter(
            botToken,
            glClient,
            shareDir.resolve(usersDb).toFile(),
            shareDir.resolve(chatDb).toFile()
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

    private GLClient initGitLabClient(
        String host,
        String accessToken
    ) {
        return new GLClient(host, accessToken);
    }

    private BotRouter initBotRouter(
        String botToken,
        GLClient glClient,
        File usersDb,
        File chatsDb
    ) {
        var pipelineMessagesStore = new Redis<Long, List<String>>();
        var usersStore = new UsersStore(usersDb, BotUser.class);
        var chatStore = new ChatStore(chatsDb, Long.class);

        var bot = new TelegramBot(botToken);
        var router = new BotRouter(
            bot,
            glClient,
            pipelineMessagesStore,
            usersStore,
            chatStore
        );
        router.register(new RebuildHandler(
            bot,
            glClient,
            pipelineMessagesStore,
            usersStore,
            chatStore
        ));
        router.register(new StartHandler(bot));
        router.register(new AnyMessageHandler(usersStore));
        router.register(new BindChatHandler(bot, chatStore));

        bot.setUpdatesListener(router, e -> log.error(e.toString()));
        return router;
    }
}
