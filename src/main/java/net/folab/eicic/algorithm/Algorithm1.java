package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_MACROS;
import static net.folab.eicic.Constants.NUM_MOBILES;
import static net.folab.eicic.Constants.NUM_RB;

import java.util.List;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class Algorithm1 implements Algorithm {

    private final Mobile[][] macroFirstMobiles = new Mobile[NUM_MACROS][NUM_RB];

    private final int macroStatesCount = 1 << NUM_MACROS;

    private StateContext state;

    private final Edge<?>[][] bestEdges = new Edge[NUM_MOBILES][NUM_RB];

    private final Edge<?>[][] edges = new Edge[NUM_MOBILES][NUM_RB];

    @Override
    public int getNumber() {
        return 1;
    }

    @Override
    public StateContext calculate(Macro[] macros, Pico[] picos, Mobile[] mobiles) {

        for (int m = 0; m < macros.length; m++) {
            Macro macro = macros[m];
            List<Edge<Macro>>[] sortedEdges = macro.getSortedEdges();
            for (int i = 0; i < NUM_RB; i++) {
                macroFirstMobiles[macro.idx][i] = sortedEdges[i].get(0).mobile;
            }
        }

        int bestMacroState = -1;
        double mostLambdaRSum = Double.NEGATIVE_INFINITY;
        // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
        for (int macroState = 0; macroState < macroStatesCount; macroState++) {

            StateContext state = StateContext.getStateContext(macroState,
                    macros, picos, mobiles);

            double lambdaRSum = 0.0;
            for (int m = 0; m < macros.length; m++) {
                Macro macro = macros[m];

                if (state.macroIsOn(m)) {
                    // Mobile의 Macro가 켜졌다면
                    // : 첫 번째 모바일은 Macro로 연결하고, 다른 모바일은 Pico로 연결 시도

                    for (int u = 0; u < macro.getMobiles().size(); u++) {
                        Mobile mobile = macro.getMobiles().get(u);

                        Edge<?>[] mobileEdges = edges[mobile.idx];
                        Pico pico = mobile.getPico();
                        boolean isAbs = state.picoIsAbs(pico.idx);
                        List<Edge<Pico>>[] sortedEdges;
                        if (isAbs)
                            sortedEdges = pico.getSortedAbsEdges();
                        else
                            sortedEdges = pico.getSortedNonEdges();

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
                        lambdaRSum += calculatePicoLambdaRSum(state, mobile,
                                edges[mobile.idx]);
                    }

                }

            }

            if (lambdaRSum > mostLambdaRSum) {
                bestMacroState = macroState;
                mostLambdaRSum = lambdaRSum;
                this.state = state;
                for (int u = 0; u < mobiles.length; u++)
                    for (int i = 0; i < NUM_RB; i++)
                        bestEdges[u][i] = edges[u][i];
            }

        }

        for (int u = 0; u < mobiles.length; u++)
            for (int i = 0; i < NUM_RB; i++)
                if (bestEdges[u][i] != null)
                    bestEdges[u][i].setActivated(i, true);

        assert bestMacroState >= 0;

        return state;

    }

    /**
     * Mobile이 Pico에 연결했을 때 모든 Subchannel에서 수신할 수 있는 Lambda R 값의 합을 계산한다.
     *
     * @param state
     *
     * @param mobile
     *            Pico로 연결할 Mobile
     * @param edges
     *            Mobile의 Subchannel 연결 상태를 확인한 결과를 저장할 배열; 메소드 호출 후 배열의 내용이
     *            바뀐다.
     *
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public double calculatePicoLambdaRSum(StateContext state, Mobile mobile,
            Edge<?>[] edges) {
        // Mobile의 Lambda R 합
        double lambdaRSum = 0.0;
        Pico pico = mobile.getPico();

        // Mobile이 연결하려는 Pico의 각 Subchannel에 정렬된 Mobile 목록
        List<Edge<Pico>>[] sortedEdges;
        boolean isAbs = state.picoIsAbs(pico.idx);
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
