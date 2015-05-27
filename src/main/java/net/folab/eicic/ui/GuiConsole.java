package net.folab.eicic.ui;

import static org.eclipse.swt.SWT.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import net.folab.eicic.algorithm.Algorithm2;
import net.folab.eicic.core.Algorithm;
import net.folab.eicic.core.Console;
import net.folab.eicic.core.Controller;
import net.folab.eicic.model.Macro;
import net.folab.eicic.model.Mobile;
import net.folab.eicic.model.Pico;
import net.folab.eicic.model.StateContext;
import net.folab.eicic.ui.GuiButtonPanel.UpdateFrequencyListener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class GuiConsole implements Console {

    private Display display;

    private Shell shell;

    private GuiButtonPanel buttonPanel;

    private int saved;

    private GuiTablePanel tablePanel;

    private GuiStatusPanel statusPanel;

    private final Controller controller;

    public GuiConsole(Controller controller) {

        this.controller = controller;

        display = new Display();

        shell = new Shell(display);
        shell.setText("eICIC");
        buildShell(shell);

    }

    public void buildShell(Shell parent) {

        buttonPanel = new GuiButtonPanel(shell, controller);
        buttonPanel.setUpdateFrequencyListener(new UpdateFrequencyListener() {
            @Override
            public void updateFrequencyModified(int updateFrequency) {
                GuiConsole.this.updateFrequency = updateFrequency;
                tablePanel.setUpdateFrequency(updateFrequency);
            }
        });

        tablePanel = new GuiTablePanel(shell, controller);

        statusPanel = new GuiStatusPanel(shell, controller);

        Menu menuBar = new Menu(parent, BAR);
        parent.setMenuBar(menuBar);

        // - - -

        MenuItem fileHeader = new MenuItem(menuBar, CASCADE);
        fileHeader.setText("&File");

        Menu file = new Menu(parent, DROP_DOWN);
        fileHeader.setMenu(file);

        MenuItem open = new MenuItem(file, PUSH);
        open.setText("&Open");
        open.setAccelerator(CTRL + 'O');
        open.setEnabled(false);

        MenuItem save = new MenuItem(file, PUSH);
        save.setText("&Save");
        save.setAccelerator(CTRL + 'S');
        save.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                save();
            };
        });

        new MenuItem(file, SEPARATOR);

        MenuItem exit = new MenuItem(file, PUSH);
        exit.setText("E&xit");
        exit.setAccelerator(CTRL + 'Q');
        exit.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            };
        });

        // - - -

        MenuItem helpHeader = new MenuItem(menuBar, CASCADE);
        helpHeader.setText("&Help");

        Menu help = new Menu(parent, DROP_DOWN);
        helpHeader.setMenu(help);

        MenuItem about = new MenuItem(help, PUSH);
        about.setText("&About");
        about.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Shell dialog = new Shell(shell, DIALOG_TRIM | APPLICATION_MODAL);
                dialog.setText("About eICIC");

                int width = 300;
                int height = 400;
                Rectangle bound = display.getBounds();
                int x = (bound.width - width) / 2;
                int y = (bound.height - height) / 2;
                dialog.setBounds(x, y, width, height);

                dialog.open();
            };
        });

        // - - -

        parent.setLayout(new FormLayout());
        FormData layoutData;

        // buttonPanel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 8);
        layoutData.left = new FormAttachment(0, 8);
        layoutData.right = new FormAttachment(100, -8);
        // layoutData.bottom = new FormAttachment(statusBar, 8);
        buttonPanel.getControl().setLayoutData(layoutData);

        // tablePanel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(buttonPanel.getControl(), 8);
        layoutData.left = new FormAttachment(0, 8);
        layoutData.right = new FormAttachment(100, -8);
        layoutData.bottom = new FormAttachment(statusPanel.getControl(), -8);
        tablePanel.getControl().setLayoutData(layoutData);

        // statusBar
        layoutData = new FormData();
        // layoutData.top = new FormAttachment(0, 0);
        layoutData.left = new FormAttachment(0, 8);
        layoutData.right = new FormAttachment(100, -8);
        layoutData.bottom = new FormAttachment(100, -8);
        statusPanel.getControl().setLayoutData(layoutData);

    }

    public boolean save() {
        FileDialog dialog = new FileDialog(shell, SAVE);
        dialog.setText("Save");
        // dialog.setFilterPath(filterPath);
        String[] filterExt = { "*.csv", "*.txt", "*.*" };
        dialog.setFilterExtensions(filterExt);
        String fileName = buttonPanel.getDefaultSaveFileName();
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

            Charset charset = Charset.forName(System
                    .getProperty("file.encoding"));

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(selected)), charset));

            statusPanel.save(writer, selected);

            tablePanel.save(writer, selected);

            saved = controller.getSeq();

            writer.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void notifyStarted() {

        buttonPanel.setFocus();

        // - - -

        tablePanel.notifyStarted();

        // - - -

        int width = 1280;
        int height = 800;
        Rectangle bound = display.getBounds();
        int x = (bound.width - width) / 2;
        int y = (bound.height - height) / 2;
        shell.setBounds(x, y, width, height);

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

    private boolean dumped = true;

    protected int updateFrequency;

    @Override
    public void dump(final int seq, final StateContext state,
            final Macro[] macros, final Pico[] picos, final Mobile[] mobiles,
            final long elapsed) {

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

                if (seq == controller.getTotalSeq())
                    updateFrequency = 1; // TODO ?

                double throughput = tablePanel.dump(seq, state, macros, picos,
                        mobiles);

                statusPanel.dump(seq, elapsed, throughput);

                dumped = true;
            }
        });

    }

    @Override
    public void notifyPaused() {
        setRunningState(false);
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
        statusPanel.setTotalSeq(totalSeq);
    }

    @Override
    public void setAlgorithm(Algorithm algorithm) {
        buttonPanel.setAlgorithm(algorithm);
    }

    private void setRunningState(boolean isRunning) {
        buttonPanel.setEnabled(!isRunning);
        statusPanel.setEnabled(!isRunning);
    }

}
