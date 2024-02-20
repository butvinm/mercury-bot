package butvinm.mercury.bot;

import java.io.File;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import com.pengrad.telegrambot.TelegramBot;

import butvinm.mercury.bot.exceptions.ShareDirMissedException;
import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.stores.ChatsStore;
import butvinm.mercury.bot.stores.FiltersStore;
import butvinm.mercury.bot.stores.MessagesStore;
import butvinm.mercury.bot.stores.UsersStore;
import butvinm.mercury.bot.telegram.BotRouter;
import butvinm.mercury.bot.telegram.handlers.BindChatHandler;
import butvinm.mercury.bot.telegram.handlers.FiltersHandler;
import butvinm.mercury.bot.telegram.handlers.JoinChatHandler;
import butvinm.mercury.bot.telegram.handlers.LoginHandler;
import butvinm.mercury.bot.telegram.handlers.RebuildHandler;
import butvinm.mercury.bot.telegram.handlers.StartHandler;
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
    private final MessagesStore messagesStore;

    @Getter(onMethod = @__({ @Bean }))
    private final UsersStore usersStore;

    @Getter(onMethod = @__({ @Bean }))
    private final ChatsStore chatsStore;

    @Getter(onMethod = @__({ @Bean }))
    private final FiltersStore filtersStore;

    public Application(
        @Value("${share}") Path shareDir,
        @Value("${users.db:users.db}") Path usersDb,
        @Value("${chats.db:chats.db}") Path chatsDb,
        @Value("${filters.db:filters.db}") Path filtersDb,
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

        this.messagesStore = new MessagesStore();

        this.usersStore = new UsersStore(
            shareDir.resolve(usersDb).toFile()
        );

        this.chatsStore = new ChatsStore(
            shareDir.resolve(chatsDb).toFile()
        );

        this.filtersStore = new FiltersStore(
            shareDir.resolve(filtersDb).toFile()
        );

        this.router = initBotRouter(
            bot,
            glClient,
            messagesStore,
            usersStore,
            chatsStore
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
        MessagesStore messagesStore,
        UsersStore usersStore,
        ChatsStore chatsStore
    ) {
        var router = new BotRouter(
            bot,
            glClient,
            messagesStore,
            usersStore,
            chatsStore
        );
        router.register(new RebuildHandler(
            bot,
            glClient,
            messagesStore,
            usersStore,
            chatsStore
        ));
        router.register(new StartHandler(bot));
        router.register(new LoginHandler(bot, usersStore));
        router.register(new BindChatHandler(bot, chatsStore, usersStore));
        router.register(new JoinChatHandler(bot, chatsStore));
        router.register(new FiltersHandler(bot, filtersStore));

        bot.setUpdatesListener(router, e -> log.error(e.toString()));
        return router;
    }
}
