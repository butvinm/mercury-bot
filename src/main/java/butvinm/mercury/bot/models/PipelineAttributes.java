package butvinm.mercury.bot.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class PipelineAttributes {
    @JsonProperty("id")
    private final int id;

    @JsonProperty("iid")
    private final int iid;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("ref")
    private final String ref;

    @JsonProperty("source")
    private final String source;

    @JsonProperty("status")
    private final Status status;

    @JsonProperty("stages")
    private final List<String> stages;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("finished_at")
    private final String finishedAt;

    @JsonProperty("duration")
    private final Float duration;
}
