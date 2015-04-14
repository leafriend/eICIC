package net.folab.eicic.model;

public class Mobile {

    public final int idx;

    public final double x;

    public final double y;

    public final double qos;

    Edge<Macro> macroEdge;

    Edge<Pico> picoEdge;

    public Mobile(int idx, double x, double y, double qos) {
        super();
        this.idx = idx;
        this.x = x;
        this.y = y;
        this.qos = qos;
    }

}
