package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class Pipeline {
    @JsonProperty("project")
    private final Project project;

    @JsonProperty("pipeline_id")
    private final Long pipelineId;

    @JsonProperty("job_id")
    private final Long jobId;
}
