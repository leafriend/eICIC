package net.folab.eicic;

import static java.lang.Math.log;
import static java.lang.String.format;
import static java.lang.System.out;
import static net.folab.eicic.Constants.SIMULATION_TIME;

import java.util.List;

import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class TextConsole implements Console {

    public void dump(int t, List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles, long elapsed, long execute) {

//        for (Mobile mobile : mobiles) {
//            for (int i = 0; i < NUM_RB; i++) { System.out.print(mobile.connectionStates[i] + "\t"); }
//            System.out.println();
//        }

        double throughput = 0.0;
        for (Mobile mobile : mobiles) {
            throughput += mobile.getThroughput() == 0.0 ? 0.0 : log(mobile.getThroughput() / t);
        }

        out.print("idx\t" + "   Rate User\t" + "       (log)\t" + "  Throughput\t" + "       (log)\t"
                + "      lambda\t" + "          mu\n");

        for (Mobile mobile : mobiles) {
            out.print(format("%3d", mobile.idx) + "\t");
            out.print(format("%12.6f", mobile.getUserRate()) + "\t");
            out.print(format("%12.6f", log(mobile.getUserRate())) + "\t");
            out.print(format("%12.6f", mobile.getThroughput() / t) + "\t");
            out.print(format("%12.6f", log(mobile.getThroughput() / t)) + "\t");
            out.print(format("%12.6f", mobile.getLambda()) + "\t");
            out.print(format("%12.6f", mobile.getMu()) + "\n");
        }

        out.print("Time: " + format("%7d/%7d", t, SIMULATION_TIME) + "\t");
        out.print("Util: " + format("%8.4f", throughput) + "\t");
        out.print("Elap: " + format("%8.4f", secondFrom(elapsed)) + "\t");
        out.print("Exec: " + format("%8.4f", secondFrom(execute)) + "\n");

    }

    public static double secondFrom(long start) {
        return ((double) System.currentTimeMillis() - start) / 1000;
    }

}
