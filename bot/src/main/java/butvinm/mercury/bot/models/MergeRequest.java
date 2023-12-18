package butvinm.mercury.bot.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class MergeRequest {
    @JsonProperty("id")
    private final int id;

    @JsonProperty("iid")
    private final int iid;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("target_branch")
    private final String targetBranch;

    @JsonProperty("target_project_id")
    private final int targetProjectId;

    @JsonProperty("source_branch")
    private final String sourceBranch;

    @JsonProperty("source_project_id")
    private final int sourceProjectId;

    @JsonProperty("state")
    private final MergeRequestState state;

    @JsonProperty("status")
    private final MergeRequestStatus status;
}