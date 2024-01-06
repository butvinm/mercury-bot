package butvinm.mercury.bot.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class Project {
    @JsonProperty("id")
    private final Long id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("web_url")
    private final String webUrl;

    @JsonProperty("avatar_url")
    private final String avatarUrl;

    @JsonProperty("git_ssh_url")
    private final String gitSshUrl;

    @JsonProperty("git_http_url")
    private final String gitHttpUrl;

    @JsonProperty("namespace")
    private final String namespace;

    @JsonProperty("path_with_namespace")
    private final String pathWithNamespace;

    @JsonProperty("default_branch")
    private final String defaultBranch;

    @JsonProperty("homepage")
    private final String homepage;

    @JsonProperty("url")
    private final String url;

    @JsonProperty("ssh_url")
    private final String sshUrl;

    @JsonProperty("http_url")
    private final String httpUrl;
}
