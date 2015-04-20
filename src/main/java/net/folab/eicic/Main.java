package net.folab.eicic;

import static java.util.Arrays.asList;
import static net.folab.eicic.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Main {

    private static final String DEFAULT_CONSOLE_CLASS_NAME = "net.folab.eicic.TextConsole";

    private boolean isHelp;

    private String consoleClassName;

    private Console console;

    private String algorithmClassName;

    private Algorithm algorithm;

    private int totalSeq;

    public abstract class Generator<T> {

        private final Class<T> type;

        public Generator(Class<T> type) {
            super();
            this.type = type;
        }

        public abstract T generate(int idx, double[] values);

        public Class<T> getType() {
            return type;
        }

    }

    public static void main(String[] args) throws IOException {

        Main main = new Main(args);
        if (main.isSufficient())
            main.start();
        else
            main.printHelp();

    }

    public static <T> T[] loadObject(String file, Generator<T> generator)
            throws IOException {
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
        T[] array = (T[]) Array.newInstance(generator.getType(), list.size());
        return list.toArray(array);
    }

    private Main(String[] arguments) {

        OptionParser parser = new OptionParser();

        OptionSpec<String> consoleOption = parser.accepts("c")
                .withRequiredArg().ofType(String.class);

        OptionSpec<String> algorithmOption = parser.accepts("a")
                .withRequiredArg().ofType(String.class);

        OptionSpec<Integer> seqOption = parser.accepts("s").withRequiredArg()
                .ofType(Integer.class);

        OptionSpec<Void> helpOption = parser.acceptsAll(asList("h", "help"));

        OptionSet optionSet = parser.parse(arguments);

        totalSeq = 0;
        if (optionSet.has(seqOption))
            totalSeq = optionSet.valueOf(seqOption).intValue();

        if (optionSet.has(algorithmOption)) {
            algorithmClassName = "net.folab.eicic.algorithm.Algorithm"
                    + optionSet.valueOf(algorithmOption);
        }

        consoleClassName = DEFAULT_CONSOLE_CLASS_NAME;
        if (optionSet.has(consoleOption)) {
            consoleClassName = optionSet.valueOf(consoleOption);
        }

        if (optionSet.has(helpOption))
            isHelp = true;

    }

    private boolean isSufficient() {

        if (isHelp)
            return false;

        if (consoleClassName != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<Console> consoleClass = (Class<Console>) Class
                        .forName(consoleClassName);
                console = consoleClass.newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (InstantiationException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            } catch (ClassCastException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (algorithmClassName != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<Algorithm> algorithmClass = (Class<Algorithm>) Class
                        .forName(algorithmClassName);
                algorithm = algorithmClass.newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (InstantiationException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            } catch (ClassCastException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (!"net.folab.eicic.GuiConsole".equals(consoleClassName)
                && algorithm == null)
            return false;

        if (!"net.folab.eicic.GuiConsole".equals(consoleClassName)
                && totalSeq < 1)
            return false;

        return true;

    }

    private void start() throws IOException {

        Macro[] macros = loadObject("res/macro.txt", new Generator<Macro>(
                Macro.class) {
            @Override
            public Macro generate(int idx, double[] values) {
                return new Macro(idx, values[0], values[1], MACRO_TX_POWER);
            }
        });
        Pico[] picos = loadObject("res/pico.txt", new Generator<Pico>(
                Pico.class) {
            @Override
            public Pico generate(int idx, double[] values) {
                return new Pico(idx, values[0], values[1], PICO_TX_POWER);
            }
        });
        Mobile[] mobiles = loadObject("res/mobile.txt", new Generator<Mobile>(
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

        Calculator calculator = new Calculator(macros, picos, mobiles, console);

        console.setAlgorithm(algorithm);
        console.setTotalSeq(totalSeq);
        console.start(calculator);

    }

    private void printHelp() {
        print("");
        print("usage: java [JVM_OPTS] %s [options]", Main.class.getName());
        print("");
        print("You may need `-cp` option for GUI with SWT in JVM options.");
        print("Regarding details of JVM options, check below URL:");
        print(": http://www.oracle.com/technetwork/articles/java/vmoptions-jsp-140102.html");
        print("");
        print("Options:");

        print(" -s <arg>                               Set sequence count to simulate.");
        print("                                        It's optional when consle class");
        print("                                        (`-c`) is net.folab.eicic.GuiConsole,");
        print("                                        otherwise mandatory.");

        print(" -a <arg>                               Set algorithm number.");
        print("                                        It should be one of 1/2/3.");
        print("                                        It's optional when consle class");
        print("                                        (`-c`) is net.folab.eicic.GuiConsole,");
        print("                                        otherwise mandatory.");

        print(" -c <arg>                               Set console class name.");
        print("                                        Default: %s",
                DEFAULT_CONSOLE_CLASS_NAME);

        print(" -h                                     Print this help.");
    }

    private void print(String string, Object... args) {
        System.out.println(String.format(string, args));
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

}
