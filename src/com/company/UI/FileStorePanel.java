package com.company.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;;

public class FileStorePanel extends OshiJPanel {

    private static final long serialVersionUID = 1L;

    private static final String USED = "已使用";
    private static final String AVAILABLE = "未使用";

    public FileStorePanel(SystemInfo si) {
        super();
        init(si.getOperatingSystem().getFileSystem());
    }

    private void init(FileSystem fs) {
        OSFileStore[] fileStores = fs.getFileStores();
        DefaultPieDataset[] fsData = new DefaultPieDataset[fileStores.length];
        JFreeChart[] fsCharts = new JFreeChart[fsData.length];

        JPanel fsPanel = new JPanel();
        fsPanel.setLayout(new GridBagLayout());
        GridBagConstraints fsConstraints = new GridBagConstraints();
        fsConstraints.weightx = 1d;
        fsConstraints.weighty = 1d;
        fsConstraints.fill = GridBagConstraints.BOTH;

        int modBase = (int) (fileStores.length * (Config.GUI_HEIGHT + Config.GUI_WIDTH)
                / (Config.GUI_WIDTH * Math.sqrt(fileStores.length)));
        for (int i = 0; i < fileStores.length; i++) {
            fsData[i] = new DefaultPieDataset();
            fsCharts[i] = ChartFactory.createPieChart(null, fsData[i], true, true, false);
            configurePlot(fsCharts[i]);
            fsConstraints.gridx = i % modBase;
            fsConstraints.gridy = i / modBase;
            fsPanel.add(new ChartPanel(fsCharts[i]), fsConstraints);
        }
        updateDatasets(fs, fsData, fsCharts);

        add(fsPanel, BorderLayout.CENTER);

        Timer timer = new Timer(Config.REFRESH_SLOWER, e -> {
            if (!updateDatasets(fs, fsData, fsCharts)) {
                ((Timer) e.getSource()).stop();
                fsPanel.removeAll();
                init(fs);
                fsPanel.revalidate();
                fsPanel.repaint();
            }
        });
        timer.start();
    }

    private static boolean updateDatasets(FileSystem fs, DefaultPieDataset[] fsData, JFreeChart[] fsCharts) {
        OSFileStore[] fileStores = fs.getFileStores();
        if (fileStores.length != fsData.length) {
            return false;
        }
        for (int i = 0; i < fsData.length; i++) {
            fsCharts[i].setTitle(fileStores[i].getName());
            List<TextTitle> subtitles = new ArrayList<>();
            if (SystemInfo.getCurrentPlatformEnum().equals(PlatformEnum.WINDOWS)) {
                subtitles.add(new TextTitle(fileStores[i].getLabel()));
            }
            long usable = fileStores[i].getUsableSpace();
            long total = fileStores[i].getTotalSpace();
            subtitles.add(new TextTitle(
                    "未使用空间: " + FormatUtil.formatBytes(usable) + "/" + FormatUtil.formatBytes(total)));
            fsCharts[i].setSubtitles(subtitles);
            fsData[i].setValue(USED, (double) total - usable);
            fsData[i].setValue(AVAILABLE, usable);
        }
        return true;
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
