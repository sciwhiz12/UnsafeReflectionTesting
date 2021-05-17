package tk.sciwhiz12.unsafereflectiontesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface Reflector {
    void changeValue(Field field, Object context, Object newValue) throws Exception;
}
