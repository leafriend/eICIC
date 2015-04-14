package net.folab.eicic.model;

import static java.lang.Math.*;
import static net.folab.eicic.Constants.*;

public class Edge<T extends BaseStation> {

    public final T baseStation;

    public final Mobile mobile;

    public final double distance;

    private final double channelGainFactor;

    public Edge(T baseStation, Mobile mobile) {
        super();
        this.baseStation = baseStation;
        this.mobile = mobile;
        this.distance = sqrt(pow(baseStation.x - mobile.x, 2)
                + pow(baseStation.y - mobile.y, 2));
        this.channelGainFactor = baseStation.txPower
                * pow((1.0 / distance), PATH_LOSS_EXPO);

        if (baseStation instanceof Macro) {
            if (mobile.macroEdge == null
                    || distance < mobile.macroEdge.distance) {
                if (mobile.macroEdge != null) {
                    mobile.macroEdge.baseStation.mobiles
                            .remove(mobile.macroEdge);
                }
                @SuppressWarnings("unchecked")
                Edge<Macro> macroEdge = (Edge<Macro>) this;
                mobile.macroEdge = macroEdge;
            }
        } else if (baseStation instanceof Pico) {
            if (mobile.picoEdge == null || distance < mobile.picoEdge.distance) {
                if (mobile.picoEdge != null) {
                    mobile.picoEdge.baseStation.mobiles
                            .remove(mobile.picoEdge);
                }
                @SuppressWarnings("unchecked")
                Edge<Pico> picoEdge = (Edge<Pico>) this;
                mobile.picoEdge = picoEdge;
            }
        }

    }
}
