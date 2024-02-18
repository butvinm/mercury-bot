package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class JobEvent {
    @JsonProperty("build_created_at")
    private final String createdAt;

    @JsonProperty("build_duration")
    private final Float duration;

    @JsonProperty("build_failure_reason")
    private final String failureReason;

    @JsonProperty("build_finished_at")
    private final String finishedAt;

    @JsonProperty("build_id")
    private final Long id;

    @JsonProperty("build_name")
    private final String name;

    @JsonProperty("build_queued_duration")
    private final Float queuedDuration;

    @JsonProperty("build_stage")
    private final String stage;

    @JsonProperty("build_started_at")
    private final String startedAt;

    @JsonProperty("build_status")
    private final Status status;

    @JsonProperty("pipeline_id")
    private final Long pipeline_id;

    @JsonProperty("object_kind")
    private final String objectKind;

    @JsonProperty("user")
    private final User user;

    @JsonProperty("project")
    private final Project project;

    @JsonProperty("ref")
    private final String ref;
}
