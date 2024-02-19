package butvinm.mercury.bot.stores;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import butvinm.mercury.bot.telegram.models.BotFilter;
import butvinm.mercury.bot.utils.storage.Mongo;

public class FiltersStore extends Mongo<BotFilter> {
    public FiltersStore(File store) {
        super(store);
    }

    @Override
    protected TypeReference<Map<String, BotFilter>> getDataType() {
        return new TypeReference<Map<String, BotFilter>>() {};
    }
}
