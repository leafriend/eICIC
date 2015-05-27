package net.folab.eicic.core;

import static net.folab.eicic.ui.Util.newInstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

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
        String delim = "( |\t)+";
        if (file.toLowerCase().endsWith(".csv"))
            delim = ",";
        try {

            List<T> list = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int idx = 0;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(delim);
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

    public Controller(String consoleClassName, Algorithm algorithm, int totalSeq) {
        super();
        console = newInstance(consoleClassName, this);
        if (console == null) {
            //return parser;
        }
        this.algorithm = algorithm;
        this.totalSeq = totalSeq;
    }

    public static void dump(String string, int seq, Mobile[] mobiles) {

        try {
            FileWriter writer = new FileWriter(new File(string + ".csv"), true);
            writer.write(String.valueOf(seq));
            double sum = 0;
            StringBuilder sb = new StringBuilder();
            for (int u = 0; u < mobiles.length; u++) {
                double util = Math.log(mobiles[u].getThroughput() / seq);
                sb.append(",").append(util);
                sum += util;
            }
            writer.write("," + sum);
            writer.write(sb.toString());
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void display() {

        reset();

        calculator = new Calculator(macros, picos, mobiles, console);
        calculator.setAlgorithm(algorithm);

        runner = new Runnable() {
            @Override
            public void run() {
                long started = System.currentTimeMillis();

                StateContext state = null;
                while (isRunning && seq++ < nextSeq) {

                    state = calculator.calculateInternal(seq);
                    long execute = System.currentTimeMillis() - started
                            + accumuMillis;
                    console.dump(seq, state, macros, picos, mobiles, execute);

                    // TODO 옵션으로 추출
                    if (seq % 100 == 0) {
                        dump(algorithm.getClass().getSimpleName(), seq, mobiles);
                    }

                    if (seq == nextSeq)
                        break;

                }

                long execute = System.currentTimeMillis() - started
                        + accumuMillis;
                console.dump(seq, state, macros, picos, mobiles, execute);
                accumuMillis = execute;

                isRunning = false;
                if (seq == totalSeq) {
                    algorithm.tearDown(macros, picos, mobiles);
                    console.notifyEnded();
                }

            }
        };

        console.setAlgorithm(algorithm);
        console.setTotalSeq(totalSeq);
        console.setController(this);
        console.notifyStarted();

    }

    public void reset() {

        seq = 0;
        accumuMillis = 0;

        macros = loadObject("res/macro.csv", new Generator<Macro>(Macro.class) {
            @Override
            public Macro generate(int idx, double[] values) {
                return new Macro(idx, values[1], values[2], values[3]);
            }
        });
        picos = loadObject("res/pico.csv", new Generator<Pico>(Pico.class) {
            @Override
            public Pico generate(int idx, double[] values) {
                return new Pico(idx, values[1], values[2], values[3]);
            }
        });
        mobiles = loadObject("res/mobile.csv", new Generator<Mobile>(
                Mobile.class) {
            @Override
            public Mobile generate(int idx, double[] values) {
                return new Mobile(idx, values[1], values[2], values[3],
                        values[4], values[5], values[6]);
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

        console.dump(seq, null, macros, picos, mobiles, accumuMillis);

    }

    public void start() {
        console.notifyStarted();
        if (seq == 0) {
            algorithm.setUp(macros, picos, mobiles);
        }
        isRunning = true;
        nextSeq = totalSeq;
        executorService.execute(runner);
    }

    public void next() {
        isRunning = true;
        nextSeq = seq + 1;
        executorService.execute(runner);
    }

    public void pause() {
        console.notifyPaused();
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
        calculator.setAlgorithm(algorithm);
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

    public long getElapsed() {
        return accumuMillis;
    }

}
