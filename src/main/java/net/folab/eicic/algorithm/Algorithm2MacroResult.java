package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_RB;

import java.util.List;

import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;

public class Algorithm2MacroResult {

    private final Algorithm2MacroStates states;

    private final Macro macro;

    private final boolean macroState;

    private final List<Mobile> mobiles;

    private final int size;

    private int mobileStatesCount;

    private final Algorithm2MobileResult[] results;

    double lambdaRSum;

    public Algorithm2MacroResult(Algorithm2MacroStates states,
            boolean macroState, Macro macro) {
        this.states = states;
        this.macro = macro;
        this.macroState = macroState;
        this.mobiles = macro.getMobiles();
        this.size = mobiles.size();
        this.mobileStatesCount = 1 << size;
        this.results = new Algorithm2MobileResult[1 << size];
    }

    public void run() {

        List<Mobile> mobilesTS = macro.getMobiles();

        if (macroState) {

            double mostLambdaR = Double.NEGATIVE_INFINITY;

            // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
            for (int cellAssoc = 1; cellAssoc < mobileStatesCount; cellAssoc++) {

                Algorithm2MobileResult result;
                if (results[cellAssoc] == null)
                    results[cellAssoc] = new Algorithm2MobileResult(
                            states.state, cellAssoc, macro, macroState);
                result = results[cellAssoc];
                result.run();

                if (mostLambdaR < result.lambdaRSum) {
                    mostLambdaR = result.lambdaRSum;
                    for (int u = 0; u < size; u++) {
                        Mobile mobile = mobilesTS.get(u);
                        for (int i = 0; i < NUM_RB; i++) {
                            states.edges[mobile.idx][i] = result.edges[mobile.idx][i];
                        }
                    }
                }

            }

            this.lambdaRSum = mostLambdaR;

        } else {

            Algorithm2MobileResult result;
            if (results[0] == null)
                results[0] = new Algorithm2MobileResult(states.state, 0, macro,
                        macroState);
            result = results[0];
            result.run();

            this.lambdaRSum = result.lambdaRSum;

        }

    }

}
