package com.company.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.util.Collections;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;

public class MemoryPanel extends OshiJPanel {

    private static final long serialVersionUID = 1L;

    private static final String PHYSICAL_MEMORY = "PHYSICAL_MEMORY";
    private static final String VIRTUAL_MEMORY = "VIRTUAL_MEMORY";

    private static final String USED = "used";
    private static final String AVAILABLE = "available";

    public MemoryPanel(SystemInfo si) {
        super();
        init(si.getHardware().getMemory());
    }

    private void init(GlobalMemory memory) {
        DefaultPieDataset physMemData = new DefaultPieDataset();
        DefaultPieDataset virtMemData = new DefaultPieDataset();
        updateDatasets(memory, physMemData, virtMemData);

        JFreeChart physMem = ChartFactory.createPieChart(PHYSICAL_MEMORY, physMemData, true, true, false);
        JFreeChart virtMem = ChartFactory.createPieChart(VIRTUAL_MEMORY, virtMemData, true, true, false);
        configurePlot(physMem);
        configurePlot(virtMem);
        physMem.setSubtitles(Collections.singletonList(new TextTitle(updatePhysTitle(memory))));
        virtMem.setSubtitles(Collections.singletonList(new TextTitle(updateVirtTitle(memory))));

        GridBagConstraints pmConstraints = new GridBagConstraints();
        pmConstraints.weightx = 1d;
        pmConstraints.weighty = 1d;
        pmConstraints.fill = GridBagConstraints.BOTH;
        GridBagConstraints vmConstraints = (GridBagConstraints) pmConstraints.clone();
        vmConstraints.gridx = 1;
        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.gridy = 1;
        textConstraints.gridwidth = 2;
        textConstraints.fill = GridBagConstraints.BOTH;

        JPanel memoryPanel = new JPanel();
        memoryPanel.setLayout(new GridBagLayout());
        memoryPanel.add(new ChartPanel(physMem), pmConstraints);
        memoryPanel.add(new ChartPanel(virtMem), vmConstraints);

        JTextArea textArea = new JTextArea(60, 20);
        textArea.setText(updateMemoryText(memory));
        memoryPanel.add(textArea, textConstraints);

        add(memoryPanel, BorderLayout.CENTER);

        Timer timer = new Timer(Config.REFRESH_SLOW, e -> {
            updateDatasets(memory, physMemData, virtMemData);
            physMem.setSubtitles(Collections.singletonList(new TextTitle(updatePhysTitle(memory))));
            virtMem.setSubtitles(Collections.singletonList(new TextTitle(updateVirtTitle(memory))));
            textArea.setText(updateMemoryText(memory));
        });
        timer.start();
    }

    private static String updatePhysTitle(GlobalMemory memory) {
        return memory.toString();
    }

    private static String updateVirtTitle(GlobalMemory memory) {
        return memory.getVirtualMemory().toString();
    }

    private static String updateMemoryText(GlobalMemory memory) {
        StringBuilder sb = new StringBuilder();
        PhysicalMemory[] pmArray = memory.getPhysicalMemory();
        if (pmArray.length > 0) {
            for (PhysicalMemory pm : pmArray) {
                sb.append('\n').append(pm.toString());
            }
        }
        return sb.toString();
    }

    private static void updateDatasets(GlobalMemory memory, DefaultPieDataset physMemData,
                                       DefaultPieDataset virtMemData) {
        physMemData.setValue(USED, (double) memory.getTotal() - memory.getAvailable());
        physMemData.setValue(AVAILABLE, memory.getAvailable());

        VirtualMemory virtualMemory = memory.getVirtualMemory();
        virtMemData.setValue(USED, virtualMemory.getSwapUsed());
        virtMemData.setValue(AVAILABLE, (double) virtualMemory.getSwapTotal() - virtualMemory.getSwapUsed());
    }

    private static void configurePlot(JFreeChart chart) {
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint(USED, Color.red);
        plot.setSectionPaint(AVAILABLE, Color.green);
        plot.setExplodePercent(USED, 0.10);
        plot.setSimpleLabels(true);

        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator("{0}: {1} ({2})",
                new DecimalFormat("0"), new DecimalFormat("0%"));
        plot.setLabelGenerator(labelGenerator);
    }

}
