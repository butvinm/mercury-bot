package butvinm.mercury.bot.models;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum PipelineStatus {
    RUNNING("running"),
    SUCCESS("success");

    @Getter
    @JsonValue
    private final String label;

    private PipelineStatus(String label) {
        this.label = label;
    }
}
