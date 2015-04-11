package org.yats.common;


import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created
 * Date: 11/04/15
 * Time: 08:50
 */

public class Mapping<K,V>
{

    public Enumeration<K> keys() {
        return map.keys();
    }

    public List<K> keyList() {
        return Collections.list(map.keys());
    }

    public Collection<V> values() {
        return map.values();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public void put(K key, V value) {
        map.put(key, value);
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public V remove(Object key) {
        V v = get(key);
        map.remove(key);
        return v;
    }

    public V get(Object key) {
        if(!containsKey(key)) throw new CommonExceptions.KeyNotFoundException("unknown key:"+key);
        return map.get(key);
    }

    public void clear() {
        map.clear();
    }

    @Override
    public String toString()
    {
        return map.toString();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    public Mapping()
    {
        this.map = new ConcurrentHashMap<K, V>();
    }

    ////////////////////////////////////////////////////////////////

    private ConcurrentHashMap<K,V> map;
}
