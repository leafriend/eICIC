package net.folab.eicic.model;

import static net.folab.eicic.Constants.*;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseStation<T extends BaseStation<T>> {

    public final int idx;

    public final double x;

    public final double y;

    public final double txPower;

    final List<Edge<T>> edges = new ArrayList<>();

    @SuppressWarnings("unchecked")
    final Edge<T>[] activeEdges = new Edge[NUM_RB];

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
        for (Edge<T> edge : edgesInterfered) {
            edge.generateChannelGain();
        }
    }

    public List<Edge<T>> getEdges() {
        return edges;
    }

    @Deprecated
    public List<Mobile> getMobiles() {
        return mobiles;
    }

    @Override
    public String toString() {
        return String.format("%s<%d@%.3f,%.3f>", getClass().getSimpleName(),
                idx, x, y);
    }

}
