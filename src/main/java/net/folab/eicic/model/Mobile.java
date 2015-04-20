package net.folab.eicic.model;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static net.folab.eicic.Constants.BW_PER_RB;
import static net.folab.eicic.Constants.MEGA;
import static net.folab.eicic.Constants.NOISE;
import static net.folab.eicic.Constants.NUM_RB;
import static net.folab.eicic.Constants.RATE_MAX;
import static net.folab.eicic.Constants.STEPSIZE2;
import static net.folab.eicic.Constants.STEPSIZE3;
import static net.folab.eicic.Constants.STEPSIZE4;

import java.util.ArrayList;
import java.util.List;

public class Mobile {

    public final int idx;

    public final double x;

    public final double y;

    public final double qos;

    private double instantRate;

    private double throughput;

    private double userRate;

    private double lambda;

    private double mu;

    @SuppressWarnings("unchecked")
    final Edge<? extends BaseStation<?>>[] activeEdges = new Edge[NUM_RB];

    Edge<Macro> macroEdge;

    final List<Edge<Macro>> allMacroEdges = new ArrayList<>();

    final double[] macroDataRate = new double[NUM_RB];

    final double[] macroLambdaR = new double[NUM_RB];

    Edge<Pico> picoEdge;

    final List<Edge<Pico>> allPicoEdges = new ArrayList<>();

    final double[] absPicoDataRate = new double[NUM_RB];

    final double[] absPicoLambdaR = new double[NUM_RB];

    final double[] nonPicoDataRate = new double[NUM_RB];

    final double[] nonPicoLambdaR = new double[NUM_RB];

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

            double macroChannelGain = 0.0;
            for (Edge<Macro> edge : allMacroEdges)
                macroChannelGain += edge.channelGain[_i];

            double picoChannelGain = 0.0;
            for (Edge<Pico> edge : allPicoEdges)
                picoChannelGain += edge.channelGain[_i];

            macroDataRate[i] = calculateDataRate(BW_PER_RB,
                    macroEdge.channelGain[i], //
                    macroChannelGain + picoChannelGain
                            - macroEdge.channelGain[i] + NOISE)
                    / MEGA;
            macroLambdaR[i] = lambda * macroDataRate[i];

            absPicoDataRate[i] = calculateDataRate(BW_PER_RB,
                    picoEdge.channelGain[i], //
                    /* macroChannelGain + */picoChannelGain
                            - picoEdge.channelGain[i] + NOISE)
                    / MEGA;
            ;
            absPicoLambdaR[i] = lambda * absPicoDataRate[i];

            nonPicoDataRate[i] = calculateDataRate(BW_PER_RB,
                    picoEdge.channelGain[i], //
                    macroChannelGain + picoChannelGain
                            - picoEdge.channelGain[i] + NOISE)
                    / MEGA;
            ;
            nonPicoLambdaR[i] = lambda * nonPicoDataRate[i];

        }

    }

    private double calculateDataRate(double bandwidth, double numerator,
            double denominator) {
        return bandwidth * log(1 + numerator / denominator);
    }

    public void calculateThroughput(StateContext state) {
        instantRate = 0.0;
        for (int i = 0; i < NUM_RB; i++) {
            Edge<?> edge = activeEdges[i];
            if (edge != null) {
                assert edge.isActivated(i);
                if (edge.baseStation instanceof Macro) {
                    instantRate += macroDataRate[i];

                } else if (edge.baseStation instanceof Pico) {
                    if (state.picoIsAbs(((Pico) edge.baseStation).idx)) {
                        instantRate += absPicoDataRate[i];
                    } else {
                        instantRate += nonPicoDataRate[i];
                    }

                }
            }
        }
        throughput += instantRate;
    }

    public void calculateUserRate() {
        if (lambda == 0.0)
            userRate = RATE_MAX;
        else
            userRate = 0.8 * userRate + 0.2 * (1.0 + mu) / lambda;
    }

    public void calculateDualVariables(int t) {
        final double step_size = 1.0 / ((double) t);
        final double step_size2 = (t > 100000) ? STEPSIZE4
                : ((t < 10000) ? STEPSIZE2 : STEPSIZE3);

        final double lambda;
        if ((abs(throughput / t - userRate) * this.lambda < 0.05))
            lambda = this.lambda - step_size * (instantRate - userRate);
        else
            lambda = this.lambda - step_size2 * (instantRate - userRate);
        this.lambda = lambda > 0 ? lambda : 0.0;

        final double mu;
        if ((abs(log(userRate) - qos) * this.mu < 0.01))
            mu = this.mu - step_size * (log(userRate) - qos);
        else
            mu = this.mu - step_size2 * (log(userRate) - qos);
        this.mu = (0.0 > mu) ? 0.0 : mu;
    }

    @Override
    public String toString() {
        return String.format("Mobile<%d>", idx);
    }

    /* bean getter/setter *************************************************** */

    public Macro getMacro() {
        return macroEdge.baseStation;
    }

    public Edge<Macro> getMacroEdge() {
        return macroEdge;
    }

    public Pico getPico() {
        return picoEdge.baseStation;
    }

    public Edge<Pico> getPicoEdge() {
        return picoEdge;
    }

    public double[] getMacroLambdaR() {
        return macroLambdaR;
    }

    public double[] getAbsPicoLambdaR() {
        return absPicoLambdaR;
    }

    public double[] getNonPicoLambdaR() {
        return nonPicoLambdaR;
    }

    public double getThroughput() {
        return throughput;
    }

    public double getUserRate() {
        return userRate;
    }

    public double getLambda() {
        return lambda;
    }

    public double getMu() {
        return mu;
    }

    public Edge<? extends BaseStation<?>>[] getActiveEdges() {
        return activeEdges;
    }

}
