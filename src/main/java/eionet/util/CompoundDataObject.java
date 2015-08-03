package eionet.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public final class CompoundDataObject implements Map<String, Object> {

    private final HashMap<String, Object> data;
    
    public CompoundDataObject() {
        this.data = new HashMap<String, Object>();
    }
    
    public <T> T get(String key) {
        return (T) this.data.get(key);
    }
    
    @Override
    public int size() {
        return this.data.size();
    }

    @Override
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.data.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return this.data.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return this.data.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return this.data.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        this.data.putAll(m);
    }

    @Override
    public void clear() {
        this.data.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.data.keySet();
    }

    @Override
    public Collection<Object> values() {
        return this.data.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.data.entrySet();
    }
    
}
