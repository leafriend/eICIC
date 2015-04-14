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

    public Edge(T baseStation, Mobile mobile) {
        super();
        this.baseStation = baseStation;
        this.mobile = mobile;
        this.distance = sqrt(pow(baseStation.x - mobile.x, 2)
                + pow(baseStation.y - mobile.y, 2));
        this.channelGainFactor = baseStation.txPower
                * pow((1.0 / distance), PATH_LOSS_EXPO);

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
                    mobile.macroEdge.baseStation.mobiles.remove(mobile);
                }
                mobile.picoEdge = picoEdge;
                baseStation.edges.add(this);
                baseStation.mobiles.add(mobile);
            }
        }

    }

    public void generateChannelGain() {
        double rayleigh = sqrt(pow(RANDOM.nextGaussian() / 1.2533, 2)
                + pow(RANDOM.nextGaussian() / 1.2533, 2));
        double logNormal = pow(10, RANDOM.nextGaussian() * LN_SHAD * 0.1);
        for (int i = 0; i < NUM_RB; i++)
            channelGain[i] = channelGainFactor * rayleigh * logNormal;
    }

}
