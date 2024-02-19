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

import butvinm.mercury.bot.stores.UsersStore;
import butvinm.mercury.bot.telegram.models.BotUser;
import butvinm.mercury.bot.utils.FancyStringBuilder;
import lombok.Data;

@Data
public class LoginHandler implements Handler {
    private final TelegramBot bot;

    private final UsersStore usersStore;

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

        if (!text.startsWith("/login")) {
            return Optional.empty();
        }

        var args = text.split(" ");
        if (args.length != 2) {
            return Optional.of(handleWrongCommand(message));
        }
        return Optional.of(handleLogin(message, args[1]));
    }

    private Object handleLogin(
        Message message,
        String totallySecurePasswordWithoutAnyVulnerabilities
    ) throws IOException {
        if (isAdmin(totallySecurePasswordWithoutAnyVulnerabilities)) {
            return loginAdmin(message);
        }
        if (isUser(totallySecurePasswordWithoutAnyVulnerabilities)) {
            return loginUser(message);
        }
        return loginFailure(message);
    }

    private Object loginAdmin(Message message) throws IOException {
        var chatRequest = new KeyboardButtonRequestChat(666, false);
        var keyboard = new ReplyKeyboardMarkup(
            new KeyboardButton("Bind group").requestChat(chatRequest)
        ).resizeKeyboard(true);

        var request = new SendMessage(message.chat().id(), "Hi, admin!")
            .replyMarkup(keyboard);

        var user = BotUser.builder()
            .chatId(message.chat().id())
            .username(message.from().username())
            .admin(true)
            .build();

        usersStore.put(user.getChatId().toString(), user);
        return bot.execute(request);
    }

    private Object loginUser(Message message) throws IOException {
        var request = new SendMessage(
            message.chat().id(),
            "Hi, person who even hasn't admin rights'!"
        );

        var user = BotUser.builder()
            .chatId(message.chat().id())
            .username(message.from().username())
            .admin(false)
            .build();

        usersStore.put(user.getChatId().toString(), user);
        return bot.execute(request);
    }

    private Object loginFailure(Message message) throws IOException {
        var request = new SendMessage(
            message.chat().id(),
            "Wrong password"
        );
        return bot.execute(request);
    }

    private Object handleWrongCommand(Message message) throws IOException {
        var fsb = new FancyStringBuilder()
            .l("Wrong command format.")
            .l("Usage:")
            .t().l("/login {password} - Log in.");

        var request = new SendMessage(message.chat().id(), fsb.toString());
        return bot.execute(request);
    }

    private Boolean isAdmin(String password) {
        return bot.getToken().equals(password);
    }

    private Boolean isUser(String password) {
        return bot.getToken().startsWith(password);
    }
}
