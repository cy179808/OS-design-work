package com.company;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import com.company.UI.*;
import oshi.SystemInfo;


public class InformationWindow {

    private JFrame mainFrame;
    private JButton jMenu;
    private JMenuBar menuBar;

    private SystemInfo si = new SystemInfo();

    public void init() {
        // Create the external frame
        mainFrame = new JFrame(Config.GUI_TITLE);
        mainFrame.setSize(Config.GUI_WIDTH, Config.GUI_HEIGHT);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setResizable(true);
        mainFrame.setLocationByPlatform(true);
        mainFrame.setLayout(new BorderLayout());
        // Add a menu bar
        menuBar = new JMenuBar();
        mainFrame.setJMenuBar(menuBar);
        // Create the first menu option in this thread
        jMenu = getJMenu("操作系统和硬件信息", 'O', "操作系统和硬件信息", new OsHwTextPanel(si));
        menuBar.add(jMenu);
        // Add later menu items in their own threads
        new Thread(new AddMenuBarTask("内存", 'M', "内存", new MemoryPanel(si))).start();
        new Thread(new AddMenuBarTask("CPU", 'C', "CPU", new ProcessorPanel(si))).start();
        new Thread(new AddMenuBarTask("文件存储", 'F', "文件存储", new FileStorePanel(si))).start();
        new Thread(new AddMenuBarTask("进程", 'P', "进程", new ProcessPanel(si))).start();
        new Thread(new AddMenuBarTask("USB 设备", 'U', "USB 设备列表", new UsbPanel(si))).start();
        mainFrame.setVisible(true);
    }


    private JButton getJMenu(String title, char mnemonic, String toolTip, OshiJPanel panel) {
        JButton button = new JButton(title);
        button.setMnemonic(mnemonic);
        button.setToolTipText(toolTip);
        button.addActionListener(e -> {
            Container contentPane = this.mainFrame.getContentPane();
            if (contentPane.getComponents().length <= 0 || contentPane.getComponent(0) != panel) {
                resetMainGui();
                this.mainFrame.getContentPane().add(panel);
                refreshMainGui();
            }
        });

        return button;
    }

    private void resetMainGui() {
        this.mainFrame.getContentPane().removeAll();
    }

    private void refreshMainGui() {
        this.mainFrame.revalidate();
        this.mainFrame.repaint();
    }

    /**
     * Runnable class to add a JMenu to the menubar.
     */
    private final class AddMenuBarTask implements Runnable {
        private String title;
        private char mnemonic;
        private String toolTip;
        private OshiJPanel panel;

        private AddMenuBarTask(String title, char mnemonic, String toolTip, OshiJPanel panel) {
            this.title = title;
            this.mnemonic = mnemonic;
            this.toolTip = toolTip;
            this.panel = panel;
        }

        @Override
        public void run() {
            menuBar.add(getJMenu(title, mnemonic, toolTip, panel));
        }
    }
}

