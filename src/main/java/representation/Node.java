package representation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by vitorteixeira on 6/18/16.
 */
public class Node {
    public void set(Field field, Object value) {
        Class fieldClass = field.getType();
        try {
            for (int i = 0; i < fieldClass.getConstructors().length; i++) {
                Constructor conts = fieldClass.getConstructors()[i];
                if (conts.getParameterTypes().length == 1 && conts.getParameterTypes()[0] == String.class) {
                    Object fieldInstance = conts.newInstance(value);
                    field.set(this, fieldInstance);
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
