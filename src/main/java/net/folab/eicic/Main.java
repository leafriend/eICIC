package net.folab.eicic;

import static java.lang.Math.*;
import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.Arrays.*;
import static net.folab.eicic.Constants.*;
import static org.javatuples.Pair.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.javatuples.Pair;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.algorithm.Algorithm3;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Main {

    public static void main(String[] args) throws IOException {

        long execute = System.currentTimeMillis();
        long elapsed =  System.currentTimeMillis();

        List<Macro> macros = loadObject(
                "res/macro.txt",
                pair -> new Macro(pair.getValue0(), pair.getValue1()[0], pair
                        .getValue1()[1], MACRO_TX_POWER));

        List<Pico> picos = loadObject(
                "res/pico.txt",
                pair -> new Pico(pair.getValue0(), pair.getValue1()[0], pair
                        .getValue1()[1], PICO_TX_POWER));

        List<Mobile> mobiles = loadObject(
                "res/mobile.txt",
                pair -> new Mobile(pair.getValue0(), pair.getValue1()[0], pair
                        .getValue1()[1], MOBILE_QOS, pair.getValue1()[2], pair
                        .getValue1()[3], pair.getValue1()[4]));

        for (Iterator<Macro> iter = macros.iterator(); iter.hasNext();) {
            Macro macro = iter.next();
            mobiles.forEach(mobile -> new Edge<>(macro, mobile);
        }
        for (Iterator<Macro> iter = macros.iterator(); iter.hasNext();) {
            Macro macro = iter.next();
            picos.forEach(pico -> pico.checkInterference(macro);
        }
        picos.forEach(pico -> mobiles.forEach(mobile -> new Edge<>(pico, mobile)));
        picos.forEach(Pico::init);

//        for (Iterator<Macro> iter = macros.iterator(); iter.hasNext();) {
//            Macro macro = iter.next(); {
//            System.out.print(macro.idx + ":\t");
//            macro.forEachMobiles(mobile -> System.out.print(mobile.idx + "\t"));
//            System.out.println();
//        }

//        picos.forEach(pico -> {
//            System.out.print(pico.idx + ":\t");
//            pico.forEachMobiles(mobile -> System.out.print(mobile.idx + "\t"));
//            System.out.println();
//        });

        Algorithm algorithm = new Algorithm3();

        for (int t = 1; t <= SIMULATION_TIME; t++) {

            for (Iterator<Macro> iter = macros.iterator(); iter.hasNext();) {
                Macro macro = iter.next();
                macro.generateChannelGain();
            }
            picos.forEach(pico -> pico.generateChannelGain());

            mobiles.forEach(mobile -> mobile.calculateDataRate());

            picos.forEach(pico -> pico.sortMobiles());

            algorithm.calculate(macros, picos, mobiles);

//            mobiles.forEach(mobile -> {
//                System.out.println("m: " + mobile.idx);
//                mobile.allMacroEdges.forEach(edge -> {
//                    System.out.print("    M: " + edge.baseStation.idx + "\t");
//                    for (int i = 0; i < NUM_RB; i++) { System.out.print(String.format("%8.4f",
//                            edge.channelGain[i] * 1000000000l) + "\t"); }
//                    System.out.println();
//                });
//                System.out.println();
//            });

//            mobiles.forEach(mobile -> {
//                for (int i = 0; i < NUM_RB; i++) { System.out.print(mobile.connectionStates[i] + "\t"); }
//                System.out.println();
//            });

            mobiles.forEach(mobile -> mobile.calculateThroughput());
            mobiles.forEach(mobile -> mobile.calculateUserRate());
            final int _t = t;
            mobiles.forEach(mobile -> mobile.calculateDualVariables(_t));

            if (t % 100 == 0) {
                dump(t, macros, picos, mobiles, elapsed, execute);
                elapsed =  System.currentTimeMillis();
            }

        }

        dump(SIMULATION_TIME, macros, picos, mobiles, elapsed, execute);
        System.out.println(secondFrom(execute));

    }

    public static <T> List<T> loadObject(String file,
            Function<Pair<Integer, double[]>, T> generator) throws IOException {
        List<T> macros = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int idx = 0;
        while ((line = reader.readLine()) != null) {
            double[] values = stream(line.split("( |\t)+")).mapToDouble(
                    Double::parseDouble).toArray();
            macros.add(generator.apply(with(idx++, values)));
        }
        reader.close();
        return macros;
    }

    private static void dump(int t, List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles, long elapsed, long execute) {

      mobiles.forEach(mobile -> {
          for (int i = 0; i < NUM_RB; i++) { System.out.print(mobile.connectionStates[i] + "\t"); }
          System.out.println();
      });

        double throughput =
        mobiles.stream().map(mobile -> mobile.getThroughput() == 0.0 ? 0.0 : log(mobile.getThroughput() / t)).reduce(0.0, Double::sum);

        out.print("idx\t" + "   Rate User\t" + "       (log)\t" + "  Throughput\t" + "       (log)\t"
                + "      lambda\t" + "          mu\n");

        mobiles.forEach(mobile -> {
            out.print(format("%3d", mobile.idx) + "\t");
            out.print(format("%12.6f", mobile.getUserRate()) + "\t");
            out.print(format("%12.6f", log(mobile.getUserRate())) + "\t");
            out.print(format("%12.6f", mobile.getThroughput() / t) + "\t");
            out.print(format("%12.6f", log(mobile.getThroughput() / t)) + "\t");
            out.print(format("%12.6f", mobile.getLambda()) + "\t");
            out.print(format("%12.6f", mobile.getMu()) + "\n");
        });

        out.print("Time: " + format("%7d/%7d", t, SIMULATION_TIME) + "\t");
        out.print("Util: " + format("%8.4f", throughput) + "\t");
        out.print("Elap: " + format("%8.4f", secondFrom(elapsed)) + "\t");
        out.print("Exec: " + format("%8.4f", secondFrom(execute)) + "\n");

    }

    public static double secondFrom(long start) {
        return ((double) System.currentTimeMillis() - start) / 1000;
    }

}
