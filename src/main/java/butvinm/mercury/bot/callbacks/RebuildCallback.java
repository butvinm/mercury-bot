package butvinm.mercury.bot.callbacks;

import java.util.Optional;

import lombok.Data;

@Data
public class RebuildCallback {
    private static final String prefix = "rebuild";
    private static final String sep = ":";

    private final Integer projectId;
    private final Integer pipelineId;

    public String pack() {
        return String.join(sep, projectId.toString(), pipelineId.toString());
    }

    public static Optional<RebuildCallback> unpack(String callbackData) {
        if (!callbackData.startsWith(prefix + sep)) {
            return Optional.empty();
        }

        var parts = callbackData.split(sep);
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                "Bad RebuildCallback data: expect projectId and pipelineId params.");
        }

        var callback = new RebuildCallback(Integer.valueOf(parts[1]),
            Integer.valueOf(parts[2]));

        return Optional.of(callback);
    }
}
