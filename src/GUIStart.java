import skyline.ISkySel;
import tool.DataHandler;
import tool.FileHandler;
import tool.KNNFilled;

import java.util.*;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;

public class GUIStart extends JFrame {
    private String[] attributeName = null;
    private double[][] data = null;
    private double[][] dataNormalized = null;
    private boolean readData = false;
    private boolean normalization = false;

    private double[][] dataSelected;
    private double[][] dataNormSelected;
    private String[] attributeSelectedName;
    private boolean selectData = false;
    private boolean[] selectDimension;
    private int topk = 0;
    private int scaleStart;
    private int scaleEnd;

    private boolean[] smallerFlag;

    private final int fontSize = 18;

    private void warning(String message, String title) {
        JLabel label = new JLabel(message);
        Font labelFont = new Font(label.getFont().getName(), Font.PLAIN, fontSize);
        label.setFont(labelFont);
        JOptionPane.showMessageDialog(null,
                label, title, JOptionPane.WARNING_MESSAGE);
    }

    private void info(String message, String title) {
        JLabel label = new JLabel(message);
        Font labelFont = new Font(label.getFont().getName(), Font.PLAIN, fontSize);
        label.setFont(labelFont);
        JOptionPane.showMessageDialog(null,
                label, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private Object[][] transferTableData(double[][] tableData, int[] lineId) {
        int n = tableData.length;
        int d = tableData[0].length;
        Object[][] dataObj = new Object[n][d + 1];
        for (int i = 0; i < n; i++) {
            dataObj[i][0] = lineId[i];
            for (int j = 1; j <= d; j++) {
                if (tableData[i][j - 1] < 0) {
                    dataObj[i][j] = "";
                } else {
                    dataObj[i][j] = String.format("%.3f", tableData[i][j - 1]);
                }
            }
        }
        return dataObj;
    }

    private Object[] transferTableColumn(String[] attributes, int attributesNum) {
        Object[] colName = new Object[attributesNum + 1];
        if (attributes == null) {
            colName[0] = "LineId";
            for (int i = 1; i <= attributesNum; i++) {
                colName[i] = i;
            }
        } else {
            colName[0] = "LineId";
            System.arraycopy(attributes, 0, colName, 1, attributesNum);
        }
        return colName;
    }

    private JTable buildTable(Object[][] dataObj, Object[] colName) {
        JTable table = new JTable(dataObj, colName);
        table.setFont(new Font(table.getFont().getName(), Font.PLAIN, fontSize));
        table.setRowHeight(25);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font(header.getFont().getName(), Font.PLAIN, fontSize));

        return table;
    }

    private JTable buildTable(double[][] tableData, String[] attributes, int rowStart) {
        int n = tableData.length;
        int d = tableData[0].length;

        int[] lineId = new int[tableData.length];
        for (int i = 0; i < n; i++) {
            lineId[i] = rowStart + i;
        }

        Object[][] dataObj = transferTableData(tableData, lineId);
        Object[] colName = transferTableColumn(attributes, d);

        return buildTable(dataObj, colName);
    }

    private void initParam() {
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setTitle("ISkySel");
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, 1500, 800);
    }

    private JButton myJButton(String text) {
        JButton button = new JButton(text);
        Font textFont = new Font(button.getFont().getName(), Font.PLAIN, fontSize);
        button.setFont(textFont);
        return button;
    }

    private JLabel myJLabel(String text) {
        JLabel label = new JLabel(text);
        Font textFont = new Font(label.getFont().getName(), Font.PLAIN, fontSize);
        label.setFont(textFont);
        return label;
    }

    private JCheckBox myJCheckBox(String text) {
        JCheckBox chkbox = new JCheckBox(text);
        Font textFont = new Font(chkbox.getFont().getName(), Font.PLAIN, fontSize);
        chkbox.setFont(textFont);
        return chkbox;
    }

    private static File[] readFileGUI(String title) {
        JFileChooser fDialog = new JFileChooser();
        fDialog.setCurrentDirectory(new File("./"));
        // set title
        fDialog.setDialogTitle(title);
        // multiple selectoin
        fDialog.setMultiSelectionEnabled(true);
        int returnVal = fDialog.showOpenDialog(null);
        if (JFileChooser.APPROVE_OPTION == returnVal) {
            return fDialog.getSelectedFiles();
        } else {
            System.out.println("Choose File Stopped!");
            return null;
        }
    }

