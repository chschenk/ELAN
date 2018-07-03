package mpi.eudico.client.annotator.timeseries;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.spi.TSConfigPanel;
import mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider;
import mpi.eudico.client.annotator.timeseries.spi.TSServiceRegistry;
import mpi.eudico.client.annotator.util.FileUtility;


/**
 * Configuration related ui handler.
 *
 * @author Han Sloetjes
 */
public class TSConfigurationUI {
    /**
     * Creates a message dialog when there are no configurable time series
     * sources linked to the document.
     *
     * @param parent the parent component or null
     */
    public void showNoConfigMessage(Component parent) {
        showWarningMessage(parent,
            ElanLocale.getString("TimeSeriesViewer.Config.NoConfigurableSource"));
    }

    /**
     * Creates a warning message dialog with the specified message.
     *
     * @param parent the parent component
     * @param message the message
     */
    private void showWarningMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Creates an input dialog with a combobox containing all linked  time
     * series sources. The user can select the one to configure.
     *
     * @param parent the parent component
     * @param sources a list of time series sources
     *
     * @return the selected time series source
     */
    public TSSourceConfiguration selectConfigurableSource(Component parent,
        List<TSSourceConfiguration> sources) {
        if (sources == null) {
            return null;
        }

        TSSourceConfiguration config = null;
        String[] urls = new String[sources.size()];

        for (int i = 0; i < sources.size(); i++) {
            urls[i] = sources.get(i).getSource();
        }

        Object selection = JOptionPane.showInputDialog(parent,
                ElanLocale.getString("TimeSeriesViewer.Config.SelectSource"),
                ElanLocale.getString("Button.Select"),
                JOptionPane.QUESTION_MESSAGE, null, urls, urls[0]);

        if (selection != null) {
            TSSourceConfiguration key;

            for (int i = 0; i < sources.size(); i++) {
                key = sources.get(i);

                if (key.getSource() == selection) {
                    config = key;

                    break;
                }
            }
        }

        return config;
    }

    /**
     * Creates a dialog with the configuration panel for the selected time
     * series source (as returned by the Service Provider).
     *
     * @param parent the parent component
     * @param cfg the configuration wrapper for the source
     * @param li a listener to changes in the configuration
     */
    public void showConfigDialog(Component parent, TSSourceConfiguration cfg,
        TimeSeriesChangeListener li) {
        if ((cfg == null) || !FileUtility.fileExists(cfg.getSource())) {
            showWarningMessage(parent,
                ElanLocale.getString("TimeSeriesViewer.Config.Message.NoSource"));

            return;
        }

        TSServiceRegistry registry = TSServiceRegistry.getInstance();
        TSServiceProvider provider;

        if (cfg.getProviderClassName() != null) {
            provider = registry.getProviderByClassName(cfg.getProviderClassName());
        } else {
            provider = registry.getProviderForFile(cfg.getSource());
        }

        if (provider == null) {
            showWarningMessage(parent,
                ElanLocale.getString(
                    "TimeSeriesViewer.Config.Message.NoProvider"));

            return;
        }

        TSConfigPanel panel = provider.getConfigPanel(cfg);

        if (panel == null) {
            showWarningMessage(parent,
                ElanLocale.getString(
                    "TimeSeriesViewer.Config.Message.NoConfigPanel"));

            return;
        }

        panel.addTimeSeriesChangeListener(li);

        ConfigDialog dialog = null;
        Window w = SwingUtilities.windowForComponent(parent);

        if (w instanceof Dialog) {
            dialog = new ConfigDialog((Dialog) w);
        } else if (w instanceof Frame) {
            dialog = new ConfigDialog((Frame) w);
        } else {
            dialog = new ConfigDialog();
        }

        dialog.setModal(true);
        dialog.setTitle(ElanLocale.getString(
                "TimeSeriesViewer.Config.Tracks.Title"));
        dialog.addConfigPanel(panel);
        dialog.pack();
        int maxH = Toolkit.getDefaultToolkit().getScreenSize().height - 
        	Toolkit.getDefaultToolkit().getScreenInsets(w.getGraphicsConfiguration()).top - 
        	Toolkit.getDefaultToolkit().getScreenInsets(w.getGraphicsConfiguration()).bottom;
        if (dialog.getHeight() > maxH) {
        	dialog.setBounds(new Rectangle(dialog.getWidth(), maxH));
        }

        dialog.setLocationRelativeTo(w);
        dialog.setResizable(true);
        dialog.setVisible(true);
    }

    /**
     * A custom dialog for a configuration panel.
     * 
     * @author Han SLoetjes
     * @version 1.0
      */
    public class ConfigDialog extends JDialog {
        /**
         * Creates a new ConfigDialog instance
         */
        public ConfigDialog() {
            super();
            initComponents();
        }

        /**
         * Creates a new ConfigDialog instance
         *
         * @param parent the parent dialog
         */
        public ConfigDialog(Dialog parent) {
            super(parent);
            initComponents();
        }

        /**
         * Creates a new ConfigDialog instance
         *
         * @param parent the parent frame
         */
        public ConfigDialog(Frame parent) {
            super(parent);
            initComponents();
        }

        private void initComponents() {
            getContentPane().setLayout(new GridBagLayout());

            JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 2, 2));
            JButton closeButton = new JButton(ElanLocale.getString(
                        "Button.Close"));
            closeButton.addActionListener(new ActionListener() {
                    @Override
					public void actionPerformed(ActionEvent ae) {
                        ConfigDialog.this.dispose();
                    }
                });
            buttonPanel.add(closeButton);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.SOUTH;
            gbc.gridy = 1;
            gbc.insets = new Insets(4, 4, 4, 4);
            getContentPane().add(buttonPanel, gbc);
        }

        /**
         * Adds the configuration panel to the content pane of the dialog.
         *
         * @param panel the configuration panel
         */
        public void addConfigPanel(JComponent panel) {
            if (panel != null) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.NORTH;
                gbc.gridy = 0;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.insets = new Insets(4, 4, 4, 4);
                getContentPane().add(panel, gbc);
            }
        }
    }
}
