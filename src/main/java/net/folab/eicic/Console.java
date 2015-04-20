package net.folab.eicic;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;
import net.folab.eicic.ui.Controller;

public interface Console {

    void dump(int t, StateContext state, Macro[] macros, Pico[] picos,
            Mobile[] mobiles, long elapsed);

    void notifyStarted();

    void notifyEnded();

    void setTotalSeq(int totalSeq);

    void setAlgorithm(Algorithm algorithm);

    void setController(Controller controller);

}
