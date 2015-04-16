package net.folab.eicic;

import java.util.List;

import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public interface Console {

    void dump(int t, List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles, long elapsed, long execute);

}
