package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.KeyboardButtonRequestChat;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import butvinm.mercury.bot.stores.ChatsStore;
import lombok.Data;

@Data
public class StartHandler implements Handler {
    private final TelegramBot bot;

    private final ChatsStore chatsStore;

    @Override
    public Optional<Object> handleUpdate(
        Update update
    ) throws IOException {
        var message = update.message();
        if (message == null) {
            return Optional.empty();
        }

        var text = message.text();
        if (text == null) {
            return Optional.empty();
        }

        if (!text.equals("/start")) {
            return Optional.empty();
        }
        return Optional.of(handleStart(message));
    }

    private Object handleStart(Message message) throws IOException {
        var chatRequest = new KeyboardButtonRequestChat(666, false);
        var keyboard = new ReplyKeyboardMarkup(
            new KeyboardButton("Bind group").requestChat(chatRequest)
        ).resizeKeyboard(true);

        var request = new SendMessage(message.chat().id(), "Hi!")
            .replyMarkup(keyboard);

        chatsStore.put(
            message.chat().id().toString(),
            message.chat().id()
        );
        return bot.execute(request);
    }
}
