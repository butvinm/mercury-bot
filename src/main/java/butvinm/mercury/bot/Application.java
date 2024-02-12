package butvinm.mercury.bot;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import com.pengrad.telegrambot.TelegramBot;

import butvinm.mercury.bot.exceptions.ShareDirMissedException;
import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.storage.Redis;
import butvinm.mercury.bot.telegram.BotRouter;
import butvinm.mercury.bot.telegram.ChatStore;
import butvinm.mercury.bot.telegram.UsersStore;
import butvinm.mercury.bot.telegram.handlers.AnyMessageHandler;
import butvinm.mercury.bot.telegram.handlers.BindChatHandler;
import butvinm.mercury.bot.telegram.handlers.RebuildHandler;
import butvinm.mercury.bot.telegram.handlers.StartHandler;
import butvinm.mercury.bot.telegram.models.BotUser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Slf4j
public class Application {
    private final File shareDir;

    private final BotRouter router;

    @Getter(onMethod = @__({ @Bean }))
    private final TelegramBot bot;

    @Getter(onMethod = @__({ @Bean }))
    private final GLClient glClient;

    @Getter(onMethod = @__({ @Bean }))
    private final Redis<Long, List<String>> pipelinesMessagesStore;

    @Getter(onMethod = @__({ @Bean }))
    private final UsersStore usersStore;

    @Getter(onMethod = @__({ @Bean }))
    private final ChatStore chatStore;

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

        this.bot = new TelegramBot(botToken);

        this.pipelinesMessagesStore = new Redis<Long, List<String>>();

        this.usersStore = new UsersStore(
            shareDir.resolve(usersDb).toFile(),
            BotUser.class
        );

        this.chatStore = new ChatStore(
            shareDir.resolve(chatDb).toFile(),
            Long.class
        );

        this.router = initBotRouter(
            bot,
            glClient,
            pipelinesMessagesStore,
            usersStore,
            chatStore
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private GLClient initGitLabClient(
        String host,
        String accessToken
    ) {
        return new GLClient(host, accessToken);
    }

    private BotRouter initBotRouter(
        TelegramBot bot,
        GLClient glClient,
        Redis<Long, List<String>> pipelinesMessagesStore,
        UsersStore usersStore,
        ChatStore chatStore
    ) {
        var router = new BotRouter(
            bot,
            glClient,
            pipelinesMessagesStore,
            usersStore,
            chatStore
        );
        router.register(new RebuildHandler(
            bot,
            glClient,
            pipelinesMessagesStore,
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
