package net.codeocean.cheese.ui;

import javax.swing.*;

public class ProjectConfigUI {
    private JPanel root;
    private JPanel main;
    private JTextField projectname;
    private JTextField pkg;
    private JComboBox language;
    private JComboBox ui;

    public JComponent getComponent(){
        return main;
    }
    public JTextField getName(){
        return projectname;
    }
    public JTextField getPkg(){
        return pkg;
    }
    public JComboBox getLanguage(){
        return language;
    }
    public JComboBox getUi(){
        return ui;
    }
}
