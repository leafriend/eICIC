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
            if (mobile.macroEdge == null || distance < mobile.macroEdge.distance) {
                @SuppressWarnings("unchecked")
                Edge<Macro> macro = (Edge<Macro>) this;
                mobile.macroEdge = macro;
            }
        }
    }
}
