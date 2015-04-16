package net.folab.eicic;

import java.util.List;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Calculator {

    private List<Macro> macros;

    private List<Pico> picos;

    private List<Mobile> mobiles;

    private int time = 1;

    private boolean running = false;

    public Calculator(List<Macro> macros, List<Pico> picos, List<Mobile> mobiles) {
        super();
        this.macros = macros;
        this.picos = picos;
        this.mobiles = mobiles;
    }

    public void calculate(final Console console, final Algorithm algorithm,
            final int times) {

        running = true;

        new Thread() {
            public void run() {

                long execute = System.currentTimeMillis();
                long elapsed = System.currentTimeMillis();

                for (; running && time <= times; time++) {

                    for (Macro macro : macros)
                        macro.generateChannelGain();
                    for (Pico pico : picos)
                        pico.generateChannelGain();

                    for (Mobile mobile : mobiles)
                        mobile.calculateDataRate();

                    for (Macro macro : macros)
                        macro.sortMobiles();
                    for (Pico pico : picos)
                        pico.sortMobiles();

                    algorithm.calculate(macros, picos, mobiles);

                    // for (Mobile mobile : mobiles) {
                    // System.out.println("m: " + mobile.idx);
                    // for (Edge<Macro> edge : mobile.allMacroEdges) {
                    // System.out.print("    M: " + edge.baseStation.idx +
                    // "\t");
                    // for (int i = 0; i < NUM_RB; i++) {
                    // System.out.print(String.format("%8.4f",
                    // edge.channelGain[i] * 1000000000l) + "\t"); }
                    // System.out.println();
                    // }
                    // System.out.println();
                    // }

                    // for (Mobile mobile : mobiles) {
                    // for (int i = 0; i < NUM_RB; i++) {
                    // System.out.print(mobile.connectionStates[i] + "\t"); }
                    // System.out.println();
                    // }

                    for (Mobile mobile : mobiles)
                        mobile.calculateThroughput();
                    for (Mobile mobile : mobiles)
                        mobile.calculateUserRate();
                    final int _t = time;
                    for (Mobile mobile : mobiles)
                        mobile.calculateDualVariables(_t);

                    elapsed = console.dump(time, macros, picos, mobiles,
                            elapsed, execute);

                }

                elapsed = console.dump(time - 1, macros, picos, mobiles,
                        elapsed, execute);

            };
        }.start();

    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public int getTime() {
        return time;
    }

}
