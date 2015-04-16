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
                        return new Pico(idx, values[0], values[1], PICO_TX_POWER);
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

        Algorithm algorithm = new Algorithm3();
        Console console = new GuiConsole(algorithm);
        Calculator calculator = new Calculator(macros, picos, mobiles, algorithm, console);
        console.start(calculator);
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
