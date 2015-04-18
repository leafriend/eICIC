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
    private final List<Edge<Pico>>[] sortedAbsEdges = new List[NUM_RB];

    @SuppressWarnings("unchecked")
    private final List<Edge<Pico>>[] sortedNonEdges = new List[NUM_RB];

    private static class PicoEdgeComparator implements Comparator<Edge<Pico>> {

        private boolean isAbs;

        private final int i;

        public PicoEdgeComparator(boolean isAbs, int i) {
            this.isAbs = isAbs;
            this.i = i;
        }

        @Override
        public int compare(Edge<Pico> a, Edge<Pico> b) {
            if (isAbs) {
                return (int) signum( //
                        b.mobile.absPicoLambdaR[i] - a.mobile.absPicoLambdaR[i] //
                        );
            } else {
                return (int) signum( //
                        b.mobile.nonPicoLambdaR[i] - a.mobile.nonPicoLambdaR[i] //
                        );
            }
        }

    }

    private static final PicoEdgeComparator[] ABS_COMPARATORS = new PicoEdgeComparator[NUM_RB];
    private static final PicoEdgeComparator[] NON_COMPARATORS = new PicoEdgeComparator[NUM_RB];
    static {
        for (int i = 0; i < NUM_RB; i++) {
            ABS_COMPARATORS[i] = new PicoEdgeComparator(true, i);
            NON_COMPARATORS[i] = new PicoEdgeComparator(false, i);
        }
    }

    public Pico(int idx, double x, double y, double txPower) {
        super(idx, x, y, txPower);
    }

    public void init() {
        for (int i = 0; i < NUM_RB; i++) {
            sortedAbsEdges[i] = new ArrayList<>(edges);
            sortedNonEdges[i] = new ArrayList<>(edges);
        }
    }

    public void checkInterference(Macro macro) {
        double distance = sqrt(pow(macro.x - this.x, 2)
                + pow(macro.y - this.y, 2));
        if (distance < MACRO_INTERFERING_RANGE_ON_PICO)
            macrosInterfering.add(macro);
    }

    private Boolean isAbs;

    public boolean isAbs() {
        if (isAbs == null) {
            for (Macro macro : macrosInterfering) {
                if (macro.state)
                    isAbs = Boolean.FALSE;
            }
            if (isAbs == null)
                isAbs = Boolean.TRUE;
        }
        return isAbs.booleanValue();
    }

    public void sortMobiles() {
        isAbs = null;
        for (int i = 0; i < NUM_RB; i++) {
            sort(sortedAbsEdges[i], ABS_COMPARATORS[i]);
            sort(sortedNonEdges[i], NON_COMPARATORS[i]);
        }
    }

    public int absIndexOf(int i, Mobile mobile) {
        return sortedAbsEdges[i].indexOf(mobile.picoEdge);
    }

    public int nonIndexOf(int i, Mobile mobile) {
        return sortedNonEdges[i].indexOf(mobile.picoEdge);
    }

    public List<Edge<Pico>>[] getSortedAbsEdges() {
        return sortedAbsEdges;
    }

    public List<Edge<Pico>>[] getSortedNonEdges() {
        return sortedNonEdges;
    }

}
