package net.folab.eicic.model;

public class Macro extends BaseStation<Macro> {

    public boolean state;

    public Macro(int idx, double x, double y, double txPower) {
        super(idx, x, y, txPower);
    }

    @Override
    public String toString() {
        return String.format("%s<%d@%.3f,%.3f>", (state ? getClass()
                .getSimpleName().toUpperCase() : getClass().getSimpleName()
                .toLowerCase()), idx, x, y);
    }

}
