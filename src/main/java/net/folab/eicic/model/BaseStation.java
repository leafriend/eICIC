package net.folab.eicic.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseStation<T extends BaseStation<T>> {

    public final int idx;

    public final double x;

    public final double y;

    public final double txPower;

    final List<Edge<T>> edges = new ArrayList<>();

    final List<Edge<T>> edgesInterfered = new ArrayList<>();

    final List<Mobile> mobiles = new ArrayList<>();

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
        edgesInterfered.forEach(edge -> edge.generateChannelGain());
    }

    public void forEachMobiles(Consumer<Mobile> action) {
        mobiles.forEach(action);
    }

    @Override
    public String toString() {
        return String.format("%s<%d@%.3f,%.3f>", getClass().getSimpleName(), idx, x, y);
    }

}
