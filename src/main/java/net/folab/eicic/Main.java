package net.folab.eicic;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.folab.eicic.core.Controller;

public class Main {

    private static final String DEFAULT_CONSOLE_CLASS_NAME = "net.folab.eicic.ui.CliConsole";

    public static void main(String[] args) {
        new File("logs").mkdir(); // TODO remove this code when available
        OptionParser parser = run(args);
        if (parser != null)
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static OptionParser run(String[] args) {

        OptionParser parser = new OptionParser();

        OptionSpec<String> consoleOption = parser
                .acceptsAll(
                        asList("c", "console"),
                        String.format("Set console class name. Default: `%s`",
                                DEFAULT_CONSOLE_CLASS_NAME)).withRequiredArg()
                .ofType(String.class);

        OptionSpec<String> algorithmOption = parser
                .acceptsAll(
                        asList("a", "algorithm"),
                        "Set algorithm number. It should be one of 1/2/3. It's optional when console class  (`-c`) is `net.folab.eicic.GuiConsole`, otherwise mandatory.")
                .withRequiredArg().ofType(String.class);

        OptionSpec<Integer> seqOption = parser
                .acceptsAll(
                        asList("s", "sequence"),
                        "Set sequence count to simulate. It's optional when consle class (`-c`) is `net.folab.eicic.GuiConsole`, otherwise mandatory.")
                .withRequiredArg().ofType(Integer.class);

        OptionSpec<Void> helpOption = parser.acceptsAll(asList("h", "help"),
                "Prints this help").forHelp();

        OptionSet optionSet = parser.parse(args);

        Integer totalSeq = null;
        if (optionSet.has(seqOption))
            totalSeq = optionSet.valueOf(seqOption);

        String algorithmClassName = null;
        if (optionSet.has(algorithmOption)) {
            algorithmClassName = "net.folab.eicic.algorithm.Algorithm"
                    + optionSet.valueOf(algorithmOption);
        }

        String consoleClassName = DEFAULT_CONSOLE_CLASS_NAME;
        if (optionSet.has(consoleOption)) {
            consoleClassName = optionSet.valueOf(consoleOption);
        }

        // - - -

        if (consoleClassName == null) {
            return parser;
        }

        if (!"net.folab.eicic.ui.GuiConsole".equals(consoleClassName)
                && algorithmClassName == null) {
            return parser;
        }

        if (!"net.folab.eicic.ui.GuiConsole".equals(consoleClassName)
                && totalSeq != null && totalSeq.intValue() < 1) {
            return parser;
        }

        // - - -

        if (optionSet.has(helpOption)) {
            return parser;
        }

        Controller controller = new Controller(consoleClassName, algorithmClassName, totalSeq);
        controller.display();

        return null;

    }

}
