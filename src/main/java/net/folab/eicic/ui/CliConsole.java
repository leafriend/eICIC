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

    private boolean dumpped = true;

    @Override
    public long dump(int seq, Macro[] macros, Pico[] picos, Mobile[] mobiles,
            long elapsed, long execute) {

        throughput = 0.0;
        for (int u = 0; u < mobiles.length; u++) {
            Mobile mobile = mobiles[u];
            throughput += mobile.getThroughput() == 0.0 ? 0.0 : log(mobile
                    .getThroughput() / seq);
        }

        if (dumpped)
            return -1;

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

        // out.print("Time: " + format("%7d/%7d", seq, totalSeq) + "\t");
        // out.print("Util: " + format("%8.4f", throughput) + "\t");
        // out.print("Elap: " + format("%8.4f", secondFrom(elapsed)) + "\t");
        // out.print("Exec: " + format("%8.4f", secondFrom(execute)) + "\n");

        dumpped = true;

        return -1;
    }

    @Override
    public void notifyStarted() {
        Console console = System.console();
        boolean isRunning = true;
        do {

            while (!dumpped)
                delay(0);

            console.format("PA%d: %.4f @ %d/%d> ", algorithm.getNumber(),
                    throughput, controller.getSeq(), controller.getTotalSeq());
            String command = console.readLine();

            if ("start".equals(command)) {
                controller.start();
            }

            if ("stop".equals(command)) {
                controller.pause();
            }

            if ("reset".equals(command)) {
                controller.reset();
            }

            if ("dump".equals(command)) {
                dumpped = false;
            }

            if ("exit".equals(command)) {
                controller.stop();
                isRunning = false;
            }

        } while (isRunning);
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
