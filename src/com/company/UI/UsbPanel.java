package com.company.UI;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.text.DefaultCaret;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.UsbDevice;

public class UsbPanel extends OshiJPanel {

    private static final long serialVersionUID = 1L;

    private static final String USB_DEVICES = "USB 设备";

    public UsbPanel(SystemInfo si) {
        super();
        init(si.getHardware());
    }

    private void init(HardwareAbstractionLayer hal) {

        JLabel usb = new JLabel(USB_DEVICES);
        add(usb, BorderLayout.NORTH);
        JTextArea usbArea = new JTextArea(60, 20);
        JScrollPane scrollV = new JScrollPane(usbArea);
        scrollV.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        DefaultCaret caret = (DefaultCaret) usbArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        usbArea.setText(getUsbString(hal));
        add(scrollV, BorderLayout.CENTER);

        Timer timer = new Timer(Config.REFRESH_SLOW, e -> usbArea.setText(getUsbString(hal)));
        timer.start();
    }

    private static String getUsbString(HardwareAbstractionLayer hal) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (UsbDevice usbDevice : hal.getUsbDevices(true)) {
            if (first) {
                first = false;
            } else {
                sb.append('\n');
            }
            sb.append(String.valueOf(usbDevice));
        }
        return sb.toString();
    }
}
