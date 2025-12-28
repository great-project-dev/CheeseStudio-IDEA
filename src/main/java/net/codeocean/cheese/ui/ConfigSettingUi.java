package net.codeocean.cheese.ui;

import javax.swing.*;

public class ConfigSettingUi {
    private JTextField port;
    private JTextField home;
    private JComboBox build;
    private JPanel root;
    private JPanel main;

    private JTextField GitHubProxy;



    public JComponent getComponent(){
        return main;
    }
    public JTextField getHome(){
        return home;
    }

    public JTextField getPort(){
        return port;
    }


    public JComboBox getBuild(){
        return build;
    }

    public JTextField getGitHubProxy(){
        return GitHubProxy;
    }


}
