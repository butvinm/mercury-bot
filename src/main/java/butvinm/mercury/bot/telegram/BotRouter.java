package butvinm.mercury.bot.telegram;

import java.util.LinkedList;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import butvinm.mercury.bot.gitlab.GLClient;
import butvinm.mercury.bot.stores.ChatStore;
import butvinm.mercury.bot.stores.MessagesStore;
import butvinm.mercury.bot.stores.UsersStore;
import butvinm.mercury.bot.telegram.handlers.Handler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class BotRouter implements UpdatesListener {
    private final TelegramBot bot;

    private final GLClient glClient;

    private final MessagesStore pipelinesMessagesStore;

    private final UsersStore usersStore;

    private final ChatStore chatStore;

    private final List<Handler> handlers = new LinkedList<>();

    public void register(Handler handler) {
        this.handlers.add(handler);
    }

    public void unregister(Handler handler) {
        this.handlers.remove(handler);
    }

    @Override
    public int process(List<Update> updates) {
        for (var update : updates) {
            for (var handler : handlers) {
                var res = handler.handleUpdate(update);
                log.info(res.toString());
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }
}
