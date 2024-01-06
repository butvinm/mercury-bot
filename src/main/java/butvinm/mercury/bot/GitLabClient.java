package butvinm.mercury.bot;

import java.util.logging.Logger;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;

public class GitLabClient {
    private final Logger logger;

    private final UnirestInstance unirest;

    public GitLabClient(
        String gitlabHost,
        String accessToken,
        Logger logger
    ) {
        this.logger = logger;
        this.unirest = initUnirest(gitlabHost, accessToken);
    }

    private UnirestInstance initUnirest(
        String gitlabHost,
        String accessToken
    ) {
        var unirest = Unirest.spawnInstance();
        unirest.config()
            .defaultBaseUrl(gitlabHost + "/api/v4")
            .addDefaultHeader("PRIVATE-TOKEN", accessToken);
        return unirest;
    }

    public HttpResponse<JsonNode> retryJob(Long projectId, Long jobId) {
        var request = unirest.post("/projects/{projectId}/jobs/{jobId}/retry")
            .routeParam("projectId", projectId.toString())
            .routeParam("jobId", jobId.toString());
        return request.asJson();
    }
}
