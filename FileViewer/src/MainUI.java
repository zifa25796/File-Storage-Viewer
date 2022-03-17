import com.sun.source.tree.Tree;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Array;
import java.util.*;

public class MainUI {
    private JFrame frame;
    private JPanel headPanel, middlePanel, middleLeft, middleRight;
    private JPanel rootFile, blackList;
    private JLabel rootFile_LABEL, blackList_LABEL;
    private JButton startScanning_BTN, lastFile_BTN, compactPieChart_BTN;
    private JTextField rootFile_TXT, blackList_TXT;
    private ChartPanel chartPanel;
    private JScrollPane scrollPane;
    private JTable table;
    private JPanel leftBottomPanel;

    private TreeNode currentNode;
    private Stack<TreeNode> lastNode = new Stack<>();

    public int WindowWidth = 900;
    public int WindowHeight = 600;
    private int rootFileTextWidth = WindowWidth / 3 + 100;
    private int rootFileTextHeight = 20;

    public MainUI() {
        initFrame();
        initPanel();
    }

    private void initFrame() {
        frame = new JFrame("Storage Space Viewer");
        Dimension ScreenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((ScreenDimension.width - WindowWidth) / 2, (ScreenDimension.height - WindowHeight) / 2);
        frame.setPreferredSize(new Dimension(WindowWidth, WindowHeight));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(new ImageIcon("icon\\icon.png").getImage());

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                WindowWidth = frame.getWidth();
                WindowHeight = frame.getHeight();
                rootFileTextWidth = WindowWidth / 3 + 100;

                rootFile.setPreferredSize(new Dimension(WindowWidth - 100, 30));
                rootFile_TXT.setPreferredSize(new Dimension(rootFileTextWidth, rootFileTextHeight));
                rootFile_LABEL.setPreferredSize(new Dimension(100, rootFileTextHeight));
                blackList.setPreferredSize(new Dimension(WindowWidth - 100, 30));
                blackList_TXT.setPreferredSize(new Dimension(rootFileTextWidth, rootFileTextHeight));
                blackList_LABEL.setPreferredSize(new Dimension(100, rootFileTextHeight));
                try {
                    chartPanel.setPreferredSize(new Dimension(WindowWidth / 5 * 3, WindowHeight));
                    scrollPane.setPreferredSize(new Dimension(WindowWidth / 5 * 2, WindowHeight - 30));
                    lastFile_BTN.setPreferredSize(new Dimension(WindowWidth / 5 * 2, 30));
                } catch (Exception error) {}

                headPanel.repaint();
                headPanel.revalidate();
                middleRight.repaint();
                middleRight.revalidate();
                middleLeft.repaint();
                middleLeft.revalidate();
            }
        });
    }

    private void initPanel() {
        headPanel = new JPanel();
        headPanel.setLayout(new GridLayout(2, 1));
        headPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        middleLeft = new JPanel();
//        middleLeft.setLayout(new GridLayout(1, 1));
        middleLeft.setLayout(new BoxLayout(middleLeft, BoxLayout.PAGE_AXIS));
        middleLeft.setBorder(BorderFactory.createEmptyBorder(10, 15, 10 , 15));

        middleRight = new JPanel();
        middleRight.setLayout(new GridLayout(1, 1));

        middlePanel = new JPanel();
        middlePanel.setLayout(new GridLayout(1, 2));

        rootFile = new JPanel();
        rootFile.setLayout(new BoxLayout(rootFile, BoxLayout.X_AXIS));
        headPanel.add(rootFile);

        rootFile_LABEL = new JLabel("Root Location: ");
        rootFile.add(rootFile_LABEL);

        rootFile_TXT = new JTextField(20);
        rootFile_TXT.setText("C:\\Root");
        rootFile.add(rootFile_TXT);

        startScanning_BTN = new JButton("Start Scanning");
        rootFile.add(startScanning_BTN);

        startScanning_BTN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User.getInstance().setRootFileLocation(rootFile_TXT.getText());
                User.getInstance().setBlackListFileLocation(blackList_TXT.getText().split(";"));
                User.getInstance().startScanner();
            }
        });

        blackList = new JPanel();
        blackList.setLayout(new BoxLayout(blackList, BoxLayout.X_AXIS));
        headPanel.add(blackList);

        blackList_LABEL = new JLabel("Black List: ");
        blackList.add(blackList_LABEL);

        blackList_TXT = new JTextField(20);
        blackList_TXT.setText("C:\\Windows");
        blackList.add(blackList_TXT);

        frame.getContentPane().add(headPanel, BorderLayout.PAGE_START);
        middlePanel.add(middleLeft);
        middlePanel.add(middleRight);
        frame.getContentPane().add(middlePanel, BorderLayout.CENTER);
    }

    public void renderFrame() {
        frame.pack();
        frame.setVisible(true);
    }

    public void addPieChart(TreeNode root) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (String key: root.getChildrenKeys()) {
            dataset.setValue(key, root.getChild(key).getFileSize());
        }


        StandardChartTheme chartTheme = new StandardChartTheme("CN");
        chartTheme.setExtraLargeFont(new Font("黑体", Font.BOLD, 20));
        chartTheme.setLargeFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        chartTheme.setRegularFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        ChartFactory.setChartTheme(chartTheme);


        JFreeChart pieChart = ChartFactory.createPieChart(
                root.getPath(),
                dataset,
                true,
                true,
                false
        );
        chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(WindowWidth / 5 * 3, WindowHeight));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(Color.white);

        middleRight.removeAll();
        middleRight.add(chartPanel);
        middleRight.revalidate();
        middleRight.repaint();
        frame.repaint();

        currentNode = root;
    }

    public void addPieChartWithLessFiles(TreeNode root) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        HashMap<String, Long> fileDataset = new HashMap<>();
        for (String key: root.getChildrenKeys()) {
            TreeNode tempNode = root.getChild(key);
            if (tempNode.getObjType() == 0) {
                dataset.setValue(key, tempNode.getFileSize());
            } else if (tempNode.getObjType() == 1) {
                String[] fileEndings = tempNode.getName().split("\\.");
                String trail = fileEndings[fileEndings.length - 1];
                long value = (fileDataset.get(trail) == null) ? 0 : fileDataset.get(trail);
                dataset.setValue(trail, value + tempNode.getFileSize());
            }
        }


        StandardChartTheme chartTheme = new StandardChartTheme("CN");
        chartTheme.setExtraLargeFont(new Font("黑体", Font.BOLD, 20));
        chartTheme.setLargeFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        chartTheme.setRegularFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        ChartFactory.setChartTheme(chartTheme);


        JFreeChart pieChart = ChartFactory.createPieChart(
                root.getPath(),
                dataset,
                true,
                true,
                false
        );
        chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(WindowWidth / 5 * 3, WindowHeight));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(Color.white);

        middleRight.removeAll();
        middleRight.add(chartPanel);
        middleRight.revalidate();
        middleRight.repaint();
        frame.repaint();

        currentNode = root;
    }

    public void addFileNavigation(TreeNode root) {
        Object[][] data = new Object[root.getChildNum()][3];

        int arrayCounter = 0;
        for (String key: root.getChildrenKeys()) {
            TreeNode node = root.getChild(key);
            data[arrayCounter][0] = node.getName();
            data[arrayCounter][1] = TreeNode.getStringFileSize(node.getFileSize());
            data[arrayCounter][2] = (node.getObjType() == 0) ? "Folder" : "File";
            arrayCounter++;
        }

        fileComparator fc = new fileComparator();
        java.util.Arrays.sort(data, fc);

        table = new JTable(data, new Object[]{"Name", "Size", "Type"});
        scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(WindowWidth / 5 * 2, WindowHeight - 30));
        table.setFillsViewportHeight(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JTable tempTable = (JTable)e.getSource();
                int row = tempTable.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && tempTable.getSelectedRow() != -1) {
                    TreeNode nextNode = currentNode.getChild(table.getValueAt(row, 0).toString());
                    if (nextNode.getObjType() == 1) {
                        return;
                    }
                    lastNode.push(currentNode);
                    addPieChart(nextNode);
                    addFileNavigation(nextNode);
                }
            }
        });

        DefaultTableModel tableModel = new DefaultTableModel(data,  new Object[]{"Name", "Size", "Type"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
        table.setModel(tableModel);

        lastFile_BTN = new JButton("< Back");
//        lastFile_BTN.setPreferredSize(new Dimension(WindowWidth / 5 * 2, 30));
        lastFile_BTN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    currentNode = lastNode.pop();
                    addPieChart(currentNode);
                    addFileNavigation(currentNode);
                } catch (Exception error) {
                    return;
                }
            }
        });

        compactPieChart_BTN = new JButton("Compact Pie Chart");
