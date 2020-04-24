package com.company.UI;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

public class ProcessPanel extends OshiJPanel {

    private static final long serialVersionUID = 1L;

    private static final String PROCESSES = "进程";
    private static final String[] COLUMNS = { "进程ID", "父进程ID", "线程", "Cpu占用", "mulative", "VSZ", "RSS", "内存占用",
            "进程名" };
    private static final double[] COLUMN_WIDTH_PERCENT = { 0.07, 0.07, 0.07, 0.07, 0.09, 0.1, 0.1, 0.08, 0.35 };

    private transient Map<Integer, OSProcess> priorSnapshotMap = new HashMap<>();

    private transient ButtonGroup cpuOption = new ButtonGroup();
    private transient JRadioButton perProc = new JRadioButton("属于处理器");
    private transient JRadioButton perSystem = new JRadioButton("属于系统");

    private transient ButtonGroup sortOption = new ButtonGroup();
    private transient JRadioButton cpuButton = new JRadioButton("Cpu占用");
    private transient JRadioButton cumulativeCpuButton = new JRadioButton("Cumulative CPU");
    private transient JRadioButton memButton = new JRadioButton("内存占用");

    public ProcessPanel(SystemInfo si) {
        super();
        init(si);
    }

    private void init(SystemInfo si) {
        OperatingSystem os = si.getOperatingSystem();
        JLabel procLabel = new JLabel(PROCESSES);
        add(procLabel, BorderLayout.NORTH);

        JPanel settings = new JPanel();

        JLabel cpuChoice = new JLabel("          CPU %:");
        settings.add(cpuChoice);
        cpuOption.add(perProc);
        settings.add(perProc);
        cpuOption.add(perSystem);
        settings.add(perSystem);
        if (SystemInfo.getCurrentPlatformEnum().equals(PlatformEnum.WINDOWS)) {
            perSystem.setSelected(true);
        } else {
            perProc.setSelected(true);
        }

        JLabel sortChoice = new JLabel("          排序:");
        settings.add(sortChoice);
        sortOption.add(cpuButton);
        settings.add(cpuButton);
        sortOption.add(cumulativeCpuButton);
        settings.add(cumulativeCpuButton);
        sortOption.add(memButton);
        settings.add(memButton);
        cpuButton.setSelected(true);

        TableModel model = new DefaultTableModel(parseProcesses(os.getProcesses(0, null), si), COLUMNS);
        JTable procTable = new JTable(model);
        JScrollPane scrollV = new JScrollPane(procTable);
        scrollV.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        resizeColumns(procTable.getColumnModel());

        add(scrollV, BorderLayout.CENTER);
        add(settings, BorderLayout.SOUTH);

        Timer timer = new Timer(Config.REFRESH_SLOW, e -> {
            DefaultTableModel tableModel = (DefaultTableModel) procTable.getModel();
            Object[][] newData = parseProcesses(os.getProcesses(0, null), si);
            int rowCount = tableModel.getRowCount();
            for (int row = 0; row < newData.length; row++) {
                if (row < rowCount) {
                    // Overwrite row
                    for (int col = 0; col < newData[row].length; col++) {
                        tableModel.setValueAt(newData[row][col], row, col);
                    }
                } else {
                    // Add row
                    tableModel.addRow(newData[row]);
                }
            }
            // Delete any extra rows
            for (int row = rowCount - 1; row >= newData.length; row--) {
                tableModel.removeRow(row);
            }
        });
        timer.start();
    }

    private Object[][] parseProcesses(OSProcess[] procs, SystemInfo si) {
        long totalMem = si.getHardware().getMemory().getTotal();
        int cpuCount = si.getHardware().getProcessor().getLogicalProcessorCount();
        // Build a map with a value for each process to control the sort
        Map<OSProcess, Double> processSortValueMap = new HashMap<>();
        for (OSProcess p : procs) {
            int pid = p.getProcessID();
            // Ignore the Idle process on Windows
            if (pid > 0 || !SystemInfo.getCurrentPlatformEnum().equals(PlatformEnum.WINDOWS)) {
                // Set up for appropriate sort
                if (cpuButton.isSelected()) {
                    processSortValueMap.put(p, p.getProcessCpuLoadBetweenTicks(priorSnapshotMap.get(pid)));
                } else if (cumulativeCpuButton.isSelected()) {
                    processSortValueMap.put(p, p.getProcessCpuLoadCumulative());
                } else {
                    processSortValueMap.put(p, (double) p.getResidentSetSize());
                }
            }
        }
        // Now sort the list by the values
        List<Entry<OSProcess, Double>> procList = new ArrayList<>(processSortValueMap.entrySet());
        procList.sort(Entry.comparingByValue());
        // Insert into array in reverse order (lowest sort value last)
        int i = procList.size();
        Object[][] procArr = new Object[i][COLUMNS.length];
        // These are in descending CPU order
        for (Entry<OSProcess, Double> e : procList) {
            OSProcess p = e.getKey();
            // Matches order of COLUMNS field
            i--;
            int pid = p.getProcessID();
            procArr[i][0] = pid;
            procArr[i][1] = p.getParentProcessID();
            procArr[i][2] = p.getThreadCount();
            if (perProc.isSelected()) {
                procArr[i][3] = String.format("%.1f",
                        100d * p.getProcessCpuLoadBetweenTicks(priorSnapshotMap.get(pid)) / cpuCount);
                procArr[i][4] = String.format("%.1f", 100d * p.getProcessCpuLoadCumulative() / cpuCount);
            } else {
                procArr[i][3] = String.format("%.1f",
                        100d * p.getProcessCpuLoadBetweenTicks(priorSnapshotMap.get(pid)));
                procArr[i][4] = String.format("%.1f", 100d * p.getProcessCpuLoadCumulative());
            }
            procArr[i][5] = FormatUtil.formatBytes(p.getVirtualSize());
            procArr[i][6] = FormatUtil.formatBytes(p.getResidentSetSize());
            procArr[i][7] = String.format("%.1f", 100d * p.getResidentSetSize() / totalMem);
            procArr[i][8] = p.getName();
        }
        // Re-populate snapshot map
        priorSnapshotMap.clear();
        for (OSProcess p : procs) {
            priorSnapshotMap.put(p.getProcessID(), p);
        }
        return procArr;
    }

    private void resizeColumns(TableColumnModel tableColumnModel) {
        TableColumn column;
        int tW = tableColumnModel.getTotalColumnWidth();
        int cantCols = tableColumnModel.getColumnCount();
        for (int i = 0; i < cantCols; i++) {
            column = tableColumnModel.getColumn(i);
            int pWidth = (int) Math.round(COLUMN_WIDTH_PERCENT[i] * tW);
            column.setPreferredWidth(pWidth);
        }
    }

}