    public GUIStart(int bins, int k) {
        initParam();

        Container container = getContentPane();
        SpringLayout springLayout = new SpringLayout();
        container.setLayout(springLayout);
        JButton read = myJButton("Read File");
        JButton readAttribute = myJButton("Read Attributes");
        JButton normalized = myJButton("Normalize");
        JButton select = myJButton("Select");
        JButton run = myJButton("Run");

        Spring st = Spring.constant(10);
        Spring st2 = Spring.constant(30);

        container.add(read);
        springLayout.putConstraint(SpringLayout.NORTH, read, st, SpringLayout.NORTH, container);
        springLayout.putConstraint(SpringLayout.WEST, read, st, SpringLayout.WEST, container);
        springLayout.putConstraint(SpringLayout.EAST, read, 0, SpringLayout.EAST, readAttribute);

        container.add(readAttribute);
        springLayout.putConstraint(SpringLayout.NORTH, readAttribute, st, SpringLayout.SOUTH, read);
        springLayout.putConstraint(SpringLayout.WEST, readAttribute, st, SpringLayout.WEST, container);

        JPanel cards = new JPanel(new CardLayout());
        container.add(cards);
        springLayout.putConstraint(SpringLayout.NORTH, cards, st, SpringLayout.NORTH, container);
        springLayout.putConstraint(SpringLayout.EAST, cards, st, SpringLayout.EAST, container);
        springLayout.putConstraint(SpringLayout.SOUTH, cards, st, SpringLayout.SOUTH, container);
        springLayout.putConstraint(SpringLayout.WEST, cards, st2, SpringLayout.EAST, read);

        JTextArea textArea = new JTextArea();
        cards.add(textArea, "info");
        textArea.setEditable(false);

        container.add(select);
        springLayout.putConstraint(SpringLayout.NORTH, select, st2, SpringLayout.SOUTH, readAttribute);
        springLayout.putConstraint(SpringLayout.WEST, select, st, SpringLayout.WEST, container);
        springLayout.putConstraint(SpringLayout.EAST, select, 0, SpringLayout.EAST, read);

        container.add(normalized);
        springLayout.putConstraint(SpringLayout.NORTH, normalized, st2, SpringLayout.SOUTH, select);
        springLayout.putConstraint(SpringLayout.WEST, normalized, st, SpringLayout.WEST, container);
        springLayout.putConstraint(SpringLayout.EAST, normalized, 0, SpringLayout.EAST, readAttribute);

        container.add(run);
        springLayout.putConstraint(SpringLayout.NORTH, run, st2, SpringLayout.SOUTH, normalized);
        springLayout.putConstraint(SpringLayout.WEST, run, st, SpringLayout.WEST, container);
        springLayout.putConstraint(SpringLayout.EAST, run, 0, SpringLayout.EAST, readAttribute);

        // 1. execute the read file logic
        read.addActionListener(o -> {
            File[] files = readFileGUI("Read File");
            boolean success = true;
            if (files == null) {
                success = false;
                warning("Wrong File! Please Select One File!", "File Read Warning");
            } else if (files.length != 1) {
                warning("Wrong File! Please Select Only One File!", "Format Warning");
            } else {
                try {
                    this.data = FileHandler.readData(files[0]);
                } catch (Exception e) {
                    success = false;
                    warning("Wrong File! Please Select The Right Formatted File!", "Format Warning");
                }
            }
            if (success && data == null) {
                success = false;
                warning("Wrong File! Please Select The Right Formatted File!", "Format Warning");
            }
            if (success) {
                readData = true;
                JTable table = buildTable(this.data, this.attributeName, 1);
                JScrollPane scrollPane = new JScrollPane(table);
                cards.add(scrollPane, "origin data");
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, "origin data");
            }
        });
        // Read Attribute
        readAttribute.addActionListener(o -> {
            File[] files = readFileGUI("Read File");
            if (files == null || files.length != 1) {
                warning("Wrong Attribute File! Please Select The Right Formatted File or Choose One File!", "File Read Warning");
            } else {
                try {
                    this.attributeName = FileHandler.readAttribute(files[0]);
                    JTable table = buildTable(this.data, this.attributeName, 1);
                    JScrollPane scrollPane = new JScrollPane(table);
                    cards.add(scrollPane, "origin data");
                    CardLayout cl = (CardLayout) (cards.getLayout());
                    cl.show(cards, "origin data");
                } catch (Exception e) {
                    warning("Wrong File! Please Select The Right Formatted File!", "Format Warning");
                }
            }
        });

