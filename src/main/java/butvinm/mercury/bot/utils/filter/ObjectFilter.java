package butvinm.mercury.bot.utils.filter;

import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class ObjectFilter {
    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<JsonPointer, Pattern> patterns;

    public Boolean test(Object entity) {
        var tree = mapper.valueToTree(entity);

        for (var entry : patterns.entrySet()) {
            var path = entry.getKey();
            var pattern = entry.getValue().asPredicate();

            var value = tree.at(path);
            if (value.isMissingNode()) {
                return false;
            }

            if (!pattern.test(value.toString())) {
                return false;
            }
        }
        return true;
    }
}
