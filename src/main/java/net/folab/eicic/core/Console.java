package net.folab.eicic.core;

import static java.lang.String.format;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public interface Console {

    void dump(int t, StateContext state, Macro[] macros, Pico[] picos,
            Mobile[] mobiles, long elapsed);

    void notifyStarted();

    void notifyPaused();

    void notifyEnded();

    void setTotalSeq(int totalSeq);

    void setAlgorithm(Algorithm algorithm);

    static String milisToTimeString(final long elapsed) {
        long sec = elapsed / 1000;
        // long mil = elapsed - sec * 1000;

        long min = sec / 60;
        sec -= min * 60;

        long hour = min / 60;
        min -= hour * 60;

        String format = format("%02d:%02d:%02d", hour, min, sec);
        return format;
    }

}
