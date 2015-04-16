package net.folab.eicic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class Gui {

    public static void main(String[] args) {

        Display display = new Display();

        Shell shell = new Shell(display);
        shell.setText("eICIC");

        Composite parent = shell;

        Table table = new Table(parent, SWT.BORDER);
        table.setLinesVisible (true);
        table.setHeaderVisible (true);

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

}
