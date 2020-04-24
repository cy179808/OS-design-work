package com.company.UI;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Instant;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.software.os.OperatingSystem;
import oshi.util.EdidUtil;
import oshi.util.FormatUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class OsHwTextPanel extends OshiJPanel {

    private static final long serialVersionUID = 1L;

    private static final String OPERATING_SYSTEM = "操作系统";
    private static final String HARDWARE_INFORMATION = "硬件信息";
    private static final String PROCESSOR = "处理器";
    private static final String DISPLAYS = "Display";
    private String osPrefix;

    public OsHwTextPanel(SystemInfo si) {
        super();
        init(si);
    }

    private void init(SystemInfo si) {
        osPrefix = getOsPrefix(si);

        GridBagConstraints osLabel = new GridBagConstraints();
        GridBagConstraints osConstraints = new GridBagConstraints();
        osConstraints.gridy = 1;
        osConstraints.fill = GridBagConstraints.BOTH;
        osConstraints.insets = new Insets(0, 0, 15, 15); // T,L,B,R

        GridBagConstraints procLabel = (GridBagConstraints) osLabel.clone();
        procLabel.gridy = 2;
        GridBagConstraints procConstraints = (GridBagConstraints) osConstraints.clone();
        procConstraints.gridy = 3;

        GridBagConstraints displayLabel = (GridBagConstraints) procLabel.clone();
        displayLabel.gridy = 4;
        GridBagConstraints displayConstraints = (GridBagConstraints) osConstraints.clone();
        displayConstraints.gridy = 5;
        displayConstraints.insets = new Insets(0, 0, 0, 15); // T,L,B,R

        GridBagConstraints csLabel = (GridBagConstraints) osLabel.clone();
        csLabel.gridx = 1;
        GridBagConstraints csConstraints = new GridBagConstraints();
        csConstraints.gridx = 1;
        csConstraints.gridheight = 6;
        csConstraints.fill = GridBagConstraints.BOTH;

        JPanel oshwPanel = new JPanel();
        oshwPanel.setLayout(new GridBagLayout());

        JTextArea osArea = new JTextArea(0, 0);
        osArea.setText(updateOsData(si));
        oshwPanel.add(new JLabel(OPERATING_SYSTEM), osLabel);
        oshwPanel.add(osArea, osConstraints);

        JTextArea procArea = new JTextArea(0, 0);
        procArea.setText(getProc(si));
        oshwPanel.add(new JLabel(PROCESSOR), procLabel);
        oshwPanel.add(procArea, procConstraints);

        JTextArea displayArea = new JTextArea(0, 0);
        displayArea.setText(getDisplay(si));
        oshwPanel.add(new JLabel(DISPLAYS), displayLabel);
        oshwPanel.add(displayArea, displayConstraints);

        JTextArea csArea = new JTextArea(0, 0);
        csArea.setText(getHw(si));
        oshwPanel.add(new JLabel(HARDWARE_INFORMATION), csLabel);
        oshwPanel.add(csArea, csConstraints);

        add(oshwPanel, BorderLayout.CENTER);

        // Update up time every second
        Timer timer = new Timer(Config.REFRESH_FAST, e -> osArea.setText(updateOsData(si)));
        timer.start();
    }

    private static String getOsPrefix(SystemInfo si) {
        StringBuilder sb = new StringBuilder(OPERATING_SYSTEM);

        OperatingSystem os = si.getOperatingSystem();
        sb.append(String.valueOf(os));
        sb.append("\n\n").append("Booted: ").append(Instant.ofEpochSecond(os.getSystemBootTime())).append('\n')
                .append("Uptime: ");
        return sb.toString();
    }

    private static String getHw(SystemInfo si) {
        StringBuilder sb = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        ComputerSystem computerSystem = si.getHardware().getComputerSystem();
        try {
            sb.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(computerSystem));
        } catch (JsonProcessingException e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    private static String getProc(SystemInfo si) {
        StringBuilder sb = new StringBuilder();
        CentralProcessor proc = si.getHardware().getProcessor();
        sb.append(proc.toString());

        return sb.toString();
    }

    private static String getDisplay(SystemInfo si) {
        StringBuilder sb = new StringBuilder();
        Display[] displays = si.getHardware().getDisplays();
        if (displays.length < 1) {
            sb.append("None detected.");
        } else {
            for (int i = 0; i < displays.length; i++) {
                byte[] edid = displays[i].getEdid();
                byte[][] desc = EdidUtil.getDescriptors(edid);
                String name = "Display " + i;
                for (byte[] b : desc) {
                    if (EdidUtil.getDescriptorType(b) == 0xfc) {
                        name = EdidUtil.getDescriptorText(b);
                    }
                }
                if (i > 0) {
                    sb.append('\n');
                }
                sb.append(name).append(": ");
                int hSize = EdidUtil.getHcm(edid);
                int vSize = EdidUtil.getVcm(edid);
                sb.append(String.format("%d x %d cm (%.1f x %.1f in)", hSize, vSize, hSize / 2.54, vSize / 2.54));
            }
        }
        return sb.toString();
    }

    private String updateOsData(SystemInfo si) {
        return osPrefix + FormatUtil.formatElapsedSecs(si.getOperatingSystem().getSystemUptime());
    }

}
