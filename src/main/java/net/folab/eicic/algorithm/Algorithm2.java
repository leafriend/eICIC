package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_MACROS;
import static net.folab.eicic.Constants.NUM_MOBILES;
import static net.folab.eicic.Constants.NUM_RB;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class Algorithm2 implements Algorithm {

    private static final int NUM_MACRO_STATES = 1 << NUM_MACROS;

    public static final ExecutorService executor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private StateContext state;

    private Edge<?>[][] bestEdges = new Edge[NUM_MOBILES][NUM_RB];

    private Algorithm2MacroStates[] macroStateResults = new Algorithm2MacroStates[NUM_MACRO_STATES];

    public static void delay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public int getNumber() {
        return 2;
    }

    @Override
    public StateContext calculate(Macro[] macros, Pico[] picos, Mobile[] mobiles) {

        for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
            if (macroStateResults[macroState] == null)
                macroStateResults[macroState] = new Algorithm2MacroStates(
                        StateContext.getStateContext(macroState, macros, picos,
                                mobiles));
            macroStateResults[macroState].finished = false;
        }

        // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
        for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
            macroStateResults[macroState].macros = macros;
            executor.execute(macroStateResults[macroState]);
        }

        waiting: do {
            for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
                if (!macroStateResults[macroState].finished) {
                    delay(0);
                    continue waiting;
                }
            }
            break;
        } while (true);

        int bestMacroState = -1;
        double mostLambdaRSum = Double.NEGATIVE_INFINITY;
        for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
            Algorithm2MacroStates result = macroStateResults[macroState];
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
