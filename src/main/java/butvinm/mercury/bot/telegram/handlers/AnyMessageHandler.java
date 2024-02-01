package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Optional;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import butvinm.mercury.bot.storage.Mongo;
import butvinm.mercury.bot.telegram.models.BotUser;
import lombok.Data;

/**
 * Handle all messages in chat to register members.
 */
@Data
public class AnyMessageHandler implements Handler {
    private final Mongo<BotUser> usersStore;

    @Override
    public Optional<Object> handleUpdate(Update update) {
        var message = update.message();
        if (message != null) {
            return Optional.of(handleMessage(message));
        }
        return Optional.empty();
    }

    private BotUser handleMessage(Message message) {
        try {
            var user = BotUser.builder()
                .id(message.from().id())
                .username(message.from().username())
                .admin(false)
                .build();
            if (usersStore.get(user.getId().toString()).isPresent()) {
                return null;
            }
            return usersStore.put(user.getId().toString(), user);
        } catch (IOException err) {
            return null;
        }
    }
}
