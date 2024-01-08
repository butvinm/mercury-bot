package butvinm.mercury.bot;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class Redis<K, V> implements Map<K, V> {
    private final Map<K, RedisEntry> map = new HashMap<>();

    private final Period expireIn = Period.ofDays(1);

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet().stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().value))
            .collect(Collectors.toSet());
    }

    @Override
    public V get(Object key) {
        var entry = map.get(key);
        return entry == null ? null : entry.value;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public V put(K key, V value) {
        updateEntries();
        var entry = map.put(key, new RedisEntry(value, LocalDateTime.now()));
        return entry == null ? null : entry.value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> other) {
        other.forEach((k, v) -> put(k, v));
    }

    @Override
    public V remove(Object key) {
        var entry = map.remove(key);
        return entry == null ? null : entry.value;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<V> values() {
        return map.values().stream().map(e -> e.value).toList();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private void updateEntries() {
        map.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    @Data
    public class RedisEntry {
        private final V value;
        private final LocalDateTime createdAt;

        public Boolean isExpired() {
            return createdAt.plus(expireIn).isBefore(LocalDateTime.now());
        }
    }
}
