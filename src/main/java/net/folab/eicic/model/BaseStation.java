package net.folab.eicic.model;

import java.util.Set;

public abstract class BaseStation {

    public final int idx;

    public final double x;

    public final double y;

    public final double txPower;

    Set<Mobile> mobiles;

    public BaseStation(int idx, double x, double y, double txPower) {
        super();
        this.idx = idx;
        this.x = x;
        this.y = y;
        this.txPower = txPower;
    }

}
