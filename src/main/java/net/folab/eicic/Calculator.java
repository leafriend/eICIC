package net.folab.eicic;

import java.util.List;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Calculator {

    private final Macro[] macros;

    private final Pico[] picos;

    private final Mobile[] mobiles;

    private int seq = 1;

    private long accumuMillis = 0;

    private boolean running = false;

    private Algorithm algorithm;

    private Console console;

    @Deprecated
    public Calculator(List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles, Console console) {
        this(macros.toArray(new Macro[0]), picos.toArray(new Pico[0]), mobiles
                .toArray(new Mobile[0]), console);
    }

    public Calculator(Macro[] macros, Pico[] picos, Mobile[] mobiles,
            Console console) {
        super();
        this.macros = macros;
        this.picos = picos;
        this.mobiles = mobiles;
        this.console = console;
    }

    public void calculate(final int times) {

        running = true;

        final long started = System.currentTimeMillis();
        final long baseAccumuMillis = accumuMillis;

        new Thread() {
            public void run() {

                long elapsed = System.currentTimeMillis();

                while (running && seq <= times) {

                    calculateInternal();
                    accumuMillis = baseAccumuMillis
                            + System.currentTimeMillis() - started;
                    dump(-1);

                }

                accumuMillis = baseAccumuMillis + System.currentTimeMillis()
                        - started;
                elapsed = dump(elapsed);

                accumuMillis = baseAccumuMillis + System.currentTimeMillis()
                        - started;
                console.end();

            }
        }.start();

    }

    public void calculate() {

        final long started = System.currentTimeMillis();

        new Thread() {
            public void run() {

                calculateInternal();
                accumuMillis += System.currentTimeMillis() - started;
                dump(-1);

            }
        }.start();

    }

    private void calculateInternal() {
        for (int m = 0; m < macros.length; m++)
            macros[m].initializeEdges();
        for (int p = 0; p < picos.length; p++)
            picos[p].initializeEdges();

        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateDataRate();

        for (int m = 0; m < macros.length; m++)
            macros[m].sortMobiles();
        for (int p = 0; p < picos.length; p++)
            picos[p].sortMobiles();

        algorithm.calculate(macros, picos, mobiles);

        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateThroughput();
        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateUserRate();
        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateDualVariables(seq);

        for (int m = 0; m < macros.length; m++)
            macros[m].count();
        for (int p = 0; p < picos.length; p++)
            picos[p].count();

        if (seq % 100 == 0) {
            Main.dump(algorithm.getClass().getSimpleName(), seq, mobiles);
        }

        seq++;
    }

    private long dump(long elapsed) {
        elapsed = console.dump(seq - 1, macros, picos, mobiles, accumuMillis,
                elapsed);
        return elapsed;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public int getSeq() {
        return seq;
    }

    public Macro[] getMacros() {
        return macros;
    }

    public Pico[] getPicos() {
        return picos;
    }

    public Mobile[] getMobiles() {
        return mobiles;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

}
