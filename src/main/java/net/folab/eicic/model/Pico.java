package net.folab.eicic.model;

import static java.lang.Math.pow;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
//import static java.util.Arrays.*;
import static java.util.Collections.sort;
import static net.folab.eicic.Constants.MACRO_INTERFERING_RANGE_ON_PICO;
import static net.folab.eicic.Constants.NUM_RB;
import static net.folab.eicic.Constants.forEachRbs;

import java.util.ArrayList;
import java.util.List;

public class Pico extends BaseStation<Pico> {

    final List<Macro> macrosInterfering = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private final List<Edge<Pico>>[] absEdges = new List[NUM_RB];

    @SuppressWarnings("unchecked")
    private final List<Edge<Pico>>[] nonEdges = new List[NUM_RB];

    public Pico(int idx, double x, double y, double txPower) {
        super(idx, x, y, txPower);
    }

    public void init() {
        forEachRbs(i -> {
            absEdges[i] = new ArrayList<>(edges);
            nonEdges[i] = new ArrayList<>(edges);
        });
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

    public void sortMobiles() {
        forEachRbs(i -> {
            sort(absEdges[i], (a, b) -> (int) signum( //
                    a.mobile.absPicoLambdaR[i] - b.mobile.absPicoLambdaR[i] //
                    ));
        });
    }

    public int absIndexOf(int i, Mobile mobile) {
        return absEdges[i].indexOf(mobile.picoEdge);
    }

    public int nonIndexOf(int i, Mobile mobile) {
        return nonEdges[i].indexOf(mobile.picoEdge);
    }

}
