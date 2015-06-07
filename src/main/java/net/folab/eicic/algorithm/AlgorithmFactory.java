package net.folab.eicic.algorithm;

import net.folab.eicic.core.Algorithm;

public class AlgorithmFactory {

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
            return (Algorithm) Class.forName(AlgorithmFactory.class.getPackage().getName() + "." + algorithmName).newInstance();
        } catch (InstantiationException | IllegalAccessException
                | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
