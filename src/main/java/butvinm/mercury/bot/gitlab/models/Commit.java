package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class Commit {
    @JsonProperty("author_email")
    private final String authorEmail;

    @JsonProperty("author_name")
    private final String authorName;

    @JsonProperty("author_url")
    private final String authorUrl;

    @JsonProperty("duration")
    private final Float duration;

    @JsonProperty("finished_at")
    private final String finishedAt;

    @JsonProperty("id")
    private final Long id;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("sha")
    private final String sha;

    @JsonProperty("started_at")
    private final String startedAt;

    @JsonProperty("status")
    private final String status;
}
