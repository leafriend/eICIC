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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import net.folab.eicic.algorithm.AlgorithmFactory;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class Controller {

    private static final String ALGORITHM_CLASS_NAME = "algorithmClassName";

    private static final String TOTAL_SEQ = "totalSeq";

    private static final String NEED_TO_SAVE_BEFORE_EXIT = "needToSaveBeforeExit";

    private static final String TOPOLOGY_FILE = "topologyFile";

    public static final List<FieldView<Mobile, ?>> COLUMNS = unmodifiableList(new ArrayList<FieldView<Mobile, ?>>() {

        private static final long serialVersionUID = -6689823013265960946L;

        {
            add("#", u -> u.idx, 32);
            add("X", u -> u.x, 80);
            add("Y", u -> u.y, 80);
            add("M", u -> u.getMacro().idx, 32);
            add("P", u -> u.getPico().idx, 32);
            addC("#M", u -> u.getMacroCount(), 64);
            addC("#P", u -> u.getPicoCount(), 64);
            addC("C", u -> u.getConnection(), 32);
            addC("User Rate", u -> u.getUserRate(), 96);
            addC("log(User Rate)", u -> log(u.getUserRate()), 96);
            addC("Throughput", u -> u.getThroughput(), 96);
            addC("log(Throughput)", u -> log(u.getThroughput()), 96);
            addC("λ", u -> u.getLambda(), 96);
            addC("μ", u -> u.getMu(), 96);
        }

        <T> void add(String name, Function<Mobile, T> getter, int width) {
            add(new FieldView<>(name, getter, width, false));
        }

        <T> void addC(String name, Function<Mobile, T> getter, int width) {
            add(new FieldView<>(name, getter, width, true));
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

        public abstract T generate(double[] values);

    }

    public static <T> T[] loadObject(String file, Generator<T> generator) {
        String delim = "( |\t)+";
        if (file.toLowerCase().endsWith(".csv"))
            delim = ",";
        try {

            List<T> list = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(delim);
                double[] values = new double[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    values[i] = Double.parseDouble(tokens[i]);
                }
                list.add(generator.generate(values));
            }
            reader.close();

            @SuppressWarnings("unchecked")
            T[] array = (T[]) Array.newInstance(generator.type, list.size());
            return list.toArray(array);

        } catch (IOException e) {
            throw new RuntimeException("File: " + file, e);
        }
    }

    public Controller(String consoleClassName, String algorithmClassName, Integer totalSeq) {
        super();

        configuration = new Configuration(getClass());

        reset(getTopologyFile());

        if (algorithmClassName == null)
            algorithmClassName = configuration.getConfiguration(ALGORITHM_CLASS_NAME, null);

        if (algorithmClassName != null) {
            this.algorithm = newInstance(algorithmClassName);
        } else {

        }

        calculator = new Calculator(macros, picos, mobiles);
        calculator.setAlgorithm(algorithm);

        console = newInstance(consoleClassName, this);
        if (console == null) {
            //return parser;
        }

        if (totalSeq == null) {
            this.totalSeq = configuration.getInteger(TOTAL_SEQ, -1);
            if (this.totalSeq < 0) {
                configuration.setInteger(TOTAL_SEQ, 0);
                this.totalSeq = 0;
            }
        } else {
            this.totalSeq = totalSeq.intValue();
        }

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
                    console.notifyEnded();
                }

            }
        };

        console.setAlgorithm(algorithm);
        console.setTotalSeq(totalSeq);
        console.notifyStarted();

    }

    public void reset(String file) {

        configuration.set(TOPOLOGY_FILE, file);

        seq = 0;
        accumuMillis = 0;

        String delim = "( |\t)+";
        if (file.toLowerCase().endsWith(".csv"))
            delim = ",";

        List<Macro> macros = new ArrayList<>();
        List<Pico> picos = new ArrayList<>();
        List<Mobile> mobiles = new ArrayList<>();

        Generator<Macro> macroGenerator = new Generator<Macro>(Macro.class) {
            @Override
            public Macro generate(double[] values) {
                return new Macro((int) values[0], values[1], values[2],
                        values[3]);
            }
        };
        Generator<Pico> picoGenerator = new Generator<Pico>(Pico.class) {
            @Override
            public Pico generate(double[] values) {
                return new Pico((int) values[0], values[1], values[2],
                        values[3]);
            }
        };
        Generator<Mobile> mobileGenerator = new Generator<Mobile>(Mobile.class) {
            @Override
            public Mobile generate(double[] values) {
                return new Mobile((int) values[0], values[1], values[2],
                        values[3], values[4], values[5], values[6]);
            }
        };

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            @SuppressWarnings("rawtypes")
            List list = null;
            Generator<?> generator = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    if (line.startsWith("#Macro")) {
                        list = macros;
                        generator = macroGenerator;
                    } else if (line.startsWith("#Pico")) {
                        list = picos;
                        generator = picoGenerator;
                    } else if (line.startsWith("#Mobile")) {
                        list = mobiles;
                        generator = mobileGenerator;
                    }
                    continue;
                }

                String[] tokens = line.split(delim);
                double[] values = new double[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].isEmpty())
                        // TODO 토큰이 빈 문자열인 경우 처리
                        values[i] = 0.0;
                    else
                        values[i] = Double.parseDouble(tokens[i]);
                }
                list.add(generator.generate(values));
            }

        } catch (IOException e) {
            throw new RuntimeException("File: " + file, e);
        }

        Macro[] macroArray = (Macro[]) Array.newInstance(Macro.class, macros.size());
        this.macros = macros.toArray(macroArray);

        Pico[] picoArray = (Pico[]) Array.newInstance(Pico.class, picos.size());
        this.picos = picos.toArray(picoArray);

        Mobile[] mobileArray = (Mobile[]) Array.newInstance(Mobile.class, mobiles.size());
        this.mobiles = mobiles.toArray(mobileArray);

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

        if (console != null)
            console.dump(0, null, this.macros, this.picos, this.mobiles, 0);

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

    public void terminate() {
        isRunning = false;
        executorService.shutdown();
        AlgorithmFactory.terminate();
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
        String algorithmName = algorithm.getClass().getSimpleName();
        return format("logs/%s-%d.%s", algorithmName, seq, extension);
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
            COLUMNS.forEach(view -> {
                String d = "";
                if (isFirst[0]) {
                    isFirst[0] = false;
                } else {
                    d = delim;
                }
                headers.append(d).append(view.name);
            });
            writer.write(headers.toString());
            writer.write("\n");

            for (int u = 0; u < mobiles.length; u++) {
                StringBuilder values = new StringBuilder();
                Mobile mobile = mobiles[u];
                isFirst[0] = true;
                COLUMNS.forEach(view -> {
                    String d = "";
                    if (isFirst[0]) {
                        isFirst[0] = false;
                    } else {
                        d = delim;
                    }
                    values.append(d).append(view.getter.apply(mobile));
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

    public String getTopologyFile() {
        return configuration.getConfiguration(TOPOLOGY_FILE).orElse(null);
    }

    /* bean getter/setter *************************************************** */

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        if (algorithm == null)
            throw new RuntimeException(); // FIXME
        configuration.set(ALGORITHM_CLASS_NAME, algorithm.getClass().getName());
        calculator.setAlgorithm(algorithm);
    }

    public int getTotalSeq() {
        return totalSeq;
    }

    public void setTotalSeq(int totalSeq) {
        this.totalSeq = totalSeq;
        configuration.setInteger(TOTAL_SEQ, totalSeq);
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
