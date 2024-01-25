package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum MergeRequestStatus {
    CHECKING("checking");

    @Getter
    @JsonValue
    private final String label;

    private MergeRequestStatus(String label) {
        this.label = label;
    }
}
