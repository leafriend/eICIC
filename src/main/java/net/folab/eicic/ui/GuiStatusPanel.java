package net.folab.eicic.ui;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.LEAD;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.READ_ONLY;
import static org.eclipse.swt.SWT.RIGHT;
import static org.eclipse.swt.SWT.TRAIL;

import java.io.BufferedWriter;
import java.io.IOException;

import net.folab.eicic.core.Console;
import net.folab.eicic.core.Controller;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GuiStatusPanel {

    private Controller controller;

    private Composite control;

    private Text seqText;

    private Text executionTimeText;

    private Text utilityText;

    private Label estimationTimeLabel;

    private Text totalSeqText;

    public GuiStatusPanel(Composite wrapper, Controller controller) {

        this.controller = controller;

        control = new Composite(wrapper, NONE);

        seqText = new Text(control, READ_ONLY | RIGHT);
        seqText.setText("0");

        Label seqSlashLabel = new Label(control, NONE);
        seqSlashLabel.setText(" / ");

        totalSeqText = new Text(control, BORDER | RIGHT);
        totalSeqText.setText("0");
        totalSeqText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String number = totalSeqText.getText().replaceAll(",", "");
                controller.setTotalSeq(Integer.parseInt(number));
            }
        });

        // - - -

        executionTimeText = new Text(control, READ_ONLY | RIGHT);
        executionTimeText.setText("00:00:00");

        estimationTimeLabel = new Label(control, RIGHT);
        estimationTimeLabel.setText(" + 00:00:00 = 00:00:00");

        // - - -

        Label utilityLabel = new Label(control, NONE);
        utilityLabel.setText("Sum Utility:");

        utilityText = new Text(control, READ_ONLY | RIGHT);
        utilityText.setText("0.000");

        // - - -

        control.setLayout(new FormLayout());
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

    public Control getControl() {
        return control;
    }

    public void save(BufferedWriter writer, String selected) {

        try {

            int seq = controller.getSeq();

            String delim = selected.toLowerCase().endsWith(".csv") ? "," : "\t";

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
                writer.write(valueOf(controller.getMacros()[m]
                        .getAllocationCount()));
            }
            writer.write("\n");
            writer.flush();

            writer.write("#Macro %");
            for (int m = 0; m < controller.getMacros().length; m++) {
                writer.write(delim);
                double percent = 100.0
                        * controller.getMacros()[m].getAllocationCount() / seq;
                writer.write(format("%.2f%%", percent));
            }
            writer.write("\n");
            writer.flush();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void dump(int seq, long elapsed, double throughput) {

        seqText.setText(format("%,d", seq));

        String elapsedTime = Console.milisToTimeString(elapsed);

        long estimated = seq == 0 ? 0 : elapsed * controller.getTotalSeq()
                / seq;
        long left = 1000 * ((estimated / 1000) - (elapsed / 1000));
        String estimatedTime = " + " + Console.milisToTimeString(left) + " = "
                + Console.milisToTimeString(estimated);

        executionTimeText.setText(elapsedTime);
        estimationTimeLabel.setText(estimatedTime);

        utilityText.setText(format("%.3f", throughput));

    }

    public void setTotalSeq(int totalSeq) {
        final Display display = control.getDisplay();
        if (!display.isDisposed()) {
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    totalSeqText.setText(format("%,d", totalSeq));
                }
            });
        }
    }

    public void setEnabled(boolean enabled) {
        final Display display = control.getDisplay();
        if (!display.isDisposed()) {
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    totalSeqText.setEnabled(enabled);
                }
            });
        }
    }

}
