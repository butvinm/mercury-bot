package butvinm.mercury.bot.stores;

import java.io.File;

import butvinm.mercury.bot.telegram.models.BotChat;
import butvinm.mercury.bot.utils.storage.Mongo;

public class ChatsStore extends Mongo<BotChat> {
    public ChatsStore(File store, Class<BotChat> valueType) {
        super(store, valueType);
    }
}
