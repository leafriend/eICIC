package net.folab.eicic.model;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseStation<T extends BaseStation<T>> {

    public final int idx;

    public final double x;

    public final double y;

    public final double txPower;

    final Set<Edge<T>> edges = new HashSet<>();

    public BaseStation(int idx, double x, double y, double txPower) {
        super();
        this.idx = idx;
        this.x = x;
        this.y = y;
        this.txPower = txPower;
    }

    public void generateChannelGain() {
        edges.forEach(edge -> edge.generateChannelGain());
    }

}
