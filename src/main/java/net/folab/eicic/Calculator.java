package net.folab.eicic;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

public class Calculator {

    private final Macro[] macros;

    private final Pico[] picos;

    private final Mobile[] mobiles;

    private Algorithm algorithm;

    public Calculator(Macro[] macros, Pico[] picos, Mobile[] mobiles,
            Console console) {
        super();
        this.macros = macros;
        this.picos = picos;
        this.mobiles = mobiles;
    }

    public void calculateInternal(int seq) {
        for (int m = 0; m < macros.length; m++)
            macros[m].initializeEdges();
        for (int p = 0; p < picos.length; p++)
            picos[p].initializeEdges();

        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateDataRate();

        for (int m = 0; m < macros.length; m++)
            macros[m].sortMobiles();
        for (int p = 0; p < picos.length; p++)
            picos[p].sortMobiles();

        algorithm.calculate(macros, picos, mobiles);

        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateThroughput();
        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateUserRate();
        for (int u = 0; u < mobiles.length; u++)
            mobiles[u].calculateDualVariables(seq);

        for (int m = 0; m < macros.length; m++)
            macros[m].count();
        for (int p = 0; p < picos.length; p++)
            picos[p].count();

    }

    public Macro[] getMacros() {
        return macros;
    }

    public Pico[] getPicos() {
        return picos;
    }

    public Mobile[] getMobiles() {
        return mobiles;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

}
