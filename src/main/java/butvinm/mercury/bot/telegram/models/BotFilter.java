package butvinm.mercury.bot.telegram.models;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class BotFilter {
    @JsonProperty("jobs")
    @Builder.Default
    private final Map<JsonPointer, Pattern> jobsFilters = new HashMap<>();

    @JsonProperty("pipelines")
    @Builder.Default
    private final Map<JsonPointer, Pattern> pipelinesFilters = new HashMap<>();
}
