package net.folab.eicic.ui;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static net.folab.eicic.algorithm.AlgorithmFactory.getAlgorithmClasses;
import static net.folab.eicic.algorithm.AlgorithmFactory.getInstance;
import static net.folab.eicic.ui.Util.array;
import static org.eclipse.swt.SWT.APPLICATION_MODAL;
import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.CHECK;
import static org.eclipse.swt.SWT.DIALOG_TRIM;
import static org.eclipse.swt.SWT.LEAD;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.PUSH;
import static org.eclipse.swt.SWT.READ_ONLY;
import static org.eclipse.swt.SWT.RIGHT;
import static org.eclipse.swt.SWT.TRAIL;

import java.lang.reflect.Field;
import java.util.Random;

import net.folab.eicic.algorithm.StaticAlgorithm;
import net.folab.eicic.core.Algorithm;
import net.folab.eicic.core.Controller;
import net.folab.eicic.core.Option;
import net.folab.eicic.model.Edge;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GuiButtonPanel {

    public static interface UpdateFrequencyListener {

        public void updateFrequencyModified(int updateFrequency);

    }

    private static final String START = "Sta&rt";

    private static final String PAUSE = "P&ause";

    private Controller controller;

    private Algorithm algorithm;

    private Composite control;

    private Combo algorithmCombo;

    private Button optionButton;

    private Text absNumeratorText;

    private Text absDenominatorText;

    private Button executeButton;

    private Button resetButton;

    private Button nextButton;

    private Text creText;

    private UpdateFrequencyListener updateFrequencyListener;

    public GuiButtonPanel(Composite wrapper, Controller controller) {

        this.controller = controller;

        control = new Composite(wrapper, NONE);

        Combo randomCombo = new Combo(control, READ_ONLY);
        randomCombo.setItems(array("Random", "Pseudo", "Static"));
        randomCombo.select(0);
        randomCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switch (randomCombo.getSelectionIndex()) {

                case 0:
                    Edge.setRandom(new Random(System.currentTimeMillis()));
                    break;

                case 1:
                    Edge.setRandom(new Random(0));
                    break;

                case 2:
                    Edge.setRandom(new Random() {
                        private static final long serialVersionUID = -7928935803738991985L;

                        public synchronized double nextGaussian() {
                            return 0;
                        };
                    });
                    break;

                default:
                    break;
                }
            }
        });

        algorithmCombo = new Combo(control, READ_ONLY);
        algorithmCombo.setItems(stream(getAlgorithmClasses())
                .map(c -> c.getSimpleName())
                .map(s -> {
                    StringBuilder sb = new StringBuilder();
                    for (char ch : s.toCharArray()) {
                        if (('A' <= ch && ch <= 'Z')
                                || ('0' <= ch && ch <= '9')) {
                            sb.append(' ');
                        }
                        sb.append(ch);
                    }
                    return sb.toString().trim();
                }).toArray(s -> new String[s]));
        algorithmCombo.select(0);
        algorithmCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setAlgorithm();
            }
        });

        optionButton = new Button(control, PUSH);
        optionButton.setText("O&ption");
        optionButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                Shell dialog = new Shell(control.getShell(), DIALOG_TRIM | APPLICATION_MODAL);
                int index = algorithmCombo.getSelectionIndex();
                dialog.setText("Option - " + algorithmCombo.getItem(index));

                Control focus = null;

                Control previous = null;

                Algorithm algorithm = controller.getAlgorithm();
                for (Field field : algorithm.getClass().getDeclaredFields()) {
                    Option option = field.getAnnotation(Option.class);
                    if (option != null) {

                        Composite row = new Composite(dialog, NONE);
                        row.setLayout(new FormLayout());

                        String name = option.name();
                        if (name.isEmpty()) {
                            // TODO
                            name = field.getName();
                        }
                        FormData layoutData;

                        Label label = new Label(row, NONE);
                        label.setText(name);

                        layoutData = new FormData();
                        layoutData.top = new FormAttachment(0, 0);
                        layoutData.left = new FormAttachment(0, 0);
                        layoutData.right = new FormAttachment(50, 0);
                        label.setLayoutData(layoutData);

                        Text text = new Text(row, BORDER);
                        if (focus == null)
                            focus = text;

                        layoutData = new FormData();
                        layoutData.top = new FormAttachment(0, 0);
                        layoutData.left = new FormAttachment(50, 0);
                        layoutData.right = new FormAttachment(100, 0);
                        text.setLayoutData(layoutData);

                        layoutData = new FormData();
                        if (previous == null) {
                            layoutData.top = new FormAttachment(0, 8);
                        } else {
                            layoutData.top = new FormAttachment(previous, 8);
                        }
                        layoutData.left = new FormAttachment(0, 8);
                        layoutData.right = new FormAttachment(100, -8);
                        row.setLayoutData(layoutData);

                        previous = row;

                    }
                }

                int width = 400;
                int height = 300;
                Rectangle bound = control.getDisplay().getPrimaryMonitor().getClientArea();
                int x = (bound.width - width) / 2;
                int y = (bound.height - height) / 2;
                dialog.setBounds(x, y, width, height);

                Button okButton = new Button(dialog, PUSH);
                okButton.setText("&OK");
                okButton.setFocus();
                okButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        dialog.close();
                    }
                });
                if (focus == null)
                    focus = okButton;

                Button cancelButton = new Button(dialog, PUSH);
                cancelButton.setText("Cance&l");
                cancelButton.setFocus();
                cancelButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        dialog.close();
                    }
                });

                dialog.setLayout(new FormLayout());
                FormData layoutData;

                layoutData = new FormData();
                layoutData.left = new FormAttachment(cancelButton, -8 - 64, LEAD);
                layoutData.right = new FormAttachment(cancelButton, -8, LEAD);
                layoutData.bottom = new FormAttachment(100, -8);
                okButton.setLayoutData(layoutData);

                layoutData = new FormData();
                layoutData.left = new FormAttachment(100, -8 - 64);
                layoutData.right = new FormAttachment(100, -8);
                layoutData.bottom = new FormAttachment(100, -8);
                cancelButton.setLayoutData(layoutData);

                dialog.open();
                focus.setFocus();

            };
        });

        Label absLabel = new Label(control, NONE);
        absLabel.setText("ABS");

        absNumeratorText = new Text(control, BORDER | RIGHT);
        absNumeratorText.setText("000");
        absNumeratorText.addModifyListener(e -> {
            try {
                int absNumerator = parseInt(absNumeratorText.getText());
                if (controller != null && controller.getAlgorithm() instanceof StaticAlgorithm) {
                    StaticAlgorithm staticAlgorithm = (StaticAlgorithm) controller.getAlgorithm();
                    staticAlgorithm.setAbsNumerator(absNumerator);
                    double cre = parseDouble(creText.getText());
                    double creBias = pow(10, cre / 10);
                    staticAlgorithm.setCreBias(creBias);
                }
            } catch (NumberFormatException ex) {
                // TODO handle caught exception
            }
        });

        Label absSlashLabel = new Label(control, NONE);
        absSlashLabel.setText("/");

        absDenominatorText = new Text(control, BORDER | RIGHT);
        absDenominatorText.setText("000");
        absDenominatorText.addModifyListener(e -> {
            try {
                int absDenominator = parseInt(absDenominatorText.getText());
                if (controller != null && controller.getAlgorithm() instanceof StaticAlgorithm) {
                    StaticAlgorithm staticAlgorithm = (StaticAlgorithm) controller.getAlgorithm();
                    staticAlgorithm.setAbsDenominator(absDenominator);
                }
            } catch (NumberFormatException ex) {
                // TODO handle caught exception
            }
        });

        Label creLabel = new Label(control, NONE);
        creLabel.setText("CRE");
        creText = new Text(control, BORDER | RIGHT);
        Label creBiasLabel = new Label(control, NONE);

        creText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    double cre = parseDouble(creText.getText());
                    double creBias = pow(10, cre / 10);
                    if (controller != null
                            && controller.getAlgorithm() instanceof StaticAlgorithm) {
                        StaticAlgorithm staticAlgorithm = (StaticAlgorithm) controller
                                .getAlgorithm();
                        staticAlgorithm.setCreBias(creBias);
                    }
                    creBiasLabel.setText(format("\u21D2 CRE bias: %.3f",
                            creBias));
                } catch (NumberFormatException ex) {
                    // TODO handle catched exception
                }
            }
        });
        creText.setText("0");

        Button showActiveButton = new Button(control, CHECK);
        showActiveButton.setText("Show active only");
        showActiveButton.setSelection(true);

        Combo updateSeq = new Combo(control, READ_ONLY);
        String[] items = array( //
                "No Update", //
                "Update for each 1 seq", //
                "Update for each 10 seq", //
                "Update for each 100 seq", //
                "Update for each 1000 seq", //
                "Update for each 10000 seq" //
        );
        updateSeq.setItems(items);
        updateSeq.select(0);
        updateSeq.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int updateFrequency;
                switch (updateSeq.getSelectionIndex()) {
                case 0:
                    updateFrequency = 0;
                    // No Update
                    break;
                case 1:
                    updateFrequency = 1;
                    // Update for each 1 seq
                    break;
                case 2:
                    updateFrequency = 10;
                    // Update for each 10 seq
                    break;
                case 3:
                    updateFrequency = 100;
                    // Update for each 100 seq
                    break;
                case 4:
                    updateFrequency = 1000;
                    // Update for each 1000 seq
                    break;
                case 5:
                    updateFrequency = 10000;
                    // Update for each 10000 seq
                    break;
                default:
                    throw new RuntimeException("Unsupported frequency: "
                            + updateSeq.getItem(updateSeq.getSelectionIndex()));
                }
                if (updateFrequencyListener != null)
                    updateFrequencyListener
                            .updateFrequencyModified(updateFrequency);
            }
        });

        resetButton = new Button(control, PUSH);
        resetButton.setText("Rese&t");
        resetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                controller.reset();
                // FIXME console.dump(seq, null, macros, picos, mobiles, accumuMillis);
            }
        });

        executeButton = new Button(control, PUSH);
        executeButton.setText(START);
        SelectionAdapter executeButtonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String text = executeButton.getText();
                randomCombo.setEnabled(false);
                algorithmCombo.setEnabled(false);
                if (PAUSE.endsWith(text)) {
                    controller.pause();
                    executeButton.setText(START);
                } else if (START.endsWith(text)) {
                    setAlgorithm();
                    controller.start();
                    executeButton.setText(PAUSE);
                }
            }
        };
        executeButton.addSelectionListener(executeButtonListener);

        // - - -

        nextButton = new Button(control, PUSH);
        nextButton.setText("N&ext");
        nextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                controller.next();
            }
        });

        // - - -

        control.setLayout(new FormLayout());
        FormData layoutData;

        // randomCombo
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1);
        layoutData.left = new FormAttachment(0);
        randomCombo.setLayoutData(layoutData);

        // algorithmeCombo
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1);
        layoutData.left = new FormAttachment(randomCombo, 8);
        algorithmCombo.setLayoutData(layoutData);

        // optionButton
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1);
        layoutData.left = new FormAttachment(algorithmCombo, 8);
        layoutData.right = new FormAttachment(algorithmCombo, 8 + 64, TRAIL);
        optionButton.setLayoutData(layoutData);

        // absLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3 + 2);
        layoutData.left = new FormAttachment(optionButton, 8);
        absLabel.setLayoutData(layoutData);

        // absNumeratorText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1 + 1);
        layoutData.left = new FormAttachment(absLabel, 8);
        absNumeratorText.setLayoutData(layoutData);
        absNumeratorText.setText("20");

        // absSlashLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3 + 2);
        layoutData.left = new FormAttachment(absNumeratorText, 0);
        absSlashLabel.setLayoutData(layoutData);

        // absDenominatorText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1 + 1);
        layoutData.left = new FormAttachment(absSlashLabel, 0);
        absDenominatorText.setLayoutData(layoutData);
        absDenominatorText.setText("100");

        // creLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3 + 2);
        layoutData.left = new FormAttachment(absDenominatorText, 8);
        creLabel.setLayoutData(layoutData);

        // creText
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 1 + 1);
        layoutData.left = new FormAttachment(creLabel, 8);
        layoutData.right = new FormAttachment(creLabel, 8 + 48, TRAIL);
        creText.setLayoutData(layoutData);

        // creBiasLabel
        layoutData = new FormData();
        layoutData.top = new FormAttachment(0, 3 + 2);
        layoutData.left = new FormAttachment(creText, 8);
        layoutData.right = new FormAttachment(creText, 8 + 120, TRAIL);
        creBiasLabel.setLayoutData(layoutData);

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

    public Composite getControl() {
        return control;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        if (algorithm != null) {
            String algorithmName = algorithm.getClass().getSimpleName();
            for (int i = 0; i < algorithmCombo.getItemCount(); i++) {
                if (algorithmCombo.getItem(i).replaceAll(" ", "").equals(algorithmName)) {
                    algorithmCombo.select(i);
                    break;
                }
            }
        }
    }

    public void setAlgorithm() {
        int index = algorithmCombo.getSelectionIndex();
        creText.setEnabled(false);
        String algorithmName = algorithmCombo.getItem(index).replaceAll(" ", "");
        this.algorithm = getInstance(algorithmName);
        if ("StaticAlgorithm".equals(algorithmName)) {
            // FIXME Remove direct access to StaticAlgorithm
            double absNumerator = parseInt(absNumeratorText.getText());
            StaticAlgorithm staticAlgorithm = (StaticAlgorithm) algorithm;
            staticAlgorithm.setAbsNumerator(absNumerator);
            double cre = parseDouble(creText.getText());
            double creBias = pow(10, cre / 10);
            staticAlgorithm.setCreBias(creBias);
            creText.setEnabled(true);
        }
        controller.setAlgorithm(algorithm);
    }

    public void setFocus() {
        executeButton.setFocus();
    }

    public void setEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
        nextButton.setEnabled(enabled);
    }

    public UpdateFrequencyListener getUpdateFrequencyListener() {
        return updateFrequencyListener;
    }

    public void setUpdateFrequencyListener(
            UpdateFrequencyListener updateFrequencyListener) {
        this.updateFrequencyListener = updateFrequencyListener;
    }

}
