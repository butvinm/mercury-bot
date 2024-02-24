package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class Repository {
    @JsonProperty("description")
    private final String description;

    @JsonProperty("git_http_url")
    private final String gitHttpUrl;

    @JsonProperty("git_ssh_url")
    private final String gitSshUrl;

    @JsonProperty("homepage")
    private final String homepage;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("url")
    private final String url;

    @JsonProperty("visibility_level")
    private final Integer visibilityLevel;
}
