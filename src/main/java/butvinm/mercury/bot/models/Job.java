package butvinm.mercury.bot.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class Job {
    @JsonProperty("id")
    private final Integer id;

    @JsonProperty("stage")
    private final String stage;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("status")
    private final Status status;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("started_at")
    private final String startedAt;

    @JsonProperty("finished_at")
    private final String finishedAt;

    @JsonProperty("duration")
    private final Float duration;

    @JsonProperty("queued_duration")
    private final Float queuedDuration;

    @JsonProperty("failure_reason")
    private final String failureReason;

    @JsonProperty("when")
    private final String when;

    @JsonProperty("manual")
    private final Boolean manual;

    @JsonProperty("allow_failure")
    private final Boolean allowFailure;

    @JsonProperty("user")
    private final User user;
}
