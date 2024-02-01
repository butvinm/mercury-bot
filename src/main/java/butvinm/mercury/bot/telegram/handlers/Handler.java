package butvinm.mercury.bot.telegram.handlers;

import java.util.Optional;

import com.pengrad.telegrambot.model.Update;

public interface Handler {
    Optional<Object> handleUpdate(Update update);
}
