package net.folab.eicic.algorithm;

import java.util.List;

import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public interface Algorithm {

    @Deprecated
    void calculate(List<Macro> macros, List<Pico> picos, List<Mobile> mobiles);

    void calculate(Macro[] macros, Pico[] picos, Mobile[] mobiles);

}
