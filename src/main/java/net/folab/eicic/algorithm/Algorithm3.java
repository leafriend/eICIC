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

    }

}
