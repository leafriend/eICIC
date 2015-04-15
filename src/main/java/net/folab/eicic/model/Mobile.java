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
import java.util.Iterator;
import java.util.List;

public class Mobile {

    public final int idx;

    public final double x;

    public final double y;

    public final double qos;

    public ConnectionState[] connectionStates = new ConnectionState[NUM_RB];

    private double instantRate;

    private double throughput;

    private double userRate;

    private double lambda;

    private double mu;

    Edge<Macro> macroEdge;

    final List<Edge<Macro>> allMacroEdges = new ArrayList<>();

    final double[] macroDataRateInMegaBps = new double[NUM_RB];

    final double[] macroLambdaR = new double[NUM_RB];

    Edge<Pico> picoEdge;

    final List<Edge<Pico>> allPicoEdges = new ArrayList<>();

    final double[] absPicoDataRateInMegaBps = new double[NUM_RB];

    final double[] absPicoLambdaR = new double[NUM_RB];

    final double[] nonPicoDataRateInMegaBps = new double[NUM_RB];

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
            for (Iterator<Edge<Macro>> ei = allMacroEdges.iterator(); ei.hasNext();) {
                Edge<Macro> edge = ei.next();
                macroChannelGain += edge.channelGain[_i];
            }

            double picoChannelGain = 0.0;
            for (Iterator<Edge<Pico>> ei = allPicoEdges.iterator(); ei.hasNext();) {
                Edge<Pico> edge = ei.next();
                picoChannelGain += edge.channelGain[_i];
            }

            macroDataRateInMegaBps[i] = calculateDataRate(BW_PER_RB,
                    macroEdge.channelGain[i], //
                    macroChannelGain + picoChannelGain
                            - macroEdge.channelGain[i] + NOISE, //
                    MEGA);
            macroLambdaR[i] = lambda * macroDataRateInMegaBps[i];

            absPicoDataRateInMegaBps[i] = calculateDataRate(BW_PER_RB,
                    picoEdge.channelGain[i], //
                    /* macroChannelGain + */picoChannelGain
                            - picoEdge.channelGain[i] + NOISE, //
                    MEGA);
            absPicoLambdaR[i] = lambda * absPicoDataRateInMegaBps[i];

            nonPicoDataRateInMegaBps[i] = calculateDataRate(BW_PER_RB,
                    picoEdge.channelGain[i], //
                    macroChannelGain + picoChannelGain
                            - picoEdge.channelGain[i] + NOISE, //
                    MEGA);
            nonPicoLambdaR[i] = lambda * nonPicoDataRateInMegaBps[i];

        }

    }

    private double calculateDataRate(double bandwidth, double numerator,
            double denominator, double unit) {
        return bandwidth * log(1 + numerator / denominator) / unit;
    }

    public void calculateThroughput() {
        instantRate = 0.0;
        for (int i = 0; i < NUM_RB; i++) {
            switch (connectionStates[i]) {
            case NOTHING:
                // instantRate += 0.0;
                break;
            case MACRO:
                instantRate += macroDataRateInMegaBps[i];
                break;
            case ABS_PICO:
                instantRate += absPicoDataRateInMegaBps[i];
                break;
            case NON_PICO:
                instantRate += nonPicoDataRateInMegaBps[i];
                break;
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
        return String.format("Mobile<%d@%.3f,%.3f>", idx, x, y);
    }

    /* bean getter/setter *************************************************** */

    public Macro getMacro() {
        return macroEdge.baseStation;
    }

    public Pico getPico() {
        return picoEdge.baseStation;
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

}
