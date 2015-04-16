package net.folab.eicic.model;

import static java.lang.Math.signum;
import static java.util.Collections.sort;
import static net.folab.eicic.Constants.NUM_RB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Macro extends BaseStation<Macro> {

    private static class MacroEdgeComparator implements Comparator<Edge<Macro>> {

        private final int i;

        public MacroEdgeComparator(int i) {
            this.i = i;
        }

        @Override
        public int compare(Edge<Macro> a, Edge<Macro> b) {
            return (int) signum( //
            b.mobile.macroLambdaR[i] - a.mobile.macroLambdaR[i] //
            );
        }

    }

    private static final MacroEdgeComparator[] COMPARATORS = new MacroEdgeComparator[NUM_RB];
    static {
        for (int i = 0; i < COMPARATORS.length; i++) {
            COMPARATORS[i] = new MacroEdgeComparator(i);
        }
    }

    public boolean state;

    @SuppressWarnings("unchecked")
    private final List<Edge<Macro>>[] sortedEdges = new List[NUM_RB];;

    public Macro(int idx, double x, double y, double txPower) {
        super(idx, x, y, txPower);
    }

    @Override
    public String toString() {
        return String.format("%s<%d@%.3f,%.3f>", (state ? getClass()
                .getSimpleName().toUpperCase() : getClass().getSimpleName()
                .toLowerCase()), idx, x, y);
    }

    public void init() {
        for (int i = 0; i < NUM_RB; i++) {
            sortedEdges[i] = new ArrayList<>(edges);
        }
    }

    public void sortMobiles() {
        for (int i = 0; i < NUM_RB; i++) {
            sort(sortedEdges[i], COMPARATORS[i]);
        }
    }

    public List<Edge<Macro>>[] getSortedEdges() {
        return sortedEdges;
    }

}
