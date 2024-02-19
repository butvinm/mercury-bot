package butvinm.mercury.bot.telegram.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
@With
public class BotChat {
    @JsonProperty("chat_id")
    private final Long chatId;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("bind")
    private final Boolean bind;
}
