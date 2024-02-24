package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class JobEvent {
    @JsonProperty("before_sha")
    private final String beforeSha;

    @JsonProperty("build_allow_failure")
    private final Boolean buildAllowFailure;

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

    @JsonProperty("commit")
    private final Commit commit;

    @JsonProperty("environment")
    private final String environment;

    @JsonProperty("environment_tier")
    private final String environmentTier;

    @JsonProperty("environment_slug")
    private final String environmentSlug;

    @JsonProperty("environment_external_url")
    private final String environmentExternalUrl;

    @JsonProperty("pipeline_id")
    private final Long pipeline_id;

    @JsonProperty("object_kind")
    private final String objectKind;

    @JsonProperty("project")
    private final Project project;

    @JsonProperty("project_id")
    private final Long projectId;

    @JsonProperty("project_name")
    private final String projectName;

    @JsonProperty("ref")
    private final String ref;

    @JsonProperty("repository")
    private final Repository repository;

    @JsonProperty("retries_count")
    private final Integer retriesCount;

    @JsonProperty("runner")
    private final Runner runner;

    @JsonProperty("sha")
    private final String sha;

    @JsonProperty("tag")
    private final Boolean tag;

    @JsonProperty("user")
    private final User user;

    @JsonProperty("source_pipeline")
    private final Pipeline sourcePipeline;
}
