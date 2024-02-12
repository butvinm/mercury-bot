package butvinm.mercury.bot.stores;

import java.io.File;

import butvinm.mercury.bot.telegram.models.BotUser;
import butvinm.mercury.bot.utils.storage.Mongo;

public class UsersStore extends Mongo<BotUser> {
    public UsersStore(File store, Class<BotUser> valueType) {
        super(store, valueType);
    }
}