//        compactPieChart_BTN.setPreferredSize();
        compactPieChart_BTN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (compactPieChart_BTN.getText().equals("Compact Pie Chart")) {
                    addPieChartWithLessFiles(currentNode);
                    compactPieChart_BTN.setText("Normal Pie Chart");
                } else {
                    addPieChart(currentNode);
                    compactPieChart_BTN.setText("Compact Pie Chart");
                }
            }
        });

        leftBottomPanel = new JPanel();
        leftBottomPanel.setLayout(new GridLayout(1, 2));
        leftBottomPanel.add(lastFile_BTN);
        leftBottomPanel.add(compactPieChart_BTN);

        middleLeft.removeAll();
        middleLeft.add(scrollPane);
        middleLeft.add(leftBottomPanel);
        middleLeft.revalidate();
        middleLeft.repaint();
        frame.repaint();

        currentNode = root;
    }

    public void showMsg(String msg) {
        JOptionPane.showMessageDialog(frame, msg);
    }
}

class fileComparator implements Comparator<Object[]> {

    @Override
    public int compare(Object[] o1, Object[] o2) {
        double one = strToLong((String)(o1[1]));
        double two = strToLong((String)(o2[1]));

        if (one == two) {
            return 0;
        } else {
            return (one < two) ? 1 : -1;
        }
    }

    private double strToLong(String str) {
        String fileSizeTypeOne = str.split(" ")[1];
        double fileSizeOne = Double.valueOf(str.split(" ")[0]);
        switch (fileSizeTypeOne) {
            case "GB":
                return fileSizeOne * 1024 * 1024 * 1024;
            case "MB":
                return fileSizeOne * 1024 * 1024;
            case "KB":
                return fileSizeOne * 1024;
            default:
                return fileSizeOne;
        }
    }
}