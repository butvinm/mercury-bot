package butvinm.mercury.bot.gitlab.models;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum MergeRequestState {
    OPENED("opened");

    @Getter
    @JsonValue
    private final String label;

    private MergeRequestState(String label) {
        this.label = label;
    }
}
