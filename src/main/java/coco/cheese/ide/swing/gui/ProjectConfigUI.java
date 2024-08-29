package coco.cheese.ide.swing.gui;

import javax.swing.*;

public class ProjectConfigUI {
    private JPanel root;
    private JPanel main;
    private JTextField projectname;
    private JTextField pkg;
    private JComboBox type;

    public JComponent getComponent(){
        return main;
    }
    public JTextField getName(){
        return projectname;
    }
    public JTextField getPkg(){
        return pkg;
    }
    public JComboBox getType(){
        return type;
    }

}
