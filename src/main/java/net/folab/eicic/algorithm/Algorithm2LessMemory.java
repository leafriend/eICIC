package net.folab.eicic.algorithm;

import static net.folab.eicic.model.Constants.NUM_MACROS;
import static net.folab.eicic.model.Constants.NUM_MOBILES;
import static net.folab.eicic.model.Constants.NUM_RB;
import net.folab.eicic.core.Algorithm;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class Algorithm2LessMemory implements Algorithm {

    private static final int NUM_MACRO_STATES = 1 << NUM_MACROS;

    private StateContext state;

    private Edge<?>[][] bestEdges = new Edge[NUM_MOBILES][NUM_RB];

    @Override
    public StateContext calculate(int seq, Macro[] macros, Pico[] picos,
            Mobile[] mobiles) {

        int bestMacroState = -1;
        double mostLambdaRSum = Double.NEGATIVE_INFINITY;
        for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
            Algorithm2MacroStates result = new Algorithm2MacroStates(
                    StateContext.getStateContext(macroState, macros, picos,
                            mobiles));
            result.macros = macros;
            result.run();

            if (result.lambdaRSum > mostLambdaRSum) {
                bestMacroState = macroState;
                mostLambdaRSum = result.lambdaRSum;
                this.state = result.state;
                for (int u = 0; u < mobiles.length; u++)
                    for (int i = 0; i < NUM_RB; i++) {
                        bestEdges[u][i] = result.edges[u][i];
                    }
            }
        }

        for (int u = 0; u < mobiles.length; u++)
            for (int i = 0; i < NUM_RB; i++)
                if (bestEdges[u][i] != null)
                    bestEdges[u][i].setActivated(i, true);

        assert bestMacroState >= 0;

        return state;

    }

}
