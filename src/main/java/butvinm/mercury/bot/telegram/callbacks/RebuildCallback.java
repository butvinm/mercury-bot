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
    private final String jobsPrefix;
    private final List<String> jobIds;

    public RebuildCallback(Long projectId, List<String> jobIds) {
        this.projectId = projectId;
        this.jobsPrefix = extractJobsPrefix(jobIds);
        this.jobIds = trimJobIds(this.jobsPrefix, jobIds);
    }

    public RebuildCallback(
        Long projectId,
        String jobsPrefix,
        List<String> jobIds
    ) {
        this.projectId = projectId;
        this.jobsPrefix = jobsPrefix;
        this.jobIds = buildJobIds(this.jobsPrefix, jobIds);
    }

    private String extractJobsPrefix(List<String> jobIds) {
        var sb = new StringBuilder();

        var minLength = jobIds.stream()
            .map(s -> s.length())
            .min(Integer::compare)
            .get();

        for (int i = 0; i < minLength; ++i) {
            Character common = null;
            for (var id : jobIds) {
                if (common == null) {
                    common = id.charAt(i);
                } else if (common != id.charAt(i)) {
                    return sb.toString();
                }
            }
            sb.append(common);
        }
        return sb.toString();
    }

    private List<String> trimJobIds(String jobsPrefix, List<String> jobIds) {
        return jobIds.stream()
            .map(id -> id.replaceFirst("^" + jobsPrefix, ""))
            .toList();
    }

    private List<String> buildJobIds(String jobsPrefix, List<String> jobIds) {
        return jobIds.stream()
            .map(id -> jobsPrefix + id)
            .toList();
    }

    public String pack() {
        var packedJobIds = String.join(
            listSep,
            jobIds.stream().map(id -> id.toString()).toList()
        );
        return String.join(
            fieldsSep,
            prefix,
            projectId.toString(),
            jobsPrefix.toString(),
            packedJobIds
        );
    }

    public static Optional<RebuildCallback> unpack(String callbackData) {
        if (!callbackData.startsWith(prefix + fieldsSep)) {
            return Optional.empty();
        }

        var parts = callbackData.split(fieldsSep);
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                "Bad RebuildCallback data: expect <rebuild:projectId:jobsPrefix:jobIds>."
            );
        }

        var projectId = Long.valueOf(parts[1]);
        var jobsPrefix = parts[2];
        var jobIds = List.of(parts[3].split(listSep));
        return Optional.of(new RebuildCallback(projectId, jobsPrefix, jobIds));
    }
}
