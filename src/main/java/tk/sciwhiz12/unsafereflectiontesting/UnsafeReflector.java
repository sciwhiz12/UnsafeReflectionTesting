package tk.sciwhiz12.unsafereflectiontesting;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeReflector implements Reflector {
    static Unsafe UNSAFE = null;

    static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        if (UNSAFE == null) {
            final Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafeField.get(null); // :ablobsweat:
        }
        return UNSAFE;
    }

    @Override
    public void changeValue(Field field, Object context, Object newValue) throws Exception {
        Object realContext = context == null ? field.getDeclaringClass() : context;
        long offset = context == null ? getUnsafe().staticFieldOffset(field) : getUnsafe().objectFieldOffset(field);
        getUnsafe().getAndSetObject(realContext, offset, newValue);
    }
}
