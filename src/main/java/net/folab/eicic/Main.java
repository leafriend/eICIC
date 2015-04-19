package net.folab.eicic;

import static net.folab.eicic.Constants.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static interface Generator<T> {
        public T generate(int idx, double[] values);
    }

    public static void main(String[] args) throws IOException {

        Main main = new Main(args);
        if (main.isSufficient())
            main.start();
        else
            main.printHelp();

    }

    public static <T> List<T> loadObject(String file, Generator<T> generator)
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
        return list;
    }

    private Main(String[] arguments) {

        consoleClassName = DEFAULT_CONSOLE_CLASS_NAME;

        totalSeq = 0;

        for (int i = 0; i < arguments.length; i++) {
            String arg = arguments[i];
            String next = i < arguments.length - 1 ? arguments[i + 1] : null;
            boolean isNext = true;
            if (arg.length() > 2) {
                next = arg.substring(2);
                arg = arg.substring(0, 2);
                isNext = false;
            }

            switch (arg) {

            case "-s":
                totalSeq = Integer.parseInt(next);
                if (isNext)
                    i++;
                break;

            case "-a":
                if ("1".equals(next) || "2".equals(next) || "3".equals(next)) {
                    algorithmClassName = "net.folab.eicic.algorithm.Algorithm"
                            + next;
                    if (isNext)
                        i++;
                }
                break;

            case "-c":
                consoleClassName = next;
                if (isNext)
                    i++;
                break;

            case "-h":
                isHelp = true;
                break;

            default:
                break;

            }

        }

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

        if (!"net.folab.eicic.GuiConsole".equals(consoleClassName) && algorithm == null)
            return false;

        if (!"net.folab.eicic.GuiConsole".equals(consoleClassName) && totalSeq < 1)
            return false;

        return true;

    }

    private void start() throws IOException {

        List<Macro> macros = loadObject("res/macro.txt",
                new Generator<Macro>() {
                    @Override
                    public Macro generate(int idx, double[] values) {
                        return new Macro(idx, values[0], values[1],
                                MACRO_TX_POWER);
                    }
                });
        List<Pico> picos = loadObject("res/pico.txt", new Generator<Pico>() {
            @Override
            public Pico generate(int idx, double[] values) {
                return new Pico(idx, values[0], values[1], PICO_TX_POWER);
            }
        });
        List<Mobile> mobiles = loadObject("res/mobile.txt",
                new Generator<Mobile>() {
                    @Override
                    public Mobile generate(int idx, double[] values) {
                        return new Mobile(idx, values[0], values[1],
                                MOBILE_QOS, values[2], values[3], values[4]);
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
        calculator.setAlgorithm(algorithm);
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

}
