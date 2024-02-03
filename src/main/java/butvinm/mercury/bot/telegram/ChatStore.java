package butvinm.mercury.bot.telegram;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import butvinm.mercury.bot.storage.Mongo;

public class ChatStore extends Mongo<Long> {
    private static final String TARGET_KEY = "key";

    public ChatStore(File store, Class<Long> valueType) {
        super(store, valueType);
    }

    public Long setTargetChat(Long chatId) throws IOException {
        return put(TARGET_KEY, chatId);
    }

    public Optional<Long> getTargetChat() throws IOException {
        return get(TARGET_KEY);
    }
}
