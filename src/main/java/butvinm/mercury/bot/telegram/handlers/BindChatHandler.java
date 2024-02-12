package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatShared;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.stores.ChatStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class BindChatHandler implements Handler {
    private final TelegramBot bot;

    private final ChatStore chatStore;

    @Override
    public Optional<Object> handleUpdate(Update update) {
        var message = update.message();
        if (message == null) {
            return Optional.empty();
        }

        var chat = message.chatShared();
        if (chat != null) {
            try {
                bindChat(message, chat);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
        return Optional.empty();
    }

    private SendResponse bindChat(Message message, ChatShared chat) throws IOException {
        chatStore.setTargetChat(chat.chatId());

        var request = new SendMessage(message.chat().id(), "Chat was bind");
        return bot.execute(request);
    }
}
