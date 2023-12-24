package butvinm.mercury.bot.models;

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
    private final ObjectAttributes objectAttributes;

    @JsonProperty("merge_request")
    private final ObjectAttributes mergeRequest;
}
