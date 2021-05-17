package tk.sciwhiz12.unsafereflectiontesting.reflector;

import tk.sciwhiz12.unsafereflectiontesting.Reflector;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Java12Reflector implements Reflector {
    @Override
    public void changeValue(Field field, Object context, Object newValue) throws Exception {
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
        final VarHandle modifiersHandle = lookup.findVarHandle(Field.class, "modifiers", int.class);

        modifiersHandle.set(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(context, newValue);
    }
}
