package net.folab.eicic.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class Util {

    @SafeVarargs
    public static <T> T[] array(T... items) {
        return items;
    }

    public static <T> T newInstance(String className, Object... args) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> type = (Class<T>) Class.forName(className);
            Class<?>[] params = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                params[i] = args[i].getClass();
            }
            Constructor<T> ctor = type.getConstructor(params);
            return ctor.newInstance(args);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

}
