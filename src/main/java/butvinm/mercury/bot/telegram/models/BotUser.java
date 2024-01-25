package butvinm.mercury.bot.telegram.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class BotUser {
    @JsonProperty("id")
    private final Long id;

    @JsonProperty("username")
    private final String username;

    @JsonProperty("is_admin")
    private final Boolean admin;
}
