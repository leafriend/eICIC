package net.folab.eicic;

public interface Constants {

    int SIMULATION_TIME = 1000;

    // - - -

    double PATH_LOSS_EXPO = 4.0;

    double LN_SHAD = 4.0;

    int NUM_MACROS = 7;

    int NUM_PICOS = 15;

    int NUM_MOBILES = 50;

    int NUM_RB = 100;

    double MACRO_TX_POWER = 40.0;

    double PICO_TX_POWER = 1.0;

    double MOBILE_QOS_FACTOR = 0.0001;

    double MOBILE_QOS = Math.log(MOBILE_QOS_FACTOR);

    double BANDWIDTH = 20000000;

    double BW_PER_RB = 180000;

    double NOISE_FACTOR = -174;

    double NOISE = (BW_PER_RB * Math.pow(10, (NOISE_FACTOR / 10)));

    double MEGA = 1048576;

}
