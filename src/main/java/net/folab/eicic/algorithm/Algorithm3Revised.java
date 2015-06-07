package net.folab.eicic.algorithm;

import net.folab.eicic.model.Mobile;

public class Algorithm3Revised extends Algorithm3 {

    @Override
    public void chooseMobileConnection(Mobile[] mobiles) {

        for (Mobile mobile : mobiles) {

            double macroChannel = mobile.getActiveMacroChannel();
            double picoChannel = mobile.getActivePicoChannel();

            if (macroChannel > picoChannel) {
                // Macro로 할당받은 R이 더 큰 경우
                mobileConnectsMacro[mobile.idx] = true;

            } else if (macroChannel < picoChannel) {
                // Pico로 할당받은 R이 더 큰 경우
                mobileConnectsMacro[mobile.idx] = false;

            } else {

                if (macroChannel != 0 && picoChannel != 0) {
                    // Macro와 Pico로 할당받은 R이 같은 경우 (0은 아님)
                    // : 항상 Pico로 연결
                    mobileConnectsMacro[mobile.idx] = false;

                } else {
                    // Macro와 Pico로 부터 하나도 할당받지 못한 경우 (0인 경우)
                    double allMacroChannel = mobile.getAllMacroChannel();
                    double allPicoChannel = mobile.getAllPicoChannel();

                    if (allMacroChannel > allPicoChannel) {
                        // Macro의 모든 채널값 합이 더 큰 경우
                        mobileConnectsMacro[mobile.idx] = true;

                    } else if (allMacroChannel < allPicoChannel) {
                        // Pico의 모든 채널값 합이 더 큰 경우
                        mobileConnectsMacro[mobile.idx] = false;

                    } else {
                        mobileConnectsMacro[mobile.idx] = false;

                    }

                }

            }

        }

    }

}
