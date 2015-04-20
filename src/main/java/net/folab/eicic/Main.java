package net.folab.eicic;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.ui.Controller;

public class Main {

    private static final String DEFAULT_CONSOLE_CLASS_NAME = "net.folab.eicic.TextConsole";

    private boolean isHelp;

    private String consoleClassName;

    private Console console;

    private String algorithmClassName;

    private Algorithm algorithm;

    private int totalSeq;

    public static void main(String[] args) throws IOException {

        Main main = new Main(args);
        if (main.isSufficient()) {
            Controller controller = new Controller(main.console,
                    main.algorithm, main.totalSeq);
            controller.start();
        } else
            main.printHelp();

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
            console = newInstance(consoleClassName);
            if (console == null)
                return false;
        }

        if (algorithmClassName != null) {
            algorithm = newInstance(algorithmClassName);
            if (algorithm == null)
                return false;
        }

        if (!"net.folab.eicic.GuiConsole".equals(consoleClassName)
                && algorithm == null)
            return false;

        if (!"net.folab.eicic.GuiConsole".equals(consoleClassName)
                && totalSeq < 1)
            return false;

        return true;

    }

    public static <T> T newInstance(String className) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> type = (Class<T>) Class.forName(className);
            return type.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
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
