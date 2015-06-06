package net.folab.eicic.algorithm;

import static net.folab.eicic.model.Constants.NUM_MOBILES;
import net.folab.eicic.core.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class Algorithm3Revised implements Algorithm {

    private StateContext state;

    final boolean[] mobileConnectsMacro = new boolean[NUM_MOBILES];

    @Override
    public int getNumber() {
        return 3;
    }

    @Override
    public StateContext calculate(int seq, Macro[] macros, Pico[] picos,
            Mobile[] mobiles) {
        return StateContext.getStateContext(0, macros, picos, mobiles);
    }

}
