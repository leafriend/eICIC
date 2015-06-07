package net.folab.eicic.algorithm;

import java.util.LinkedHashMap;
import java.util.Map;

import net.folab.eicic.core.Algorithm;

public class AlgorithmFactory {

    private static final Map<Class<? extends Algorithm>, Algorithm> INSTANCES = new LinkedHashMap<Class<? extends Algorithm>, Algorithm>();

    public static Class<? extends Algorithm>[] getAlgorithmClasses() {
        @SuppressWarnings("unchecked")
        Class<? extends Algorithm>[] classes = new Class[7];
        classes[0] = StaticAlgorithm.class;
        classes[1] = Algorithm1.class;
        classes[2] = Algorithm2.class;
        classes[3] = Algorithm3.class;
        classes[4] = Algorithm3Revised.class;
        classes[5] = Algorithm3Revised2.class;
        classes[6] = Algorithm3Revised3.class;
        return classes;
    }

    public static Algorithm getInstance(String algorithmName) {
        try {

            @SuppressWarnings("unchecked")
            Class<? extends Algorithm> algorithmClass = (Class<? extends Algorithm>) Class
                    .forName(AlgorithmFactory.class.getPackage().getName()
                            + "." + algorithmName);

            if (INSTANCES.containsKey(algorithmClass)) {
                return INSTANCES.get(algorithmClass);
            }

            Algorithm instance = algorithmClass.newInstance();
            INSTANCES.put(algorithmClass, instance);

            return instance;

        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public static void terminate() {
        for (Algorithm algorithm : INSTANCES.values()) {
            algorithm.terminate();
        }
    }

}
