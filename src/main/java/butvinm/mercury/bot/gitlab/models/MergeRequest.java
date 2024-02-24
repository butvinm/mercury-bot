package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class MergeRequest {
    @JsonProperty("id")
    private final Long id;

    @JsonProperty("iid")
    private final Long iid;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("target_branch")
    private final String targetBranch;

    @JsonProperty("target_project_id")
    private final Long targetProjectId;

    @JsonProperty("source_branch")
    private final String sourceBranch;

    @JsonProperty("source_project_id")
    private final Long sourceProjectId;

    @JsonProperty("state")
    private final MergeRequestState state;

    @JsonProperty("status")
    private final MergeRequestStatus status;

    @JsonProperty("detailed_status")
    private final MergeRequestStatus detailedStatus;

    @JsonProperty("url")
    private final String url;
}
