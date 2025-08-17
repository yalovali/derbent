package tech.derbent.abstracts.domains;

import java.lang.reflect.Method;

public class CIconSetLoader {

    private static Method getClazz(final String className, final String methodName)
            throws ClassNotFoundException, NoSuchMethodException {
        final Class<?> clazz = Class.forName(className);

        if (!CInterfaceIconSet.class.isAssignableFrom(clazz)) {
            throw new RuntimeException("Class " + className + " does not implement CInterfaceIconSet");
        }
        final Method method = clazz.getMethod(methodName);

        if (method.getReturnType() != String.class) {
            throw new RuntimeException("Method " + methodName + " in class " + className + " does not return String");
        }
        return method;
    }

    public static String getIconColorCode(final String className) {

        try {
            final Method method = getClazz(className, "getIconColorCode");
            return (String) method.invoke(null); // static method, no instance needed
        } catch (final Exception e) {
            throw new RuntimeException("Failed to get icon color code from: " + className, e);
        }
    }

    public static String getIconFilename(final String className) {

        try {
            final Method method = getClazz(className, "getIconFilename");
            return (String) method.invoke(null);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to get icon filename from: " + className, e);
        }
    }
}