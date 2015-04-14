package net.folab.eicic;

public interface Constants {

    double PATH_LOSS_EXPO = 4.0;

    double LN_SHAD = 4.0;

    int NUM_RB = 100;

    double MACRO_TX_POWER = 40.0;

    double PICO_TX_POWER = 1.0;

    double MOBILE_QOS_FACTOR = 0.0001;

    double MOBILE_QOS = Math.log(MOBILE_QOS_FACTOR);

}
