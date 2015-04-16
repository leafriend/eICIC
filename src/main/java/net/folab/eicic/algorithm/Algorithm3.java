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

                    for (Mobile mobile : macro.getMobiles()) {

                        Edge<?>[] mobileEdges = edges[mobile.idx];
                        if (mobileConnectsMacro[mobile.idx]) {

                            lambdaRSum += calculateMacroLambdaRSum(mobile,
                                    mobileEdges, mobileConnectsMacro);

                        } else {

                            lambdaRSum += calculatePicoLambdaRSum(mobile,
                                    mobileEdges, mobileConnectsMacro);

                        }

                    }

                } else {
                    // Mobile의 Macro가 꺼졌다면
                    // Mobile의 Pico의 ABS 여부에 따라 lambdaR 가산

                    for (Mobile mobile : macro.getMobiles())
                        lambdaRSum += calculatePicoLambdaRSum(mobile,
                                edges[mobile.idx], mobileConnectsMacro);

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

    /**
     * Mobile이 Macro에 연결했을 때 모든 Subchannel에서 수신할 수 있는 Lambda R 값의 합을 계산한다.
     *
     * @param mobile
     *            Macro로 연결할 Mobile
     * @param edges
     *            Mobile의 Subchannel 연결 상태를 확인한 결과를 저장할 배열; 메소드 호출 후 배열의 내용이
     *            바뀐다.
     * @param mobileConnectsMacro
     *            각 Mobile의 Macro 연결 여부; Macro Subchannel 순위에서 Macro에 연결되지 않은
     *            Mobile을 제외할 때 사용
     *
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public static double calculateMacroLambdaRSum(Mobile mobile, Edge<?>[] edges,
            boolean[] mobileConnectsMacro) {
        double lambdaRSum = 0;
        List<Edge<Macro>>[] sortedEdges = mobile.getMacro().getSortedEdges();
        for (int i = 0; i < NUM_RB; i++) {
            Edge<Macro> firstEdge = null;
            for (Edge<Macro> edge : sortedEdges[i]) {
                if (!mobileConnectsMacro[edge.mobile.idx])
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
     * @param mobileConnectsMacro
     *            각 Mobile의 Macro 연결 여부; Pico Subchannel 순위에서 Macro에 연결된 Mobile을
     *            제외할 때 사용
     *
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public static double calculatePicoLambdaRSum(Mobile mobile,
            Edge<?>[] edges, boolean[] mobileConnectsMacro) {
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
                if (mobileConnectsMacro[edge.mobile.idx])
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
