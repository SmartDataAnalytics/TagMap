package org.aksw.commons.collections.tagmap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;

public class ValidationUtils
{
    public static <T> T createValidatingProxy(T delegate, T validate) {
        @SuppressWarnings("unchecked")
        T result = (T)Proxy.newProxyInstance(
                TagMap.class.getClassLoader(),
                new Class[] { TagMap.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method m, Object[] args) throws Throwable {
                        Object actual = m.invoke(delegate, args);

                        Object expected = m.invoke(validate, args);
                        if(!Objects.equals(actual, expected)) {
                            throw new AssertionError("At invocation of: " + m + "\nWith Args: " + Arrays.toString(args) + "\nActual: " + actual + "\nExpected: " + expected);
                        }

                        return actual;
                    }
                });
        return result;
    }
}
