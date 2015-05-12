package net.folab.eicic.core;

import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public interface Algorithm {

    int getNumber();

    StateContext calculate(Macro[] macros, Pico[] picos, Mobile[] mobiles);

    default void setUp(Macro[] macros, Pico[] picos, Mobile[] mobiles) {
        // DO NOTHING
    }

    default void tearDown(Macro[] macros, Pico[] picos, Mobile[] mobiles) {
        // DO NOTHING
    }

}
