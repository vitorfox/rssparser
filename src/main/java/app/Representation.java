package app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vitorteixeira on 6/18/16.
 */
public class Representation {

    private Map<String, String> map = new HashMap<>();
    public String name;

    public Representation(String className) {
        this.name = className;
    }

    public void set(String field, String value) {
        this.map.put(field, value);
    }

    public String get(String fieldName) {
        return map.get(fieldName);
    }

    public Map getMap() {
        return this.map;
    }

    public String toString() {
        return this.name;
    }
}
