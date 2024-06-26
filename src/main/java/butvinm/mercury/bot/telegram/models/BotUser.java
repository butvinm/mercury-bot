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
public class BotUser {
    @JsonProperty("chat_id")
    private final Long chatId;

    @JsonProperty("username")
    private final String username;

    @JsonProperty("is_admin")
    private final Boolean admin;
}
