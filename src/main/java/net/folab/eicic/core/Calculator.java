package net.folab.eicic.core;

import net.folab.eicic.algorithm.StaticAlgorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class Calculator {

    public static enum MaximizeTarget {

        SUM_UTILITY,

        SUM_RATE,

    }

    private MaximizeTarget maximizeTarget = MaximizeTarget.SUM_UTILITY;

    private final Macro[] macros;

    private final Pico[] picos;

    private final Mobile[] mobiles;

    private Algorithm algorithm;

    private boolean isStatic = false;

    public Calculator(Macro[] macros, Pico[] picos, Mobile[] mobiles) {
        super();
        this.macros = macros;
        this.picos = picos;
        this.mobiles = mobiles;
    }

    public StateContext calculateInternal(int seq) {

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

        StateContext state = algorithm.calculate(seq, macros, picos, mobiles);

        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateThroughput(state);

        if (isStatic) {

            switch (maximizeTarget) {

            case SUM_UTILITY:
                for (int u = 0; u < mobiles.length; u++)
                    mobiles[u].calculateUserRateMSU();
                for (int u = 0; u < mobiles.length; u++)
                    mobiles[u].calculateDualVariablesMSUStatic(seq);
                break;

            case SUM_RATE:
                for (int u = 0; u < mobiles.length; u++)
                    mobiles[u].calculateUserRateMSR();
                for (int u = 0; u < mobiles.length; u++)
                    mobiles[u].calculateDualVariablesMSR(seq);
                break;

            default:
                throw new RuntimeException("Unsupported maximize target: " + maximizeTarget);
            }

        } else {

            switch (maximizeTarget) {

            case SUM_UTILITY:
                for (int u = 0; u < mobiles.length; u++)
                    mobiles[u].calculateUserRateMSU();
                for (int u = 0; u < mobiles.length; u++)
                    mobiles[u].calculateDualVariablesMSU(seq);
                break;

            case SUM_RATE:
                for (int u = 0; u < mobiles.length; u++)
                    mobiles[u].calculateUserRateMSR();
                for (int u = 0; u < mobiles.length; u++)
                    mobiles[u].calculateDualVariablesMSR(seq);
                break;

            default:
                throw new RuntimeException("Unsupported maximize target: " + maximizeTarget);
            }

        }


        for (int m = 0; m < macros.length; m++)
            macros[m].count(state);
        for (int p = 0; p < picos.length; p++)
            picos[p].count(state);

        return state;

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
        this.isStatic = algorithm instanceof StaticAlgorithm;
    }

}
