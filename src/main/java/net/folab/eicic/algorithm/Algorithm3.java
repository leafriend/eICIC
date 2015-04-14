package net.folab.eicic.algorithm;

import static java.util.Arrays.*;
import static net.folab.eicic.Constants.*;

import java.util.List;

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
        boolean[] mobileConnectsMacro = new boolean[NUM_MOBILES];

        mobiles.stream().forEach(mobile -> {

            double macroLambdaR = stream(mobile.getMacroLambdaR()) //
                    .reduce(0.0, Double::sum);
            double macroRatio = macroLambdaR / mobile.getMacro().pa3LambdaR;

            double picoLambdaR = stream(mobile.getNonPicoLambdaR()) //
                    .reduce(0.0, Double::sum);
            double picoRatio = picoLambdaR / mobile.getPico().pa3LambdaR;

            mobileConnectsMacro[mobile.idx] = macroRatio > picoRatio;

        });

        double best_sum_lambda_r = Double.MIN_VALUE;
        boolean[] bestMacroStates = new boolean[NUM_MACROS];

        /* 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문 */
        int num_macro_state = 1 << NUM_MACROS;
        for (int mask = 0; mask < num_macro_state; mask++) {

            boolean[] macroStates = new boolean[NUM_MACROS];
            /* Macro 상태(ON/OFF) 지정 */
            final int _mask = mask;
            macros.forEach(macro -> macro.state = 1 == (((1 << macro.idx) & _mask) >> macro.idx));

            double curr_sum_lambda_r = 0.0;

        }

        macros.forEach(macro -> macro.state = bestMacroStates[macro.idx]);

    }
}
