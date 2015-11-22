package net.folab.eicic.model;

import static java.lang.Math.signum;
import static java.util.Collections.sort;
import static net.folab.eicic.model.Constants.NUM_RB;

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

    private final List<Pico> childPicos = new ArrayList<>();

    private final List<Mobile> childMobiles = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private final List<Edge<Macro>>[] sortedEdges = new List[NUM_RB];

    private int allocationCount;;

    public Macro(int idx, double x, double y, double txPower) {
        super(idx, x, y, txPower);
    }

    public void init() {
        for (int i = 0; i < NUM_RB; i++) {
            sortedEdges[i] = new ArrayList<>(edges);
        }
    }

    public void registerChildPico(Pico pico) {
        if (pico.parentMacro != this)
            throw new RuntimeException(
                    "Pico tried to register as child to Macro which is not parent");
        childPicos.add(pico);
    }

    public void registerChildMobile(Mobile mobile) {
        if (mobile.parentMacro != this)
            throw new RuntimeException(
                    "Mobile tried to register as child to Macro which is not parent");
        childMobiles.add(mobile);
    }

    public void sortMobiles() {
        for (int i = 0; i < NUM_RB; i++) {
            sort(sortedEdges[i], COMPARATORS[i]);
        }
    }

    public List<Edge<Macro>>[] getSortedEdges() {
        return sortedEdges;
    }

    public void count(StateContext state) {
        if (state.macroIsOn(idx))
            allocationCount++;
    }

    public int getAllocationCount() {
        return allocationCount;
    }

}
