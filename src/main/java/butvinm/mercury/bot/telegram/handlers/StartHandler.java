package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Optional;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import lombok.Data;

@Data
public class StartHandler implements Handler {
    private final TelegramBot bot;

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

        if (!text.startsWith("/start")) {
            return Optional.empty();
        }
        return Optional.of(handleStart(message));
    }

    private Object handleStart(
        Message message
    ) throws IOException {
        var request = new SendMessage(
            message.chat().id(),
            "Hi! Send `/login {password}` to login."
        );
        return bot.execute(request);
    }
}
