package net.folab.eicic.model;

import static net.folab.eicic.Constants.NUM_MACROS;

public class StateContext {

    private static final StateContext[] INSTANCES = new StateContext[1 << NUM_MACROS];

    private final Macro[] macros;

    private final boolean[] macrosIsOn;

    private final Pico[] picos;

    private final boolean[] picosIsAbs;

    private final Mobile[] mobiles;

    public static StateContext getStateContext(int macroState, Macro[] macros,
            Pico[] picos, Mobile[] mobiles) {
        if (INSTANCES[macroState] == null) {
            INSTANCES[macroState] = new StateContext(macroState, macros, picos,
                    mobiles);
        }
        return INSTANCES[macroState];
    }

    private StateContext(int macroState, Macro[] macros, Pico[] picos,
            Mobile[] mobiles) {

        this.macros = macros;
        this.macrosIsOn = new boolean[macros.length];
        for (int m = 0; m < macros.length; m++)
            macrosIsOn[m] = 1 == (((1 << m) & macroState) >> m);

        this.picos = picos;
        this.picosIsAbs = new boolean[picos.length];
        for (int p = 0; p < picos.length; p++) {
            Pico pico = picos[p];
            boolean isAbs = true;
            for (int m = 0; m < pico.macrosInterfering.size(); m++) {
                Macro macro = pico.macrosInterfering.get(m);
                if (macrosIsOn[macro.idx]) {
                    isAbs = false;
                    break;
                }
            }
            picosIsAbs[p] = isAbs;
        }

        this.mobiles = mobiles;

    }

    public int macros() {
        return macros.length;
    }

    public Macro macro(int m) {
        return macros[m];
    }

    public boolean macroIsOn(int m) {
        return macrosIsOn[m];
    }

    public int picos() {
        return picos.length;
    }

    public Pico pico(int p) {
        return picos[p];
    }

    public boolean picoIsAbs(int p) {
        return picosIsAbs[p];
    }

    public int mobiles() {
        return mobiles.length;
    }

    public Mobile mobile(int u) {
        return mobiles[u];
    }

}
