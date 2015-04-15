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

        // 각 Mobile 별 Macro가 켜졌을때 Cell Association 결정
        // 여기서는 다른 곳과 달리 Pico의 ABS여부(is_abs())를 확인하지 않고
        // 무조건 non-ABS 값만 취한다
        final boolean[] mobileConnectsMacro = new boolean[NUM_MOBILES];

        for (Mobile mobile : mobiles) {

            double macroLambdaR = 0.0;
            for (int i = 0; i < mobile.getMacroLambdaR().length; i++)
                macroLambdaR += mobile.getMacroLambdaR()[i];
            double macroRatio = macroLambdaR / mobile.getMacro().pa3LambdaR;

            double picoLambdaR = 0.0;
            for (int i = 0; i < mobile.getNonPicoLambdaR().length; i++)
                picoLambdaR += mobile.getMacroLambdaR()[i];
            double picoRatio = picoLambdaR / mobile.getPico().pa3LambdaR;

            mobileConnectsMacro[mobile.idx] = macroRatio > picoRatio;

        }

        double best_sum_lambda_r = Double.NEGATIVE_INFINITY;
        boolean[] bestMacroStates = new boolean[NUM_MACROS];
        ConnectionState[][] bestConnectionStates = new ConnectionState[NUM_MOBILES][NUM_RB];

        /* 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문 */
        int num_macro_state = 1 << NUM_MACROS;
        for (int mask = 1; mask < num_macro_state; mask++) {

            /* Macro 상태(ON/OFF) 지정 */
            final int _mask = mask;
            for (Macro macro : macros)
                macro.state = 1 == (((1 << macro.idx) & _mask) >> macro.idx);

            ConnectionState[][] states = new ConnectionState[NUM_MOBILES][NUM_RB];
            for (Mobile mobile : mobiles)
                for (int i = 0; i < NUM_RB; i++) {
                    states[mobile.idx][i] = NOTHING;
                }
            double curr_sum_lambda_r = 0.0;
            for (Macro macro : macros) {

                if (macro.state) {
                    // Mobile의 Macro가 켜졌다면
                    // 위에서 정한 Cell Association에 따라 lambda_r 가산
                    // 각 서브 채널별 할당 대상 결정

                    int[] macro_rb_mobile = new int[NUM_RB];
                    double[] macro_rb_lambda_r = new double[NUM_RB];

                    for (Mobile mobile : macro.getMobiles()) {

                        if (mobileConnectsMacro[mobile.idx]) {

                            for (int i = 0; i < NUM_RB; i++) {
                                double lambda_r = mobile.getMacroLambdaR()[i];
                                if (macro_rb_lambda_r[i] < lambda_r) {
                                    macro_rb_lambda_r[i] = lambda_r;
                                    macro_rb_mobile[i] = mobile.idx;
                                }
                                states[mobile.idx][i] = NOTHING;
                            }

                        }

                    }

                    for (int ri = 0; ri < NUM_RB; ri++) {
                        if (macro_rb_mobile[ri] >= 0) {
                            curr_sum_lambda_r += macro_rb_lambda_r[ri];
                            states[macro_rb_mobile[ri]][ri] = MACRO;
                        }

                    }

                } else {
                    // Mobile의 Macro가 꺼졌다면
                    // Mobile의 Pico의 ABS 여부에 따라 lambda_r 가산

                    for (Mobile mobile : macro.getMobiles()) {
                        Pico pico = mobile.getPico();
                        boolean isAbs = pico.isAbs();
                        for (int ri = 0; ri < NUM_RB; ri++) {
                            if (isAbs) {
                                if (pico.absIndexOf(ri, mobile) == 0) {
                                    curr_sum_lambda_r += mobile.getAbsPicoLambdaR()[ri];
                                    states[mobile.idx][ri] = ABS_PICO;
                                } else {
                                    states[mobile.idx][ri] = NOTHING;
                                }
                            } else {
                                if (pico.nonIndexOf(ri, mobile) == 0) {
                                    curr_sum_lambda_r += mobile.getNonPicoLambdaR()[ri];
                                    states[mobile.idx][ri] = NON_PICO;
                                } else {
                                    states[mobile.idx][ri] = NOTHING;
                                }
                            }
                        }
                    }

                }

            }

            if (curr_sum_lambda_r > best_sum_lambda_r) {
                best_sum_lambda_r = curr_sum_lambda_r;
                for (Macro macro : macros)
                    bestMacroStates[macro.idx] = macro.state;
                for (Mobile mobile : mobiles)
                    for (int i = 0; i < NUM_RB; i++) {
                        bestConnectionStates[mobile.idx][i] = states[mobile.idx][i];
                    }
            }

        }

        for (Macro macro : macros)
            macro.state = bestMacroStates[macro.idx];

        for (Mobile mobile : mobiles)
            for (int i = 0; i < NUM_RB; i++) {
                mobile.connectionStates[i] = bestConnectionStates[mobile.idx][i];
            }

    }

}
