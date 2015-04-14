package net.folab.eicic.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class BaseStation<T extends BaseStation<T>> {

    public final int idx;

    public final double x;

    public final double y;

    public final double txPower;

    final Set<Edge<T>> edges = new HashSet<>();

    final Set<Mobile> mobiles = new HashSet<>();

    // PA3

    public double pa3LambdaR = 1.0;

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

    public void forEachMobiles(Consumer<Mobile> action) {
        mobiles.forEach(action);
    }

}
