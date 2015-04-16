package net.folab.eicic;

import static java.lang.Math.*;
import static java.lang.String.*;
import static net.folab.eicic.Constants.*;

import java.util.List;

import net.folab.eicic.algorithm.Algorithm;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class GuiConsole implements Console {

    public static final String LAMBDA = "\u03bb";

    public static final String MU = "\u03bc";

    private Algorithm algorithm;

    private Display display;

    private Shell shell;

    private Table table;

    public GuiConsole(Algorithm algorithm) {

        this.algorithm = algorithm;

        display = new Display();

        shell = new Shell(display);
        shell.setText("eICIC");

        Composite parent = shell;

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

        for (int i = 0; i < NUM_MOBILES; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, valueOf(i));
        }

        FormData layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
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
            column.setWidth(128);
    }

    @Override
    public void start(Main executor) {
        executor.execute(this, algorithm, SIMULATION_TIME);

        shell.open();

        while (!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();

        display.dispose();

    }

    @Override
    public long dump(final int t, List<Macro> macros, List<Pico> picos,
            final List<Mobile> mobiles, long elapsed, long execute) {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                for (Mobile mobile : mobiles) {
                    TableItem item = table.getItem(mobile.idx);
                    item.setText(1, format("%12.6f", mobile.getUserRate()));
                    item.setText(2, format("%12.6f", log(mobile.getUserRate())));
                    item.setText(3, format("%12.6f", mobile.getThroughput() / t));
                    item.setText(4, format("%12.6f", log(mobile.getThroughput() / t)));
                    item.setText(5, format("%12.6f", mobile.getLambda()));
                    item.setText(6, format("%12.6f", mobile.getMu()));
                }
            }
        });
        return System.currentTimeMillis();
    }

}
