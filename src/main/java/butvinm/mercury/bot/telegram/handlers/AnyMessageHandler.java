package butvinm.mercury.bot.telegram.handlers;

import java.io.IOException;
import java.util.Optional;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import butvinm.mercury.bot.stores.UsersStore;
import butvinm.mercury.bot.telegram.models.BotUser;
import lombok.Data;

/**
 * Handle all messages in chat to register members.
 */
@Data
public class AnyMessageHandler implements Handler {
    private final UsersStore usersStore;

    @Override
    public Optional<Object> handleUpdate(Update update) {
        var message = update.message();
        if (message != null) {
            try {
                return Optional.of(handleMessage(message));
            } catch (IOException e) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    private BotUser handleMessage(Message message) throws IOException {
        var user = BotUser.builder()
            .id(message.from().id())
            .username(message.from().username())
            .admin(false)
            .build();
        var orig = usersStore.get(user.getId().toString());
        if (orig.isPresent()) {
            return orig.get();
        }
        return usersStore.put(user.getId().toString(), user);
    }
}
