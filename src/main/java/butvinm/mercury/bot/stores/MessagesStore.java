package butvinm.mercury.bot.stores;

import java.util.List;

import butvinm.mercury.bot.utils.storage.Redis;

public class MessagesStore extends Redis<Long, List<String>> {}
