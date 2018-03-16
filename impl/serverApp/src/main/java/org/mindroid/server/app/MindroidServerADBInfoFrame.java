package org.mindroid.server.app;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class MindroidServerADBInfoFrame extends JFrame {

    /*
    JMenuItem adbDevicesMenuItem = new JMenuItem();
        adbDevicesMenuItem.setAction(new AbstractAction("Show ADB Devices") {
        @Override
        public void actionPerformed(ActionEvent e) {
            MindroidServerADBInfoFrame adbDevicesFrame = MindroidServerADBInfoFrame.getMindroidServerADBInfoFrame();
            adbDevicesFrame.setVisible(true);
        }
    });
        consoleMenuItem.setMnemonic('d');
     */

    private static final MindroidServerADBInfoFrame console = new MindroidServerADBInfoFrame();

    public static MindroidServerADBInfoFrame getMindroidServerADBInfoFrame() {
        return console;
    }

    private JPanel contentPane = new JPanel();

    private MindroidServerADBInfoFrame() {
        initMenubar();
        initPane();
    }

    private void initMenubar() {
        //Menubar
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
        //JMenu helpMenu = new JMenu("Help");
        JMenuItem refreshDevices;

        JMenuItem exitMenuItem = new JMenuItem();
        exitMenuItem.setAction(new AbstractAction("Quit") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.exit(0);
            }
        });
        exitMenuItem.setMnemonic('q');

        refreshDevices = new JMenuItem();
        refreshDevices.setAction(new AbstractAction("Refresh Devices") {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDevices();
            }
        });
        refreshDevices.setMnemonic('r');

        fileMenu.add(refreshDevices);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        this.setJMenuBar(menuBar);
    }

    private void initPane(){
        contentPane.setLayout(new GridLayout());
        setContentPane(contentPane);
    }


    private void updateDevices() {
        try {
            String[] devices = getDevices();
            clearContentPane();

            for (int i = 0; i < devices.length; i++) {
                JTextField txtField = new JTextField(devices[i]);
                contentPane.add(txtField);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    private void clearContentPane(){
        contentPane.removeAll();
    }


    public String[] getDevices() throws IOException, JadbException {
        JadbConnection jadb = new JadbConnection();
        String[] devices_arr;

        List<JadbDevice> devices = jadb.getDevices();
        if (!devices.isEmpty()) {
            devices_arr = new String[devices.size()];
            for (int i = 0; i < devices.size(); i++) {
                devices_arr[i] = devices.toString();
            }
            return devices_arr;
        } else {
            return new String[]{""};
        }
    }

}