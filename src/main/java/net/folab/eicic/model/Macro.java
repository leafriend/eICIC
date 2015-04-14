package net.folab.eicic.model;

public class Macro extends BaseStation<Macro> {

    public boolean state;

    public double pa3LambdaR;

    public Macro(int idx, double x, double y, double txPower) {
        super(idx, x, y, txPower);
    }

}
