package net.folab.eicic.algorithm;

import static net.folab.eicic.model.Constants.NUM_MACROS;
import static net.folab.eicic.model.Constants.NUM_MOBILES;
import static net.folab.eicic.model.Constants.NUM_RB;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import net.folab.eicic.core.Algorithm;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

public class Algorithm2 implements Algorithm {

    private static final int NUM_MACRO_STATES = 1 << NUM_MACROS;

    private static final ExecutorService executor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private StateContext state;

    private Edge<?>[][] bestEdges = new Edge[NUM_MOBILES][NUM_RB];

    private Algorithm2MacroStates[] macroStateResults = new Algorithm2MacroStates[NUM_MACRO_STATES];

    private JdbcTemplate jdbc;

    private BasicDataSource dataSource;

    public Algorithm2() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite:logs/" + getClass().getSimpleName()
                + "-" + dateFormat.format(new Date()) + ".db");
        jdbc = new JdbcTemplate(dataSource);

        jdbc.update("CREATE TABLE MACRO (SEQ INTEGER NOT NULL, IDX INTEGER NOT NULL, CH REAL NOT NULL, LR REAL NOT NULL, IS_ON INTEGER NOT NULL)");
        jdbc.update("CREATE TABLE PICO (SEQ INTEGER NOT NULL, IDX INTEGER NOT NULL, CH REAL NOT NULL, LR REAL NOT NULL)");
        jdbc.update("CREATE TABLE MOBILE (SEQ INTEGER NOT NULL, IDX INTEGER NOT NULL, MACRO INTEGER NOT NULL, PICO INTEGER NOT NULL, CONN TEXT NOT NULL"
                + ", M_ALL_CH REAL NOT NULL, M_ALL_LR REAL NOT NULL"
                + ", M_ACT_CH REAL NOT NULL, M_ACT_LR REAL NOT NULL"
                + ", M_ACT_COUNT INTEGER NOT NULL"
                + ", P_ALL_CH REAL NOT NULL, P_ALL_LR REAL NOT NULL"
                + ", P_ACT_CH REAL NOT NULL, P_ACT_LR REAL NOT NULL"
                + ", P_ACT_COUNT INTEGER NOT NULL" + ")");

    }

    @Override
    public void terminate() {
        executor.shutdown();
        try {
            dataSource.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void delay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public StateContext calculate(int seq, Macro[] macros, Pico[] picos,
            Mobile[] mobiles) {

        for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
            if (macroStateResults[macroState] == null)
                macroStateResults[macroState] = new Algorithm2MacroStates(
                        StateContext.getStateContext(macroState, macros, picos,
                                mobiles));
            macroStateResults[macroState].finished = false;
        }

        // 가능한 모든 Macro 상태(2 ^ NUM_MACRO = 1 << NUM_MACRO)에 대한 반복문
        for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
            macroStateResults[macroState].macros = macros;
            executor.execute(macroStateResults[macroState]);
        }

        waiting: do {
            for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
                if (!macroStateResults[macroState].finished) {
                    delay(0);
                    continue waiting;
                }
            }
            break;
        } while (true);

        int bestMacroState = -1;
        double mostLambdaRSum = Double.NEGATIVE_INFINITY;
        for (int macroState = 0; macroState < NUM_MACRO_STATES; macroState++) {
            Algorithm2MacroStates result = macroStateResults[macroState];
            if (result.lambdaRSum > mostLambdaRSum) {
                bestMacroState = macroState;
                mostLambdaRSum = result.lambdaRSum;
                this.state = result.state;
                for (int u = 0; u < mobiles.length; u++)
                    for (int i = 0; i < NUM_RB; i++) {
                        bestEdges[u][i] = result.edges[u][i];
                    }
            }
        }

        for (int u = 0; u < mobiles.length; u++)
            for (int i = 0; i < NUM_RB; i++)
                if (bestEdges[u][i] != null)
                    bestEdges[u][i].setActivated(i, true);

        assert bestMacroState >= 0;

        logOnDb(seq, bestMacroState, state, macros, picos, mobiles);

        return state;

    }

    private void logOnDb(int seq, int bestMacroState, StateContext state2,
            Macro[] macros, Pico[] picos, Mobile[] mobiles) {

        for (Macro macro : macros) {
            double channel = macro.getChannel();
            double lambdaR = macro.getLambdaR();
            boolean isOn = (bestMacroState >> macro.idx & 1) == 1;
            jdbc.update(
                    "INSERT INTO MACRO (SEQ, IDX, CH, LR, IS_ON) VALUES (?, ?, ?, ?, ?)",
                    seq, macro.idx, channel, lambdaR, isOn);
        }

        for (Pico pico : picos) {
            double channel = pico.getChannel();
            double lambdaR = pico.getLambdaR();
            jdbc.update(
                    "INSERT INTO PICO (SEQ, IDX, CH, LR) VALUES (?, ?, ?, ?)",
                    seq, pico.idx, channel, lambdaR);
        }

        for (Mobile mobile : mobiles) {
            double allMacroChannel = mobile.getMacro().getChannel();
            double allMacroLambdaR = mobile.getMacro().getLambdaR();
            double activeMacroChannel = mobile.getActiveMacroChannel();
            double activeMacroLambdaR = mobile.getActiveMacroLambdaR();
            double activeMacroChannelCount = mobile
                    .getActiveMacroChannelCount();
            double allPicoChannel = mobile.getPico().getChannel();
            double allPicoLambdaR = mobile.getPico().getLambdaR();
            double activePicoChannel = mobile.getActivePicoChannel();
            double activePicoLambdaR = mobile.getActivePicoLambdaR();
            double activePicoChannelCount = mobile.getActivePicoChannelCount();
            jdbc.update("INSERT INTO MOBILE (SEQ, IDX, MACRO, PICO, CONN" //
                    + ", M_ALL_CH, M_ALL_LR, M_ACT_CH, M_ACT_LR, M_ACT_COUNT" //
                    + ", P_ALL_CH, P_ALL_LR, P_ACT_CH, P_ACT_LR, P_ACT_COUNT" //
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    seq, mobile.idx,
                    mobile.getMacro().idx,
                    mobile.getPico().idx,
                    mobile.getConnection() //
                    , allMacroChannel, allMacroLambdaR, activeMacroChannel,
                    activeMacroLambdaR,
                    activeMacroChannelCount //
                    , allPicoChannel, allPicoLambdaR, activePicoChannel,
                    activePicoLambdaR, activePicoChannelCount //
            );

        }

    }

}
