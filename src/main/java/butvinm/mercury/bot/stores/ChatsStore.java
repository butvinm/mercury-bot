package butvinm.mercury.bot.stores;

import java.io.File;

import butvinm.mercury.bot.utils.storage.Mongo;

public class ChatsStore extends Mongo<Long> {
    public ChatsStore(File store, Class<Long> valueType) {
        super(store, valueType);
    }
}
