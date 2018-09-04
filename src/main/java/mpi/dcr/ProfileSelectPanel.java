package mpi.dcr;

import mpi.dcr.isocat.Profile;
import mpi.dcr.isocat.RestDCRConnector;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;


/**
 * A panel containing elements to select a profile.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class ProfileSelectPanel extends JPanel {
    private ILATDCRConnector connector = null;
    private JScrollPane scrollPane;
    private List<Profile> selectedProfs;
    private JList allProfs;
    private ResourceBundle resBundle;

    /**
     * Creates a new ProfileSelectPanel instance
     */
    public ProfileSelectPanel() {
        super();
        initComponents();
    }

    /**
     * Creates a new ProfileSelectPanel instance
     *
     * @param connector the connector
     */
    public ProfileSelectPanel(ILATDCRConnector connector) {
        super();

        if (connector instanceof RestDCRConnector) {
            this.connector = (RestDCRConnector) connector;
        }

        initComponents();
    }

    /**
     * Creates a new ProfileSelectPanel instance
     *
     * @param connector the connector
     * @param resBundle a resource bundle containing localized texts
     */
    public ProfileSelectPanel(ILATDCRConnector connector, ResourceBundle resBundle) {
        super();

        if (connector instanceof RestDCRConnector) {
            this.connector = (RestDCRConnector) connector;
        }

        this.resBundle = resBundle;

        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        scrollPane = new JScrollPane();

        Dimension dim = new Dimension(200, 300);
        scrollPane.setMinimumSize(dim);
        scrollPane.setPreferredSize(dim);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(2, 6, 2, 6);
        add(scrollPane, gbc);

        selectedProfs = new ArrayList(8);

        initList();

        if (resBundle != null) {
            setBorder(new TitledBorder(resBundle.getString(
                        "DCR.Label.SelectProfiles")));
        } else {
            setBorder(new TitledBorder("Select Profiles"));
        }
    }

    private void initList() {
        if (connector == null) {
            connector = new RestDCRConnector();
        }

        try {
        	ArrayList<Profile> profiles = (ArrayList<Profile>) connector.getProfiles();
            //ProfileList profileList = connector.getProfiles();
            //Profile[] profs = profileList.getProfiles();
            //List<String> ap = new ArrayList<String>(16);
            
            /*
            for (int i = 0; i < profs.length; i++) {
                ap.add(profs[i].getContentAsString());
            }
			*/
            //allProfs = new JList(ap.toArray(new String[] {  }));
        	allProfs = new JList(profiles.toArray(new Profile[] {  }));
            allProfs.getSelectionModel()
                    .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } catch (DCRConnectorException dce) {
            System.out.println(dce.getMessage());
            dce.printStackTrace();
            allProfs = new JList();
        }

        if (allProfs != null) {
            scrollPane.setViewportView(allProfs);
        }
    }

    /**
     * Returns a list of selected profiles.
     *
     * @return a list of names of selected profiles
     */
    public List<Profile> getSelectedProfiles() {
        selectedProfs.clear();

        Object[] selObj = allProfs.getSelectedValues();

        for (int i = 0; i < selObj.length; i++) {
            selectedProfs.add((Profile)selObj[i]);
        }

        return selectedProfs;
    }
}
