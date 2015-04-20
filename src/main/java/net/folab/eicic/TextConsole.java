package net.folab.eicic;

import static java.lang.Math.log;
import static java.lang.String.format;
import static java.lang.System.out;
import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.ui.Controller;

public class TextConsole implements Console {

    private int totalSeq;

    private Algorithm algorithm;

    public TextConsole() {
    }

    @Override
    public void start(Controller controller) {
        System.out.println(algorithm.getClass().getSimpleName());
        controller.start();
    }

    @Override
    public long dump(final int seq, final Macro[] macros, final Pico[] picos,
            final Mobile[] mobiles, final long elapsed, final long execute) {

        if (seq % 100 == 0) {

            double throughput = 0.0;
            for (int u = 0; u < mobiles.length; u++) {
                Mobile mobile = mobiles[u];
                throughput += mobile.getThroughput() == 0.0 ? 0.0 : log(mobile.getThroughput() / seq);
            }

            out.print("idx\t" + "   Rate User\t" + "       (log)\t" + "  Throughput\t" + "       (log)\t"
                    + "      lambda\t" + "          mu\n");

            for (int u = 0; u < mobiles.length; u++) {
                Mobile mobile = mobiles[u];
                out.print(format("%3d", mobile.idx) + "\t");
                out.print(format("%12.6f", mobile.getUserRate()) + "\t");
                out.print(format("%12.6f", log(mobile.getUserRate())) + "\t");
                out.print(format("%12.6f", mobile.getThroughput() / seq) + "\t");
                out.print(format("%12.6f", log(mobile.getThroughput() / seq)) + "\t");
                out.print(format("%12.6f", mobile.getLambda()) + "\t");
                out.print(format("%12.6f", mobile.getMu()) + "\n");
            }

            out.print("Time: " + format("%7d/%7d", seq, totalSeq) + "\t");
            out.print("Util: " + format("%8.4f", throughput) + "\t");
            out.print("Elap: " + format("%8.4f", secondFrom(elapsed)) + "\t");
            out.print("Exec: " + format("%8.4f", secondFrom(execute)) + "\n");

            return System.currentTimeMillis();

        } else {

            return elapsed;

        }

    }

    public static double secondFrom(long start) {
        return ((double) System.currentTimeMillis() - start) / 1000;
    }

    @Override
    public void end() {
    }

    @Override
    public void setTotalSeq(int totalSeq) {
        this.totalSeq = totalSeq;
    }

    @Override
    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

}
