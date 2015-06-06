package net.folab.eicic.algorithm;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;

public class Algorithm3Revised2 extends Algorithm3 {

    @Override
    public int getNumber() {
        return 5;
    }

    @Override
    public void chooseMobileConnection(Mobile[] mobiles) {

        mob: for (Mobile mobile : mobiles) {

            for (Edge<Macro> edge : mobile.getMacro().getActiveEdges()) {
                if (edge.mobile.equals(mobile)) {
                    mobileConnectsMacro[mobile.idx] = true;
                    continue mob;
                }
            }
            mobileConnectsMacro[mobile.idx] = false;

        }

    }

}
