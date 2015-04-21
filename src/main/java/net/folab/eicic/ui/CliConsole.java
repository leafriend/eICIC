package net.folab.eicic.ui;

import static java.lang.Math.log;
import static java.lang.String.format;
import static java.lang.System.out;
import net.folab.eicic.Console;
import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class CliConsole implements Console {

    private Algorithm algorithm;

    private Controller controller;

    private double throughput;

    private int frequency;

    private int seq;

    private int saved;

    private long elapsed;

    @Override
    public synchronized void dump(int seq, StateContext state, Macro[] macros,
            Pico[] picos, Mobile[] mobiles, long elapsed) {

        this.elapsed = elapsed;
        this.seq = seq;

        throughput = 0.0;
        for (int u = 0; u < mobiles.length; u++) {
            Mobile mobile = mobiles[u];
            throughput += mobile.getThroughput() == 0.0 ? 0.0 : log(mobile
                    .getThroughput() / seq);
        }

        if (frequency > 0 && seq % frequency == 0) {
            System.out.println();
            dump();
            prompt();
        }

    }

    private void dump() {
        int seq = controller.getSeq();
        Mobile[] mobiles = controller.getMobiles();

        out.print("idx\t" + "   Rate User\t" + "       (log)\t"
                + "  Throughput\t" + "       (log)\t" + "      lambda\t"
                + "          mu\n");

        for (int u = 0; u < mobiles.length; u++) {
            Mobile mobile = mobiles[u];
            out.print(format("%3d", mobile.idx) + "\t");
            out.print(format("%12.6f", mobile.getUserRate()) + "\t");
            out.print(format("%12.6f", log(mobile.getUserRate())) + "\t");
            out.print(format("%12.6f", mobile.getThroughput() / seq) + "\t");
            out.print(format("%12.6f", log(mobile.getThroughput() / seq))
                    + "\t");
            out.print(format("%12.6f", mobile.getLambda()) + "\t");
            out.print(format("%12.6f", mobile.getMu()) + "\n");
        }

    }

    @Override
    public void notifyStarted() {
        java.io.Console console = System.console();
        if (console == null)
            throw new RuntimeException("Faield to get Console instance");
        do {

            prompt();
            String command = console.readLine().trim();

            if (command.length() == 0)
                continue;

            if (command.equals("start")) {
                controller.start();
                continue;
            }

            if (command.equals("stop")) {
                controller.pause();
                continue;
            }

            if (command.equals("reset")) {
                if (confirm(console))
                    controller.reset();
                continue;
            }

            if (command.startsWith("until ")) {
                try {
                    String number = command.substring("until ".length()).trim();
                    int totalSeq = Integer.parseInt(number);
                    controller.setTotalSeq(totalSeq);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (command.equals("dump")) {
                dump();
                continue;
            }

            if (command.startsWith("dump ")) {
                try {
                    String number = command.substring("dump ".length()).trim();
                    frequency = Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if ("help".equals(command)) {
                console.printf("start           Start calculation.\n");
                console.printf("stop            Stop running calculation.\n");
                console.printf("reset           Reset calculation result.\n");
                console.printf("until TOTAL_SEQ Set total sequence.\n");
                console.printf("dump            Dump calculation dump.\n");
                console.printf("exit            Exit program.\n");
                console.printf("help            Print this help.\n");
                continue;
            }

            if ("exit".equals(command)) {
                if (confirm(console)) {
                    controller.stop();
                    break;
                } else {
                    continue;
                }
            }

            console.printf("%s: Unkonwn command. Try `help`.\n", command);

        } while (true);

    }

    public void prompt() {

        int seq = controller.getSeq();
        int totalSeq = controller.getTotalSeq();
        long estimated = seq == 0 ? 0 : elapsed * totalSeq / seq;
        long left = 1000 * ((estimated / 1000) - (elapsed / 1000));

        String time = Console.milisToTimeString(elapsed) + " + "
                + Console.milisToTimeString(left) + " = "
                + Console.milisToTimeString(estimated);

        out.print(String.format("PA%d:%7.3f @ %d/%d : %s> ",
                algorithm.getNumber(), throughput, seq, totalSeq, time));
    }

    private boolean confirm(java.io.Console console) {
        if (seq == saved)
            return true;
        console.printf("Simulation result is not saved, therfore, your result will be lost.\nDo you want to save result before exit?\n");
        do {
            console.printf("[y/n]: ");
            String answer = console.readLine().toLowerCase();
            if (answer.equals("y")) {
                return true;
            } else if (answer.equals("n")) {
                return false;
            }
        } while (true);
    }


    @Override
    public void notifyEnded() {
    }

    @Override
    public void setTotalSeq(int totalSeq) {
    }

    @Override
    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    // TODO 중복 구현 제거
    public static void delay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
