package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_MACROS;
import static net.folab.eicic.Constants.NUM_MOBILES;
import static net.folab.eicic.Constants.NUM_RB;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.StateContext;

public class Algorithm2MacroStates implements Runnable {

    final StateContext state;

    final int[] cellAssocs = new int[NUM_MACROS];

    private final Algorithm2MacroResult[] algorithm2MacroResults = new Algorithm2MacroResult[NUM_MACROS];

    double lambdaRSum;

    Edge<?>[][] edges = new Edge[NUM_MOBILES][NUM_RB];

    public Macro[] macros;

    public boolean finished;

    public Algorithm2MacroStates(StateContext state) {
        this.state = state;
    }

    public void run() {

        this.lambdaRSum = 0.0;

        for (int m = 0; m < NUM_MACROS; m++) {

            if (algorithm2MacroResults[m] == null)
                algorithm2MacroResults[m] = new Algorithm2MacroResult(this,
                        state.macroIsOn(m), macros[m]);

            algorithm2MacroResults[m].run();
            lambdaRSum += algorithm2MacroResults[m].lambdaRSum;

        }

        finished = true;

    }

}
