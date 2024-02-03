package butvinm.mercury.bot.telegram;

import java.io.File;

import butvinm.mercury.bot.storage.Mongo;
import butvinm.mercury.bot.telegram.models.BotUser;

public class UsersStore extends Mongo<BotUser> {
    public UsersStore(File store, Class<BotUser> valueType) {
        super(store, valueType);
    }
}
