package butvinm.mercury.bot.utils.storage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import lombok.Data;

@Data
public class Mongo<T> {
    private final File store;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Class<T> valueType;

    public Optional<T> get(String key) throws IOException, DatabindException {
        var value = readStore().get(key);
        return Optional.ofNullable(value);
    }

    public T put(String key, T value) throws IOException, DatabindException {
        var map = readStore();
        map.put(key, value);
        writeStore(map);
        return value;
    }

    public Map<String, T> list() throws IOException, DatabindException {
        return readStore();
    }

    private Map<String, T> readStore() throws IOException, DatabindException {
        store.createNewFile();
        try {
            var type = mapper.getTypeFactory()
                .constructMapType(Map.class, String.class, valueType);
            return mapper.readValue(store, type);
        } catch (MismatchedInputException err) {
            // Would be better to handle precise error when file is just empty,
            // but it is Java, so just calm down and return empty map even if
            // its probably syntax error
            return new LinkedHashMap<>();
        }
    }

    private void writeStore(Map<String, T> map)
        throws IOException, DatabindException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(store, map);
    }
}
