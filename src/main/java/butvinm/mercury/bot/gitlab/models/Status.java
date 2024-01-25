package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum Status {
    CREATED("created"),
    PENDING("pending"),
    RUNNING("running"),
    SUCCESS("success"),
    FAILED("failed"),
    SKIPPED("skipped"),
    CANCELED("canceled");

    @Getter
    @JsonValue
    private final String label;

    private Status(String label) {
        this.label = label;
    }

    public static Boolean isFinished(Status status) {
        return status == SUCCESS || status == FAILED || status == SKIPPED || status == CANCELED;
    }
}
