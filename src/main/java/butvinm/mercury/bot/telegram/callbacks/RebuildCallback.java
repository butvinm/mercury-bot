package butvinm.mercury.bot.telegram.callbacks;

import java.util.List;
import java.util.Optional;

import lombok.Data;

@Data
public class RebuildCallback {
    private static final String prefix = "rebuild";
    private static final String fieldsSep = ":";
    private static final String listSep = ",";

    private final Long projectId;
    private final List<Long> jobIds;

    public String pack() {
        var packedJobIds = String.join(
            listSep,
            jobIds.stream().map(id -> id.toString()).toList()
        );
        return String.join(
            fieldsSep, prefix, projectId.toString(), packedJobIds
        );
    }

    public static Optional<RebuildCallback> unpack(String callbackData) {
        if (!callbackData.startsWith(prefix + fieldsSep)) {
            return Optional.empty();
        }

        var parts = callbackData.split(fieldsSep);
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                "Bad RebuildCallback data: expect projectId and jobIds params."
            );
        }

        var projectId = Long.valueOf(parts[1]);
        var jobIds = List.of(parts[2].split(listSep)).stream()
            .map(Long::valueOf).toList();
        return Optional.of(new RebuildCallback(projectId, jobIds));
    }
}
