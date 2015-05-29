package net.folab.eicic.algorithm;

import static net.folab.eicic.model.Constants.*;
import static java.lang.Math.*;
import net.folab.eicic.core.Algorithm;
import net.folab.eicic.model.BaseStation;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class StaticAlgorithm implements Algorithm {

    private double absNumerator = 20;

    private double absDenominator = 100;

    private double creBias = 1;

    @Override
    public int getNumber() {
        return 0;
    }

    @Override
    public void setUp(Macro[] macros, Pico[] picos, Mobile[] mobiles) {
        for (int u = 0; u < mobiles.length; u++) {
            Mobile mobile = mobiles[u];
            Edge<Macro> macroEdge = mobile.getMacroEdge();
            Edge<Pico> picoEdge = mobile.getPicoEdge();
            double macroPower = macroEdge.baseStation.txPower
                    / pow(macroEdge.distance, 4);
            double picoPower = picoEdge.baseStation.txPower
                    / pow(picoEdge.distance, 4);
            if (macroPower <= creBias * picoPower)
                mobile.saConnectToMacro = false;
            else
                mobile.saConnectToMacro = true;
        }
    }

    @Override
    public StateContext calculate(int seq, Macro[] macros, Pico[] picos,
            Mobile[] mobiles) {

        boolean isAbs = (seq % absDenominator) < absNumerator;

        int macroState = 0;
        if (!isAbs) {
            for (int m = 0; m < macros.length; m++) {
                macroState = macroState | (1 << m);
            }
        }

        for (int m = 0; m < mobiles.length; m++) {
            Mobile mobile = mobiles[m];
            for (int r = 0; r < NUM_RB; r++) {
                Edge<? extends BaseStation<?>> edge;
                if (mobile.saConnectToMacro) {
                    if (isAbs) {
                        edge = null;
                    } else {
                        if (mobile.getMacro().getSortedEdges()[r].get(0) == mobile
                                .getMacroEdge()) {
                            edge = mobile.getMacroEdge();
                        } else {
                            edge = null;
                        }
                    }
                } else {
                    if (isAbs) {
                        if (mobile.getPico().getSortedAbsEdges()[r].get(0) == mobile
                                .getPicoEdge()) {
                            edge = mobile.getPicoEdge();
                        } else {
                            edge = null;
                        }
                    } else {
                        if (mobile.getPico().getSortedNonEdges()[r].get(0) == mobile
                                .getPicoEdge()) {
                            edge = mobile.getPicoEdge();
                        } else {
                            edge = null;
                        }
                    }
                }
                if (edge != null)
                    edge.setActivated(r, true);
                mobile.getActiveEdges()[r] = edge;
            }
        }

        StateContext state = StateContext.getStateContext(macroState, macros,
                picos, mobiles);
        return state;
    }

    public double getAbsNumerator() {
        return absNumerator;
    }

    public void setAbsNumerator(double absNumerator) {
        this.absNumerator = absNumerator;
    }

    public double getAbsDenominator() {
        return absDenominator;
    }

    public void setAbsDenominator(double absDenominator) {
        this.absDenominator = absDenominator;
    }

    public double getCreBias() {
        return creBias;
    }

    public void setCreBias(double creBias) {
        this.creBias = creBias;
    }

}
