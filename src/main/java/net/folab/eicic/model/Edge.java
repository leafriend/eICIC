package net.folab.eicic.model;

import static java.lang.Math.*;
import static net.folab.eicic.Constants.*;

import java.util.Random;

public class Edge<T extends BaseStation<T>> {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

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
        this.distance = sqrt(pow(baseStation.x - mobile.x, 2)
                + pow(baseStation.y - mobile.y, 2));
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

            if (mobile.picoEdge == null || distance < mobile.picoEdge.distance) {
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

    public void generateChannelGain() {
        for (int i = 0; i < NUM_RB; i++) {
            double rayleigh = sqrt(pow(RANDOM.nextGaussian() / 1.2533, 2)
                    + pow(RANDOM.nextGaussian() / 1.2533, 2));
            double logNormal = pow(10, RANDOM.nextGaussian() * LN_SHAD * 0.1);
            channelGain[i] = channelGainFactor * rayleigh * logNormal;
        }
    }

    public boolean isActivated(int i) {
        return isActivated[i];
    }

    @SuppressWarnings("unchecked")
    public void setActivated(int i, boolean isActivated) {
        this.isActivated[i] = isActivated;
        if (isActivated) {
            Edge<T> activeBaseStationEdge = baseStation.activeEdges[i];
            Edge<T> activeMobileEdge = null;
            if (baseStation instanceof Macro) {
                activeMobileEdge = (Edge<T>) mobile.activeMacroEdges[i];
            } else if (baseStation instanceof Macro) {
                activeMobileEdge = (Edge<T>) mobile.activePicoEdges[i];
            }
            assert activeBaseStationEdge == activeMobileEdge;

            if (activeBaseStationEdge != null) {
                activeBaseStationEdge.isActivated[i] = false;
            }
            baseStation.activeEdges[i] = this;
            if (baseStation instanceof Macro) {
                mobile.activeMacroEdges[i] = (Edge<Macro>) this;
            } else if (baseStation instanceof Macro) {
                mobile.activePicoEdges[i] = (Edge<Pico>) this;
            }
        } else {
            assert baseStation.activeEdges[i] == this;
            baseStation.activeEdges[i] = null;
            if (baseStation instanceof Macro) {
                mobile.activeMacroEdges[i] = null;
            } else if (baseStation instanceof Macro) {
                mobile.activePicoEdges[i] = null;
            }
        }
    }

    @Override
    public String toString() {
        return "Edge<" + baseStation + "-" + mobile + ">";
    }

}
