package net.folab.eicic.model;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static net.folab.eicic.Constants.*;

import java.util.HashSet;
import java.util.Set;

public class Pico extends BaseStation<Pico> {

    public double pa3LambdaR;

    final Set<Macro> macrosInterfering = new HashSet<>();

    public Pico(int idx, double x, double y, double txPower) {
        super(idx, x, y, txPower);
    }

    public void checkInterference(Macro macro) {
        double distance = sqrt(pow(macro.x - this.x, 2)
                + pow(macro.y - this.y, 2));
        if (distance < MACRO_INTERFERING_RANGE_ON_PICO)
            macrosInterfering.add(macro);
    }

    public boolean isAbs() {
        return macrosInterfering.stream().map(macro -> macro.state)
                .reduce(false, Boolean::logicalOr);
    }

}
