package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_MACROS;
import static net.folab.eicic.Constants.NUM_MOBILES;
import static net.folab.eicic.Constants.NUM_RB;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;

public class Algorithm2MacroStates implements Runnable {

    final int macroState;

    final int[] cellAssocs = new int[NUM_MACROS];

    private final Algorithm2MacroResult[] algorithm2MacroResults = new Algorithm2MacroResult[NUM_MACROS];

    double lambdaRSum;

    boolean[] macroStates = new boolean[NUM_MACROS];

    Edge<?>[][] edges = new Edge[NUM_MOBILES][NUM_RB];

    public Macro[] macros;

    public boolean finished;

    public Algorithm2MacroStates(int macroState) {
        this.macroState = macroState;
        // Macro 상태(ON/OFF) 지정
        for (int m = 0; m < NUM_MACROS; m++)
            macroStates[m] = 1 == (((1 << m) & macroState) >> m);
    }

    public void run() {

        this.lambdaRSum = 0.0;

        for (int m = 0; m < NUM_MACROS; m++) {

            if (algorithm2MacroResults[m] == null)
                algorithm2MacroResults[m] = new Algorithm2MacroResult(this,
                        macroStates[m], macros[m]);

            algorithm2MacroResults[m].run();
            lambdaRSum += algorithm2MacroResults[m].lambdaRSum;

        }

        finished = true;

    }

}
