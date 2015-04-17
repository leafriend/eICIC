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
import org.eclipse.swt.widgets.Combo;
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

    private Combo updateSeq;

    private int selectedIndex;

    private Button executeButton;

    private Button nextButton;

    private Composite tablePanel;

    private Table macroTable;

    private Table picoTable;

    private Table mobileTable;

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

        updateSeq = new Combo(parent, SWT.READ_ONLY);
        String[] items = new String[] { //
        "No Update", //
                "Update for each 1 seq", //
                "Update for each 10 seq", //
                "Update for each 100 seq", //
                "Update for each 1000 seq", //
                "Update for each 10000 seq" //
        };
        updateSeq.setItems(items);
        updateSeq.select(0);
        updateSeq.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedIndex = updateSeq.getSelectionIndex();
                boolean enabled = selectedIndex != 0;
                macroTable.setEnabled(enabled);
                picoTable.setEnabled(enabled);
                mobileTable.setEnabled(enabled);
            }
        });

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

        // updateSeq
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1);
        // layoutData.left = new FormAttachment(100, 100, -64 - 8 - 64);
        layoutData.right = new FormAttachment(executeButton, -8, SWT.LEAD);
        // layoutData.top = new FormAttachment(100, 0);
        updateSeq.setLayoutData(layoutData);

        // executeButton
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(nextButton, -8 - 64, SWT.LEAD);
        layoutData.right = new FormAttachment(nextButton, -8);
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
        macroTable.setEnabled(false);

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
        picoTable.setEnabled(false);

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

        mobileTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        mobileTable.setLinesVisible(true);
        mobileTable.setHeaderVisible(true);
        mobileTable.setEnabled(false);

        mobileTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask & SWT.CTRL) == SWT.CTRL && e.keyCode == 'c') {
                    String text = "";
                    int columnCount = mobileTable.getColumnCount();
                    if ((e.stateMask & SWT.SHIFT) == SWT.SHIFT) {
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
                    clipboard.setContents(new Object[] { text },
                            new Transfer[] { TextTransfer.getInstance() });
                }
                if ((e.stateMask & SWT.CTRL) == SWT.CTRL && e.keyCode == 'a') {
                    mobileTable.setSelection(0, mobileTable.getItemCount() - 1);
                }
            }
        });

        addColumn(mobileTable, 32, "#");
        addColumn(mobileTable, 80, "X");
        addColumn(mobileTable, 80, "Y");
        addColumn(mobileTable, 32, "M");
        addColumn(mobileTable, 80, "M. Dist.");
        addColumn(mobileTable, 80, "M. " + LAMBDA + "R");
        addColumn(mobileTable, 80, "M. M. " + LAMBDA + "R");
        addColumn(mobileTable, 32, "P");
        addColumn(mobileTable, 80, "P. Dist.");
        addColumn(mobileTable, 80, "P. " + LAMBDA + "R");
        addColumn(mobileTable, 80, "P. M. " + LAMBDA + "R");
        addColumn(mobileTable, 96, "User Rate");
        addColumn(mobileTable, 96, "log(User Rate)");
        addColumn(mobileTable, 96, "Throughput");
        addColumn(mobileTable, 96, "log(Throughput)");
        addColumn(mobileTable, 96, LAMBDA);
        addColumn(mobileTable, 96, MU);

        for (int i = 0; i < NUM_RB; i++) {
            addColumn(mobileTable, 80, LAMBDA + "R " + i);
            addColumn(mobileTable, 48, "BS " + i, SWT.LEFT);
        }

        for (int i = 0; i < NUM_MOBILES; i++) {
            TableItem item = new TableItem(mobileTable, SWT.NONE);
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
        mobileTable.setLayoutData(layoutData);

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
        layoutData.right = new FormAttachment(utilityLabel, 64, SWT.TRAIL);
        utilityText.setLayoutData(layoutData);

    }

    public static void addColumn(Table table, int width, String text) {
        addColumn(table, width, text, SWT.RIGHT);
    }

    public static void addColumn(Table table, int width, String text, int alignment) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(text);
        column.setAlignment(alignment);
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
            TableItem item = mobileTable.getItem(mobile.idx);
            showMobile(mobile, item);
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

    public void showMobile(Mobile mobile, TableItem item) {
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
        texts[i++] = format("%.6f", Double.NEGATIVE_INFINITY);
        texts[i++] = format("%.6f", mobile.getLambda());
        texts[i++] = format("%.6f", mobile.getMu());

        item.setText(texts);
    }

    private boolean dumped = true;

    @Override
    public long dump(final int seq, final List<Macro> macros,
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
                seqText.setText(valueOf(seq));

                long sec = elapsed / 1000;
                long mil = elapsed - sec * 1000;

                long min = sec / 60;
                sec -= min * 60;

                long hour = min / 60;
                min -= hour * 60;

                elapsedText.setText(format("%02d:%02d:%02d.%03d", hour, min,
                        sec, mil));

                int frequncy;
                switch (selectedIndex) {
                case 0:
                    frequncy = 0;
                    // No Update
                    break;
                case 1:
                    frequncy = 1;
                    // Update for each 1 seq
                    break;
                case 2:
                    frequncy = 10;
                    // Update for each 10 seq
                    break;
                case 3:
                    frequncy = 100;
                    // Update for each 100 seq
                    break;
                case 4:
                    frequncy = 1000;
                    // Update for each 1000 seq
                    break;
                case 5:
                    frequncy = 10000;
                    // Update for each 10000 seq
                    break;
                default:
                    throw new RuntimeException("Unsupported frequency: "
                            + updateSeq.getItem(updateSeq.getSelectionIndex()));
                }
                if (seq == SIMULATION_TIME)
                    frequncy = 1;

                if (frequncy > 0 && seq % frequncy == 0) {

                    for (Macro macro : macros) {
                        TableItem item = macroTable.getItem(macro.idx);
                        item.setText(4, valueOf(macro.state ? "ON" : "OFF"));
                    }

                    for (Pico pico : picos) {
                        TableItem item = picoTable.getItem(pico.idx);
                        item.setText(4, valueOf(pico.isAbs() ? "ABS" : "non"));
                    }

                }

                double throughput = 0.0;
                for (Mobile mobile : mobiles) {

                    throughput += log(mobile.getThroughput() / seq);

                    if (frequncy > 0 && seq % frequncy == 0) {

                        TableItem item = mobileTable.getItem(mobile.idx);
                        String[] texts = new String[17 + NUM_RB * 2];
                        int index = 1;
                        texts[index++] = null;
                        texts[index++] = null;
                        texts[index++] = null;
                        texts[index++] = null;
                        texts[index++] = format("%.3f",
                                mobile.getMacro().pa3LambdaR);
                        texts[index++] = format("%.3f",
                                mobile.getMacro().pa3MobileLambdaR[mobile.idx]);

                        texts[index++] = null;
                        texts[index++] = null;
                        texts[index++] = format("%.3f", mobile.getPico().pa3LambdaR);
                        texts[index++] = format("%.3f",
                                mobile.getPico().pa3MobileLambdaR[mobile.idx]);

                        texts[index++] = format("%.6f", mobile.getUserRate());
                        texts[index++] = format("%.6f", log(mobile.getUserRate()));
                        texts[index++] = format("%.6f", mobile.getThroughput()
                                / seq);
                        texts[index++] = format("%.6f", log(mobile.getThroughput()
                                / seq));
                        texts[index++] = format("%.6f", mobile.getLambda());
                        texts[index++] = format("%.6f", mobile.getMu());

                        Edge<? extends BaseStation<?>>[] activeEdges = mobile
                                .getActiveEdges();
                        double[] macroLambdaR = mobile.getMacroLambdaR();
                        double[] absPicoLambdaR = mobile.getAbsPicoLambdaR();
                        double[] nonPicoLambdaR = mobile.getNonPicoLambdaR();
                        boolean isAbs = mobile.getPico().isAbs();
                        for (int j = 0; j < NUM_RB; j++) {
                            String bs = null;
                            double lambdaR = 0;
                            if (activeEdges[j] != null) {
                                if (activeEdges[j].baseStation instanceof Macro) {
                                    bs = "MAC";
                                    lambdaR = macroLambdaR[j];
                                } else if (activeEdges[j].baseStation instanceof Pico) {
                                    if (isAbs) {
                                        bs = "abs";
                                        lambdaR = absPicoLambdaR[j];
                                    } else {
                                        bs = "non";
                                        lambdaR = nonPicoLambdaR[j];
                                    }
                                }
                            }
                            texts[index + j * 2] = bs == null ? "" : format("%.3f",
                                    1000 * lambdaR);
                            texts[index + j * 2 + 1] = bs == null ? "" : bs;
                        }
                        item.setText(texts);
                    }

                }

                utilityText.setText(format("%.3f", throughput));

                dumped = true;
            }
        });
        return System.currentTimeMillis();
    }

}
