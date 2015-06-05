package net.folab.eicic.core;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static net.folab.eicic.ui.Util.newInstance;
import static java.util.Collections.*;
import static java.lang.Math.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class Controller {

    private static final String NEED_TO_SAVE_BEFORE_EXIT = "needToSaveBeforeExit";

    public static final Map<String, Function<Mobile, String>> COLUMNS = unmodifiableMap(new LinkedHashMap<String, Function<Mobile, String>>() {
        private static final long serialVersionUID = -6689823013265960946L;
        {
            put("#", u -> valueOf(u.idx));
            put("X", u -> valueOf(u.x));
            put("Y", u -> valueOf(u.y));
            put("M", u -> valueOf(u.getMacro().idx));
            put("P", u -> valueOf(u.getPico().idx));
            put("User Rate", u -> valueOf(u.getUserRate()));
            put("log(User Rate)", u -> valueOf(log(u.getUserRate())));
            put("Throughput", u -> valueOf(u.getThroughput()));
            put("log(User Rate)", u -> valueOf(log(u.getUserRate())));
            put("λ", u -> valueOf(u.getLambda()));
            put("μ", u -> valueOf(u.getMu()));
        }
    });

    private Configuration configuration;

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

    private boolean saved = true;

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

        configuration = new Configuration(getClass());

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
                double util = Math.log(mobiles[u].getThroughput());
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

        calculator = new Calculator(macros, picos, mobiles);
        calculator.setAlgorithm(algorithm);

        runner = new Runnable() {
            @Override
            public void run() {
                long started = System.currentTimeMillis();

                StateContext state = null;
                while (isRunning && seq++ < nextSeq) {

                    saved = false;

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
        // TODO console.notifyStarted();
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

    /**
     * Returns default file name based current algorithm and seq.
     *
     * @param extension
     *            Suffix used as extension without period (<code>.</code>)
     *
     * @return default file name
     */
    public String getDefaultSaveFileName(final String extension) {
        return format("PA%d-%d." + extension, algorithm.getNumber(), seq);
    }

    public void save(String fileName) {
        try {

            Charset charset = Charset.forName(System
                    .getProperty("file.encoding"));

            String delim = fileName.toLowerCase().endsWith(".csv") ? "," : "\t";

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(fileName)), charset));

            // - - -

            writer.write("#Utitlity");
            writer.write(delim);
            // writer.write(utilityText.getText());
            writer.write("\n");
            writer.flush();

            writer.write("#Seq");
            writer.write(delim);
            writer.write(valueOf(seq));
            writer.write("\n");
            writer.flush();

            writer.write("#Time");
            writer.write(delim);
            writer.write(Console.milisToTimeString(accumuMillis));
            writer.write("\n");
            writer.flush();

            writer.write("#Macro Count");
            for (int m = 0; m < macros.length; m++) {
                writer.write(delim);
                writer.write(valueOf(macros[m].getAllocationCount()));
            }
            writer.write("\n");
            writer.flush();

            writer.write("#Macro %");
            for (int m = 0; m < macros.length; m++) {
                writer.write(delim);
                double percent = 100.0 * macros[m].getAllocationCount() / seq;
                writer.write(format("%.2f%%", percent));
            }
            writer.write("\n");
            writer.flush();

            // - - -

            final boolean[] isFirst = new boolean[1];

            StringBuilder headers = new StringBuilder();
            isFirst[0] = true;
            COLUMNS.forEach((name, func) -> {
                String d = "";
                if (isFirst[0]) {
                    isFirst[0] = false;
                } else {
                    d = delim;
                }
                headers.append(d).append(name);
            });
            writer.write(headers.toString());
            writer.write("\n");

            for (int u = 0; u < mobiles.length; u++) {
                StringBuilder values = new StringBuilder();
                Mobile mobile = mobiles[u];
                isFirst[0] = true;
                COLUMNS.forEach((name, func) -> {
                    String d = "";
                    if (isFirst[0]) {
                        isFirst[0] = false;
                    } else {
                        d = delim;
                    }
                    values.append(d).append(func.apply(mobile));
                });
                writer.write(values.toString());
                writer.write("\n");
            }

            // - - -

            saved = true;

            writer.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isNeedToSaveBeforeExit() {
        if (isSaved())
            return false;
        return configuration.getBoolean(NEED_TO_SAVE_BEFORE_EXIT);
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

    public boolean isSaved() {
        return saved;
    }

}
