package butvinm.mercury.bot.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * https://docs.gitlab.com/ee/user/project/integrations/webhook_events.html#merge-request-events
 */
@Data
@Jacksonized
@Builder
public class PipelineEvent {
    @JsonProperty("object_kind")
    private final String objectKind;

    @JsonProperty("user")
    private final User user;

    @JsonProperty("project")
    private final Project project;

    @JsonProperty("object_attributes")
    private final PipelineAttributes attributes;

    @JsonProperty("merge_request")
    private final PipelineAttributes mergeRequest;

    @JsonProperty("builds")
    private final List<Job> jobs;
}
