package coco.cheese.ide.swing.gui;

import javax.swing.*;

public class ConfigSettingUi {
    private JTextField port;
    private JTextField home;
    private JTextField esp;
    private JTextField sdk;
    private JComboBox build;
    private JPanel root;
    private JPanel main;
    private JComboBox comboBox1;

    public JComponent getComponent(){
        return main;
    }
    public JTextField getHome(){
        return home;
    }

    public JTextField getPort(){
        return port;
    }

    public JTextField getSdk(){
        return sdk;
    }

    public JTextField getEsp(){
        return esp;
    }

    public JComboBox getBuild(){
        return build;
    }
}
