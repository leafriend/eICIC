package net.folab.eicic.algorithm;

import static net.folab.eicic.Constants.*;
import static net.folab.eicic.model.ConnectionState.*;

import java.util.List;

import net.folab.eicic.model.ConnectionState;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Algorithm3 implements Algorithm {

    @Override
    public void calculate(List<Macro> macros, List<Pico> picos,
            List<Mobile> mobiles) {

        final boolean[] mobileConnectsMacro = chooseMobileConnection(mobiles);

        double mostLambdaRSum = Double.NEGATIVE_INFINITY;
        boolean[] bestMacroStates = new boolean[NUM_MACROS];
        ConnectionState[][] bestMobileStates = new ConnectionState[NUM_MOBILES][NUM_RB];

        // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
        int macroStatesCount = 1 << NUM_MACROS;
        for (int mask = 1; mask < macroStatesCount; mask++) {

            // Macro 상태(ON/OFF) 지정
            final int _mask = mask;
            for (Macro macro : macros)
                macro.state = 1 == (((1 << macro.idx) & _mask) >> macro.idx);

            ConnectionState[][] mobileStates = new ConnectionState[NUM_MOBILES][NUM_RB];
            for (Mobile mobile : mobiles)
                for (int i = 0; i < NUM_RB; i++)
                    mobileStates[mobile.idx][i] = NOTHING;

            double lambdaRSum = 0.0;
            for (Macro macro : macros) {

                if (macro.state) {
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
                                mobileStates[mobile.idx][i] = NOTHING;
                            }

                        }

                    }

                    for (int ri = 0; ri < NUM_RB; ri++) {
                        if (macroMobiles[ri] >= 0) {
                            lambdaRSum += macroLambdaRs[ri];
                            mobileStates[macroMobiles[ri]][ri] = MACRO;
                        }

                    }

                } else {
                    // Mobile의 Macro가 꺼졌다면
                    // Mobile의 Pico의 ABS 여부에 따라 lambdaR 가산

                    for (Mobile mobile : macro.getMobiles()) {
                        Pico pico = mobile.getPico();
                        boolean isAbs = pico.isAbs();
                        for (int ri = 0; ri < NUM_RB; ri++) {
                            if (isAbs) {
                                if (pico.absIndexOf(ri, mobile) == 0) {
                                    lambdaRSum += mobile.getAbsPicoLambdaR()[ri];
                                    mobileStates[mobile.idx][ri] = ABS_PICO;
                                } else {
                                    mobileStates[mobile.idx][ri] = NOTHING;
                                }
                            } else {
                                if (pico.nonIndexOf(ri, mobile) == 0) {
                                    lambdaRSum += mobile.getNonPicoLambdaR()[ri];
                                    mobileStates[mobile.idx][ri] = NON_PICO;
                                } else {
                                    mobileStates[mobile.idx][ri] = NOTHING;
                                }
                            }
                        }
                    }

                }

            }

            if (lambdaRSum > mostLambdaRSum) {
                mostLambdaRSum = lambdaRSum;
                for (Macro macro : macros)
                    bestMacroStates[macro.idx] = macro.state;
                for (Mobile mobile : mobiles)
                    for (int i = 0; i < NUM_RB; i++)
                        bestMobileStates[mobile.idx][i] = mobileStates[mobile.idx][i];
            }

        }

        for (Macro macro : macros)
            macro.state = bestMacroStates[macro.idx];

        for (Mobile mobile : mobiles)
            for (int i = 0; i < NUM_RB; i++)
                mobile.connectionStates[i] = bestMobileStates[mobile.idx][i];

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
