package butvinm.mercury.bot.stores;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import butvinm.mercury.bot.telegram.models.BotUser;
import butvinm.mercury.bot.utils.storage.Mongo;

public class UsersStore extends Mongo<BotUser> {
    public UsersStore(File store) {
        super(store);
    }

    @Override
    protected TypeReference<Map<String, BotUser>> getDataType() {
        return new TypeReference<Map<String, BotUser>>() {};
    }
}
