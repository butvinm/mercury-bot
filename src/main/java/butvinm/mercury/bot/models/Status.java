package butvinm.mercury.bot.models;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum Status {
    CREATED("created"),
    RUNNING("running"),
    SUCCESS("success"),
    FAILED("failed"),
    SKIPPED("skipped");

    @Getter
    @JsonValue
    private final String label;

    private Status(String label) {
        this.label = label;
    }
}