        // 2. execute the select logic
        select.addActionListener(o -> {
            if (!this.readData) {
                warning("Please Read Data First", "warning");
            } else {
                SpringLayout selectSpring = new SpringLayout();
                JPanel panel = new JPanel(selectSpring);
                cards.add(panel, "select");

                JLabel scaleLabel = myJLabel("scale");
                JTextField scaleStart = new JTextField(5);
                JTextField scaleEnd = new JTextField(5);
                JLabel scaleTmpLabel = myJLabel("-");

                Font f = new Font(scaleStart.getFont().getName(), Font.PLAIN, fontSize);
                scaleStart.setFont(f);
                scaleEnd.setFont(f);

                JLabel dimensionLabel = myJLabel("dimension");
                JButton selectAll = myJButton("select all");

                JLabel kLabel = myJLabel("k");
                JTextField kField = new JTextField(5);
                kField.setFont(f);

                JButton confirm = myJButton("confirm");

                int d = data[0].length;
                JCheckBox[] boxes = new JCheckBox[d];

                if (attributeName == null) {
                    for (int i = 0; i < d; i++) {
                        if (i + 1 < 10) {
                            boxes[i] = myJCheckBox("Attribute " + (i + 1) + " ");
                        } else {
                            boxes[i] = myJCheckBox("Attribute " + (i + 1));
                        }
                    }
                } else {
                    for (int i = 0; i < d; i++) {
                        boxes[i] = myJCheckBox(attributeName[i]);
                    }
                }

                panel.add(scaleLabel);
                selectSpring.putConstraint(SpringLayout.NORTH, scaleLabel, st, SpringLayout.NORTH, container);
                selectSpring.putConstraint(SpringLayout.EAST, scaleLabel, 0, SpringLayout.EAST, selectAll);

                panel.add(scaleStart);
                selectSpring.putConstraint(SpringLayout.NORTH, scaleStart, st, SpringLayout.NORTH, container);
                selectSpring.putConstraint(SpringLayout.WEST, scaleStart, st2, SpringLayout.EAST, scaleLabel);

                panel.add(scaleTmpLabel);
                selectSpring.putConstraint(SpringLayout.NORTH, scaleTmpLabel, st, SpringLayout.NORTH, container);
                selectSpring.putConstraint(SpringLayout.WEST, scaleTmpLabel, st2, SpringLayout.EAST, scaleStart);

                panel.add(scaleEnd);
                selectSpring.putConstraint(SpringLayout.NORTH, scaleEnd, st, SpringLayout.NORTH, container);
                selectSpring.putConstraint(SpringLayout.WEST, scaleEnd, st2, SpringLayout.EAST, scaleTmpLabel);

                panel.add(dimensionLabel);
                selectSpring.putConstraint(SpringLayout.NORTH, dimensionLabel, st2, SpringLayout.SOUTH, scaleLabel);
                selectSpring.putConstraint(SpringLayout.EAST, dimensionLabel, 0, SpringLayout.EAST, selectAll);

                // check boxes layout
                Spring st20 = Spring.constant(20);
                panel.add(boxes[0]);
                selectSpring.putConstraint(SpringLayout.NORTH, boxes[0], st20, SpringLayout.SOUTH, scaleLabel);
                selectSpring.putConstraint(SpringLayout.WEST, boxes[0], st20, SpringLayout.EAST, dimensionLabel);

                int lineId = 0;
                for (int i = 1; i < d; i++) {
                    // six checkboxes in one line
                    if (i % 5 == 0) {
                        panel.add(boxes[i]);
                        selectSpring.putConstraint(SpringLayout.NORTH, boxes[i], st20, SpringLayout.SOUTH, boxes[i - 5]);
                        selectSpring.putConstraint(SpringLayout.WEST, boxes[i], st20, SpringLayout.EAST, dimensionLabel);
                        lineId += 1;
                    } else {
                        panel.add(boxes[i]);
                        if (lineId == 0) {
                            selectSpring.putConstraint(SpringLayout.NORTH, boxes[i], st20, SpringLayout.SOUTH, scaleLabel);
                        } else {
                            selectSpring.putConstraint(SpringLayout.NORTH, boxes[i], st20, SpringLayout.SOUTH, boxes[i - 5]);
                        }
                        selectSpring.putConstraint(SpringLayout.WEST, boxes[i], st20, SpringLayout.EAST, boxes[i - 1]);
                    }
                }

                panel.add(selectAll);
                selectSpring.putConstraint(SpringLayout.NORTH, selectAll, st, SpringLayout.SOUTH, dimensionLabel);
                selectSpring.putConstraint(SpringLayout.WEST, selectAll, st, SpringLayout.WEST, container);
                selectAll.addActionListener(o2 -> {
                    for (JCheckBox box : boxes) {
                        box.doClick();
                    }
                });

                panel.add(kLabel);
                selectSpring.putConstraint(SpringLayout.NORTH, kLabel, st2, SpringLayout.SOUTH, boxes[boxes.length - 1]);
                selectSpring.putConstraint(SpringLayout.EAST, kLabel, 0, SpringLayout.EAST, selectAll);

                panel.add(kField);
                selectSpring.putConstraint(SpringLayout.NORTH, kField, 0, SpringLayout.NORTH, kLabel);
                selectSpring.putConstraint(SpringLayout.WEST, kField, st2, SpringLayout.EAST, kLabel);

                panel.add(confirm);
                selectSpring.putConstraint(SpringLayout.NORTH, confirm, st2, SpringLayout.SOUTH, kLabel);
                selectSpring.putConstraint(SpringLayout.EAST, confirm, 0, SpringLayout.EAST, selectAll);

                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, "select");

                // show data
                confirm.addActionListener(o2 -> {
                    int scaleStartTmp = checkLegal(scaleStart.getText());
                    int scaleEndTmp = checkLegal(scaleEnd.getText());
                    int kTmp = checkLegal(kField.getText());

                    if (scaleStartTmp < 0 || scaleEndTmp < 0 || kTmp < 0) {
                        warning("Input Should Be Bigger Than Zero!", "Illegal Number");
                    } else if (scaleStartTmp > data.length || scaleEndTmp > data.length) {
                        warning("Scale Out of Data Range", "Illegal Number");
                    } else if (kTmp > (data.length / data[0].length)) {
                        warning("K Is Out of Data Range", "Illegal Number");
                    } else {
                        this.scaleStart = scaleStartTmp;
                        this.scaleEnd = scaleEndTmp;
                        this.topk = kTmp;

                        this.selectDimension = new boolean[d];
                        int cnt = 0;
                        for (int i = 0; i < d; i++) {
                            selectDimension[i] = boxes[i].isSelected();
                            if (boxes[i].isSelected()) {
                                cnt += 1;
                            }
                        }
                        if (cnt == 0) {
                            warning("Please Select At Least One QoS", "Illegal QoS");
                        } else {
                            this.dataSelected = DataHandler.dataSlice(this.data, this.scaleStart, this.scaleEnd, this.selectDimension);
                            this.selectData = true;

                            String[] attributes = null;
                            if (this.attributeName != null) {
                                attributes = new String[dataSelected[0].length];
                                int index = 0;
                                for (int i = 0; i < selectDimension.length; i++) {
                                    if (selectDimension[i]) {
                                        attributes[index++] = this.attributeName[i];
                                    }
                                }
                            }
                            this.attributeSelectedName = attributes;
                            JTable table = buildTable(this.dataSelected, this.attributeSelectedName, this.scaleStart);
                            JScrollPane scrollPane = new JScrollPane(table);
                            cards.add(scrollPane, "after select");
                            cl.show(cards, "after select");
                        }
                    }
                });
            }
        });

        // 3. execute the normalize logic
        normalized.addActionListener(o -> {
            if (!readData) {
                warning("Please Read Data first!", "warning");
            } else if (!this.selectData) {
                warning("Please Select Data first!", "warning");
            } else {
                SpringLayout normalSpring = new SpringLayout();
//                JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 20));
                JPanel jPanel = new JPanel(normalSpring);
                int chkBoxNum = this.attributeSelectedName.length;
                System.out.println("chkBoxNum: " + chkBoxNum);
                JLabel text = myJLabel("Please Select the Smaller the Better Attributes");

                jPanel.add(text);
                normalSpring.putConstraint(SpringLayout.NORTH, text, 0, SpringLayout.NORTH, normalized);
                normalSpring.putConstraint(SpringLayout.WEST, text, st, SpringLayout.EAST, normalized);

                JCheckBox[] boxes = new JCheckBox[chkBoxNum];
                for (int i = 0; i < boxes.length; i++) {
                    if (this.attributeName == null) {
                        boxes[i] = myJCheckBox("Attribute " + (i + 1));
                    } else {
                        boxes[i] = myJCheckBox(this.attributeSelectedName[i]);
                    }
                    jPanel.add(boxes[i]);
                }

                // check boxes layout
                Spring st20 = Spring.constant(20);
                jPanel.add(boxes[0]);
                normalSpring.putConstraint(SpringLayout.NORTH, boxes[0], st2, SpringLayout.SOUTH, text);
                normalSpring.putConstraint(SpringLayout.WEST, boxes[0], st, SpringLayout.EAST, normalized);

                int lineId = 0;
                for (int i = 1; i < boxes.length; i++) {
                    // six checkboxes in one line
                    if (i % 5 == 0) {
                        jPanel.add(boxes[i]);
                        normalSpring.putConstraint(SpringLayout.NORTH, boxes[i], st20, SpringLayout.SOUTH, boxes[i - 5]);
                        normalSpring.putConstraint(SpringLayout.WEST, boxes[i], st, SpringLayout.EAST, normalized);
                        lineId += 1;
                    } else {
                        jPanel.add(boxes[i]);
                        if (lineId == 0) {
                            normalSpring.putConstraint(SpringLayout.NORTH, boxes[i], st20, SpringLayout.SOUTH, normalized);
                        } else {
                            normalSpring.putConstraint(SpringLayout.NORTH, boxes[i], st20, SpringLayout.SOUTH, boxes[i - 5]);
                        }
                        normalSpring.putConstraint(SpringLayout.WEST, boxes[i], st20, SpringLayout.EAST, boxes[i - 1]);
                    }
                }

                cards.add(jPanel, "normal logic");
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, "normal logic");

                // confirm action
                JButton confirm = myJButton("confirm");
                jPanel.add(confirm);
                normalSpring.putConstraint(SpringLayout.NORTH, confirm, st2, SpringLayout.SOUTH, boxes[boxes.length - 1]);
                normalSpring.putConstraint(SpringLayout.WEST, confirm, st, SpringLayout.EAST, normalized);
                confirm.addActionListener(tmp -> {
                    smallerFlag = new boolean[chkBoxNum];
                    for (int i = 0; i < boxes.length; i++) {
                        smallerFlag[i] = boxes[i].isSelected();
                    }

                    this.dataNormSelected = DataHandler.dataNormalized(this.dataSelected, smallerFlag);

                    this.normalization = true;

                    JTable table;
                    if (!this.selectData) {
                        table = buildTable(dataNormalized, attributeName, scaleStart);
                    } else {
                        table = buildTable(dataNormSelected, this.attributeSelectedName, scaleStart);
                    }
                    JScrollPane scrollPane = new JScrollPane(table);
                    cards.add(scrollPane, "normal data");
                    cl.show(cards, "normal data");
                    this.normalization = true;
                    info("Normalization is Succeed", "info");
                });
            }
        });


        // 4. execute the run logic
        run.addActionListener(o -> {
            if (!this.readData) {
                warning("Please Read Data first!", "warning");
            } else if (!this.normalization) {
                warning("Please Execute Data Normalization", "warning");
            } else if (!this.selectData) {
                warning("Please Select the Param", "warning");
            } else {
                ISkySel iSkySel = new ISkySel();
                KNNFilled knn = new KNNFilled();
                double[][][] dataFilled = knn.knnFilled(this.dataNormSelected, k);
                double[][][] dataFilledInterval = DataHandler.buildIntervalData(Arrays.asList(dataFilled));
                int[] skyline = iSkySel.getSkyline(this.dataNormSelected, dataFilledInterval, bins, this.topk);

                double[][] values = new double[skyline.length][this.dataSelected[0].length];
                for (int i = 0; i < values.length; i++) {
                    int index = skyline[i];
                    for (int j = 0; j < values[0].length; j++) {
                        values[i][j] = dataSelected[index][j];
                    }
                }

                int[] lineId = new int[skyline.length];
                for (int i = 0; i < lineId.length; i++) {
                    lineId[i] = skyline[i] + this.scaleStart;
                }

                Object[][] dataObj = transferTableData(values, lineId);
                Object[] columnName = transferTableColumn(this.attributeSelectedName, values[0].length);

                JTable table = buildTable(dataObj, columnName);
                JScrollPane scrollPane = new JScrollPane(table);
                cards.add(scrollPane, "result");
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, "result");
            }
        });
    }

    /**
     * check the text if a number
     * or return -1
     */
    private int checkLegal(String text) {
        if (text == null) {
            return -1;
        } else if (text.matches("[0-9]+") && text.length() < 10) {
            return Integer.parseInt(text);
        } else {
            return -1;
        }
    }

    public static void main(String[] args) {
        // We Use Histogram to Build PDF
        // bins is the number of columns
        int bins = 20;

        // We Use KNN to Impute Data
        // k Is the Multiple Impute Number (Neighbours=k)
        int k = 5;

        new GUIStart(bins, k);
    }
}