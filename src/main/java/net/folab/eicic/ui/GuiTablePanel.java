package net.folab.eicic.ui;

import static java.lang.Math.log;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static net.folab.eicic.model.Constants.NUM_MACROS;
import static net.folab.eicic.model.Constants.NUM_MOBILES;
import static net.folab.eicic.model.Constants.NUM_PICOS;
import static net.folab.eicic.model.Constants.NUM_RB;
import static net.folab.eicic.ui.Util.array;
import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.CTRL;
import static org.eclipse.swt.SWT.FULL_SELECTION;
import static org.eclipse.swt.SWT.LEFT;
import static org.eclipse.swt.SWT.MULTI;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.RIGHT;
import static org.eclipse.swt.SWT.SHIFT;

import net.folab.eicic.core.Controller;
import net.folab.eicic.model.BaseStation;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class GuiTablePanel {

    public static final String LAMBDA = "\u03bb";

    public static final String MU = "\u03bc";

    private Controller controller;

    private Composite control;

    private Color colorActiveBg;

    private Table mobileTable;

    private Table picoTable;

    private Table macroTable;

    private int[] mobileIdxToItems = new int[NUM_MOBILES];

    private int updateFrequency;

    public GuiTablePanel(Composite wrapper, Controller controller) {

        this.controller = controller;

        control = new Composite(wrapper, NONE);
        Display display = control.getDisplay();
        Clipboard clipboard = new Clipboard(display);
        colorActiveBg = new Color(display, 225, 255, 225);

        // - - -

        mobileTable = new Table(control, BORDER | FULL_SELECTION | MULTI);
        mobileTable.setLinesVisible(true);
        mobileTable.setHeaderVisible(true);
        mobileTable.setEnabled(false);

        mobileTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask & CTRL) == CTRL && e.keyCode == 'c') {
                    String text = "";
                    int columnCount = mobileTable.getColumnCount();
                    if ((e.stateMask & SHIFT) == SHIFT) {
                        for (int c = 0; c < columnCount; c++) {
                            if (c > 0)
                                text += "\t";
                            text += mobileTable.getColumn(c).getText();
                        }
                    }
                    for (TableItem item : mobileTable.getSelection()) {
                        if (text.length() > 0)
                            text += "\n";
                        for (int c = 0; c < columnCount; c++) {
                            if (c > 0)
                                text += "\t";
                            text += item.getText(c);
                        }
                    }
                    clipboard.setContents(array(text),
                            array(TextTransfer.getInstance()));
                }
                if ((e.stateMask & CTRL) == CTRL && e.keyCode == 'a') {
                    mobileTable.setSelection(0, mobileTable.getItemCount() - 1);
                }
            }
        });

        addColumn(mobileTable, 32, "#");
        addColumn(mobileTable, 80, "X");
        addColumn(mobileTable, 80, "Y");
        addColumn(mobileTable, 32, "M");
        // addColumn(mobileTable, 80, "M. Dist.");
        // addColumn(mobileTable, 80, "M. " + LAMBDA + "R");
        // addColumn(mobileTable, 80, "M. M. " + LAMBDA + "R");
        addColumn(mobileTable, 32, "P");
        // addColumn(mobileTable, 80, "P. Dist.");
        // addColumn(mobileTable, 80, "P. " + LAMBDA + "R");
        // addColumn(mobileTable, 80, "P. M. " + LAMBDA + "R");
        addColumn(mobileTable, 96, "User Rate");
        addColumn(mobileTable, 96, "log(User Rate)");
        addColumn(mobileTable, 96, "Throughput");
        addColumn(mobileTable, 96, "log(Throughput)");
        addColumn(mobileTable, 96, LAMBDA);
        addColumn(mobileTable, 96, MU);

        for (int i = 0; i < NUM_RB; i++) {
            addColumn(mobileTable, 80, LAMBDA + "R " + i);
            addColumn(mobileTable, 48, "BS " + i, LEFT);
            addColumn(mobileTable, 24, "Rank  " + i);
        }

        for (int i = 0; i < NUM_MOBILES; i++) {
            TableItem item = new TableItem(mobileTable, NONE);
            item.setText(0, valueOf(i));
            mobileIdxToItems[i] = i;
        }

        // - - -

        picoTable = new Table(control, BORDER | FULL_SELECTION);
        picoTable.setLinesVisible(true);
        picoTable.setHeaderVisible(true);

        addColumn(picoTable, 32, "#");
        addColumn(picoTable, 80, "X");
        addColumn(picoTable, 80, "Y");
        addColumn(picoTable, 56, "Tx Power");
        addColumn(picoTable, 48, "State", LEFT);
        addColumn(picoTable, 56, "ABS%");

        for (int i = 0; i < NUM_PICOS; i++) {
            TableItem item = new TableItem(picoTable, NONE);
            item.setText(0, valueOf(i));
        }

        // - - -

        macroTable = new Table(control, BORDER | FULL_SELECTION);
        macroTable.setLinesVisible(true);
        macroTable.setHeaderVisible(true);

        addColumn(macroTable, 32, "#");
        addColumn(macroTable, 80, "X");
        addColumn(macroTable, 80, "Y");
        addColumn(macroTable, 56, "Tx Power");
        addColumn(macroTable, 48, "State", LEFT);
        addColumn(macroTable, 56, "ABS%");

        for (int i = 0; i < NUM_MACROS; i++) {
            TableItem item = new TableItem(macroTable, NONE);
            item.setText(0, valueOf(i));
        }

        macroTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                Mobile[] mobiles = controller.getMobiles();
                TableItem macroItem = (TableItem) e.item;
                int macroIdx = Integer.parseInt(macroItem.getText(0));
                boolean enabled = !picoTable.getEnabled();
                picoTable.setEnabled(enabled);
                int i = 0;
                mobileTable.removeAll();
                for (Mobile mobile : mobiles) {
                    if (enabled || mobile.getMacro().idx == macroIdx) {
                        TableItem item = new TableItem(mobileTable, NONE);
                        item.setText(0, valueOf(mobile.idx));
                        showMobile(mobile, item);
                        mobileIdxToItems[mobile.idx] = i;
                        i++;
                    } else {
                        mobileIdxToItems[mobile.idx] = -1;
                    }
                }
                int seq = controller.getSeq();
                Macro[] macros = controller.getMacros();
                Pico[] picos = controller.getPicos();
                dump(seq, null, macros, picos, mobiles);
            }
        });

        // - - -

        control.setLayout(new FormLayout());
        FormData layoutData;

        // macroTable
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(0, 380);
        // layoutData.bottom = new FormAttachment(100, 0);
        macroTable.setLayoutData(layoutData);

        // picoTable
        layoutData = new FormData();
        layoutData.top = new FormAttachment(macroTable, 8);
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(0, 380);
        // layoutData.bottom = new FormAttachment(100, 0);
        picoTable.setLayoutData(layoutData);

        // table
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(macroTable, 8);
        layoutData.right = new FormAttachment(100, 0);
        layoutData.bottom = new FormAttachment(100, 0);
        mobileTable.setLayoutData(layoutData);

    }

    public static void addColumn(Table table, int width, String text) {
        addColumn(table, width, text, RIGHT);
    }

    public static void addColumn(Table table, int width, String text,
            int alignment) {
        TableColumn column = new TableColumn(table, NONE);
        column.setText(text);
        column.setAlignment(alignment);
        column.setWidth(width);
    }

    public void notifyStarted() {

        Macro[] macros = controller.getMacros();
        for (Macro macro : macros) {
            TableItem item = macroTable.getItem(macro.idx);
            item.setText(1, valueOf(format("%.3f", macro.x)));
            item.setText(2, valueOf(format("%.3f", macro.y)));
            item.setText(3, valueOf(format("%.2f", macro.txPower)));
        }

        Pico[] picos = controller.getPicos();
        for (Pico pico : picos) {
            TableItem item = picoTable.getItem(pico.idx);
            item.setText(1, valueOf(format("%.3f", pico.x)));
            item.setText(2, valueOf(format("%.3f", pico.y)));
            item.setText(3, valueOf(format("%.2f", pico.txPower)));
        }

        Mobile[] mobiles = controller.getMobiles();
        for (Mobile mobile : mobiles) {
            TableItem item = mobileTable.getItem(mobile.idx);
            showMobile(mobile, item);
        }

    }

    public void showMobile(Mobile mobile, TableItem item) {
        String[] texts = new String[11 + NUM_RB];
        int i = 1;
        texts[i++] = format("%.3f", mobile.x);
        texts[i++] = format("%.3f", mobile.y);
        texts[i++] = valueOf(mobile.getMacro().idx);
        // texts[i++] = format("%.3f", mobile.getMacroEdge().distance);
        // texts[i++] = format("%.3f", mobile.getMacro().pa3LambdaR);
        // texts[i++] = null;

        texts[i++] = valueOf(mobile.getPico().idx);
        // texts[i++] = format("%.3f", mobile.getPicoEdge().distance);
        // texts[i++] = format("%.3f", mobile.getPico().pa3LambdaR);
        // texts[i++] = null;

        texts[i++] = format("%.6f", mobile.getUserRate());
        texts[i++] = format("%.6f", log(mobile.getUserRate()));
        texts[i++] = format("%.6f", 0.0);
        texts[i++] = format("%.6f", Double.NEGATIVE_INFINITY);
        texts[i++] = format("%.6f", mobile.getLambda());
        texts[i++] = format("%.6f", mobile.getMu());

        item.setText(texts);
    }

    public Composite getControl() {
        return control;
    }

    public double[] dump(int seq, StateContext state, Macro[] macros,
            Pico[] picos, Mobile[] mobiles) {

        if (state != null) {

            for (int m = 0; m < macros.length; m++) {
                Macro macro = macros[m];
                TableItem item = macroTable.getItem(macro.idx);
                item.setText(4, valueOf(state.macroIsOn(m) ? "ON" : "OFF"));
                item.setText(
                        5,
                        format("%.02f", (seq - macros[m].getAllocationCount())
                                * 100.0 / seq));
            }

            for (int p = 0; p < picos.length; p++) {
                Pico pico = picos[p];
                TableItem item = picoTable.getItem(pico.idx);
                item.setText(4, valueOf(state.picoIsAbs(p) ? "ABS" : "non"));
                item.setText(
                        5,
                        format("%.02f", (seq - picos[p].getNonAbsCount())
                                * 100.0 / seq));
            }

        }

        double sumUtility = 0.0;
        double sumRate = 0.0;
        for (int u = 0; u < mobiles.length; u++) {
            Mobile mobile = mobiles[u];

            sumUtility += log(mobile.getThroughput());
            sumRate += mobile.getUserRate();

            if (updateFrequency > 0 && seq % updateFrequency == 0) {

                int itemIndex = mobileIdxToItems[mobile.idx];
                if (itemIndex < 0)
                    continue;

                TableItem item = mobileTable.getItem(itemIndex);
                String[] texts = new String[11 + NUM_RB * 3];
                int index = 1;
                texts[index++] = null;
                texts[index++] = null;
                texts[index++] = null;
                // texts[index++] = null;
                // texts[index++] = format("%.3f",
                // mobile.getMacro().pa3LambdaR);
                // texts[index++] = format("%.3f",
                // mobile.getMacro().pa3MobileLambdaR[mobile.idx]);

                texts[index++] = null;
                // texts[index++] = null;
                // texts[index++] = format("%.3f",
                // mobile.getPico().pa3LambdaR);
                // texts[index++] = format("%.3f",
                // mobile.getPico().pa3MobileLambdaR[mobile.idx]);

                texts[index++] = format("%.6f", mobile.getUserRate());
                texts[index++] = format("%.6f", log(mobile.getUserRate()));
                texts[index++] = format("%.6f", mobile.getThroughput());
                texts[index++] = format("%.6f", log(mobile.getThroughput()));
                texts[index++] = format("%.6f", mobile.getLambda());
                texts[index++] = format("%.6f", mobile.getMu());

                if (state != null) {

                    Edge<? extends BaseStation<?>>[] activeEdges = mobile
                            .getActiveEdges();
                    double[] macroLambdaR = mobile.getMacroLambdaR();
                    double[] absPicoLambdaR = mobile.getAbsPicoLambdaR();
                    double[] nonPicoLambdaR = mobile.getNonPicoLambdaR();
                    boolean isAbs = state.picoIsAbs(mobile.getPico().idx);
                    for (int j = 0; j < NUM_RB; j++) {
                        String bs = null;
                        double lambdaR = 0;
                        int rank = 0;
                        if (activeEdges[j] == null) {
                            item.setBackground(index + j * 3 + 0, null);
                            item.setBackground(index + j * 3 + 1, null);
                            item.setBackground(index + j * 3 + 2, null);
                        } else {
                            if (activeEdges[j].baseStation instanceof Macro) {
                                Macro macro = (Macro) activeEdges[j].baseStation;
                                bs = "MAC";
                                lambdaR = macroLambdaR[j];
                                rank = macro.getSortedEdges()[j]
                                        .indexOf(activeEdges[j]);
                            } else if (activeEdges[j].baseStation instanceof Pico) {
                                Pico pico = (Pico) activeEdges[j].baseStation;
                                if (isAbs) {
                                    bs = "ABS";
                                    lambdaR = absPicoLambdaR[j];
                                    rank = pico.getSortedAbsEdges()[j]
                                            .indexOf(activeEdges[j]);
                                } else {
                                    bs = "non";
                                    lambdaR = nonPicoLambdaR[j];
                                    rank = pico.getSortedNonEdges()[j]
                                            .indexOf(activeEdges[j]);
                                }
                            }
                            item.setBackground(index + j * 3 + 0, colorActiveBg);
                            item.setBackground(index + j * 3 + 1, colorActiveBg);
                            item.setBackground(index + j * 3 + 2, colorActiveBg);
                        }
                        texts[index + j * 3] = bs == null ? "" : format("%.3f",
                                1000 * lambdaR);
                        texts[index + j * 3 + 1] = bs == null ? "" : bs;
                        texts[index + j * 3 + 2] = bs == null ? ""
                                : valueOf(rank);
                    }
                }

                item.setText(texts);
            }

        }

        return new double[] { sumUtility, sumRate };

    }

    public void setUpdateFrequency(int updateFrequency) {
        this.updateFrequency = updateFrequency;
        mobileTable.setEnabled(updateFrequency != 0);
    }

}
