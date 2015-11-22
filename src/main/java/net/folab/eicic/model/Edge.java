package net.folab.eicic.model;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static net.folab.eicic.model.Constants.LN_SHAD;
import static net.folab.eicic.model.Constants.NUM_RB;
import static net.folab.eicic.model.Constants.PATH_LOSS_EXPO;

import java.util.Random;

public class Edge<T extends BaseStation<T>> {

    private static Random RANDOM = new Random(System.currentTimeMillis());

    public final T baseStation;

    public final Mobile mobile;

    public final double distance;

    private final double channelGainFactor;

    final double[] channelGain = new double[NUM_RB];

    private final boolean[] isActivated = new boolean[NUM_RB];

    public Edge(T baseStation, Mobile mobile) {
        super();
        this.baseStation = baseStation;
        this.mobile = mobile;
        this.distance = sqrt(pow(baseStation.getX() - mobile.x, 2)
                + pow(baseStation.getY() - mobile.y, 2));
        this.channelGainFactor = baseStation.txPower
                * pow((1.0 / distance), PATH_LOSS_EXPO);

        baseStation.edgesInterfered.add(this);

        if (baseStation instanceof Macro) {
            @SuppressWarnings("unchecked")
            Edge<Macro> macroEdge = (Edge<Macro>) this;
            mobile.allMacroEdges.add(macroEdge);

            if (mobile.macroEdge == null
                    || distance < mobile.macroEdge.distance) {
                if (mobile.macroEdge != null) {
                    mobile.macroEdge.baseStation.edges.remove(mobile.macroEdge);
                    mobile.macroEdge.baseStation.mobiles.remove(mobile);
                }
                mobile.macroEdge = macroEdge;
                baseStation.edges.add(this);
                baseStation.mobiles.add(mobile);
            }
        } else if (baseStation instanceof Pico) {
            @SuppressWarnings("unchecked")
            Edge<Pico> picoEdge = (Edge<Pico>) this;
            mobile.allPicoEdges.add(picoEdge);

            if (mobile.picoEdge == null
                    || distance < mobile.picoEdge.distance) {
                if (mobile.picoEdge != null) {
                    mobile.picoEdge.baseStation.edges.remove(mobile.picoEdge);
                    mobile.picoEdge.baseStation.mobiles.remove(mobile);
                }
                mobile.picoEdge = picoEdge;
                baseStation.edges.add(this);
                baseStation.mobiles.add(mobile);
            }
        }

    }

    public void initialize() {
        for (int i = 0; i < NUM_RB; i++) {

            // generateChannelGain
            double rayleigh = sqrt(pow(RANDOM.nextGaussian() / 1.2533, 2)
                    + pow(RANDOM.nextGaussian() / 1.2533, 2));
            double logNormal = pow(10, RANDOM.nextGaussian() * LN_SHAD * 0.1);
            channelGain[i] = channelGainFactor * rayleigh * logNormal;
            // channelGain[i] = channelGainFactor;

            // setActivated
            setActivated(i, false);

        }
    }

    public boolean isActivated(int i) {
        return isActivated[i];
    }

    public void setActivated(int r, boolean isActivated) {
        if (isActivated) {
            Edge<T> activeBaseStationEdge = baseStation.activeEdges[r];
            Edge<? extends BaseStation<?>> activeMobileEdge = mobile.activeEdges[r];
            assert activeBaseStationEdge == activeMobileEdge : activeBaseStationEdge
                    + " != " + activeMobileEdge + " for " + this + "[" + r
                    + "]: " + isActivated;
            if (activeBaseStationEdge != null)
                activeBaseStationEdge.isActivated[r] = false;
            if (activeMobileEdge != null)
                activeMobileEdge.isActivated[r] = false;
            baseStation.activeEdges[r] = this;
            mobile.activeEdges[r] = this;
        } else {
            if (this.isActivated[r]) {
                assert baseStation.activeEdges[r] == null
                        || baseStation.activeEdges[r] == this;
                assert mobile.activeEdges[r] == null
                        || mobile.activeEdges[r] == this;
                baseStation.activeEdges[r] = null;
                mobile.activeEdges[r] = null;
            }
        }
        this.isActivated[r] = isActivated;
    }

    @Override
    public String toString() {
        return "Edge<" + baseStation + "-" + mobile + ">";
    }

    public static void setRandom(Random random) {
        assert random != null;
        RANDOM = random;
    }

}
