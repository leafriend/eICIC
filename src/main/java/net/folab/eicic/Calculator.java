package net.folab.eicic;

import java.util.List;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Calculator {

    private final List<Macro> macros;

    private final List<Pico> picos;

    private final List<Mobile> mobiles;

    private int time = 1;

    private long accumuMillis = 0;

    private boolean running = false;

    private Algorithm algorithm;

    private Console console;

    public Calculator(List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles, Algorithm algorithm, Console console) {
        super();
        this.macros = macros;
        this.picos = picos;
        this.mobiles = mobiles;
        this.algorithm = algorithm;
        this.console = console;
    }

    public void calculate(final int times) {

        running = true;

        final long started = System.currentTimeMillis();
        final long baseAccumuMillis = accumuMillis;

        new Thread() {
            public void run() {

                long elapsed = System.currentTimeMillis();

                while (running && time <= times) {

                    calculateInternal();
                    accumuMillis = baseAccumuMillis + System.currentTimeMillis() - started;
                    dump(-1);


                }

                accumuMillis = baseAccumuMillis + System.currentTimeMillis() - started;
                elapsed = dump(elapsed);

                accumuMillis = baseAccumuMillis + System.currentTimeMillis() - started;

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

        for (Mobile mobile : mobiles)
            mobile.calculateThroughput();
        for (Mobile mobile : mobiles)
            mobile.calculateUserRate();
        final int _t = time;
        for (Mobile mobile : mobiles)
            mobile.calculateDualVariables(_t);

        time++;
    }

    private long dump(long elapsed) {
        elapsed = console.dump(time - 1, macros, picos, mobiles, elapsed,
                accumuMillis);
        return elapsed;
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

    public List<Macro> getMacros() {
        return macros;
    }

    public List<Pico> getPicos() {
        return picos;
    }

    public List<Mobile> getMobiles() {
        return mobiles;
    }

}
