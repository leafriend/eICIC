package net.folab.eicic;

import static java.lang.Math.*;
import static java.lang.String.*;
import static net.folab.eicic.Constants.*;

import java.util.List;

import net.folab.eicic.algorithm.Algorithm;
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

    private Algorithm algorithm;

    private Display display;

    private Clipboard clipboard;

    private Shell shell;

    private Composite dashboard;

    private Label timeLabel;

    private Text timeText;

    private Label executeLabel;

    private Text executeText;

    private SelectionAdapter executeButtonListener;

    private Label utilityLabel;

    private Text utilityText;

    private Button executeButton;

    private Button nextButton;

    private Table macroTable;

    private Table picoTable;

    private Table table;

    private Main executor;

    public GuiConsole(final Algorithm algorithm) {

        this.algorithm = algorithm;

        display = new Display();

        clipboard = new Clipboard(display);

        shell = new Shell(display);
        shell.setText("eICIC");
        shell.setLayout(new FormLayout());

        Composite parent = shell;

        dashboard = new Composite(parent, SWT.NONE);

        FormData layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(100, 0);
        // layoutData.bottom = new FormAttachment(100, 0);
        dashboard.setLayoutData(layoutData);

        timeLabel = new Label(dashboard, SWT.NONE);
        timeLabel.setText("Time:");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 5);
        layoutData.left = new FormAttachment(0, 8);
        timeLabel.setLayoutData(layoutData);

        timeText = new Text(dashboard, SWT.READ_ONLY);
        timeText.setText("0");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 5);
        layoutData.left = new FormAttachment(timeLabel, 0);
        layoutData.right = new FormAttachment(timeLabel, 64, SWT.RIGHT);
        timeText.setLayoutData(layoutData);

        // - - -

        executeLabel = new Label(dashboard, SWT.NONE);
        executeLabel.setText("Execute:");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 5);
        layoutData.left = new FormAttachment(timeText, 8);
        //executeLabel.pack();
        executeLabel.setLayoutData(layoutData);

        executeText = new Text(dashboard, SWT.READ_ONLY | SWT.RIGHT);
        executeText.setText("00:00:00.000");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 5);
        layoutData.left = new FormAttachment(executeLabel, 0);
        //layoutData.right = new FormAttachment(executeLabel, 64, SWT.RIGHT);
        executeLabel.pack();
        executeText.setLayoutData(layoutData);

        // - - -

        utilityLabel = new Label(dashboard, SWT.NONE);
        utilityLabel.setText("Utility:");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 5);
        layoutData.left = new FormAttachment(executeText, 8);
        //utilityLabel.pack();
        utilityLabel.setLayoutData(layoutData);

        utilityText = new Text(dashboard, SWT.READ_ONLY | SWT.RIGHT);
        utilityText.setText("0.000");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 5);
        layoutData.left = new FormAttachment(utilityLabel, 0);
        //layoutData.right = new FormAttachment(utilityLabel, 64, SWT.RIGHT);
        utilityLabel.pack();
        utilityText.setLayoutData(layoutData);

        // - - -

        executeButton = new Button(dashboard, SWT.PUSH);
        executeButton.setText("&Start");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(100, 100, -8 -64 -64);
        layoutData.right = new FormAttachment(100, 100, -8 -64);
        executeButton.setLayoutData(layoutData);
        executeButtonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (executor != null) {
                    String text = executeButton.getText();
                    if ("Pau&se".endsWith(text)) {
                        executor.stop();
                        executeButton.setText("&Start");
                        nextButton.setEnabled(true);
                    } else if ("&Start".endsWith(text)) {
                        executor.execute(GuiConsole.this, algorithm, SIMULATION_TIME);
                        executeButton.setText("Pau&se");
                        nextButton.setEnabled(false);
                    }
                }
            }
        };
        executeButton.addSelectionListener(executeButtonListener);

        // - - -

        nextButton = new Button(dashboard, SWT.PUSH);
        nextButton.setText("N&ext");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(100, 100, -64);
        layoutData.right = new FormAttachment(100, 0);
        nextButton.setLayoutData(layoutData);
        nextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (executor != null) {
                    executor.execute(GuiConsole.this, algorithm, executor.getTime() + 1);
                }
            }
        });

        // - - -

        dashboard.setLayout(new FormLayout());

        macroTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        macroTable.setLinesVisible(true);
        macroTable.setHeaderVisible(true);
        layoutData = new FormData();
        layoutData.top = new FormAttachment(dashboard, 0);
        layoutData.left = new FormAttachment(0, 8);
        layoutData.right = new FormAttachment(0, 320);
        //layoutData.bottom = new FormAttachment(100, 0);
        macroTable.setLayoutData(layoutData);
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
        layoutData = new FormData();
        layoutData.top = new FormAttachment(macroTable, 8);
        layoutData.left = new FormAttachment(0, 8);
        layoutData.right = new FormAttachment(0, 320);
        //layoutData.bottom = new FormAttachment(100, 0);
        picoTable.setLayoutData(layoutData);
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
        layoutData = new FormData();
        layoutData.top = new FormAttachment(dashboard, 0);
        layoutData.left = new FormAttachment(macroTable, 8);
        layoutData.right = new FormAttachment(100, -8);
        layoutData.bottom = new FormAttachment(100, -8);
        table.setLayoutData(layoutData);

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask & SWT.CTRL) == SWT.CTRL && e.keyCode == 'c') {
                    String text = "";
                    for (TableItem item : table.getSelection()) {
                        if (text.length() > 0)
                            text += "\n";
                        for (int c = 0; c < table.getColumnCount(); c++) {
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

    }

    public static void addColumn(Table table, int width, String text) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(text);
        column.setAlignment(SWT.RIGHT);
        column.setWidth(width);
    }

    @Override
    public void start(final Main executor) {
        this.executor = executor;
        SelectionEvent e = null;
        executeButtonListener.widgetSelected(e);

        shell.open();
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                executor.stop();
            }
        });
        executeButton.setFocus();

        while (!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();

        display.dispose();

    }

    @Override
    public long dump(final int t, final List<Macro> macros, final List<Pico> picos,
            final List<Mobile> mobiles, final long elapsed, final long execute) {
        if (executor.isRunning() &&  t % 10 != 0)
            return elapsed;
        if (display.isDisposed())
            return -1;
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if (shell.isDisposed())
                    return;
                timeText.setText(valueOf(t));

                long now = System.currentTimeMillis() - execute;

                long sec = now / 1000;
                long mil = now - sec * 1000;

                long min = sec / 60;
                sec -= min * 60;

                long hour = min / 60;
                min -= hour * 60;

                executeText.setText(format("%02d:%02d:%02d.%03d", hour, min, sec, mil));

                for (Macro macro : macros) {
                    TableItem item = macroTable.getItem(macro.idx);
                    item.setText(1, valueOf(format("%.3f", macro.x)));
                    item.setText(2, valueOf(format("%.3f", macro.y)));
                    item.setText(3, valueOf(format("%.2f", macro.txPower)));
                    item.setText(4, valueOf(macro.state ? "ON" : "OFF"));
                }

                for (Pico pico : picos) {
                    TableItem item = picoTable.getItem(pico.idx);
                    item.setText(1, valueOf(format("%.3f", pico.x)));
                    item.setText(2, valueOf(format("%.3f", pico.y)));
                    item.setText(3, valueOf(format("%.2f", pico.txPower)));
                    item.setText(4, valueOf(pico.isAbs() ? "ABS" : "non"));
                }

                double throughput = 0.0;
                for (Mobile mobile : mobiles) {
                    throughput += mobile.getThroughput() == 0.0 ? 0.0 : log(mobile.getThroughput() / t);
                    TableItem item = table.getItem(mobile.idx);
                    String[] texts = new String[17 + NUM_RB];
                    int i = 1;
                    texts[i++] = format("%.3f", mobile.x);
                    texts[i++] = format("%.3f", mobile.y);
                    texts[i++] = valueOf(mobile.getMacro().idx);
                    texts[i++] = format("%.3f", mobile.getMacroEdge().distance);
                    texts[i++] = format("%.3f", mobile.getMacro().pa3LambdaR);
                    texts[i++] = format("%.3f", mobile.getMacro().pa3MobileLambdaR[mobile.idx]);

                    texts[i++] = valueOf(mobile.getPico().idx);
                    texts[i++] = format("%.3f", mobile.getPicoEdge().distance);
                    texts[i++] = format("%.3f", mobile.getPico().pa3LambdaR);
                    texts[i++] = format("%.3f", mobile.getPico().pa3MobileLambdaR[mobile.idx]);

                    texts[i++] = format("%.6f", mobile.getUserRate());
                    texts[i++] = format("%.6f", log(mobile.getUserRate()));
                    texts[i++] = format("%.6f", mobile.getThroughput() / t);
                    texts[i++] = format("%.6f", log(mobile.getThroughput() / t));
                    texts[i++] = format("%.6f", mobile.getLambda());
                    texts[i++] = format("%.6f", mobile.getMu());
                    Edge<? extends BaseStation<?>>[] activeEdges = mobile.getActiveEdges();
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
                }

                utilityText.setText(format("%.3f", throughput));

            }
        });
        return System.currentTimeMillis();
    }

}
