package grakn.simulation.db.common.agent.utils;

import java.lang.reflect.Method;

public class CheckMethod {
    public static String checkMethodExists(Object object, String methodName) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            String name = method.getName();
            if (methodName.equals(name)) {
                return methodName;
            }
        }
        throw new MethodNotFoundException(methodName, object.getClass().getName());
    }

    private static class MethodNotFoundException extends RuntimeException {
        MethodNotFoundException(String methodName, String className) {
            super(String.format("Could not find method %s in class %s", methodName, className));
        }
    }
}
