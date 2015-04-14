package net.folab.eicic;

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

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Main {

    public static void main(String[] args) throws IOException {

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
        picos.forEach(pico -> mobiles.forEach(mobile -> new Edge<>(pico, mobile)));

        for (int t = 0; t < SIMULATION_TIME; t++) {

            macros.forEach(macro -> macro.generateChannelGain());
            picos.forEach(pico -> pico.generateChannelGain());

            mobiles.forEach(mobile -> mobile.calculateDataRate());

        }

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

}
