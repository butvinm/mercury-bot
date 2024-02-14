package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatShared;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import butvinm.mercury.bot.stores.ChatsStore;
import butvinm.mercury.bot.stores.UsersStore;
import lombok.Data;

@Data
public class BindChatHandler implements Handler {
    private final TelegramBot bot;

    private final ChatsStore chatsStore;

    private final UsersStore usersStore;

    @Override
    public Optional<Object> handleUpdate(
        Update update
    ) throws IOException {
        var message = update.message();
        if (message == null) {
            return Optional.empty();
        }

        var chat = message.chatShared();
        if (chat == null) {
            return Optional.empty();
        }
        return Optional.of(bindChat(message, chat));
    }

    private SendResponse bindChat(
        Message message,
        ChatShared chat
    ) throws IOException {
        var user = usersStore.get(message.from().id().toString());
        if (user.isEmpty() || !user.get().getAdmin()) {
            var request = new SendMessage(
                message.chat().id(),
                "You are not permitted to run this action."
            );
            return bot.execute(request);
        }

        chatsStore.put(chat.chatId().toString(), chat.chatId());

        var request = new SendMessage(message.chat().id(), "Chat was bind");
        return bot.execute(request);
    }
}
