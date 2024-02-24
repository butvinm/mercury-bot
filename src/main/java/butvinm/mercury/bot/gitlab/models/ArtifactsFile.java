package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class ArtifactsFile {
    @JsonProperty("filename")
    private final String name;

    @JsonProperty("size")
    private final Integer size;
}
