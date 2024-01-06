package butvinm.mercury.bot.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class User {
    @JsonProperty("id")
    private final Long id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("username")
    private final String username;

    @JsonProperty("avatar_url")
    private final String avatarUrl;

    @JsonProperty("email")
    private final String email;
}
