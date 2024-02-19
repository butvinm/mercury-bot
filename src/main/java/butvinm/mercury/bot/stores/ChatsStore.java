package butvinm.mercury.bot.stores;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import butvinm.mercury.bot.telegram.models.BotChat;
import butvinm.mercury.bot.utils.storage.Mongo;

public class ChatsStore extends Mongo<BotChat> {
    public ChatsStore(File store) {
        super(store);
    }

    @Override
    protected TypeReference<Map<String, BotChat>> getDataType() {
        return new TypeReference<Map<String, BotChat>>(){};
    }
}
