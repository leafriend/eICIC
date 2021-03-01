package net.folab.eicic.model;

import static net.folab.eicic.model.Constants.NUM_MOBILES;
import static net.folab.eicic.model.Constants.NUM_RB;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseStation<T extends BaseStation<T>> {

    public final int idx;

    private final double x;

    private final double y;

    private double shiftX;

    private double shiftY;

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

    public double getChannel() {
        double channel = 0.0;
        for (int r = 0; r < activeEdges.length; r++) {
            Edge<T> edge = activeEdges[r];
            if (edge == null)
                continue;
            channel += edge.channelGain[r];
        }
        return channel;
    }

    public double getLambdaR() {
        double lambdaR = 0.0;
        for (int r = 0; r < activeEdges.length; r++) {
            Edge<T> edge = activeEdges[r];
            if (edge == null)
                continue;
            lambdaR += edge.channelGain[r] * edge.mobile.getLambda();
        }
        return lambdaR;
    }

    public double getX() {
        return x + shiftX;
    }

    public double getY() {
        return y + shiftY;
    }

    /**
     * @param parentMacro
     *            모바일의 매크로로서 가운데에 위치하는 매크로
     */
    public void shift(Macro parentMacro) {

        /**
         * 이 base station의 매크로로서 이동을 할 수도 있는 매크로; 하지 않을 수도 있다.
         */
        Macro selfMacro = getSelfMacro();

        switch (parentMacro.idx) {
        case 1:
            switch (selfMacro.idx) {
            case 3:
                shiftX = 2500.0;
                shiftY = -866.0;
                break;

            case 4:
                shiftX = 2500.0;
                shiftY = -866.0;
                break;

            case 5:
                shiftX = 2000.0;
                shiftY = 1732.0;
                break;

            default:
                shiftX = 0.0;
                shiftY = 0.0;
                break;
            }
            break;

        case 2:
            switch (selfMacro.idx) {
            case 4:
                shiftX = 2000.0;
                shiftY = 1732.0;
                break;

            case 5:
                shiftX = 2000.0;
                shiftY = 1732.0;
                break;

            case 6:
                shiftX = -500.0;
                shiftY = 2598.0;
                break;

            default:
                shiftX = 0.0;
                shiftY = 0.0;
                break;
            }
            break;

        case 3:
            switch (selfMacro.idx) {
            case 1:
                shiftX = -2500.0;
                shiftY = 866.0;
                break;

            case 5:
                shiftX = -500.0;
                shiftY = 2598.0;
                break;

            case 6:
                shiftX = -500.0;
                shiftY = 2598.0;
                break;

            default:
                shiftX = 0.0;
                shiftY = 0.0;
                break;
            }
            break;

        case 4:
            switch (selfMacro.idx) {
            case 1:
                shiftX = -2500.0;
                shiftY = 866.0;
                break;

            case 2:
                shiftX = -2000.0;
                shiftY = -1732.0;
                break;

            case 6:
                shiftX = -2500.0;
                shiftY = 866.0;
                break;

            default:
                shiftX = 0.0;
                shiftY = 0.0;
                break;
            }
            break;

        case 5:
            switch (selfMacro.idx) {
            case 1:
                shiftX = -2000.0;
                shiftY = -1732.0;
                break;

            case 2:
                shiftX = -2000.0;
                shiftY = -1732.0;
                break;

            case 3:
                shiftX = 500.0;
                shiftY = -2598.0;
                break;

            default:
                shiftX = 0.0;
                shiftY = 0.0;
                break;
            }
            break;

        case 6:
            switch (selfMacro.idx) {
            case 2:
                shiftX = 500.0;
                shiftY = -2598.0;
                break;

            case 3:
                shiftX = 500.0;
                shiftY = -2598.0;
                break;

            case 4:
                shiftX = 2500.0;
                shiftY = -866.0;
                break;

            default:
                shiftX = 0.0;
                shiftY = 0.0;
                break;
            }
            break;

        default:
            shiftX = 0.0;
            shiftY = 0.0;
            break;
        }

        // 매크로가 shift해야 할지 말지 판별

        // 매크로 shift

    }

    public abstract Macro getSelfMacro();

    public void revertShift() {
        shiftX = 0.0d;
        shiftY = 0.0d;
    }

}
