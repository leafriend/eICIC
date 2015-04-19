package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_MACROS;
import static net.folab.eicic.Constants.NUM_MOBILES;
import static net.folab.eicic.Constants.NUM_RB;

import java.util.List;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Algorithm2MacroResult implements Runnable {

    private static final int NUM_MAX_MOBILES_TS = 10;

    private boolean[][][] allMobileConnectsMacro = new boolean[NUM_MACROS][][];

    private boolean[] mobileConnectsMacro = new boolean[NUM_MOBILES];

    private Edge<?>[][] mobileEdges = new Edge[NUM_MOBILES][NUM_RB];

    double lambdaRSum;

    boolean[] macroStates = new boolean[NUM_MACROS];

    Edge<?>[][] testEdges = new Edge[NUM_MOBILES][NUM_RB];

    public List<Macro> macros;

    public boolean finished;

    public Algorithm2MacroResult(int mask) {
        // Macro 상태(ON/OFF) 지정
        for (int m = 0; m < NUM_MACROS; m++)
            macroStates[m] = 1 == (((1 << m) & mask) >> m);
    }

    public void run() {

        lambdaRSum = 0.0;
        for (int m = 0; m < macros.size(); m++) {
            Macro macro = macros.get(m);

            if (allMobileConnectsMacro[macro.idx] == null)
                allMobileConnectsMacro[macro.idx] = new boolean[1 << NUM_MAX_MOBILES_TS - 1][];

            List<Mobile> mobilesTS = macro.getMobiles();

            double mostMobileLambdaRSum = 0;

            // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
            int size = mobilesTS.size();
            int mobileStatesCount = 1 << size;
            for (int mobileMask = 1; mobileMask < mobileStatesCount; mobileMask++) {

                double mobileLambdaRSum = 0;

                if (allMobileConnectsMacro[macro.idx][mobileMask] == null) {
                    allMobileConnectsMacro[macro.idx][mobileMask] = new boolean[NUM_MOBILES];
                    for (int u = 0; u < size; u++)
                        allMobileConnectsMacro[macro.idx][mobileMask][mobilesTS
                                .get(u).idx] = 1 == (((1 << u) & mobileMask) >> u);
                }

                System.arraycopy(
                        allMobileConnectsMacro[macro.idx][mobileMask], 0,
                        mobileConnectsMacro, 0, NUM_MOBILES);

                for (int u = 0; u < size; u++) {
                    Mobile mobile = mobilesTS.get(u);

                    if (macroStates[macro.idx]) {
                        // Mobile의 Macro가 켜졌다면
                        // 위에서 정한 Cell Association에 따라 lambdaR 가산
                        // 각 서브 채널별 할당 대상 결정

                        if (mobileConnectsMacro[mobile.idx]) {

                            mobileLambdaRSum += calculateMacroLambdaRSum(
                                    mobile, mobileEdges[mobile.idx],
                                    mobileConnectsMacro);

                        } else {

                            mobileLambdaRSum += calculatePicoLambdaRSum(
                                    mobile, mobileEdges[mobile.idx],
                                    mobileConnectsMacro);

                        }

                    } else {
                        // Mobile의 Macro가 꺼졌다면
                        // Mobile의 Pico의 ABS 여부에 따라 lambdaR 가산

                        mobileLambdaRSum += calculatePicoLambdaRSum(mobile,
                                mobileEdges[mobile.idx],
                                mobileConnectsMacro);

                    }
                }

                if (mostMobileLambdaRSum < mobileLambdaRSum) {
                    mostMobileLambdaRSum = mobileLambdaRSum;
                    for (int u = 0; u < size; u++) {
                        Mobile mobile = mobilesTS.get(u);
                        for (int i = 0; i < NUM_RB; i++)
                            testEdges[mobile.idx][i] = mobileEdges[mobile.idx][i];
                    }
                }

            }

        }

        finished = true;

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
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public double calculateMacroLambdaRSum(Mobile mobile, Edge<?>[] edges,
            boolean[] mobileConnectsMacro) {
        double lambdaRSum = 0;
        List<Edge<Macro>>[] sortedEdges = mobile.getMacro()
                .getSortedEdges();
        for (int i = 0; i < NUM_RB; i++) {
            Edge<Macro> firstEdge = null;
            for (int e = 0; e < sortedEdges[i].size(); e++) {
                Edge<Macro> edge = sortedEdges[i].get(e);
                if (!mobileConnectsMacro[edge.mobile.idx])
                    continue;
                firstEdge = edge;
                break;
            }
            Edge<Macro> macroEdge = mobile.getMacroEdge();
            if (firstEdge == macroEdge) {
                lambdaRSum += mobile.getMacroLambdaR()[i];
                edges[i] = macroEdge;
            } else {
                edges[i] = null;
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
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public double calculatePicoLambdaRSum(Mobile mobile, Edge<?>[] edges,
            boolean[] mobileConnectsMacro) {
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
            for (int e = 0; e < sortedEdges[i].size(); e++) {
                Edge<Pico> edge = sortedEdges[i].get(e);
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
            } else {
                edges[i] = null;
            }

        }

        return lambdaRSum;
    }

}
