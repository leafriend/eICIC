package net.folab.eicic;

import static java.lang.Math.*;
import static java.lang.String.*;
import static net.folab.eicic.Constants.*;

import java.util.List;

import net.folab.eicic.model.BaseStation;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class GuiConsole implements Console {

    public static final String LAMBDA = "\u03bb";

    public static final String MU = "\u03bc";

    private Display display;

    private Clipboard clipboard;

    private Shell shell;

    private Composite statusBar;

    private Text seqText;

    private Text elapsedText;

    private Composite buttonPanel;

    private SelectionAdapter executeButtonListener;

    private Text utilityText;

    private Button executeButton;

    private Button nextButton;

    private Composite tablePanel;

    private Table macroTable;

    private Table picoTable;

    private Table table;

    private Calculator calculator;

    public GuiConsole() {

        display = new Display();

        clipboard = new Clipboard(display);

        shell = new Shell(display);
        shell.setText("eICIC");
        buildShell(shell);

    }

    public void buildShell(Composite parent) {

        buttonPanel = new Composite(parent, SWT.NONE);
        buildButtonPannel(buttonPanel);

        tablePanel = new Composite(parent, SWT.NONE);
        buildTablePanel(tablePanel);

        statusBar = new Composite(parent, SWT.NONE);
        buildStatusBar(statusBar);

        // - - -

        parent.setLayout(new FormLayout());
        FormData layoutData;

        // buttonPanel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 8);
        layoutData.left = new FormAttachment(0, 8);
        layoutData.right = new FormAttachment(100, -8);
        // layoutData.bottom = new FormAttachment(statusBar, 8);
        buttonPanel.setLayoutData(layoutData);

        // tablePanel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(buttonPanel, 8);
        layoutData.left = new FormAttachment(0, 8);
        layoutData.right = new FormAttachment(100, -8);
        layoutData.bottom = new FormAttachment(statusBar, -8);
        tablePanel.setLayoutData(layoutData);

        // statusBar
        layoutData = new FormData();
        // layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(0, 8);
        layoutData.right = new FormAttachment(100, -8);
        layoutData.bottom = new FormAttachment(100, -8);
        statusBar.setLayoutData(layoutData);

    }

    public void buildButtonPannel(Composite parent) {

        executeButton = new Button(parent, SWT.PUSH);
        executeButton.setText("&Start");
        executeButtonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (calculator != null) {
                    String text = executeButton.getText();
                    if ("Pau&se".endsWith(text)) {
                        calculator.stop();
                        executeButton.setText("&Start");
                        nextButton.setEnabled(true);
                    } else if ("&Start".endsWith(text)) {
                        calculator.calculate(SIMULATION_TIME);
                        executeButton.setText("Pau&se");
                        nextButton.setEnabled(false);
                    }
                }
            }
        };
        executeButton.addSelectionListener(executeButtonListener);

        // - - -

        nextButton = new Button(parent, SWT.PUSH);
        nextButton.setText("N&ext");
        nextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (calculator != null) {
                    calculator.calculate();
                }
            }
        });

        // - - -

        parent.setLayout(new FormLayout());
        FormData layoutData;

        // executeButton
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(100, 100, -64 - 8 - 64);
        layoutData.right = new FormAttachment(100, 100, -64 - 8);
        // layoutData.top = new FormAttachment(100, 0);
        executeButton.setLayoutData(layoutData);

        // nextButton
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(100, 100, -64);
        layoutData.right = new FormAttachment(100, 0);
        // layoutData.top = new FormAttachment(100, 0);
        nextButton.setLayoutData(layoutData);

    }

    public void buildTablePanel(Composite parent) {

        macroTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        macroTable.setLinesVisible(true);
        macroTable.setHeaderVisible(true);
        addColumn(macroTable, 32, "#");
        addColumn(macroTable, 80, "X");
        addColumn(macroTable, 80, "Y");
        addColumn(macroTable, 56, "Tx Power");
        addColumn(macroTable, 56, "State");

        for (int i = 0; i < NUM_MACROS; i++) {
            TableItem item = new TableItem(macroTable, SWT.NONE);
            item.setText(0, valueOf(i));
        }

        // - - -

        picoTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        picoTable.setLinesVisible(true);
        picoTable.setHeaderVisible(true);
        addColumn(picoTable, 32, "#");
        addColumn(picoTable, 80, "X");
        addColumn(picoTable, 80, "Y");
        addColumn(picoTable, 56, "Tx Power");
        addColumn(picoTable, 56, "State");

        for (int i = 0; i < NUM_PICOS; i++) {
            TableItem item = new TableItem(picoTable, SWT.NONE);
            item.setText(0, valueOf(i));
        }

        // - - -

        table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask & SWT.CTRL) == SWT.CTRL && e.keyCode == 'c') {
                    String text = "";
                    int columnCount = table.getColumnCount();
                    if ((e.stateMask & SWT.SHIFT) == SWT.SHIFT) {
                        for (int c = 0; c < columnCount; c++) {
                            if (c > 0)
                                text += "\t";
                            text += table.getColumn(c).getText();
                        }
                    }
                    for (TableItem item : table.getSelection()) {
                        if (text.length() > 0)
                            text += "\n";
                        for (int c = 0; c < columnCount; c++) {
                            if (c > 0)
                                text += "\t";
                            text += item.getText(c);
                        }
                    }
                    clipboard.setContents(new Object[] { text },
                            new Transfer[] { TextTransfer.getInstance() });
                }
                if ((e.stateMask & SWT.CTRL) == SWT.CTRL && e.keyCode == 'a') {
                    table.setSelection(0, table.getItemCount() - 1);
                }
            }
        });

        addColumn(table, 32, "#");
        addColumn(table, 80, "X");
        addColumn(table, 80, "Y");
        addColumn(table, 32, "M");
        addColumn(table, 80, "M. Dist.");
        addColumn(table, 80, "M. " + LAMBDA + "R");
        addColumn(table, 80, "M. M. " + LAMBDA + "R");
        addColumn(table, 32, "P");
        addColumn(table, 80, "P. Dist.");
        addColumn(table, 80, "P. " + LAMBDA + "R");
        addColumn(table, 80, "P. M. " + LAMBDA + "R");
        addColumn(table, 96, "User Rate");
        addColumn(table, 96, "log(User Rate)");
        addColumn(table, 96, "Throughput");
        addColumn(table, 96, "log(Throughput)");
        addColumn(table, 96, LAMBDA);
        addColumn(table, 96, MU);

        for (int i = 0; i < NUM_RB; i++)
            addColumn(table, 96, valueOf(i));

        for (int i = 0; i < NUM_MOBILES; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, valueOf(i));
        }

        // - - -

        parent.setLayout(new FormLayout());
        FormData layoutData;

        // macroTable
        layoutData = new FormData();
        layoutData.top = new FormAttachment(statusBar, 0);
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(0, 320);
        // layoutData.bottom = new FormAttachment(100, 0);
        macroTable.setLayoutData(layoutData);

        // picoTable
        layoutData = new FormData();
        layoutData.top = new FormAttachment(macroTable, 8);
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(0, 320);
        // layoutData.bottom = new FormAttachment(100, 0);
        picoTable.setLayoutData(layoutData);

        // table
        layoutData = new FormData();
        layoutData.top = new FormAttachment(statusBar, 0);
        layoutData.left = new FormAttachment(macroTable, 8);
        layoutData.right = new FormAttachment(100, 0);
        layoutData.bottom = new FormAttachment(100, 0);
        table.setLayoutData(layoutData);

    }

    public void buildStatusBar(Composite parent) {

        Label seqLabel = new Label(parent, SWT.NONE);
        seqLabel.setText("Seq:");

        seqText = new Text(parent, SWT.READ_ONLY);
        seqText.setText("0");

        // - - -

        Label elapsedLabel = new Label(parent, SWT.NONE);
        elapsedLabel.setText("Execute:");

        elapsedText = new Text(parent, SWT.READ_ONLY | SWT.RIGHT);
        elapsedText.setText("00:00:00.000");

        // - - -

        Label utilityLabel = new Label(parent, SWT.NONE);
        utilityLabel.setText("Utility:");

        utilityText = new Text(parent, SWT.READ_ONLY | SWT.RIGHT);
        utilityText.setText("0.000");

        // - - -

        parent.setLayout(new FormLayout());
        FormData layoutData;

        //

        // seqLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(0, 0);
        seqLabel.setLayoutData(layoutData);

        // seqText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(seqLabel, 0);
        layoutData.right = new FormAttachment(seqLabel, 64, SWT.TRAIL);
        seqText.setLayoutData(layoutData);

        //

        // elapsedLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(seqText, 8);
        elapsedLabel.setLayoutData(layoutData);

        // elapsedText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(elapsedLabel, 0);
        elapsedText.setLayoutData(layoutData);

        //

        // utilityLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(elapsedText, 8);
        utilityLabel.setLayoutData(layoutData);

        // utilityText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(utilityLabel, 0);
        utilityText.setLayoutData(layoutData);

    }

    public static void addColumn(Table table, int width, String text) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(text);
        column.setAlignment(SWT.RIGHT);
        column.setWidth(width);
    }

    @Override
    public void start(final Calculator calculator) {
        this.calculator = calculator;

        // - - -

        List<Macro> macros = calculator.getMacros();
        for (Macro macro : macros) {
            TableItem item = macroTable.getItem(macro.idx);
            item.setText(1, valueOf(format("%.3f", macro.x)));
            item.setText(2, valueOf(format("%.3f", macro.y)));
            item.setText(3, valueOf(format("%.2f", macro.txPower)));
        }

        List<Pico> picos = calculator.getPicos();
        for (Pico pico : picos) {
            TableItem item = picoTable.getItem(pico.idx);
            item.setText(1, valueOf(format("%.3f", pico.x)));
            item.setText(2, valueOf(format("%.3f", pico.y)));
            item.setText(3, valueOf(format("%.2f", pico.txPower)));
        }

        List<Mobile> mobiles = calculator.getMobiles();
        for (Mobile mobile : mobiles) {
            String[] texts = new String[17 + NUM_RB];
            int i = 1;
            texts[i++] = format("%.3f", mobile.x);
            texts[i++] = format("%.3f", mobile.y);
            texts[i++] = valueOf(mobile.getMacro().idx);
            texts[i++] = format("%.3f", mobile.getMacroEdge().distance);
            texts[i++] = format("%.3f", mobile.getMacro().pa3LambdaR);
            texts[i++] = null;

            texts[i++] = valueOf(mobile.getPico().idx);
            texts[i++] = format("%.3f", mobile.getPicoEdge().distance);
            texts[i++] = format("%.3f", mobile.getPico().pa3LambdaR);
            texts[i++] = null;

            texts[i++] = format("%.6f", mobile.getUserRate());
            texts[i++] = format("%.6f", log(mobile.getUserRate()));
            texts[i++] = format("%.6f", 0.0);
            texts[i++] = format("%.6f", Double.NaN);
            texts[i++] = format("%.6f", mobile.getLambda());
            texts[i++] = format("%.6f", mobile.getMu());

            TableItem item = table.getItem(mobile.idx);
            item.setText(texts);
            ;
        }
        // - - -

        shell.setSize(1024, 768);
        shell.open();
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                calculator.stop();
            }
        });
        executeButton.setFocus();

        while (!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();

        display.dispose();

    }

    private boolean dumped = true;

    @Override
    public long dump(final int t, final List<Macro> macros,
            final List<Pico> picos, final List<Mobile> mobiles,
            final long elapsed, final long execute) {
        // if (calculator.isRunning() && t % 5 != 0)
        // return elapsed;
        if (!dumped)
            return -1;
        dumped = false;
        if (display.isDisposed())
            return -1;
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if (shell.isDisposed())
                    return;
                seqText.setText(valueOf(t));

                long sec = elapsed / 1000;
                long mil = elapsed - sec * 1000;

                long min = sec / 60;
                sec -= min * 60;

                long hour = min / 60;
                min -= hour * 60;

                elapsedText.setText(format("%02d:%02d:%02d.%03d", hour, min,
                        sec, mil));

                // *

                for (Macro macro : macros) {
                    TableItem item = macroTable.getItem(macro.idx);
                    item.setText(4, valueOf(macro.state ? "ON" : "OFF"));
                }

                for (Pico pico : picos) {
                    TableItem item = picoTable.getItem(pico.idx);
                    item.setText(4, valueOf(pico.isAbs() ? "ABS" : "non"));
                }

                // */

                double throughput = 0.0;
                for (Mobile mobile : mobiles) {
                    throughput += log(mobile.getThroughput() / t);
                    // *
                    TableItem item = table.getItem(mobile.idx);
                    String[] texts = new String[17 + NUM_RB];
                    int i = 1;
                    texts[i++] = null;
                    texts[i++] = null;
                    texts[i++] = null;
                    texts[i++] = null;
                    texts[i++] = format("%.3f", mobile.getMacro().pa3LambdaR);
                    texts[i++] = format("%.3f",
                            mobile.getMacro().pa3MobileLambdaR[mobile.idx]);

                    texts[i++] = null;
                    texts[i++] = null;
                    texts[i++] = format("%.3f", mobile.getPico().pa3LambdaR);
                    texts[i++] = format("%.3f",
                            mobile.getPico().pa3MobileLambdaR[mobile.idx]);

                    texts[i++] = format("%.6f", mobile.getUserRate());
                    texts[i++] = format("%.6f", log(mobile.getUserRate()));
                    texts[i++] = format("%.6f", mobile.getThroughput() / t);
                    texts[i++] = format("%.6f", log(mobile.getThroughput() / t));
                    texts[i++] = format("%.6f", mobile.getLambda());
                    texts[i++] = format("%.6f", mobile.getMu());
                    Edge<? extends BaseStation<?>>[] activeEdges = mobile
                            .getActiveEdges();
                    double[] macroLambdaR = mobile.getMacroLambdaR();
                    double[] absPicoLambdaR = mobile.getAbsPicoLambdaR();
                    double[] nonPicoLambdaR = mobile.getNonPicoLambdaR();
                    boolean isAbs = mobile.getPico().isAbs();
                    for (int j = 0; j < NUM_RB; j++) {
                        String text = "";
                        if (activeEdges[j] != null) {
                            if (activeEdges[j].baseStation instanceof Macro) {
                                text = format("%.3f", 1000 * macroLambdaR[j])
                                        + " M";
                            } else if (activeEdges[j].baseStation instanceof Pico) {
                                if (isAbs)
                                    text = format("%.3f",
                                            1000 * absPicoLambdaR[j]) + " P";
                                else
                                    text = format("%.3f",
                                            1000 * nonPicoLambdaR[j]) + " p";
                            }
                        }
                        texts[i + j] = text;
                    }
                    item.setText(texts);
                    // */
                }

                utilityText.setText(format("%.3f", throughput));

                dumped = true;
            }
        });
        return System.currentTimeMillis();
    }

}
