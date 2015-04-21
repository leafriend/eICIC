package net.folab.eicic;

import static java.lang.Math.*;
import static java.lang.String.*;
import static net.folab.eicic.Constants.*;
import static org.eclipse.swt.SWT.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.algorithm.Algorithm1;
import net.folab.eicic.algorithm.Algorithm2;
import net.folab.eicic.algorithm.Algorithm3;
import net.folab.eicic.model.BaseStation;
import net.folab.eicic.model.Edge;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;
import net.folab.eicic.ui.Controller;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class GuiConsole implements Console {

    private static final String START = "Sta&rt";

    private static final String PAUSE = "P&ause";

    private static final String ALGORITHM_1 = "1: Algorithm 1";

    private static final String ALGORITHM_2 = "2: Algorithm 2";

    private static final String ALGORITHM_3 = "3: Algorithm 3";

    public static final String LAMBDA = "\u03bb";

    public static final String MU = "\u03bc";

    private Display display;

    private Clipboard clipboard;

    private Shell shell;

    private Composite statusBar;

    private Text seqText;

    private Text totalSeqText;

    private Text executionTimeText;

    private Label estimationTimeLabel;

    private Composite buttonPanel;

    private SelectionAdapter executeButtonListener;

    private Text utilityText;

    private Combo algorithmeCombo;

    private Button saveButton;

    private int saved;

    private Button showActiveButton;

    private Combo updateSeq;

    private int selectedIndex;

    private Button resetButton;

    private Button executeButton;

    private Button nextButton;

    private Composite tablePanel;

    private Table macroTable;

    private Table picoTable;

    private Table mobileTable;

    private int[] mobileIdxToItems = new int[NUM_MOBILES];

    private Controller controller;

    private Color colorActiveBg;

    public GuiConsole() {

        display = new Display();

        colorActiveBg = new Color(display, 225, 255, 225);

        clipboard = new Clipboard(display);

        shell = new Shell(display);
        shell.setText("eICIC");
        buildShell(shell);

    }

    public void buildShell(Composite parent) {

        buttonPanel = new Composite(parent, NONE);
        buildButtonPannel(buttonPanel);

        tablePanel = new Composite(parent, NONE);
        buildTablePanel(tablePanel);

        statusBar = new Composite(parent, NONE);
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

        algorithmeCombo = new Combo(parent, READ_ONLY);
        algorithmeCombo.setItems(new String[] { ALGORITHM_1, ALGORITHM_2,
                ALGORITHM_3 });
        algorithmeCombo.select(0);
        algorithmeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setAlgorithm();
            }
        });

        saveButton = new Button(parent, PUSH);
        saveButton.setText("&Save");
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                save();
            }
        });

        showActiveButton = new Button(parent, CHECK);
        showActiveButton.setText("Show active only");
        showActiveButton.setSelection(true);

        updateSeq = new Combo(parent, READ_ONLY);
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
                mobileTable.setEnabled(enabled);
            }
        });

        resetButton = new Button(parent, PUSH);
        resetButton.setText("Rese&t");
        resetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                controller.reset();
            }
        });

        executeButton = new Button(parent, PUSH);
        executeButton.setText(START);
        executeButtonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String text = executeButton.getText();
                if (PAUSE.endsWith(text)) {
                    controller.pause();
                    ;
                    setRunningState(false);
                } else if (START.endsWith(text)) {
                    setAlgorithm();
                    setRunningState(true);
                    String number = totalSeqText.getText().replaceAll(",", "");
                    controller.setTotalSeq(Integer.parseInt(number));
                    controller.start();
                }
            }
        };
        executeButton.addSelectionListener(executeButtonListener);

        // - - -

        nextButton = new Button(parent, PUSH);
        nextButton.setText("N&ext");
        nextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                controller.next();
            }
        });

        // - - -

        parent.setLayout(new FormLayout());
        FormData layoutData;

        // algorithmeCombo
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1);
        layoutData.left = new FormAttachment(0);
        // layoutData.right = new FormAttachment(updateSeq, -8, LEAD);
        // layoutData.top = new FormAttachment(100, 0);
        algorithmeCombo.setLayoutData(layoutData);

        // saveButton
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(algorithmeCombo, 8);
        layoutData.right = new FormAttachment(algorithmeCombo, 8 + 64, TRAIL);
        // layoutData.top = new FormAttachment(100, 0);
        saveButton.setLayoutData(layoutData);

        // showActiveButton
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 5);
        // layoutData.left = new FormAttachment(100, 100, -64 - 8 - 64);
        layoutData.right = new FormAttachment(updateSeq, -8, LEAD);
        // layoutData.top = new FormAttachment(100, 0);
        showActiveButton.setLayoutData(layoutData);

        // updateSeq
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1);
        // layoutData.left = new FormAttachment(100, 100, -64 - 8 - 64);
        layoutData.right = new FormAttachment(resetButton, -8, LEAD);
        // layoutData.top = new FormAttachment(100, 0);
        updateSeq.setLayoutData(layoutData);

        // resetButton
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(executeButton, -8 - 64, LEAD);
        layoutData.right = new FormAttachment(executeButton, -8);
        // layoutData.top = new FormAttachment(100, 0);
        resetButton.setLayoutData(layoutData);

        // executeButton
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(nextButton, -8 - 64, LEAD);
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

    public boolean save() {
        FileDialog dialog = new FileDialog(shell, SAVE);
        dialog.setText("Save");
        // dialog.setFilterPath(filterPath);
        String[] filterExt = { "*.csv", "*.txt", "*.*" };
        dialog.setFilterExtensions(filterExt);
        int pa = algorithmeCombo.getSelectionIndex() + 1;
        String fileName = format("PA%d-%d.csv", pa,
                controller.getSeq());
        dialog.setFileName(fileName);
        String selected = dialog.open();
        if (selected != null) {
            save(selected);
            return true;
        } else {
            return false;
        }
    }

    private void save(String selected) {
        try {

            int seq = controller.getSeq();

            String delim = selected.toLowerCase().endsWith(".csv") ? "," : "\t";

            Charset charset = Charset.forName(System
                    .getProperty("file.encoding"));

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(selected)), charset));

            writer.write("#Utitlity");
            writer.write(delim);
            writer.write(utilityText.getText());
            writer.write("\n");
            writer.flush();

            writer.write("#Seq");
            writer.write(delim);
            writer.write(valueOf(seq));
            writer.write("\n");
            writer.flush();

            writer.write("#Time");
            writer.write(delim);
            writer.write(executionTimeText.getText());
            writer.write("\n");
            writer.flush();

            writer.write("#Macro Count");
            for (int m = 0; m < controller.getMacros().length; m++) {
                writer.write(delim);
                writer.write(valueOf(controller.getMacros()[m].getAllocationCount()));
            }
            writer.write("\n");
            writer.flush();

            writer.write("#Macro %");
            for (int m = 0; m < controller.getMacros().length; m++) {
                writer.write(delim);
                double percent = 100.0 * controller.getMacros()[m].getAllocationCount() / seq;
                writer.write(format("%.2f%%", percent));
            }
            writer.write("\n");
            writer.flush();

            int cc = mobileTable.getColumnCount();

            for (int c = 0; c < cc; c++) {
                if (c > 0)
                    writer.write(delim);
                writer.write(mobileTable.getColumn(c).getText());
            }
            writer.write("\n");
            writer.flush();

            int rc = mobileTable.getItemCount();
            for (int r = 0; r < rc; r++) {
                TableItem item = mobileTable.getItem(r);
                for (int c = 0; c < cc; c++) {
                    if (c > 0)
                        writer.write(delim);
                    writer.write(item.getText(c));
                }
                writer.write("\n");
                writer.flush();
            }

            writer.close();

            saved = controller.getSeq();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void buildTablePanel(Composite parent) {

        macroTable = new Table(parent, BORDER | FULL_SELECTION);
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
                long elapsed = 0;
                dump(seq, null, macros, picos, mobiles, elapsed);
            }
        });

        // - - -

        picoTable = new Table(parent, BORDER | FULL_SELECTION);
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

        mobileTable = new Table(parent, BORDER | FULL_SELECTION | MULTI);
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
                    clipboard.setContents(new Object[] { text },
                            new Transfer[] { TextTransfer.getInstance() });
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

        parent.setLayout(new FormLayout());
        FormData layoutData;

        // macroTable
        layoutData = new FormData();
        layoutData.top = new FormAttachment(statusBar, 0);
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
        layoutData.top = new FormAttachment(statusBar, 0);
        layoutData.left = new FormAttachment(macroTable, 8);
        layoutData.right = new FormAttachment(100, 0);
        layoutData.bottom = new FormAttachment(100, 0);
        mobileTable.setLayoutData(layoutData);

    }

    public void buildStatusBar(Composite parent) {

        seqText = new Text(parent, READ_ONLY | RIGHT);
        seqText.setText("0");

        Label seqSlashLabel = new Label(parent, NONE);
        seqSlashLabel.setText(" / ");

        totalSeqText = new Text(parent, BORDER | RIGHT);
        totalSeqText.setText("0");

        // - - -

        executionTimeText = new Text(parent, READ_ONLY | RIGHT);
        executionTimeText.setText("00:00:00");

        estimationTimeLabel = new Label(parent, RIGHT);
        estimationTimeLabel.setText(" + 00:00:00 = 00:00:00");

        // - - -

        Label utilityLabel = new Label(parent, NONE);
        utilityLabel.setText("Sum Utility:");

        utilityText = new Text(parent, READ_ONLY | RIGHT);
        utilityText.setText("0.000");

        // - - -

        parent.setLayout(new FormLayout());
        FormData layoutData;

        //

        // utilityLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3);
        layoutData.left = new FormAttachment(0, 0);
        utilityLabel.setLayoutData(layoutData);

        // utilityText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3);
        layoutData.left = new FormAttachment(utilityLabel, 0);
        layoutData.right = new FormAttachment(utilityLabel, 64, TRAIL);
        utilityText.setLayoutData(layoutData);

        //

        // seqText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3);
        layoutData.left = new FormAttachment(seqSlashLabel, -8 - 64, LEAD);
        layoutData.right = new FormAttachment(seqSlashLabel, 0);
        seqText.setLayoutData(layoutData);

        // seqSlashLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3);
        layoutData.right = new FormAttachment(totalSeqText, 0);
        seqSlashLabel.setLayoutData(layoutData);

        // seqTotalText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(executionTimeText, -8 - 64, LEAD);
        layoutData.right = new FormAttachment(executionTimeText, -8);
        totalSeqText.setLayoutData(layoutData);

        //

        // elapsedText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3);
        layoutData.right = new FormAttachment(1, 1, 0);
        layoutData.right = new FormAttachment(estimationTimeLabel, 0);
        executionTimeText.setLayoutData(layoutData);

        // elapsedText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3);
        layoutData.right = new FormAttachment(1, 1, 0);
        estimationTimeLabel.setLayoutData(layoutData);

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

    @Override
    public void notifyStarted() {

        executeButton.setFocus();

        // - - -

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
        // - - -

        shell.setSize(1280, 800);
        shell.open();
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                if (saved != controller.getSeq()) {
                    MessageBox messageBox = new MessageBox(shell,
                            APPLICATION_MODAL | YES | CANCEL | NO
                                    | ICON_WARNING);
                    messageBox.setText("Result is not Saved");
                    messageBox
                            .setMessage("Simulation result is not saved, therfore, your result will be lost.\nDo you want to save result before exit?");
                    int answer = messageBox.open();
                    if (answer == YES) {
                        boolean continueClose = save();
                        e.doit = continueClose;
                        if (!continueClose)
                            return;
                    } else if (answer == CANCEL) {
                        e.doit = false;
                        return;
                    } else if (answer == NO) {
                    }

                } else {
                    MessageBox messageBox = new MessageBox(shell,
                            APPLICATION_MODAL | YES | NO | ICON_INFORMATION);
                    messageBox.setText("Exit Program");
                    messageBox.setMessage("Do you want to exit?");
                    int answer = messageBox.open();
                    if (answer == NO) {
                        e.doit = false;
                        return;
                    }
                }

                controller.stop();
                Algorithm2.executor.shutdown(); // TODO Generalize
            }
        });

        while (!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();

        display.dispose();

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

    private boolean dumped = true;

    @Override
    public void dump(final int seq, final StateContext state, final Macro[] macros,
            final Pico[] picos, final Mobile[] mobiles, final long elapsed) {

        // if (calculator.isRunning() && t % 5 != 0)
        // return elapsed;
        if (!dumped)
            return;
        dumped = false;
        if (display.isDisposed())
            return;
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if (shell.isDisposed())
                    return;

                seqText.setText(format("%,d", seq));

                String elapsedTime = milisToTImeString(elapsed);

                long estimated = seq == 0 ? 0 : elapsed
                        * controller.getTotalSeq() / seq;
                long left = 1000 * ((estimated / 1000) - (elapsed / 1000));
                String estimatedTime = " + " + milisToTImeString(left) + " = "
                        + milisToTImeString(estimated);

                executionTimeText.setText(elapsedTime);
                estimationTimeLabel.setText(estimatedTime);

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
                if (seq == controller.getTotalSeq())
                    frequncy = 1;

                if (state != null) {

                    for (int m = 0; m < macros.length; m++) {
                        Macro macro = macros[m];
                        TableItem item = macroTable.getItem(macro.idx);
                        item.setText(4, valueOf(state.macroIsOn(m) ? "ON"
                                : "OFF"));
                        item.setText(5,
                                format("%.02f", (seq - macros[m].getAllocationCount())
                                        * 100.0 / seq));
                    }

                    for (int p = 0; p < picos.length; p++) {
                        Pico pico = picos[p];
                        TableItem item = picoTable.getItem(pico.idx);
                        item.setText(4, valueOf(state.picoIsAbs(p) ? "ABS"
                                : "non"));
                        item.setText(5,
                                format("%.02f", (seq - picos[p].getNonAbsCount())
                                        * 100.0 / seq));
                    }

                }

                double throughput = 0.0;
                for (int u = 0; u < mobiles.length; u++) {
                    Mobile mobile = mobiles[u];

                    throughput += log(mobile.getThroughput() / seq);

                    if (frequncy > 0 && seq % frequncy == 0) {

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
                        texts[index++] = format("%.6f",
                                log(mobile.getUserRate()));
                        texts[index++] = format("%.6f", mobile.getThroughput()
                                / seq);
                        texts[index++] = format("%.6f",
                                log(mobile.getThroughput() / seq));
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
                                item.setBackground(index + j * 3 + 0,
                                        colorActiveBg);
                                item.setBackground(index + j * 3 + 1,
                                        colorActiveBg);
                                item.setBackground(index + j * 3 + 2,
                                        colorActiveBg);
                            }
                            texts[index + j * 3] = bs == null ? "" : format(
                                    "%.3f", 1000 * lambdaR);
                            texts[index + j * 3 + 1] = bs == null ? "" : bs;
                            texts[index + j * 3 + 2] = bs == null ? ""
                                    : valueOf(rank);
                        }
                        }

                        item.setText(texts);
                    }

                }

                utilityText.setText(format("%.3f", throughput));

                dumped = true;
            }
        });

    }

    public void setAlgorithm() {
        int index = algorithmeCombo.getSelectionIndex();
        switch (algorithmeCombo.getItem(index)) {
        case ALGORITHM_1:
            controller.setAlgorithm(new Algorithm1());
            break;
        case ALGORITHM_2:
            controller.setAlgorithm(new Algorithm2());
            break;
        case ALGORITHM_3:
            controller.setAlgorithm(new Algorithm3());
            break;
        default:
            break;
        }
    }

    public static String milisToTImeString(final long elapsed) {
        long sec = elapsed / 1000;
        // long mil = elapsed - sec * 1000;

        long min = sec / 60;
        sec -= min * 60;

        long hour = min / 60;
        min -= hour * 60;

        String format = format("%02d:%02d:%02d", hour, min, sec);
        return format;
    }

    @Override
    public void notifyEnded() {
        if (display.isDisposed())
            return;
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if (display.isDisposed())
                    return;
                setRunningState(false);
            }
        });
    }

    @Override
    public void setTotalSeq(final int totalSeq) {
        if (!display.isDisposed()) {
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    totalSeqText.setText(format("%,d", totalSeq));
                }
            });
        }
    }

    @Override
    public void setAlgorithm(Algorithm algorithm) {
        if (algorithm instanceof Algorithm1) {
            algorithmeCombo.select(0);
        } else if (algorithm instanceof Algorithm2) {
            algorithmeCombo.select(1);
        } else if (algorithm instanceof Algorithm3) {
            algorithmeCombo.select(2);
        }
    }

    private void setRunningState(boolean isRunning) {
        resetButton.setEnabled(!isRunning);
        executeButton.setText(isRunning ? PAUSE : START);
        nextButton.setEnabled(!isRunning);
        algorithmeCombo.setEnabled(!isRunning);
        saveButton.setEnabled(!isRunning);
        totalSeqText.setEnabled(!isRunning);
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

}
