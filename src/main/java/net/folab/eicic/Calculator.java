package net.folab.eicic;

import static net.folab.eicic.Constants.*;

import java.util.List;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Calculator {

    private final List<Macro> macros;

    private final List<Pico> picos;

    private final List<Mobile> mobiles;

    private int seq = 1;

    private long accumuMillis = 0;

    private boolean running = false;

    private Algorithm algorithm;

    private Console console;

    public Calculator(List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles, Console console) {
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
                    accumuMillis = baseAccumuMillis + System.currentTimeMillis() - started;
                    dump(-1);


                }

                accumuMillis = baseAccumuMillis + System.currentTimeMillis() - started;
                elapsed = dump(elapsed);

                accumuMillis = baseAccumuMillis + System.currentTimeMillis() - started;
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
        for (int m = 0; m < NUM_MACROS; m++)
            macros.get(m).initializeEdges();
        for (int p = 0; p < NUM_PICOS; p++)
            picos.get(p).initializeEdges();

        for (int u = 0; u < NUM_MOBILES; u++)
            mobiles.get(u).calculateDataRate();

        for (int m = 0; m < NUM_MACROS; m++)
            macros.get(m).sortMobiles();
        for (int p = 0; p < NUM_PICOS; p++)
            picos.get(p).sortMobiles();

        algorithm.calculate(macros, picos, mobiles);

        for (int u = 0; u < NUM_MOBILES; u++)
            mobiles.get(u).calculateThroughput();
        for (int u = 0; u < NUM_MOBILES; u++)
            mobiles.get(u).calculateUserRate();
        for (int u = 0; u < NUM_MOBILES; u++)
            mobiles.get(u).calculateDualVariables(seq);

        for (int m = 0; m < NUM_MACROS; m++)
            macros.get(m).count();
        for (int p = 0; p < NUM_PICOS; p++)
            picos.get(p).count();

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

    public List<Macro> getMacros() {
        return macros;
    }

    public List<Pico> getPicos() {
        return picos;
    }

    public List<Mobile> getMobiles() {
        return mobiles;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

}
