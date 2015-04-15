package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.*;

import java.util.List;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Algorithm3 implements Algorithm {

    @Override
    public void calculate(List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles) {

        for (Macro macro : macros)
            for (Edge<Macro> edge : macro.edges)
                for (int i = 0; i < NUM_RB; i++)
                    edge.setActivated(i, false);

        for (Pico pico : picos)
            for (Edge<Pico> edge : pico.edges)
                for (int i = 0; i < NUM_RB; i++)
                    edge.setActivated(i, false);

        boolean[] bestMacroStates = new boolean[NUM_MACROS];
        Edge<?>[][] bestEdges = new Edge[NUM_MOBILES][NUM_RB];

        final boolean[] mobileConnectsMacro = chooseMobileConnection(mobiles);

        double mostLambdaRSum = Double.NEGATIVE_INFINITY;
        // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
        int macroStatesCount = 1 << NUM_MACROS;
        for (int mask = 1; mask < macroStatesCount; mask++) {

            boolean[] macroStates = new boolean[NUM_MACROS];
            // Macro 상태(ON/OFF) 지정
            for (int m = 0; m < NUM_MACROS; m++)
                macroStates[m] = 1 == (((1 << m) & mask) >> m);

            Edge<?>[][] edges = new Edge[NUM_MOBILES][NUM_RB];

            double lambdaRSum = 0.0;
            for (Macro macro : macros) {

                if (macroStates[macro.idx]) {
                    // Mobile의 Macro가 켜졌다면
                    // 위에서 정한 Cell Association에 따라 lambdaR 가산
                    // 각 서브 채널별 할당 대상 결정

                    int[] macroMobiles = new int[NUM_RB];
                    double[] macroLambdaRs = new double[NUM_RB];

                    for (Mobile mobile : macro.getMobiles()) {

                        if (mobileConnectsMacro[mobile.idx]) {

                            for (int i = 0; i < NUM_RB; i++) {
                                double lambdaR = mobile.getMacroLambdaR()[i];
                                if (macroLambdaRs[i] < lambdaR) {
                                    macroLambdaRs[i] = lambdaR;
                                    macroMobiles[i] = mobile.idx;
                                }
                            }

                        } else {

                            lambdaRSum += calculatePicoLambdaRSum(mobile,
                                    edges[mobile.idx]);

                        }

                    }

                    for (int ri = 0; ri < NUM_RB; ri++) {
                        int mobileIdx = macroMobiles[ri];
                        if (mobileIdx >= 0) {
                            lambdaRSum += macroLambdaRs[ri];
                            edges[mobileIdx][ri] = mobiles.get(mobileIdx)
                                    .getMacroEdge();
                        }
                    }

                } else {
                    // Mobile의 Macro가 꺼졌다면
                    // Mobile의 Pico의 ABS 여부에 따라 lambdaR 가산

                    for (Mobile mobile : macro.getMobiles())
                        lambdaRSum += calculatePicoLambdaRSum(mobile,
                                edges[mobile.idx]);

                }

            }

            if (lambdaRSum > mostLambdaRSum) {
                mostLambdaRSum = lambdaRSum;
                for (int m = 0; m < NUM_MACROS; m++)
                    bestMacroStates[m] = macroStates[m];
                for (int u = 0; u < NUM_MOBILES; u++)
                    for (int i = 0; i < NUM_RB; i++)
                        bestEdges[u][i] = edges[u][i];
            }

        }

        for (Macro macro : macros)
            macro.state = bestMacroStates[macro.idx];

        for (Mobile mobile : mobiles)
            for (int i = 0; i < NUM_RB; i++)
                if (bestEdges[mobile.idx][i] != null)
                    bestEdges[mobile.idx][i].setActivated(i, true);
        ;

    }

    public double calculatePicoLambdaRSum(Mobile mobile, Edge<?>[] edges) {
        double lambdaRSum = 0.0;
        Pico pico = mobile.getPico();
        boolean isAbs = pico.isAbs();
        List<Edge<Pico>>[] absEdges = pico.getAbsEdges();
        List<Edge<Pico>>[] nonEdges = pico.getNonEdges();
        RB: for (int i = 0; i < NUM_RB; i++) {
            if (isAbs) {
                if (!(mobile.getActiveEdges()[i].baseStation instanceof Pico)) {
                    for (Edge<Pico> edge : absEdges[i]) {
                        lambdaRSum += mobile.getAbsPicoLambdaR()[i];
                        edges[i] = mobile.getPicoEdge();
                        continue RB;
                    }
                }
                edges[i] = null;
            } else {
                if (!(mobile.getActiveEdges()[i].baseStation instanceof Pico)) {
                    for (Edge<Pico> edge : absEdges[i]) {
                        lambdaRSum += mobile.getNonPicoLambdaR()[i];
                        edges[i] = mobile.getPicoEdge();
                        continue RB;
                    }
                }
                edges[i] = null;
            }
        }
        return lambdaRSum;
    }

    /**
     * 각 Mobile 별 Macro가 켜졌을때 Cell Association 결정 여기서는 다른 곳과 달리 Pico의
     * ABS여부(isAbs())를 확인하지 않고 무조건 non-ABS 값만 취한다
     *
     * @param mobiles
     *            Cell Association을 결정할 Mobile 목록
     *
     * @return Mobile.idx를 인덱스로 가지는 연결 여부 배열
     */
    public boolean[] chooseMobileConnection(List<Mobile> mobiles) {
        final boolean[] mobileConnectsMacro = new boolean[NUM_MOBILES];

        for (Mobile mobile : mobiles) {

            double macroLambdaRSum = 0.0;
            double[] macroLambdaRs = mobile.getMacroLambdaR();
            for (int i = 0; i < NUM_RB; i++)
                macroLambdaRSum += macroLambdaRs[i];
            double macroRatio = macroLambdaRSum / mobile.getMacro().pa3LambdaR;

            double picoLambdaRSum = 0.0;
            double[] nonPicoLambdaRs = mobile.getNonPicoLambdaR();
            for (int i = 0; i < NUM_RB; i++)
                picoLambdaRSum += nonPicoLambdaRs[i];
            double picoRatio = picoLambdaRSum / mobile.getPico().pa3LambdaR;

            mobileConnectsMacro[mobile.idx] = macroRatio > picoRatio;

        }

        return mobileConnectsMacro;
    }

}
