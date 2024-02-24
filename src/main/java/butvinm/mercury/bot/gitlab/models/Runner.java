package butvinm.mercury.bot.gitlab.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class Runner {
    @JsonProperty("active")
    private final Boolean active;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("id")
    private final Long id;

    @JsonProperty("is_shared")
    private final Boolean isShared;

    @JsonProperty("runner_type")
    private final String runnerType;

    @JsonProperty("tags")
    private final List<String> tags;
}
