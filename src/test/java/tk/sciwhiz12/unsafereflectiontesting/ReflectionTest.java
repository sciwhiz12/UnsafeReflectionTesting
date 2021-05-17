package tk.sciwhiz12.unsafereflectiontesting;

import org.apache.logging.log4j.core.async.ThreadNameCachingStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ReflectionTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "tk.sciwhiz12.unsafereflectiontesting.Java8Reflector",
            "tk.sciwhiz12.unsafereflectiontesting.reflector.Java12Reflector",
            "tk.sciwhiz12.unsafereflectiontesting.UnsafeReflector"})
    public void testStatic(String reflectorClass) {
        final Reflector reflector = getReflector(reflectorClass);
        System.out.println("Java version: " + System.getProperty("java.version"));

        testField(reflectorClass, ThreadNameCachingStrategy.class, null, "THREADLOCAL_NAME",
                fieldGetter(ThreadNameCachingStrategy.class, "THREADLOCAL_NAME"), new ThreadLocal<String>());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "tk.sciwhiz12.unsafereflectiontesting.Java8Reflector",
            "tk.sciwhiz12.unsafereflectiontesting.reflector.Java12Reflector",
            "tk.sciwhiz12.unsafereflectiontesting.UnsafeReflector"})
    public void testInstance(String reflectorClass) {

        final Reflector reflector = getReflector(reflectorClass);
        System.out.println("Java version: " + System.getProperty("java.version"));

        TargetEnum.class.getEnumConstants(); // Forces calculation of the constants (so the enum constants array is correct)
        testField(reflectorClass, TargetEnum.class, "enumConstants", fieldGetter(TargetEnum.class.getClass(), "enumConstants"), null);
    }

    <T, R> Function<T, R> fieldGetter(Class<?> targetClass, String fieldName) {
        return t -> {
            try {
                final Field field = targetClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (R) field.get(t);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        };
    }

    <T, R> void testField(String reflectorClass, T target, String fieldName, Function<T, R> valueRetrieval, R newValue) {
        testField(reflectorClass, target.getClass(), target, fieldName, valueRetrieval, newValue);
    }

    <T, R> void testField(String reflectorClass, Class<?> targetClass, T target, String fieldName, Function<T, R> valueRetrieval, R newValue) {
        final Reflector reflector = getReflector(reflectorClass);

        final Field valueField = assertDoesNotThrow(() -> targetClass.getDeclaredField(fieldName));
        valueField.setAccessible(true);

        final R original = valueRetrieval.apply(target);

        System.out.println("Original value: " + original);
        System.out.println("Expected value: " + newValue);

        try { // Tests to check that normal reflection fails
            valueField.set(target, newValue);
        } catch (IllegalAccessException ignored) {
        }

        assertSame(original, valueRetrieval.apply(target), "Target's value is not the original value; reflection succeeded without hacky modification");

        assertDoesNotThrow(() -> reflector.changeValue(valueField, target, newValue),
                "Modification using " + reflectorClass + " failed with exception");

        assertSame(newValue, valueRetrieval.apply(target),
                "Target's value did not change even after modification");
    }

    Reflector getReflector(String className) {
        Class<?> clz = null;
        try {
            clz = Class.forName(className);
        } catch (Throwable ignored) {
        }
        assumeTrue(clz != null,
                "Reflector class " + className + " does not exist");
        assumeTrue(Reflector.class.isAssignableFrom(clz),
                "Class " + className + " is not a subtype of Reflector");

        Constructor<?> ctor = null;
        try {
            ctor = clz.getConstructor();
        } catch (NoSuchMethodException ignored) {
        }
        assumeTrue(ctor != null,
                "Reflector class " + className + " does not have a no-args constructor");

        Reflector reflector = null;
        try {
            ctor.setAccessible(true);
            reflector = (Reflector) ctor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
        }
        assumeTrue(reflector != null,
                "Could not create instance of Reflector class " + className);

        return reflector;
    }
}
