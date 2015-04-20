package net.folab.eicic.ui;

import static net.folab.eicic.Constants.MACRO_TX_POWER;
import static net.folab.eicic.Constants.MOBILE_QOS;
import static net.folab.eicic.Constants.PICO_TX_POWER;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.folab.eicic.Calculator;
import net.folab.eicic.Console;
import net.folab.eicic.Main;
import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Controller {

    private Console console;

    private Algorithm algorithm;

    private int totalSeq;

    private Calculator calculator;

    private Macro[] macros;

    private Pico[] picos;

    private Mobile[] mobiles;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private Runnable runner;

    private boolean isRunning = false;

    private int seq = 0;

    private int nextSeq = 0;

    private long accumuMillis = 0;

    public static abstract class Generator<T> {

        public final Class<T> type;

        public Generator(Class<T> type) {
            super();
            this.type = type;
        }

        public abstract T generate(int idx, double[] values);

    }

    public static <T> T[] loadObject(String file, Generator<T> generator) {
        try {

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

            @SuppressWarnings("unchecked")
            T[] array = (T[]) Array.newInstance(generator.type, list.size());
            return list.toArray(array);

        } catch (IOException e) {
            throw new RuntimeException("File: " + file, e);
        }
    }

    public Controller(Console console, Algorithm algorithm, int totalSeq) {
        super();
        this.console = console;
        this.algorithm = algorithm;
        this.totalSeq = totalSeq;
    }

    public void display() {

        resest();

        calculator = new Calculator(macros, picos, mobiles, console);
        calculator.setAlgorithm(algorithm);

        runner = new Runnable() {
            @Override
            public void run() {

                long started = System.currentTimeMillis();

                while (isRunning && seq < nextSeq) {
                    seq++;

                    calculator.calculateInternal(seq);
                    long execute = System.currentTimeMillis() - started
                            + accumuMillis;
                    console.dump(seq, macros, picos, mobiles, execute, -1);

                    if (seq % 100 == 0) {
                        Main.dump(algorithm.getClass().getSimpleName(), seq,
                                mobiles);
                    }

                }

                long execute = System.currentTimeMillis() - started
                        + accumuMillis;
                console.dump(seq, macros, picos, mobiles, execute, -1);
                accumuMillis = execute;

            }
        };

        console.setAlgorithm(algorithm);
        console.setTotalSeq(totalSeq);
        console.setController(this);
        console.notifyStarted();

    }

    public void resest() {

        seq = 0;
        accumuMillis = 0;

        macros = loadObject("res/macro.txt", new Generator<Macro>(Macro.class) {
            @Override
            public Macro generate(int idx, double[] values) {
                return new Macro(idx, values[0], values[1], MACRO_TX_POWER);
            }
        });
        picos = loadObject("res/pico.txt", new Generator<Pico>(Pico.class) {
            @Override
            public Pico generate(int idx, double[] values) {
                return new Pico(idx, values[0], values[1], PICO_TX_POWER);
            }
        });
        mobiles = loadObject("res/mobile.txt", new Generator<Mobile>(
                Mobile.class) {
            @Override
            public Mobile generate(int idx, double[] values) {
                return new Mobile(idx, values[0], values[1], MOBILE_QOS,
                        values[2], values[3], values[4]);
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

        console.dump(seq, macros, picos, mobiles, accumuMillis, -1);

    }

    public void start() {
        isRunning = true;
        nextSeq = totalSeq;
        executorService.execute(runner);
    }

    public void next() {
        isRunning = true;
        nextSeq = seq + 1;
        executorService.execute(runner);
    }

    public void pasue() {
        isRunning = false;
    }

    public void stop() {
        isRunning = false;
        executorService.shutdown();
    }

    /* bean getter/setter *************************************************** */

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public int getTotalSeq() {
        return totalSeq;
    }

    public void setTotalSeq(int totalSeq) {
        this.totalSeq = totalSeq;
    }

    public int getSeq() {
        return seq;
    }

    public Macro[] getMacros() {
        return macros;
    }

    public Pico[] getPicos() {
        return picos;
    }

    public Mobile[] getMobiles() {
        return mobiles;
    }

}
