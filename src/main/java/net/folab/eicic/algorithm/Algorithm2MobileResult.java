package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.NUM_MOBILES;
import static net.folab.eicic.Constants.NUM_RB;

import java.util.List;

import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Algorithm2MobileResult {

    final int cellAssoc;

    private final Macro macro;

    private final boolean[] mobileConnectsMacro = new boolean[NUM_MOBILES];

    private final List<Mobile> mobiles;

    private final int size;

    private final boolean macroState;

    double lambdaRSum;

    Edge<?>[][] edges = new Edge[NUM_MOBILES][NUM_RB];

    public Algorithm2MobileResult(int cellAssoc, Macro macro, boolean macroState) {
        this.cellAssoc = cellAssoc;
        this.macro = macro;
        this.mobiles = macro.getMobiles();
        this.size = mobiles.size();
        this.macroState = macroState;

        for (int u = 0; u < size; u++) {
            mobileConnectsMacro[mobiles.get(u).idx] = 1 == (((1 << u) & cellAssoc) >> u);
        }

    }

    public void run() {

        lambdaRSum = 0.0;

        for (int u = 0; u < size; u++) {
            Mobile mobile = mobiles.get(u);

            if (macroState) {
                // Mobile의 Macro가 켜졌다면
                // 위에서 정한 Cell Association에 따라 lambdaR 가산
                // 각 서브 채널별 할당 대상 결정

                if (mobileConnectsMacro[mobile.idx]) {

                    lambdaRSum += calculateMacroLambdaRSum(mobile);

                } else {

                    lambdaRSum += calculatePicoLambdaRSum(mobile);

                }

            } else {
                // Mobile의 Macro가 꺼졌다면
                // Mobile의 Pico의 ABS 여부에 따라 lambdaR 가산

                lambdaRSum += calculatePicoLambdaRSum(mobile);

            }
        }

        System.out.print("");

    }

    /**
     * Mobile이 Macro에 연결했을 때 모든 Subchannel에서 수신할 수 있는 Lambda R 값의 합을 계산한다.
     *
     * @param mobile
     *            Macro로 연결할 Mobile
     * @param edges
     *            Mobile의 Subchannel 연결 상태를 확인한 결과를 저장할 배열; 메소드 호출 후 배열의 내용이
     *            바뀐다.
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public double calculateMacroLambdaRSum(Mobile mobile) {
        Edge<?>[] edges = this.edges[mobile.idx];
        double lambdaRSum = 0.0;
        List<Edge<Macro>>[] sortedEdges = mobile.getMacro().getSortedEdges();
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
     * @return 전달받은 Mobile의 Lambda R 합
     */
    public double calculatePicoLambdaRSum(Mobile mobile) {
        Edge<?>[] edges = this.edges[mobile.idx];
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
            Edge<Pico> firstEdge = sortedEdges[i].get(0);

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
