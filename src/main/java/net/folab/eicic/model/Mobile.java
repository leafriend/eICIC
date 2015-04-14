package net.folab.eicic.model;

import static net.folab.eicic.Constants.*;
import static java.lang.Math.*;

import java.util.HashSet;
import java.util.Set;

public class Mobile {

    public final int idx;

    public final double x;

    public final double y;

    public final double qos;

    private double lambda;

    private double mu;

    private double userRate;

    Edge<Macro> macroEdge;

    final Set<Edge<Macro>> allMacroEdges = new HashSet<>();

    final double[] macroDataRateInMegaBps = new double[NUM_RB];

    Edge<Pico> picoEdge;

    final Set<Edge<Pico>> allPicoEdges = new HashSet<>();

    final double[] absPicoDataRateInMegaBps = new double[NUM_RB];

    final double[] nonPicoDataRateInMegaBps = new double[NUM_RB];

    public Mobile(int idx, double x, double y, double qos) {
        super();
        this.idx = idx;
        this.x = x;
        this.y = y;
        this.qos = qos;
    }

    public Mobile(int idx, double x, double y, double qos, double lambda,
            double mu, double userRate) {
        this(idx, x, y, qos);
        this.lambda = lambda;
        this.mu = mu;
        this.userRate = userRate;
    }

    public void calculateDataRate() {

        for (int i = 0; i < NUM_RB; i++) {
            final int _i = i;

            double macroChannelGain = allMacroEdges.stream()
                    .map(edge -> edge.channelGain[_i]).reduce(0.0, Double::sum);

            double picoChannelGain = allPicoEdges.stream()
                    .map(edge -> edge.channelGain[_i]).reduce(0.0, Double::sum);

            macroDataRateInMegaBps[i] = calculateDataRate(BW_PER_RB,
                    macroEdge.channelGain[i], macroChannelGain
                            + picoChannelGain - macroEdge.channelGain[i]
                            + NOISE, MEGA);

            macroDataRateInMegaBps[i] = calculateDataRate(BW_PER_RB,
                    macroEdge.channelGain[i], //
                    macroChannelGain + picoChannelGain
                            - macroEdge.channelGain[i] + NOISE, //
                    MEGA);

            absPicoDataRateInMegaBps[i] = calculateDataRate(BW_PER_RB,
                    picoEdge.channelGain[i], //
                    /* macroChannelGain + */picoChannelGain
                            - macroEdge.channelGain[i] + NOISE, //
                    MEGA);

            nonPicoDataRateInMegaBps[i] = calculateDataRate(BW_PER_RB,
                    picoEdge.channelGain[i], //
                    macroChannelGain + picoChannelGain
                            - picoEdge.channelGain[i] + NOISE, //
                    MEGA);

        }

    }

    private double calculateDataRate(double bandwidth, double numerator,
            double denominator, double unit) {
        return bandwidth * log(1 + numerator / denominator) / unit;
    }
}
