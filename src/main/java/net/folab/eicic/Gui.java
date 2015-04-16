package net.folab.eicic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class Gui {

    public static final String LAMBDA = "\u03bb";

    public static final String MU = "\u03bc";

    public static void main(String[] args) {

        Display display = new Display();

        Shell shell = new Shell(display);
        shell.setText("eICIC");

        Composite parent = shell;

        Table table = new Table(parent, SWT.BORDER);
        table.setLinesVisible (true);
        table.setHeaderVisible (true);

        addColumn(table, "#");
        addColumn(table, "User Rate");
        addColumn(table, "log(User Rate)");
        addColumn(table, "Throughput");
        addColumn(table, "log(Throughput)");
        addColumn(table, LAMBDA);
        addColumn(table, MU);

        FormData layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(100, 0);
        layoutData.bottom = new FormAttachment(100, 0);
        table.setLayoutData(layoutData);

        FormLayout layout = new FormLayout();
        parent.setLayout(layout);

        shell.open ();

        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();

    }

    public static void addColumn(Table table, String text) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(text);
        column.pack();
    }

}
