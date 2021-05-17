package tk.sciwhiz12.unsafereflectiontesting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Java8Reflector implements Reflector {
    @Override
    public void changeValue(Field field, Object context, Object newValue) throws Exception {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.setAccessible(true);
        System.out.println("modifiers volatile " + Modifier.isVolatile(field.getModifiers()));
        System.out.println("modifiers final " + Modifier.isFinal(field.getModifiers()));
        field.set(context, newValue);
    }
}
