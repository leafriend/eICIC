package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_MACROS;
import static net.folab.eicic.Constants.NUM_MOBILES;
import static net.folab.eicic.Constants.NUM_RB;

import java.util.List;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Algorithm2 implements Algorithm {

    @Override
    public void calculate(List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles) {

        boolean[] bestMacroStates = new boolean[NUM_MACROS];
        Edge<?>[][] bestEdges = new Edge[NUM_MOBILES][NUM_RB];

        double mostLambdaRSum = Double.NEGATIVE_INFINITY;
        // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
        int macroStatesCount = 1 << NUM_MACROS;
        for (int mask = 0; mask < macroStatesCount; mask++) {

            boolean[] macroStates = new boolean[NUM_MACROS];
            // Macro 상태(ON/OFF) 지정
            for (int m = 0; m < NUM_MACROS; m++)
                macroStates[m] = 1 == (((1 << m) & mask) >> m);

            Edge<?>[][] _edges = new Edge[NUM_MOBILES][NUM_RB];

            double lambdaRSum = 0.0;
            for (Macro macro : macros) {

                // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
                List<Mobile> mobilesTS = macro.getMobiles();
                int mobileStatesCount = 1 << mobilesTS.size();

                int[] mobileIndexes = new int[NUM_MOBILES];
                for (int i = 0; i < mobilesTS.size(); i++)
                    mobileIndexes[mobilesTS.get(i).idx] = i;

                double mostMobileLambdaRSum = 0;

                for (int mobileMask = 0; mobileMask < macroStatesCount; mobileMask++) {

                    double mobileLambdaRSum = 0;

                    boolean[] mobileConnectsMacro = new boolean[mobileStatesCount];
                    Edge<?>[][] edges = new Edge[mobilesTS.size()][NUM_RB];
                    for (int u = 0; u < mobileStatesCount; u++)
                        mobileConnectsMacro[u] = 1 == (((1 << u) & mobileMask) >> u);

                    if (macroStates[macro.idx]) {
                        // Mobile의 Macro가 켜졌다면
                        // 위에서 정한 Cell Association에 따라 lambdaR 가산
                        // 각 서브 채널별 할당 대상 결정

                        for (Mobile mobile : mobilesTS) {

                            Edge<?>[] mobileEdges = edges[mobileIndexes[mobile.idx]];
                            if (mobileConnectsMacro[mobileIndexes[mobile.idx]]) {

                                mobileLambdaRSum += calculateMacroLambdaRSum(mobile,
                                        mobileEdges, mobileConnectsMacro,
                                        mobileIndexes);

                            } else {

                                mobileLambdaRSum += calculatePicoLambdaRSum(mobile,
                                        mobileEdges, mobileConnectsMacro,
                                        mobileIndexes);

                            }

                        }

                    } else {
                        // Mobile의 Macro가 꺼졌다면
                        // Mobile의 Pico의 ABS 여부에 따라 lambdaR 가산

                        for (Mobile mobile : mobilesTS)
                            mobileLambdaRSum += calculatePicoLambdaRSum(mobile,
                                    edges[mobileIndexes[mobile.idx]], mobileConnectsMacro,
                                    mobileIndexes);

                    }

                    if (mostMobileLambdaRSum < mobileLambdaRSum) {
                        System.out.println("mobileLambdaRSum: " + mobileLambdaRSum);
                        mostMobileLambdaRSum = mobileLambdaRSum;
                        for (int u = 0; u < mobilesTS.size(); u++)
                            for (int i = 0; i < NUM_RB; i++)
                                _edges[mobilesTS.get(u).idx][i] = edges[u][i];
                    }

                }

            }

            if (lambdaRSum > mostLambdaRSum) {
                mostLambdaRSum = lambdaRSum;
                for (int m = 0; m < NUM_MACROS; m++)
                    bestMacroStates[m] = macroStates[m];
                for (int u = 0; u < NUM_MOBILES; u++)
                    for (int i = 0; i < NUM_RB; i++)
                        bestEdges[u][i] = _edges[u][i];
            }

        }

        for (Macro macro : macros)
            macro.state = bestMacroStates[macro.idx];

        for (Mobile mobile : mobiles)
            for (int i = 0; i < NUM_RB; i++)
                if (bestEdges[mobile.idx][i] != null)
                    bestEdges[mobile.idx][i].setActivated(i, true);

        double[] macroLambdaR = new double[NUM_MOBILES];
        for (Macro macro : macros) {
            double lambdaR = 0.0;
            for (int i = 0; i < NUM_RB; i++) {
                Edge<Macro> edge = macro.getActiveEdges()[i];
                if (edge == null)
                    continue;
                Mobile mobile = edge.mobile;
                macroLambdaR[mobile.idx] += mobile.getMacroLambdaR()[i];
                lambdaR += mobile.getMacroLambdaR()[i];
            }
            for (Mobile mobile : macro.getMobiles()) {
                macro.pa3MobileLambdaR[mobile.idx] //
                = 0.8 * macro.pa3MobileLambdaR[mobile.idx] //
                        + 0.2 * macroLambdaR[mobile.idx];
            }
            macro.pa3LambdaR = 0.8 * macro.pa3LambdaR + 0.2 * lambdaR;
        }

        double[] picoLambdaR = new double[NUM_MOBILES];
        for (Pico pico : picos) {
            double lambdaR = 0.0;
            for (int i = 0; i < NUM_RB; i++) {
                Edge<Pico> edge = pico.getActiveEdges()[i];
                if (edge == null)
                    continue;
                Mobile mobile = edge.mobile;
                if (edge.baseStation.isAbs()) {
                    picoLambdaR[mobile.idx] += mobile.getAbsPicoLambdaR()[i];
                    lambdaR += mobile.getAbsPicoLambdaR()[i];
                } else {
                    picoLambdaR[mobile.idx] += mobile.getNonPicoLambdaR()[i];
                    lambdaR += mobile.getNonPicoLambdaR()[i];
                }
            }
            for (Mobile mobile : pico.getMobiles()) {
                pico.pa3MobileLambdaR[mobile.idx] //
                = 0.8 * pico.pa3MobileLambdaR[mobile.idx] //
                        + 0.2 * picoLambdaR[mobile.idx];
            }
            pico.pa3LambdaR = 0.8 * pico.pa3LambdaR + 0.2 * lambdaR;
        }

    }

    /**
     * Mobile이 Macro에 연결했을 때 모든 Subchannel에서 수신할 수 있는 Lambda R 값의 합을 계산한다.
     *
     * @param mobile
     *            Macro로 연결할 Mobile
     * @param edges
     *            Mobile의 Subchannel 연결 상태를 확인한 결과를 저장할 배열; 메소드 호출 후 배열의 내용이
     *            바뀐다.
     * @param mobileIndexes
     * @param mobileConnectsMacro
     *
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public double calculateMacroLambdaRSum(Mobile mobile, Edge<?>[] edges,
            boolean[] mobileConnectsMacro, int[] mobileIndexes) {
        double lambdaRSum = 0;
        List<Edge<Macro>>[] sortedEdges = mobile.getMacro().getSortedEdges();
        for (int i = 0; i < NUM_RB; i++) {
            Edge<Macro> firstEdge = null;
            for (Edge<Macro> edge : sortedEdges[i]) {
                if (!mobileConnectsMacro[mobileIndexes[edge.mobile.idx]])
                    continue;
                firstEdge = edge;
                break;
            }
            Edge<Macro> macroEdge = mobile.getMacroEdge();
            if (firstEdge == macroEdge) {
                lambdaRSum += mobile.getMacroLambdaR()[i];
                edges[i] = macroEdge;
            }
        }
        return lambdaRSum;
    }

    /**
     * Mobile이 Pico에 연결했을 때 모든 Subchannel에서 수신할 수 있는 Lambda R 값의 합을 계산한다.
     *
     * @param mobile
     *            Pico로 연결할 Mobile
     * @param edges
     *            Mobile의 Subchannel 연결 상태를 확인한 결과를 저장할 배열; 메소드 호출 후 배열의 내용이
     *            바뀐다.
     * @param mobileIndexes
     * @param mobileConnectsMacro
     *
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public double calculatePicoLambdaRSum(Mobile mobile, Edge<?>[] edges,
            boolean[] mobileConnectsMacro, int[] mobileIndexes) {
        // Mobile의 Lambda R 합
        double lambdaRSum = 0.0;
        Pico pico = mobile.getPico();

        // Mobile이 연결하려는 Pico의 각 Subchannel에 정렬된 Mobile 목록
        List<Edge<Pico>>[] sortedEdges;
        boolean isAbs = pico.isAbs();
        if (isAbs) {
            sortedEdges = pico.getSortedAbsEdges();
        } else {
            sortedEdges = pico.getSortedNonEdges();
        }

        for (int i = 0; i < NUM_RB; i++) {

            // 각 Subchannel에서 내가 Macro에 연결한 다른 Mobile을 제외하고 첫 번째 순위인지 확인
            Edge<Pico> firstEdge = null;
            for (Edge<Pico> edge : sortedEdges[i]) {
                if (mobileConnectsMacro[mobileIndexes[edge.mobile.idx]])
                    continue;
                firstEdge = edge;
                break;
            }

            // Pico의 현재 Subchannel의 첫 번째 Mobile이 전달받은 Mobile이라면
            Edge<Pico> picoEdge = mobile.getPicoEdge();
            if (firstEdge == picoEdge) {
                if (isAbs)
                    lambdaRSum += mobile.getAbsPicoLambdaR()[i];
                else
                    lambdaRSum += mobile.getNonPicoLambdaR()[i];
                edges[i] = picoEdge;
            }

        }

        return lambdaRSum;
    }

}
