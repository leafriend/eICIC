package net.folab.eicic.model;

import static java.lang.Math.pow;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
//import static java.util.Arrays.*;
import static java.util.Collections.sort;
import static net.folab.eicic.Constants.MACRO_INTERFERING_RANGE_ON_PICO;
import static net.folab.eicic.Constants.NUM_RB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Pico extends BaseStation<Pico> {

    final List<Macro> macrosInterfering = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private final List<Edge<Pico>>[] absEdges = new List[NUM_RB];

    @SuppressWarnings("unchecked")
    private final List<Edge<Pico>>[] nonEdges = new List[NUM_RB];

    private static class PicoEdgeComparator implements Comparator<Edge<Pico>> {

        private final int i;

        public PicoEdgeComparator(int i) {
            this.i = i;
        }

        @Override
        public int compare(Edge<Pico> a, Edge<Pico> b) {
            return (int) signum( //
            a.mobile.absPicoLambdaR[i] - b.mobile.absPicoLambdaR[i] //
            );
        }

    }

    private static final PicoEdgeComparator[] COMPARATORS = new PicoEdgeComparator[NUM_RB];
    static {
        for (int i = 0; i < COMPARATORS.length; i++) {
            COMPARATORS[i] = new PicoEdgeComparator(i);
        }
    }

    public Pico(int idx, double x, double y, double txPower) {
        super(idx, x, y, txPower);
    }

    public void init() {
        for (int i = 0; i < NUM_RB; i++) {
            absEdges[i] = new ArrayList<>(edges);
            nonEdges[i] = new ArrayList<>(edges);
        }
    }

    public void checkInterference(Macro macro) {
        double distance = sqrt(pow(macro.x - this.x, 2)
                + pow(macro.y - this.y, 2));
        if (distance < MACRO_INTERFERING_RANGE_ON_PICO)
            macrosInterfering.add(macro);
    }

    public boolean isAbs() {
        for (Macro macro : macrosInterfering) {
            if (macro.state)
                return false;
        }
        return false;
    }

    public void sortMobiles() {
        for (int i = 0; i < NUM_RB; i++) {
            sort(absEdges[i], COMPARATORS[i]);
        }
    }

    public int absIndexOf(int i, Mobile mobile) {
        return absEdges[i].indexOf(mobile.picoEdge);
    }

    public int nonIndexOf(int i, Mobile mobile) {
        return nonEdges[i].indexOf(mobile.picoEdge);
    }

    public List<Edge<Pico>>[] getAbsEdges() {
        return absEdges;
    }

    public List<Edge<Pico>>[] getNonEdges() {
        return nonEdges;
    }

}
