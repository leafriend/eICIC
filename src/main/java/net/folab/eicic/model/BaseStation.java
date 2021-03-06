package net.folab.eicic.model;

import static net.folab.eicic.model.Constants.NUM_MOBILES;
import static net.folab.eicic.model.Constants.NUM_RB;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseStation<T extends BaseStation<T>> {

    public final int idx;

    public final double x;

    public final double y;

    public final double txPower;

    public final List<Edge<T>> edges = new ArrayList<>();

    @SuppressWarnings("unchecked")
    final Edge<T>[] activeEdges = new Edge[NUM_RB];

    final List<Edge<T>> edgesInterfered = new ArrayList<>();

    final List<Mobile> mobiles = new ArrayList<>();

    // PA3

    public double pa3LambdaR = 1.0;

    public final double[] pa3MobileLambdaR = new double[NUM_MOBILES];

    public BaseStation(int idx, double x, double y, double txPower) {
        super();
        this.idx = idx;
        this.x = x;
        this.y = y;
        this.txPower = txPower;

        for (int i = 0; i < NUM_MOBILES; i++)
            pa3MobileLambdaR[i] = 1.0;
    }

    public void initializeEdges() {
        for (Edge<T> edge : edgesInterfered) {
            edge.initialize();
        }
    }

    public List<Mobile> getMobiles() {
        return mobiles;
    }

    @Override
    public String toString() {
        return String.format("%s<%d>", getClass().getSimpleName(), idx);
    }

    public Edge<T>[] getActiveEdges() {
        return activeEdges;
    }

}
