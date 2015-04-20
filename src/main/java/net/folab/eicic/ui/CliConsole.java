package net.folab.eicic.ui;

import static java.lang.Math.log;
import static java.lang.String.format;
import static java.lang.System.out;

import java.io.Console;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class CliConsole implements net.folab.eicic.Console {

    private Algorithm algorithm;

    private Controller controller;

    private double throughput;

    @Override
    public synchronized long dump(int seq, Macro[] macros, Pico[] picos, Mobile[] mobiles,
            long elapsed, long execute) {

        throughput = 0.0;
        for (int u = 0; u < mobiles.length; u++) {
            Mobile mobile = mobiles[u];
            throughput += mobile.getThroughput() == 0.0 ? 0.0 : log(mobile
                    .getThroughput() / seq);
        }

        return -1;
    }

    private void dump() {
        int seq = controller.getSeq();
        Macro[] macros = controller.getMacros();
        Pico[] picos = controller.getPicos();
        Mobile[] mobiles = controller.getMobiles();
        long elapsed = controller.getElapsed();

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
        Console console = System.console();
        do {

            console.format("PA%d: %.4f @ %d/%d> ", algorithm.getNumber(),
                    throughput, controller.getSeq(), controller.getTotalSeq());
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
                controller.stop();
                break;
            }

            console.printf("%s: Unkonwn command. Try `help`.\n", command);

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
