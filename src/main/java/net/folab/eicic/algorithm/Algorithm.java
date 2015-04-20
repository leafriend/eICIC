package net.folab.eicic.algorithm;

import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public interface Algorithm {

    void calculate(Macro[] macros, Pico[] picos, Mobile[] mobiles);

}
