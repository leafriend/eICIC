package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_MACROS;
import static net.folab.eicic.Constants.NUM_MOBILES;
import static net.folab.eicic.Constants.NUM_RB;

import java.util.List;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Algorithm1 implements Algorithm {

    private final Mobile[][] macroFirstMobiles = new Mobile[NUM_MACROS][NUM_RB];

    private final int macroStatesCount = 1 << NUM_MACROS;

    private final boolean[] bestMacroStates = new boolean[NUM_MACROS];

    private final Edge<?>[][] bestEdges = new Edge[NUM_MOBILES][NUM_RB];

    private final boolean[] macroStates = new boolean[NUM_MACROS];

    private final Edge<?>[][] edges = new Edge[NUM_MOBILES][NUM_RB];

    @Override
    public void calculate(List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles) {

        for (int m = 0; m < NUM_MACROS; m++) {
            Macro macro = macros.get(m);
            List<Edge<Macro>>[] sortedEdges = macro.getSortedEdges();
            for (int i = 0; i < NUM_RB; i++) {
                macroFirstMobiles[macro.idx][i] = sortedEdges[i].get(0).mobile;
            }
        }

        int bestMacroState = -1;
        double mostLambdaRSum = Double.NEGATIVE_INFINITY;
        // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
        for (int mask = 0; mask < macroStatesCount; mask++) {

            // Macro 상태(ON/OFF) 지정
            for (int m = 0; m < NUM_MACROS; m++)
                macroStates[m] = 1 == (((1 << m) & mask) >> m);


            double lambdaRSum = 0.0;
            for (int m = 0; m < NUM_MACROS; m++) {
                Macro macro = macros.get(m);

                if (macroStates[macro.idx]) {
                    // Mobile의 Macro가 켜졌다면
                    // : 첫 번째 모바일은 Macro로 연결하고, 다른 모바일은 Pico로 연결 시도

                    for (int u = 0; u < macro.getMobiles().size(); u++) {
                        Mobile mobile = macro.getMobiles().get(u);

                        Edge<?>[] mobileEdges = edges[mobile.idx];
                        boolean isAbs = mobile.getPico().isAbs();
                        List<Edge<Pico>>[] sortedEdges;
                        if (isAbs)
                            sortedEdges = mobile.getPico().getSortedAbsEdges();
                        else
                            sortedEdges = mobile.getPico().getSortedNonEdges();

                        for (int i = 0; i < NUM_RB; i++) {
                            if (macroFirstMobiles[mobile.getMacro().idx][i] == mobile) {
                                // Macro로 연결
                                lambdaRSum += mobile.getMacroLambdaR()[i];
                                mobileEdges[i] = mobile.getMacroEdge();
                            } else {
                                // Pico로 연결 시도
                                lambdaRSum += calculatePicoLambdaR(mobile,
                                        mobileEdges, sortedEdges, isAbs, i);
                            }
                        }

                    }

                } else {
                    // Mobile의 Macro가 꺼졌다면
                    // : Pico로 연결 시도

                    for (int u = 0; u < macro.getMobiles().size(); u++) {
                        Mobile mobile = macro.getMobiles().get(u);
                        lambdaRSum += calculatePicoLambdaRSum(mobile,
                                edges[mobile.idx]);
                    }

                }

            }

            if (lambdaRSum > mostLambdaRSum) {
                bestMacroState = mask;
                mostLambdaRSum = lambdaRSum;
                for (int m = 0; m < NUM_MACROS; m++)
                    bestMacroStates[m] = macroStates[m];
                for (int u = 0; u < NUM_MOBILES; u++)
                    for (int i = 0; i < NUM_RB; i++)
                        bestEdges[u][i] = edges[u][i];
            }

        }

        for (int m = 0; m < NUM_MACROS; m++)
           macros.get(m).state = bestMacroStates[m];

        for (int u = 0; u < mobiles.size(); u++) {
            Mobile mobile = mobiles.get(u);
            for (int i = 0; i < NUM_RB; i++)
                if (bestEdges[mobile.idx][i] != null)
                    bestEdges[mobile.idx][i].setActivated(i, true);
        }

        assert bestMacroState >= 0;

        if (bestMacroState != 127)
            System.out.println("bestMacroState: " + bestMacroState); // TODO

    }

    /**
     * Mobile이 Pico에 연결했을 때 모든 Subchannel에서 수신할 수 있는 Lambda R 값의 합을 계산한다.
     *
     * @param mobile
     *            Pico로 연결할 Mobile
     * @param edges
     *            Mobile의 Subchannel 연결 상태를 확인한 결과를 저장할 배열; 메소드 호출 후 배열의 내용이
     *            바뀐다.
     *
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public double calculatePicoLambdaRSum(Mobile mobile, Edge<?>[] edges) {
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

            lambdaRSum += calculatePicoLambdaR(mobile, edges, sortedEdges,
                    isAbs, i);

        }

        return lambdaRSum;
    }

    public double calculatePicoLambdaR(Mobile mobile, Edge<?>[] edges,
            List<Edge<Pico>>[] sortedEdges, boolean isAbs, int i) {
        // 각 Subchannel에서 내가 Macro에 연결한 다른 Mobile을 제외하고 첫 번째 순위인지 확인
        Edge<Pico> firstEdge = null;
        List<Edge<Pico>> array = sortedEdges[i];
        int count = array.size();
        for (int e = 0; e < count; e++) {
            Edge<Pico> edge = array.get(e);
            if (macroFirstMobiles[edge.mobile.getMacro().idx][i] == edge.mobile)
                continue;
            firstEdge = edge;
            break;
        }

        // Pico의 현재 Subchannel의 첫 번째 Mobile이 전달받은 Mobile이라면
        Edge<Pico> picoEdge = mobile.getPicoEdge();
        double lambdaR;
        if (firstEdge == picoEdge) {
            if (isAbs)
                lambdaR = mobile.getAbsPicoLambdaR()[i];
            else
                lambdaR = mobile.getNonPicoLambdaR()[i];
            edges[i] = picoEdge;
        } else {
            lambdaR = 0;
            edges[i] = null;
        }
        return lambdaR;
    }

}
