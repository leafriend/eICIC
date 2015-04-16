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

    private Shell shell;

    private Composite dashboard;

    private Label timeLabel;

    private Text timeText;

    private Label executeLabel;

    private Text executeText;

    private Button executeButton;

    private Table table;

    private Main executor;

    public GuiConsole(final Algorithm algorithm) {

        this.algorithm = algorithm;

        display = new Display();

        shell = new Shell(display);
        shell.setText("eICIC");

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

        executeButton = new Button(dashboard, SWT.PUSH);
        executeButton.setText("Pau&se");
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(100, 100, -64);
        layoutData.right = new FormAttachment(100, 0);
        executeButton.setLayoutData(layoutData);
        executeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (executor != null) {
                    String text = executeButton.getText();
                    if ("Pau&se".endsWith(text)) {
                        executor.stop();
                        executeButton.setText("&Start");
                    } else if ("&Start".endsWith(text)) {
                        executor.execute(GuiConsole.this, algorithm, SIMULATION_TIME);
                        executeButton.setText("Pau&se");
                    }
                }
            }
        });

        dashboard.setLayout(new FormLayout());

        table = new Table(parent, SWT.BORDER);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        addColumn(table, "#");
        addColumn(table, "User Rate");
        addColumn(table, "log(User Rate)");
        addColumn(table, "Throughput");
        addColumn(table, "log(Throughput)");
        addColumn(table, LAMBDA);
        addColumn(table, MU);

        for (int i = 0; i < NUM_RB; i++)
            addColumn(table, valueOf(i));

        for (int i = 0; i < NUM_MOBILES; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, valueOf(i));
        }

        layoutData = new FormData();
        layoutData.top = new FormAttachment(dashboard, 0);
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(100, 0);
        layoutData.bottom = new FormAttachment(100, 0);
        table.setLayoutData(layoutData);

        FormLayout layout = new FormLayout();
        parent.setLayout(layout);

    }

    public static void addColumn(Table table, String text) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(text);
        column.setAlignment(SWT.RIGHT);
        if ("#".equals(text))
            column.setWidth(32);
        else
            column.setWidth(96);
    }

    @Override
    public void start(final Main executor) {
        this.executor = executor;
        executor.execute(this, algorithm, SIMULATION_TIME);

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
    public long dump(final int t, List<Macro> macros, List<Pico> picos,
            final List<Mobile> mobiles, final long elapsed, final long execute) {
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
                //System.out.println(hour + ":" + min + ":" + (now / 1000) + "." + mil);

                executeText.setText(format("%02d:%02d:%02d.%03d:", hour, min, sec, mil));
                for (Mobile mobile : mobiles) {
                    TableItem item = table.getItem(mobile.idx);
                    String[] texts = new String[7 + NUM_RB];
                    texts[1] = format("%.6f", mobile.getUserRate());
                    texts[2] = format("%.6f", log(mobile.getUserRate()));
                    texts[3] = format("%.6f", mobile.getThroughput() / t);
                    texts[4] = format("%.6f", log(mobile.getThroughput() / t));
                    texts[5] = format("%.6f", mobile.getLambda());
                    texts[6] = format("%.6f", mobile.getMu());
                    Edge<? extends BaseStation<?>>[] activeEdges = mobile.getActiveEdges();
                    double[] macroLambdaR = mobile.getMacroLambdaR();
                    double[] absPicoLambdaR = mobile.getAbsPicoLambdaR();
                    double[] nonPicoLambdaR = mobile.getNonPicoLambdaR();
                    boolean isAbs = mobile.getPico().isAbs();
                    for (int i = 0; i < NUM_RB; i++) {
                        String text = "";
                        if (activeEdges[i] != null) {
                            if (activeEdges[i].baseStation instanceof Macro) {
                                text = format("%.3f", 1000 * macroLambdaR[i])
                                        + " M";
                            } else if (activeEdges[i].baseStation instanceof Pico) {
                                if (isAbs)
                                    text = format("%.3f",
                                            1000 * absPicoLambdaR[i]) + " P";
                                else
                                    text = format("%.3f",
                                            1000 * nonPicoLambdaR[i]) + " p";
                            }
                        }
                        texts[7 + i] = text;
                    }
                    item.setText(texts);
                }
            }
        });
        return System.currentTimeMillis();
    }

}
