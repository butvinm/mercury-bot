package butvinm.mercury.bot.gitlab;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;

public class GLClient {
    private final UnirestInstance unirest;

    public GLClient(
        String host,
        String accessToken
    ) {
        this.unirest = initUnirest(host, accessToken);
    }

    private UnirestInstance initUnirest(
        String host,
        String accessToken
    ) {
        var unirest = Unirest.spawnInstance();
        unirest.config()
            .defaultBaseUrl(host + "/api/v4")
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
