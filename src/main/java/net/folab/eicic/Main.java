package net.folab.eicic;

import static net.folab.eicic.Constants.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.algorithm.Algorithm3;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Main {

    public static interface Generator<T> {
        public T generate(int idx, double[] values);
    }

    public static void main(String[] args) throws IOException {
        new Main().execute();
    }

    public void execute() throws IOException {

        long execute = System.currentTimeMillis();
        long elapsed =  System.currentTimeMillis();
        Console console = new TextConsole();

        List<Macro> macros = loadObject(
                "res/macro.txt",
                new Generator<Macro>() {
                    @Override
                    public Macro generate(int idx, double[] values) {
                        return new Macro(idx, values[0], values[1], MACRO_TX_POWER);
                    }
                });
        List<Pico> picos = loadObject(
                "res/pico.txt",
                new Generator<Pico>() {
                    @Override
                    public Pico generate(int idx, double[] values) {
                        return new Pico(idx, values[0], values[1], MACRO_TX_POWER);
                    }
                });
        List<Mobile> mobiles = loadObject(
                "res/mobile.txt",
                new Generator<Mobile>() {
                    @Override
                    public Mobile generate(int idx, double[] values) {
                        return new Mobile(idx, values[0], values[1], MOBILE_QOS, values[2], values[3], values[4]);
                    }
                });

        for (Macro macro : macros)
            for (Mobile mobile : mobiles)
                new Edge<>(macro, mobile);
        for (Macro macro : macros)
            for (Pico pico : picos)
                pico.checkInterference(macro);
        for (Pico pico : picos)
            for (Mobile mobile : mobiles)
                new Edge<>(pico, mobile);
        for (Macro macro : macros)
            macro.init();
        for (Pico pico : picos)
            pico.init();

//        for (Macro macro : macros) {
//            System.out.print(macro.idx + ":\t");
//            macro.forEachMobiles(mobile -> System.out.print(mobile.idx + "\t"));
//            System.out.println();
//        }

//        for (Pico pico : picos) {
//            System.out.print(pico.idx + ":\t");
//            pico.forEachMobiles(mobile -> System.out.print(mobile.idx + "\t"));
//            System.out.println();
//        }

        Algorithm algorithm = new Algorithm3();

        int times = SIMULATION_TIME;

        for (int t = 1; t <= times; t++) {

            for (Macro macro : macros)
                macro.generateChannelGain();
            for (Pico pico : picos)
                pico.generateChannelGain();

            for (Mobile mobile : mobiles)
                mobile.calculateDataRate();

            for (Macro macro : macros)
                macro.sortMobiles();
            for (Pico pico : picos)
                pico.sortMobiles();

            algorithm.calculate(macros, picos, mobiles);

//            for (Mobile mobile : mobiles) {
//                System.out.println("m: " + mobile.idx);
//                for (Edge<Macro> edge : mobile.allMacroEdges) {
//                    System.out.print("    M: " + edge.baseStation.idx + "\t");
//                    for (int i = 0; i < NUM_RB; i++) { System.out.print(String.format("%8.4f",
//                            edge.channelGain[i] * 1000000000l) + "\t"); }
//                    System.out.println();
//                }
//                System.out.println();
//            }

//            for (Mobile mobile : mobiles) {
//                for (int i = 0; i < NUM_RB; i++) { System.out.print(mobile.connectionStates[i] + "\t"); }
//                System.out.println();
//            }

            for (Mobile mobile : mobiles)
                mobile.calculateThroughput();
            for (Mobile mobile : mobiles)
                mobile.calculateUserRate();
            final int _t = t;
            for (Mobile mobile : mobiles)
                mobile.calculateDualVariables(_t);

            elapsed = console.dump(t, macros, picos, mobiles, elapsed, execute);

        }

        console.dump(SIMULATION_TIME, macros, picos, mobiles, elapsed, execute);

    }

    public static <T> List<T> loadObject(String file,
            Generator<T> generator) throws IOException {
        List<T> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int idx = 0;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("( |\t)+");
            double[] values = new double[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                values[i] = Double.parseDouble(tokens[i]);
            }
            list.add(generator.generate(idx++, values));
        }
        reader.close();
        return list;
    }

}
