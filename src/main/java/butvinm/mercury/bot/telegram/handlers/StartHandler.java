package butvinm.mercury.bot.telegram.handlers;

import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.KeyboardButtonRequestChat;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import lombok.Data;

@Data
public class StartHandler implements Handler {
    private final TelegramBot bot;

    @Override
    public Optional<Object> handleUpdate(Update update) {
        var message = update.message();
        if (message == null) {
            return Optional.empty();
        }

        var text = message.text();
        if (text == null) {
            return Optional.empty();
        }

        if (text.equals("/start")) {
            return Optional.of(handleStart(message));
        }
        return Optional.empty();
    }

    private Object handleStart(Message message) {
        var chatRequest = new KeyboardButtonRequestChat(666, false);
        var keyboard = new ReplyKeyboardMarkup(
            new KeyboardButton("Bind group").requestChat(chatRequest)
        ).resizeKeyboard(true);

        var request = new SendMessage(message.chat().id(), "Hi!")
            .replyMarkup(keyboard);

        return bot.execute(request);
    }
}
