package butvinm.mercury.bot.gitlab.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class PipelineAttributes {
    @JsonProperty("before_sha")
    private final String beforeSha;

    @JsonProperty("id")
    private final Long id;

    @JsonProperty("iid")
    private final Long iid;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("tag")
    private final Boolean tag;

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

    @JsonProperty("variables")
    private final List<Variable> variables;

    @JsonProperty("url")
    private final String url;
}
