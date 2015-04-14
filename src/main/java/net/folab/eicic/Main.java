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

        long start = System.currentTimeMillis();

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

        macros.forEach(macro -> mobiles.forEach(mobile -> new Edge<>(macro, mobile)));
        macros.forEach(macro -> picos.forEach(pico -> pico.checkInterference(macro)));
        picos.forEach(pico -> mobiles.forEach(mobile -> new Edge<>(pico, mobile)));
        picos.forEach(Pico::init);

        Algorithm algorithm = new Algorithm3();

        for (int t = 1; t <= SIMULATION_TIME; t++) {

            macros.forEach(macro -> macro.generateChannelGain());
            picos.forEach(pico -> pico.generateChannelGain());

            mobiles.forEach(mobile -> mobile.calculateDataRate());

            picos.forEach(pico -> pico.sortMobiles());

            algorithm.calculate(macros, picos, mobiles);

            mobiles.forEach(mobile -> mobile.calculateThroughput());
            mobiles.forEach(mobile -> mobile.calculateUserRate());
            final int _t = t;
            mobiles.forEach(mobile -> mobile.calculateDualVariables(_t));

            if (t % 100 == 0) {
                dump(t, macros, picos, mobiles);
            }

        }

        System.out.println(secondFrom(start));

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
            List<Mobile> mobiles) {

        out.print("Time: " + format("%7d/%7d", t, SIMULATION_TIME) + "\n");

        out.print("idx\t" + "   Rate User\t" + "       (log)\t" + "  Throughput\t" + "       (log)\t"
                + "      lambda\t" + "          mu\n");

        mobiles.forEach(mobile -> {
            out.print(format("%3d", mobile.idx) + "\t");
            out.print(format("%12.6f", mobile.getUserRate()) + "\t");
            out.print(format("%12.6f", log(mobile.getUserRate())) + "\t");
            out.print(format("%12.6f", mobile.getThroughput()) + "\t");
            out.print(format("%12.6f", log(mobile.getThroughput())) + "\t");
            out.print(format("%12.6f", mobile.getLambda()) + "\t");
            out.print(format("%12.6f", mobile.getMu()) + "\n");
        });

    }

    public static double secondFrom(long start) {
        return ((double) System.currentTimeMillis() - start) / 1000;
    }

}
