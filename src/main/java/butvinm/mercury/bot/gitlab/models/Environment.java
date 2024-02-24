package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class Environment {
    @JsonProperty("action")
    private final String action;

    @JsonProperty("deployment_tier")
    private final String deploymentTier;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("slug")
    private final String slug;
}
