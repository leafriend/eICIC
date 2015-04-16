package net.folab.eicic;

import static java.lang.Math.*;
import static java.lang.String.*;
import static net.folab.eicic.Constants.*;

import java.util.List;

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

    private Display display;

    private Shell shell;

    private Table mobileTable;

    public GuiConsole() {

        display = new Display();

        shell = new Shell(display);
        shell.setText("eICIC");

        Composite parent = shell;

        mobileTable = new Table(parent, SWT.BORDER);
        mobileTable.setLinesVisible(true);
        mobileTable.setHeaderVisible(true);

        addColumn(mobileTable, "#");
        addColumn(mobileTable, "User Rate");
        addColumn(mobileTable, "log(User Rate)");
        addColumn(mobileTable, "Throughput");
        addColumn(mobileTable, "log(Throughput)");
        addColumn(mobileTable, LAMBDA);
        addColumn(mobileTable, MU);

        for (int i = 0; i < NUM_MOBILES; i++) {
            TableItem item = new TableItem(mobileTable, SWT.NONE);
            item.setText(0, valueOf(i));
        }

        FormData layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(100, 0);
        layoutData.bottom = new FormAttachment(100, 0);
        mobileTable.setLayoutData(layoutData);

        FormLayout layout = new FormLayout();
        parent.setLayout(layout);

    }

    public static void addColumn(Table table, String text) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(text);
        column.pack();
    }

    @Override
    public long dump(final int t, List<Macro> macros, List<Pico> picos,
            final List<Mobile> mobiles, long elapsed, long execute) {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                for (Mobile mobile : mobiles) {
                    TableItem item = mobileTable.getItem(mobile.idx);
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
