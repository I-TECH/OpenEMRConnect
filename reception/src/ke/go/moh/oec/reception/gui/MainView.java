/*
 * MainView.java
 */
package ke.go.moh.oec.reception.gui;

import com.griaule.grfingerjava.GrFingerJavaException;
import ke.go.moh.oec.reception.gui.helper.ProcessResult;
import java.awt.CardLayout;
import java.awt.Component;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.reception.controller.RequestDispatcher;
import ke.go.moh.oec.reception.data.RequestResult;
import ke.go.moh.oec.reception.controller.Session;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import ke.go.moh.oec.reception.data.TargetIndex;

/**
 * The application's main frame.
 */
public class MainView extends FrameView {

    private CardLayout cardLayout;
    //private Session session;
    private BufferedImage refusedFingerprint;
    private BufferedImage fingerprintNotTaken;
    private String currentCardName = "homeCard";
    private String previousCardName = "homeCard";
    RequestResult mpiRequestResult;
    RequestResult lpiRequestResult;

    public MainView(SingleFrameApplication app) {
        super(app);
        initComponents();
        cardLayout = (CardLayout) wizardPanel.getLayout();
        session = new Session();
        try {
            refusedFingerprint = ImageIO.read(new File("refused_fingerprint.png"));
            fingerprintNotTaken = ImageIO.read(new File("no_fingerprint.png"));
        } catch (IOException ex) {
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.getFrame().setTitle(Session.getApplicationName());
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = App.getApplication().getMainFrame();
            aboutBox = new AboutDialog(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        App.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        mainPanel = new javax.swing.JPanel();
        mainSplitPane = new javax.swing.JSplitPane();
        leftPanel = new javax.swing.JPanel();
        alertsListPanel = new javax.swing.JPanel();
        alertsScrollPane = new javax.swing.JScrollPane();
        alertsList = new javax.swing.JList();
        rightPanel = new javax.swing.JPanel();
        homeButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        wizardPanel = new javax.swing.JPanel();
        homeCard = new javax.swing.JPanel();
        homePanel = new javax.swing.JPanel();
        enrolledButton = new javax.swing.JButton();
        visitorButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        transferInButton = new javax.swing.JButton();
        clinicIdCard = new javax.swing.JPanel();
        clientIdPanel = new javax.swing.JPanel();
        clinicIdYesButton = new javax.swing.JButton();
        clinicIdNoButton = new javax.swing.JButton();
        basicSearchCard = new javax.swing.JPanel();
        basicSearchPanel = new javax.swing.JPanel();
        basicSearchClinicIdLabel = new javax.swing.JLabel();
        basicSearchClinicIdTextField = new javax.swing.JTextField();
        basicSearchClinicNameLabel = new javax.swing.JLabel();
        basicSearchClinicNameTextField = new javax.swing.JTextField();
        basicSearchFingerprintLabel = new javax.swing.JLabel();
        basicSearchClientRefusesCheckBox = new javax.swing.JCheckBox();
        basicSearchTakeButton = new javax.swing.JButton();
        basicSearchButton = new javax.swing.JButton();
        basicSearchFingerprintImagePanel = new ke.go.moh.oec.reception.gui.custom.ImagePanel();
        extendedSearchCard = new javax.swing.JPanel();
        extendedSearchPanel = new javax.swing.JPanel();
        extendedSearchClinicIdLabel = new javax.swing.JLabel();
        extendedSearchClinicIdTextField = new javax.swing.JTextField();
        extendedSearchClinicNameLabel = new javax.swing.JLabel();
        extendedSearchClinicNameTextField = new javax.swing.JTextField();
        extendedSearchFirstNameLabel = new javax.swing.JLabel();
        extendedSearchFirstNameTextField = new javax.swing.JTextField();
        extendedSearchMiddleNameLabel = new javax.swing.JLabel();
        extendedSearchMiddleNameTextField = new javax.swing.JTextField();
        extendedSearchLastNameLabel = new javax.swing.JLabel();
        extendedSearchLastNameTextField = new javax.swing.JTextField();
        extendedSearchSexLabel = new javax.swing.JLabel();
        extendedSearchMaleRadioButton = new javax.swing.JRadioButton();
        extendedSearchFemaleRadioButton = new javax.swing.JRadioButton();
        extendedSearchBirthdateLabel = new javax.swing.JLabel();
        extendedSearchBirthdateChooser = new com.toedter.calendar.JDateChooser();
        extendedSearchVillageLabel = new javax.swing.JLabel();
        extendedSearchVillageTextField = new javax.swing.JTextField();
        extendedSearchFingerprintLabel = new javax.swing.JLabel();
        extendedSearchClientRefusesCheckBox = new javax.swing.JCheckBox();
        extendedSearchTakeButton = new javax.swing.JButton();
        extendedSearchButton = new javax.swing.JButton();
        extendedSearchFingerprintImagePanel = new ke.go.moh.oec.reception.gui.custom.ImagePanel();
        searchResultsCard = new javax.swing.JPanel();
        searchResultsPanel = new javax.swing.JPanel();
        searchResultsScrollPane = new javax.swing.JScrollPane();
        searchResultsTable = new javax.swing.JTable();
        acceptButton = new javax.swing.JButton();
        notFoundButton = new javax.swing.JButton();
        reviewCard1 = new javax.swing.JPanel();
        reviewPanel1 = new javax.swing.JPanel();
        clinicIdLabel = new javax.swing.JLabel();
        clinicIdTextField = new javax.swing.JTextField();
        altClinicIdTextField = new javax.swing.JTextField();
        ClinicIdToggleButton = new javax.swing.JToggleButton();
        firstNameLabel = new javax.swing.JLabel();
        firstNameTextField = new javax.swing.JTextField();
        altFirstNameTextField = new javax.swing.JTextField();
        firstNameToggleButton = new javax.swing.JToggleButton();
        middleNameLabel = new javax.swing.JLabel();
        middleNameTextField = new javax.swing.JTextField();
        altMiddleNameTextField = new javax.swing.JTextField();
        middleNameToggleButton = new javax.swing.JToggleButton();
        lastNameLabel = new javax.swing.JLabel();
        lastNameTextField = new javax.swing.JTextField();
        altLastNameTextField = new javax.swing.JTextField();
        lastNameToggleButton = new javax.swing.JToggleButton();
        sexLabel = new javax.swing.JLabel();
        maleRadioButton = new javax.swing.JRadioButton();
        femaleRadioButton = new javax.swing.JRadioButton();
        altSexTextField = new javax.swing.JTextField();
        sexToggleButton = new javax.swing.JToggleButton();
        birthDateLabel = new javax.swing.JLabel();
        birthDateChooser = new com.toedter.calendar.JDateChooser();
        altBirthDateTextField = new javax.swing.JTextField();
        birthDateToggleButton = new javax.swing.JToggleButton();
        maritalStatusLabel = new javax.swing.JLabel();
        maritalStatusComboBox = new javax.swing.JComboBox();
        altMaritalStatusTextField = new javax.swing.JTextField();
        maritalStatusToggleButton = new javax.swing.JToggleButton();
        villageLabel = new javax.swing.JLabel();
        villageTextField = new javax.swing.JTextField();
        alrVillageTextField = new javax.swing.JTextField();
        altVillageToggleButton = new javax.swing.JToggleButton();
        reviewCard1NextButton = new javax.swing.JButton();
        reviewCard2 = new javax.swing.JPanel();
        reviewPanel2 = new javax.swing.JPanel();
        fathersFirstNameLabel = new javax.swing.JLabel();
        fathersFirstNameTextField = new javax.swing.JTextField();
        altFathersFirstNameTextField = new javax.swing.JTextField();
        fathersFirstNameToggleButton = new javax.swing.JToggleButton();
        fathersMiddleNameLabel = new javax.swing.JLabel();
        fathersMiddleNameTextField = new javax.swing.JTextField();
        altFathersMiddleNameTextField = new javax.swing.JTextField();
        fathersMiddleNameToggleButton = new javax.swing.JToggleButton();
        fathersLastNameLabel = new javax.swing.JLabel();
        fathersLastNameTextField = new javax.swing.JTextField();
        altFathersLastNameTextField = new javax.swing.JTextField();
        fathersLastNameToggleButton = new javax.swing.JToggleButton();
        mothersFirstNameLabel = new javax.swing.JLabel();
        mothersFirstNameTextField = new javax.swing.JTextField();
        altMothersFirstNameTextField = new javax.swing.JTextField();
        mothersFirstNameToggleButton = new javax.swing.JToggleButton();
        mothersMiddleNameLabel = new javax.swing.JLabel();
        mothersMiddleNameTextField = new javax.swing.JTextField();
        altMothersMiddleNameTextField = new javax.swing.JTextField();
        mothersMiddleNameToggleButton = new javax.swing.JToggleButton();
        mothersLastNameLabel = new javax.swing.JLabel();
        mothersLastNameTextField = new javax.swing.JTextField();
        altMothersLastNameTextField = new javax.swing.JTextField();
        mothersLastNameToggleButton = new javax.swing.JToggleButton();
        compoundHeadsFirstNameLabel = new javax.swing.JLabel();
        compoundHeadsFirstNameTextField = new javax.swing.JTextField();
        altCompoundHeadsFirstNameTextField = new javax.swing.JTextField();
        compoundHeadsMiddleNameLabel = new javax.swing.JLabel();
        compoundHeadsMiddleNameTextField = new javax.swing.JTextField();
        altCompoundHeadsMiddleNameTextField = new javax.swing.JTextField();
        compoundHeadsMiddleNameButton = new javax.swing.JButton();
        compoundHeadsFirstNameToggleButton = new javax.swing.JToggleButton();
        compoundHeadsMiddleNameToggleButton = new javax.swing.JToggleButton();
        reviewCard3 = new javax.swing.JPanel();
        reviewPanel3 = new javax.swing.JPanel();
        compoundHeadsLastNameLabel = new javax.swing.JLabel();
        compoundHeadsLastNameTextField = new javax.swing.JTextField();
        altCompoundHeadsLastNameTextField = new javax.swing.JTextField();
        compoundHeadsLastNameToggleButton = new javax.swing.JToggleButton();
        fingerprintLabel = new javax.swing.JLabel();
        fingerprintImagePanel = new ke.go.moh.oec.reception.gui.custom.ImagePanel();
        clientRefusesCheckBox = new javax.swing.JCheckBox();
        takeButton = new javax.swing.JButton();
        finishButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        sexButtonGroup = new javax.swing.ButtonGroup();
        searchResultsList = new ArrayList<Person>();
        session = new ke.go.moh.oec.reception.controller.Session();

        mainPanel.setName("mainPanel"); // NOI18N

        mainSplitPane.setDividerLocation(250);
        mainSplitPane.setName("mainSplitPane"); // NOI18N

        leftPanel.setName("leftPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getResourceMap(MainView.class);
        alertsListPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("alertsListPanel.border.title"))); // NOI18N
        alertsListPanel.setName("alertsListPanel"); // NOI18N

        alertsScrollPane.setName("alertsScrollPane"); // NOI18N

        alertsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        alertsList.setName("alertsList"); // NOI18N
        alertsScrollPane.setViewportView(alertsList);

        javax.swing.GroupLayout alertsListPanelLayout = new javax.swing.GroupLayout(alertsListPanel);
        alertsListPanel.setLayout(alertsListPanelLayout);
        alertsListPanelLayout.setHorizontalGroup(
            alertsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alertsListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(alertsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addContainerGap())
        );
        alertsListPanelLayout.setVerticalGroup(
            alertsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, alertsListPanelLayout.createSequentialGroup()
                .addComponent(alertsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(alertsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(alertsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainSplitPane.setLeftComponent(leftPanel);

        rightPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        rightPanel.setName("rightPanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getActionMap(MainView.class, this);
        homeButton.setAction(actionMap.get("goHome")); // NOI18N
        homeButton.setText(resourceMap.getString("homeButton.text")); // NOI18N
        homeButton.setName("homeButton"); // NOI18N

        backButton.setAction(actionMap.get("goBack")); // NOI18N
        backButton.setText(resourceMap.getString("backButton.text")); // NOI18N
        backButton.setName("backButton"); // NOI18N

        wizardPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        wizardPanel.setName("wizardPanel"); // NOI18N
        wizardPanel.setLayout(new java.awt.CardLayout());

        homeCard.setName("homeCard"); // NOI18N

        homePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("homePanel.border.title"))); // NOI18N
        homePanel.setName("homePanel"); // NOI18N

        enrolledButton.setAction(actionMap.get("startEnrolledClientSession")); // NOI18N
        enrolledButton.setText(resourceMap.getString("enrolledButton.text")); // NOI18N
        enrolledButton.setName("enrolledButton"); // NOI18N

        visitorButton.setAction(actionMap.get("startVisitorClientSession")); // NOI18N
        visitorButton.setText(resourceMap.getString("visitorButton.text")); // NOI18N
        visitorButton.setName("visitorButton"); // NOI18N

        newButton.setAction(actionMap.get("startNewClientSession")); // NOI18N
        newButton.setText(resourceMap.getString("newButton.text")); // NOI18N
        newButton.setName("newButton"); // NOI18N

        transferInButton.setAction(actionMap.get("startTransferInClientSession")); // NOI18N
        transferInButton.setText(resourceMap.getString("transferInButton.text")); // NOI18N
        transferInButton.setName("transferInButton"); // NOI18N

        javax.swing.GroupLayout homePanelLayout = new javax.swing.GroupLayout(homePanel);
        homePanel.setLayout(homePanelLayout);
        homePanelLayout.setHorizontalGroup(
            homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addComponent(visitorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addComponent(enrolledButton, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addComponent(transferInButton, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                .addContainerGap())
        );
        homePanelLayout.setVerticalGroup(
            homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enrolledButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(visitorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transferInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout homeCardLayout = new javax.swing.GroupLayout(homeCard);
        homeCard.setLayout(homeCardLayout);
        homeCardLayout.setHorizontalGroup(
            homeCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homeCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(homePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        homeCardLayout.setVerticalGroup(
            homeCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homeCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(homePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(277, Short.MAX_VALUE))
        );

        wizardPanel.add(homeCard, "homeCard");

        clinicIdCard.setName("clinicIdCard"); // NOI18N

        clientIdPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("clientIdPanel.border.title"))); // NOI18N
        clientIdPanel.setName("clientIdPanel"); // NOI18N

        clinicIdYesButton.setAction(actionMap.get("setKnownClinicIdToYes")); // NOI18N
        clinicIdYesButton.setText(resourceMap.getString("clinicIdYesButton.text")); // NOI18N
        clinicIdYesButton.setName("clinicIdYesButton"); // NOI18N

        clinicIdNoButton.setAction(actionMap.get("setKnownClinicIdToNo")); // NOI18N
        clinicIdNoButton.setText(resourceMap.getString("clinicIdNoButton.text")); // NOI18N
        clinicIdNoButton.setName("clinicIdNoButton"); // NOI18N

        javax.swing.GroupLayout clientIdPanelLayout = new javax.swing.GroupLayout(clientIdPanel);
        clientIdPanel.setLayout(clientIdPanelLayout);
        clientIdPanelLayout.setHorizontalGroup(
            clientIdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientIdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(clientIdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clinicIdNoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addComponent(clinicIdYesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                .addContainerGap())
        );
        clientIdPanelLayout.setVerticalGroup(
            clientIdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientIdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clinicIdYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clinicIdNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout clinicIdCardLayout = new javax.swing.GroupLayout(clinicIdCard);
        clinicIdCard.setLayout(clinicIdCardLayout);
        clinicIdCardLayout.setHorizontalGroup(
            clinicIdCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clinicIdCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clientIdPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        clinicIdCardLayout.setVerticalGroup(
            clinicIdCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clinicIdCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clientIdPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(372, Short.MAX_VALUE))
        );

        wizardPanel.add(clinicIdCard, "clinicIdCard");

        basicSearchCard.setName("basicSearchCard"); // NOI18N

        basicSearchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("basicSearchPanel.border.title"))); // NOI18N
        basicSearchPanel.setName("basicSearchPanel"); // NOI18N

        basicSearchClinicIdLabel.setText(resourceMap.getString("basicSearchClinicIdLabel.text")); // NOI18N
        basicSearchClinicIdLabel.setName("basicSearchClinicIdLabel"); // NOI18N

        basicSearchClinicIdTextField.setName("basicSearchClinicIdTextField"); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, session, org.jdesktop.beansbinding.ELProperty.create("${basicRequestParameters.clinicId}"), basicSearchClinicIdTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        basicSearchClinicIdTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                basicSearchClinicIdTextFieldKeyTyped(evt);
            }
        });

        basicSearchClinicNameLabel.setText(resourceMap.getString("basicSearchClinicNameLabel.text")); // NOI18N
        basicSearchClinicNameLabel.setName("basicSearchClinicNameLabel"); // NOI18N

        basicSearchClinicNameTextField.setName("basicSearchClinicNameTextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, session, org.jdesktop.beansbinding.ELProperty.create("${basicRequestParameters.clinicName}"), basicSearchClinicNameTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        basicSearchClinicNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                basicSearchClinicNameTextFieldKeyTyped(evt);
            }
        });

        basicSearchFingerprintLabel.setText(resourceMap.getString("basicSearchFingerprintLabel.text")); // NOI18N
        basicSearchFingerprintLabel.setName("basicSearchFingerprintLabel"); // NOI18N

        basicSearchClientRefusesCheckBox.setAction(actionMap.get("refuseFingerprintingBasic")); // NOI18N
        basicSearchClientRefusesCheckBox.setText(resourceMap.getString("basicSearchClientRefusesCheckBox.text")); // NOI18N
        basicSearchClientRefusesCheckBox.setName("basicSearchClientRefusesCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, session, org.jdesktop.beansbinding.ELProperty.create("${nonFingerprint}"), basicSearchClientRefusesCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        basicSearchTakeButton.setAction(actionMap.get("showFingerprintDialogBasic")); // NOI18N
        basicSearchTakeButton.setText(resourceMap.getString("basicSearchTakeButton.text")); // NOI18N
        basicSearchTakeButton.setName("basicSearchTakeButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, basicSearchClientRefusesCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), basicSearchTakeButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        basicSearchButton.setAction(actionMap.get("searchBasic")); // NOI18N
        basicSearchButton.setText(resourceMap.getString("basicSearchButton.text")); // NOI18N
        basicSearchButton.setName("basicSearchButton"); // NOI18N

        basicSearchFingerprintImagePanel.setName("ImagePanel"); // NOI18N

        javax.swing.GroupLayout basicSearchFingerprintImagePanelLayout = new javax.swing.GroupLayout(basicSearchFingerprintImagePanel);
        basicSearchFingerprintImagePanel.setLayout(basicSearchFingerprintImagePanelLayout);
        basicSearchFingerprintImagePanelLayout.setHorizontalGroup(
            basicSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        basicSearchFingerprintImagePanelLayout.setVerticalGroup(
            basicSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout basicSearchPanelLayout = new javax.swing.GroupLayout(basicSearchPanel);
        basicSearchPanel.setLayout(basicSearchPanelLayout);
        basicSearchPanelLayout.setHorizontalGroup(
            basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basicSearchPanelLayout.createSequentialGroup()
                        .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(basicSearchClinicNameLabel)
                            .addComponent(basicSearchClinicIdLabel)
                            .addComponent(basicSearchFingerprintLabel))
                        .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(basicSearchClinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                            .addComponent(basicSearchClinicNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                            .addGroup(basicSearchPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(basicSearchClientRefusesCheckBox))))
                    .addComponent(basicSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                .addContainerGap())
        );
        basicSearchPanelLayout.setVerticalGroup(
            basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicSearchPanelLayout.createSequentialGroup()
                .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(basicSearchClinicIdLabel)
                    .addComponent(basicSearchClinicIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(basicSearchClinicNameLabel)
                    .addComponent(basicSearchClinicNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(basicSearchFingerprintLabel)
                    .addComponent(basicSearchFingerprintImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(basicSearchClientRefusesCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(basicSearchTakeButton)
                .addGap(18, 18, 18)
                .addComponent(basicSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout basicSearchCardLayout = new javax.swing.GroupLayout(basicSearchCard);
        basicSearchCard.setLayout(basicSearchCardLayout);
        basicSearchCardLayout.setHorizontalGroup(
            basicSearchCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicSearchCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(basicSearchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        basicSearchCardLayout.setVerticalGroup(
            basicSearchCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicSearchCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(basicSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(265, Short.MAX_VALUE))
        );

        wizardPanel.add(basicSearchCard, "basicSearchCard");

        extendedSearchCard.setName("extendedSearchCard"); // NOI18N

        extendedSearchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("extendedSearchPanel.border.title"))); // NOI18N
        extendedSearchPanel.setName("extendedSearchPanel"); // NOI18N

        extendedSearchClinicIdLabel.setText(resourceMap.getString("extendedSearchClinicIdLabel.text")); // NOI18N
        extendedSearchClinicIdLabel.setName("extendedSearchClinicIdLabel"); // NOI18N

        extendedSearchClinicIdTextField.setName("extendedSearchClinicIdTextField"); // NOI18N

        extendedSearchClinicNameLabel.setText(resourceMap.getString("extendedSearchClinicNameLabel.text")); // NOI18N
        extendedSearchClinicNameLabel.setName("extendedSearchClinicNameLabel"); // NOI18N

        extendedSearchClinicNameTextField.setName("extendedSearchClinicNameTextField"); // NOI18N

        extendedSearchFirstNameLabel.setText(resourceMap.getString("extendedSearchFirstNameLabel.text")); // NOI18N
        extendedSearchFirstNameLabel.setName("extendedSearchFirstNameLabel"); // NOI18N

        extendedSearchFirstNameTextField.setText(resourceMap.getString("extendedSearchFirstNameTextField.text")); // NOI18N
        extendedSearchFirstNameTextField.setName("extendedSearchFirstNameTextField"); // NOI18N

        extendedSearchMiddleNameLabel.setText(resourceMap.getString("extendedSearchMiddleNameLabel.text")); // NOI18N
        extendedSearchMiddleNameLabel.setName("extendedSearchMiddleNameLabel"); // NOI18N

        extendedSearchMiddleNameTextField.setText(resourceMap.getString("extendedSearchMiddleNameTextField.text")); // NOI18N
        extendedSearchMiddleNameTextField.setName("extendedSearchMiddleNameTextField"); // NOI18N

        extendedSearchLastNameLabel.setText(resourceMap.getString("extendedSearchLastNameLabel.text")); // NOI18N
        extendedSearchLastNameLabel.setName("extendedSearchLastNameLabel"); // NOI18N

        extendedSearchLastNameTextField.setText(resourceMap.getString("extendedSearchLastNameTextField.text")); // NOI18N
        extendedSearchLastNameTextField.setName("extendedSearchLastNameTextField"); // NOI18N

        extendedSearchSexLabel.setText(resourceMap.getString("extendedSearchSexLabel.text")); // NOI18N
        extendedSearchSexLabel.setName("extendedSearchSexLabel"); // NOI18N

        sexButtonGroup.add(extendedSearchMaleRadioButton);
        extendedSearchMaleRadioButton.setText(resourceMap.getString("extendedSearchMaleRadioButton.text")); // NOI18N
        extendedSearchMaleRadioButton.setName("extendedSearchMaleRadioButton"); // NOI18N

        sexButtonGroup.add(extendedSearchFemaleRadioButton);
        extendedSearchFemaleRadioButton.setText(resourceMap.getString("extendedSearchFemaleRadioButton.text")); // NOI18N
        extendedSearchFemaleRadioButton.setName("extendedSearchFemaleRadioButton"); // NOI18N

        extendedSearchBirthdateLabel.setText(resourceMap.getString("extendedSearchBirthdateLabel.text")); // NOI18N
        extendedSearchBirthdateLabel.setName("extendedSearchBirthdateLabel"); // NOI18N

        extendedSearchBirthdateChooser.setName("extendedSearchBirthdateChooser"); // NOI18N

        extendedSearchVillageLabel.setText(resourceMap.getString("extendedSearchVillageLabel.text")); // NOI18N
        extendedSearchVillageLabel.setName("extendedSearchVillageLabel"); // NOI18N

        extendedSearchVillageTextField.setText(resourceMap.getString("extendedSearchVillageTextField.text")); // NOI18N
        extendedSearchVillageTextField.setName("extendedSearchVillageTextField"); // NOI18N

        extendedSearchFingerprintLabel.setText(resourceMap.getString("extendedSearchFingerprintLabel.text")); // NOI18N
        extendedSearchFingerprintLabel.setName("extendedSearchFingerprintLabel"); // NOI18N

        extendedSearchClientRefusesCheckBox.setAction(actionMap.get("refuseFingerprintingExtended")); // NOI18N
        extendedSearchClientRefusesCheckBox.setText(resourceMap.getString("extendedSearchClientRefusesCheckBox.text")); // NOI18N
        extendedSearchClientRefusesCheckBox.setName("extendedSearchClientRefusesCheckBox"); // NOI18N

        extendedSearchTakeButton.setAction(actionMap.get("showFingerprintDialogExtended")); // NOI18N
        extendedSearchTakeButton.setText(resourceMap.getString("extendedSearchTakeButton.text")); // NOI18N
        extendedSearchTakeButton.setName("extendedSearchTakeButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, extendedSearchClientRefusesCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), extendedSearchTakeButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        extendedSearchButton.setAction(actionMap.get("searchExtended")); // NOI18N
        extendedSearchButton.setText(resourceMap.getString("extendedSearchButton.text")); // NOI18N
        extendedSearchButton.setName("extendedSearchButton"); // NOI18N

        extendedSearchFingerprintImagePanel.setName("extendedSearchFingerprintImagePanel"); // NOI18N

        javax.swing.GroupLayout extendedSearchFingerprintImagePanelLayout = new javax.swing.GroupLayout(extendedSearchFingerprintImagePanel);
        extendedSearchFingerprintImagePanel.setLayout(extendedSearchFingerprintImagePanelLayout);
        extendedSearchFingerprintImagePanelLayout.setHorizontalGroup(
            extendedSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        extendedSearchFingerprintImagePanelLayout.setVerticalGroup(
            extendedSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout extendedSearchPanelLayout = new javax.swing.GroupLayout(extendedSearchPanel);
        extendedSearchPanel.setLayout(extendedSearchPanelLayout);
        extendedSearchPanelLayout.setHorizontalGroup(
            extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(extendedSearchButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                        .addComponent(extendedSearchFingerprintLabel)
                        .addGap(15, 15, 15)
                        .addComponent(extendedSearchFingerprintImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(extendedSearchClientRefusesCheckBox))
                    .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                        .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(extendedSearchClinicNameLabel)
                            .addComponent(extendedSearchClinicIdLabel)
                            .addComponent(extendedSearchFirstNameLabel)
                            .addComponent(extendedSearchMiddleNameLabel)
                            .addComponent(extendedSearchLastNameLabel)
                            .addComponent(extendedSearchSexLabel)
                            .addComponent(extendedSearchVillageLabel)
                            .addComponent(extendedSearchBirthdateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                                .addComponent(extendedSearchMaleRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(extendedSearchFemaleRadioButton))
                            .addComponent(extendedSearchLastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(extendedSearchMiddleNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(extendedSearchFirstNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(extendedSearchClinicNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(extendedSearchVillageTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(extendedSearchClinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addComponent(extendedSearchBirthdateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE))))
                .addContainerGap())
        );
        extendedSearchPanelLayout.setVerticalGroup(
            extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchClinicIdLabel)
                    .addComponent(extendedSearchClinicIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchClinicNameLabel)
                    .addComponent(extendedSearchClinicNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchFirstNameLabel)
                    .addComponent(extendedSearchFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchMiddleNameLabel)
                    .addComponent(extendedSearchMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchLastNameLabel)
                    .addComponent(extendedSearchLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchSexLabel)
                    .addComponent(extendedSearchMaleRadioButton)
                    .addComponent(extendedSearchFemaleRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(extendedSearchBirthdateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(extendedSearchBirthdateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchVillageLabel)
                    .addComponent(extendedSearchVillageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(extendedSearchFingerprintLabel)
                            .addComponent(extendedSearchClientRefusesCheckBox)))
                    .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(extendedSearchFingerprintImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(extendedSearchTakeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(extendedSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout extendedSearchCardLayout = new javax.swing.GroupLayout(extendedSearchCard);
        extendedSearchCard.setLayout(extendedSearchCardLayout);
        extendedSearchCardLayout.setHorizontalGroup(
            extendedSearchCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(extendedSearchCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(extendedSearchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        extendedSearchCardLayout.setVerticalGroup(
            extendedSearchCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(extendedSearchCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(extendedSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(114, Short.MAX_VALUE))
        );

        wizardPanel.add(extendedSearchCard, "extendedSearchCard");

        searchResultsCard.setName("searchResultsCard"); // NOI18N

        searchResultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("searchResultsPanel.border.title"))); // NOI18N
        searchResultsPanel.setName("searchResultsPanel"); // NOI18N

        searchResultsScrollPane.setName("searchResultsScrollPane"); // NOI18N

        searchResultsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        searchResultsTable.setName("searchResultsTable"); // NOI18N
        searchResultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, searchResultsList, searchResultsTable);
        org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${firstName}"));
        columnBinding.setColumnName("First Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${middleName}"));
        columnBinding.setColumnName("Middle Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${lastName}"));
        columnBinding.setColumnName("Last Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${birthdate}"));
        columnBinding.setColumnName("Birthdate");
        columnBinding.setColumnClass(java.util.Date.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${villageName}"));
        columnBinding.setColumnName("Village Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${fathersFirstName}"));
        columnBinding.setColumnName("Fathers First Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${fathersMiddleName}"));
        columnBinding.setColumnName("Fathers Middle Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${fathersLastName}"));
        columnBinding.setColumnName("Fathers Last Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${mothersFirstName}"));
        columnBinding.setColumnName("Mothers First Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${mothersMiddleName}"));
        columnBinding.setColumnName("Mothers Middle Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${mothersLastName}"));
        columnBinding.setColumnName("Mothers Last Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${compoundHeadFirstName}"));
        columnBinding.setColumnName("Compound Head First Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${compoundHeadMiddleName}"));
        columnBinding.setColumnName("Compound Head Middle Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${compoundHeadLastName}"));
        columnBinding.setColumnName("Compound Head Last Name");
        columnBinding.setColumnClass(String.class);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        searchResultsScrollPane.setViewportView(searchResultsTable);

        acceptButton.setAction(actionMap.get("acceptMatch")); // NOI18N
        acceptButton.setText(resourceMap.getString("acceptButton.text")); // NOI18N
        acceptButton.setName("acceptButton"); // NOI18N

        notFoundButton.setAction(actionMap.get("showReviewCard")); // NOI18N
        notFoundButton.setText(resourceMap.getString("notFoundButton.text")); // NOI18N
        notFoundButton.setName("notFoundButton"); // NOI18N

        javax.swing.GroupLayout searchResultsPanelLayout = new javax.swing.GroupLayout(searchResultsPanel);
        searchResultsPanel.setLayout(searchResultsPanelLayout);
        searchResultsPanelLayout.setHorizontalGroup(
            searchResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchResultsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(searchResultsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addComponent(notFoundButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                    .addComponent(acceptButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                .addContainerGap())
        );
        searchResultsPanelLayout.setVerticalGroup(
            searchResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchResultsPanelLayout.createSequentialGroup()
                .addComponent(searchResultsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(acceptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notFoundButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout searchResultsCardLayout = new javax.swing.GroupLayout(searchResultsCard);
        searchResultsCard.setLayout(searchResultsCardLayout);
        searchResultsCardLayout.setHorizontalGroup(
            searchResultsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchResultsCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchResultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        searchResultsCardLayout.setVerticalGroup(
            searchResultsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchResultsCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchResultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        wizardPanel.add(searchResultsCard, "searchResultsCard");

        reviewCard1.setName("reviewCard1"); // NOI18N

        reviewPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        reviewPanel1.setName("reviewPanel1"); // NOI18N

        clinicIdLabel.setText(resourceMap.getString("clinicIdLabel.text")); // NOI18N
        clinicIdLabel.setName("clinicIdLabel"); // NOI18N

        clinicIdTextField.setText(resourceMap.getString("clinicIdTextField.text")); // NOI18N
        clinicIdTextField.setName("clinicIdTextField"); // NOI18N

        altClinicIdTextField.setEditable(false);
        altClinicIdTextField.setText(resourceMap.getString("altClinicIdTextField.text")); // NOI18N
        altClinicIdTextField.setName("altClinicIdTextField"); // NOI18N

        ClinicIdToggleButton.setText(resourceMap.getString("ClinicIdToggleButton.text")); // NOI18N
        ClinicIdToggleButton.setName("ClinicIdToggleButton"); // NOI18N

        firstNameLabel.setText(resourceMap.getString("firstNameLabel.text")); // NOI18N
        firstNameLabel.setName("firstNameLabel"); // NOI18N

        firstNameTextField.setName("firstNameTextField"); // NOI18N

        altFirstNameTextField.setEditable(false);
        altFirstNameTextField.setName("altFirstNameTextField"); // NOI18N

        firstNameToggleButton.setText(resourceMap.getString("firstNameToggleButton.text")); // NOI18N
        firstNameToggleButton.setName("firstNameToggleButton"); // NOI18N

        middleNameLabel.setText(resourceMap.getString("middleNameLabel.text")); // NOI18N
        middleNameLabel.setName("middleNameLabel"); // NOI18N

        middleNameTextField.setName("middleNameTextField"); // NOI18N

        altMiddleNameTextField.setEditable(false);
        altMiddleNameTextField.setName("altMiddleNameTextField"); // NOI18N

        middleNameToggleButton.setText(resourceMap.getString("middleNameToggleButton.text")); // NOI18N
        middleNameToggleButton.setName("middleNameToggleButton"); // NOI18N

        lastNameLabel.setText(resourceMap.getString("lastNameLabel.text")); // NOI18N
        lastNameLabel.setName("lastNameLabel"); // NOI18N

        lastNameTextField.setName("lastNameTextField"); // NOI18N

        altLastNameTextField.setEditable(false);
        altLastNameTextField.setName("altLastNameTextField"); // NOI18N

        lastNameToggleButton.setText(resourceMap.getString("lastNameToggleButton.text")); // NOI18N
        lastNameToggleButton.setName("lastNameToggleButton"); // NOI18N

        sexLabel.setText(resourceMap.getString("sexLabel.text")); // NOI18N
        sexLabel.setName("sexLabel"); // NOI18N

        maleRadioButton.setText(resourceMap.getString("maleRadioButton.text")); // NOI18N
        maleRadioButton.setName("maleRadioButton"); // NOI18N

        femaleRadioButton.setText(resourceMap.getString("femaleRadioButton.text")); // NOI18N
        femaleRadioButton.setName("femaleRadioButton"); // NOI18N

        altSexTextField.setEditable(false);
        altSexTextField.setText(resourceMap.getString("altSexTextField.text")); // NOI18N
        altSexTextField.setName("altSexTextField"); // NOI18N

        sexToggleButton.setText(resourceMap.getString("sexToggleButton.text")); // NOI18N
        sexToggleButton.setName("sexToggleButton"); // NOI18N

        birthDateLabel.setText(resourceMap.getString("birthDateLabel.text")); // NOI18N
        birthDateLabel.setName("birthDateLabel"); // NOI18N

        birthDateChooser.setName("birthDateChooser"); // NOI18N

        altBirthDateTextField.setEditable(false);
        altBirthDateTextField.setText(resourceMap.getString("altBirthDateTextField.text")); // NOI18N
        altBirthDateTextField.setName("altBirthDateTextField"); // NOI18N

        birthDateToggleButton.setText(resourceMap.getString("birthDateToggleButton.text")); // NOI18N
        birthDateToggleButton.setName("birthDateToggleButton"); // NOI18N

        maritalStatusLabel.setText(resourceMap.getString("maritalStatusLabel.text")); // NOI18N
        maritalStatusLabel.setName("maritalStatusLabel"); // NOI18N

        maritalStatusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        maritalStatusComboBox.setName("maritalStatusComboBox"); // NOI18N

        altMaritalStatusTextField.setEditable(false);
        altMaritalStatusTextField.setText(resourceMap.getString("altMaritalStatusTextField.text")); // NOI18N
        altMaritalStatusTextField.setName("altMaritalStatusTextField"); // NOI18N

        maritalStatusToggleButton.setText(resourceMap.getString("maritalStatusToggleButton.text")); // NOI18N
        maritalStatusToggleButton.setName("maritalStatusToggleButton"); // NOI18N

        villageLabel.setText(resourceMap.getString("villageLabel.text")); // NOI18N
        villageLabel.setName("villageLabel"); // NOI18N

        villageTextField.setName("villageTextField"); // NOI18N

        alrVillageTextField.setEditable(false);
        alrVillageTextField.setName("alrVillageTextField"); // NOI18N

        altVillageToggleButton.setText(resourceMap.getString("altVillageToggleButton.text")); // NOI18N
        altVillageToggleButton.setName("altVillageToggleButton"); // NOI18N

        reviewCard1NextButton.setText(resourceMap.getString("reviewCard1NextButton.text")); // NOI18N
        reviewCard1NextButton.setName("reviewCard1NextButton"); // NOI18N

        javax.swing.GroupLayout reviewPanel1Layout = new javax.swing.GroupLayout(reviewPanel1);
        reviewPanel1.setLayout(reviewPanel1Layout);
        reviewPanel1Layout.setHorizontalGroup(
            reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(middleNameLabel)
                            .addComponent(lastNameLabel)
                            .addComponent(sexLabel))
                        .addGap(10, 10, 10)
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(firstNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(reviewPanel1Layout.createSequentialGroup()
                                .addComponent(maleRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(femaleRadioButton))
                            .addComponent(lastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(middleNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(middleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(altSexTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                                    .addComponent(altLastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(sexToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lastNameToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)))))
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(firstNameLabel)
                            .addComponent(clinicIdLabel))
                        .addGap(18, 18, 18)
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(clinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addGroup(reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altClinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ClinicIdToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(firstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)))
                    .addComponent(reviewCard1NextButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(birthDateLabel)
                            .addComponent(maritalStatusLabel)
                            .addComponent(villageLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(birthDateChooser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(alrVillageTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(altVillageToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(villageTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altBirthDateTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(birthDateToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altMaritalStatusTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maritalStatusToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(maritalStatusComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 395, Short.MAX_VALUE))))
                .addContainerGap())
        );
        reviewPanel1Layout.setVerticalGroup(
            reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clinicIdLabel)
                    .addComponent(clinicIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altClinicIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ClinicIdToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(firstNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(firstNameLabel)
                            .addComponent(firstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(altFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(middleNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(middleNameLabel)
                            .addComponent(middleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(altMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastNameLabel)
                    .addComponent(lastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maleRadioButton)
                    .addComponent(sexLabel)
                    .addComponent(femaleRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sexToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(altSexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(birthDateLabel))
                    .addComponent(birthDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(birthDateToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(altBirthDateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maritalStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maritalStatusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maritalStatusToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(altMaritalStatusTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(villageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(villageLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alrVillageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(altVillageToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reviewCard1NextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout reviewCard1Layout = new javax.swing.GroupLayout(reviewCard1);
        reviewCard1.setLayout(reviewCard1Layout);
        reviewCard1Layout.setHorizontalGroup(
            reviewCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewCard1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reviewPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        reviewCard1Layout.setVerticalGroup(
            reviewCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewCard1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reviewPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        wizardPanel.add(reviewCard1, "reviewCard1");

        reviewCard2.setName("reviewCard2"); // NOI18N

        reviewPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        reviewPanel2.setName("reviewPanel2"); // NOI18N

        fathersFirstNameLabel.setText(resourceMap.getString("fathersFirstNameLabel.text")); // NOI18N
        fathersFirstNameLabel.setName("fathersFirstNameLabel"); // NOI18N

        fathersFirstNameTextField.setName("fathersFirstNameTextField"); // NOI18N

        altFathersFirstNameTextField.setEditable(false);
        altFathersFirstNameTextField.setName("altFathersFirstNameTextField"); // NOI18N

        fathersFirstNameToggleButton.setText(resourceMap.getString("fathersFirstNameToggleButton.text")); // NOI18N
        fathersFirstNameToggleButton.setName("fathersFirstNameToggleButton"); // NOI18N

        fathersMiddleNameLabel.setText(resourceMap.getString("fathersMiddleNameLabel.text")); // NOI18N
        fathersMiddleNameLabel.setName("fathersMiddleNameLabel"); // NOI18N

        fathersMiddleNameTextField.setName("fathersMiddleNameTextField"); // NOI18N

        altFathersMiddleNameTextField.setEditable(false);
        altFathersMiddleNameTextField.setName("altFathersMiddleNameTextField"); // NOI18N

        fathersMiddleNameToggleButton.setText(resourceMap.getString("fathersMiddleNameToggleButton.text")); // NOI18N
        fathersMiddleNameToggleButton.setName("fathersMiddleNameToggleButton"); // NOI18N

        fathersLastNameLabel.setText(resourceMap.getString("fathersLastNameLabel.text")); // NOI18N
        fathersLastNameLabel.setName("fathersLastNameLabel"); // NOI18N

        fathersLastNameTextField.setName("fathersLastNameTextField"); // NOI18N

        altFathersLastNameTextField.setEditable(false);
        altFathersLastNameTextField.setName("altFathersLastNameTextField"); // NOI18N

        fathersLastNameToggleButton.setText(resourceMap.getString("fathersLastNameToggleButton.text")); // NOI18N
        fathersLastNameToggleButton.setName("fathersLastNameToggleButton"); // NOI18N

        mothersFirstNameLabel.setText(resourceMap.getString("mothersFirstNameLabel.text")); // NOI18N
        mothersFirstNameLabel.setName("mothersFirstNameLabel"); // NOI18N

        mothersFirstNameTextField.setName("mothersFirstNameTextField"); // NOI18N

        altMothersFirstNameTextField.setEditable(false);
        altMothersFirstNameTextField.setName("altMothersFirstNameTextField"); // NOI18N

        mothersFirstNameToggleButton.setText(resourceMap.getString("mothersFirstNameToggleButton.text")); // NOI18N
        mothersFirstNameToggleButton.setName("mothersFirstNameToggleButton"); // NOI18N

        mothersMiddleNameLabel.setText(resourceMap.getString("mothersMiddleNameLabel.text")); // NOI18N
        mothersMiddleNameLabel.setName("mothersMiddleNameLabel"); // NOI18N

        mothersMiddleNameTextField.setName("mothersMiddleNameTextField"); // NOI18N

        altMothersMiddleNameTextField.setEditable(false);
        altMothersMiddleNameTextField.setName("altMothersMiddleNameTextField"); // NOI18N

        mothersMiddleNameToggleButton.setText(resourceMap.getString("mothersMiddleNameToggleButton.text")); // NOI18N
        mothersMiddleNameToggleButton.setName("mothersMiddleNameToggleButton"); // NOI18N

        mothersLastNameLabel.setText(resourceMap.getString("mothersLastNameLabel.text")); // NOI18N
        mothersLastNameLabel.setName("mothersLastNameLabel"); // NOI18N

        mothersLastNameTextField.setName("mothersLastNameTextField"); // NOI18N

        altMothersLastNameTextField.setEditable(false);
        altMothersLastNameTextField.setName("altMothersLastNameTextField"); // NOI18N

        mothersLastNameToggleButton.setText(resourceMap.getString("mothersLastNameToggleButton.text")); // NOI18N
        mothersLastNameToggleButton.setName("mothersLastNameToggleButton"); // NOI18N

        compoundHeadsFirstNameLabel.setText(resourceMap.getString("compoundHeadsFirstNameLabel.text")); // NOI18N
        compoundHeadsFirstNameLabel.setName("compoundHeadsFirstNameLabel"); // NOI18N

        compoundHeadsFirstNameTextField.setName("compoundHeadsFirstNameTextField"); // NOI18N

        altCompoundHeadsFirstNameTextField.setEditable(false);
        altCompoundHeadsFirstNameTextField.setName("altCompoundHeadsFirstNameTextField"); // NOI18N

        compoundHeadsMiddleNameLabel.setText(resourceMap.getString("compoundHeadsMiddleNameLabel.text")); // NOI18N
        compoundHeadsMiddleNameLabel.setName("compoundHeadsMiddleNameLabel"); // NOI18N

        compoundHeadsMiddleNameTextField.setName("compoundHeadsMiddleNameTextField"); // NOI18N

        altCompoundHeadsMiddleNameTextField.setEditable(false);
        altCompoundHeadsMiddleNameTextField.setName("altCompoundHeadsMiddleNameTextField"); // NOI18N

        compoundHeadsMiddleNameButton.setText(resourceMap.getString("compoundHeadsMiddleNameButton.text")); // NOI18N
        compoundHeadsMiddleNameButton.setName("compoundHeadsMiddleNameButton"); // NOI18N

        compoundHeadsFirstNameToggleButton.setText(resourceMap.getString("compoundHeadsFirstNameToggleButton.text")); // NOI18N
        compoundHeadsFirstNameToggleButton.setName("compoundHeadsFirstNameToggleButton"); // NOI18N

        compoundHeadsMiddleNameToggleButton.setText(resourceMap.getString("compoundHeadsMiddleNameToggleButton.text")); // NOI18N
        compoundHeadsMiddleNameToggleButton.setName("compoundHeadsMiddleNameToggleButton"); // NOI18N

        javax.swing.GroupLayout reviewPanel2Layout = new javax.swing.GroupLayout(reviewPanel2);
        reviewPanel2.setLayout(reviewPanel2Layout);
        reviewPanel2Layout.setHorizontalGroup(
            reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                        .addComponent(compoundHeadsMiddleNameButton, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                        .addGap(10, 10, 10))
                    .addGroup(reviewPanel2Layout.createSequentialGroup()
                        .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(compoundHeadsMiddleNameLabel)
                            .addComponent(compoundHeadsFirstNameLabel)
                            .addComponent(mothersLastNameLabel)
                            .addComponent(mothersMiddleNameLabel)
                            .addComponent(fathersLastNameLabel)
                            .addComponent(mothersFirstNameLabel)
                            .addComponent(fathersMiddleNameLabel)
                            .addComponent(fathersFirstNameLabel))
                        .addGap(20, 20, 20)
                        .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fathersFirstNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altFathersFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersFirstNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(fathersMiddleNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altMothersFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersFirstNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altFathersMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersMiddleNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(fathersLastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altFathersLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersLastNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(mothersFirstNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addComponent(mothersLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addGroup(reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altMothersLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersLastNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(compoundHeadsFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addGroup(reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altCompoundHeadsFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsFirstNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altCompoundHeadsMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsMiddleNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(compoundHeadsMiddleNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altMothersMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersMiddleNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(mothersMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        reviewPanel2Layout.setVerticalGroup(
            reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fathersFirstNameLabel)
                    .addComponent(fathersFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altFathersFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fathersFirstNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fathersMiddleNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(reviewPanel2Layout.createSequentialGroup()
                        .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fathersMiddleNameLabel)
                            .addComponent(fathersMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(altFathersMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fathersLastNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(reviewPanel2Layout.createSequentialGroup()
                        .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fathersLastNameLabel)
                            .addComponent(fathersLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(altFathersLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mothersFirstNameLabel)
                    .addComponent(mothersFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altMothersFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mothersFirstNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mothersMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mothersMiddleNameLabel))
                .addGap(7, 7, 7)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altMothersMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mothersMiddleNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mothersLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mothersLastNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altMothersLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mothersLastNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compoundHeadsFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compoundHeadsFirstNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altCompoundHeadsFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compoundHeadsFirstNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compoundHeadsMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compoundHeadsMiddleNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altCompoundHeadsMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compoundHeadsMiddleNameToggleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compoundHeadsMiddleNameButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout reviewCard2Layout = new javax.swing.GroupLayout(reviewCard2);
        reviewCard2.setLayout(reviewCard2Layout);
        reviewCard2Layout.setHorizontalGroup(
            reviewCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewCard2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reviewPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        reviewCard2Layout.setVerticalGroup(
            reviewCard2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewCard2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reviewPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        wizardPanel.add(reviewCard2, "card8");

        reviewCard3.setName("reviewCard3"); // NOI18N

        reviewPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        reviewPanel3.setName("reviewPanel3"); // NOI18N

        compoundHeadsLastNameLabel.setText(resourceMap.getString("compoundHeadsLastNameLabel.text")); // NOI18N
        compoundHeadsLastNameLabel.setName("compoundHeadsLastNameLabel"); // NOI18N

        compoundHeadsLastNameTextField.setName("compoundHeadsLastNameTextField"); // NOI18N

        altCompoundHeadsLastNameTextField.setEditable(false);
        altCompoundHeadsLastNameTextField.setName("altCompoundHeadsLastNameTextField"); // NOI18N

        compoundHeadsLastNameToggleButton.setText(resourceMap.getString("compoundHeadsLastNameToggleButton.text")); // NOI18N
        compoundHeadsLastNameToggleButton.setName("compoundHeadsLastNameToggleButton"); // NOI18N

        fingerprintLabel.setText(resourceMap.getString("fingerprintLabel.text")); // NOI18N
        fingerprintLabel.setName("fingerprintLabel"); // NOI18N

        fingerprintImagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        fingerprintImagePanel.setName("fingerprintImagePanel"); // NOI18N

        javax.swing.GroupLayout fingerprintImagePanelLayout = new javax.swing.GroupLayout(fingerprintImagePanel);
        fingerprintImagePanel.setLayout(fingerprintImagePanelLayout);
        fingerprintImagePanelLayout.setHorizontalGroup(
            fingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 75, Short.MAX_VALUE)
        );
        fingerprintImagePanelLayout.setVerticalGroup(
            fingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 74, Short.MAX_VALUE)
        );

        clientRefusesCheckBox.setAction(actionMap.get("refuseFingerprintingExtended")); // NOI18N
        clientRefusesCheckBox.setText(resourceMap.getString("clientRefusesCheckBox.text")); // NOI18N
        clientRefusesCheckBox.setName("clientRefusesCheckBox"); // NOI18N

        takeButton.setAction(actionMap.get("showFingerprintDialogExtended")); // NOI18N
        takeButton.setText(resourceMap.getString("takeButton.text")); // NOI18N
        takeButton.setName("takeButton"); // NOI18N

        finishButton.setAction(actionMap.get("searchExtended")); // NOI18N
        finishButton.setText(resourceMap.getString("finishButton.text")); // NOI18N
        finishButton.setName("finishButton"); // NOI18N

        javax.swing.GroupLayout reviewPanel3Layout = new javax.swing.GroupLayout(reviewPanel3);
        reviewPanel3.setLayout(reviewPanel3Layout);
        reviewPanel3Layout.setHorizontalGroup(
            reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(finishButton, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                    .addGroup(reviewPanel3Layout.createSequentialGroup()
                        .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(compoundHeadsLastNameLabel)
                            .addComponent(fingerprintLabel))
                        .addGap(33, 33, 33)
                        .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(compoundHeadsLastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel3Layout.createSequentialGroup()
                                .addComponent(altCompoundHeadsLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsLastNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(reviewPanel3Layout.createSequentialGroup()
                                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(takeButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(fingerprintImagePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clientRefusesCheckBox)))))
                .addContainerGap())
        );
        reviewPanel3Layout.setVerticalGroup(
            reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compoundHeadsLastNameLabel)
                    .addComponent(compoundHeadsLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(altCompoundHeadsLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compoundHeadsLastNameToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel3Layout.createSequentialGroup()
                        .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(clientRefusesCheckBox)
                            .addComponent(fingerprintImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(takeButton))
                    .addComponent(fingerprintLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(finishButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout reviewCard3Layout = new javax.swing.GroupLayout(reviewCard3);
        reviewCard3.setLayout(reviewCard3Layout);
        reviewCard3Layout.setHorizontalGroup(
            reviewCard3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewCard3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reviewPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        reviewCard3Layout.setVerticalGroup(
            reviewCard3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewCard3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reviewPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(276, Short.MAX_VALUE))
        );

        wizardPanel.add(reviewCard3, "card9");

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(wizardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(homeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1))))
        );

        rightPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {backButton, homeButton});

        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(backButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(homeButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wizardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
                .addContainerGap())
        );

        rightPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {backButton, homeButton});

        mainSplitPane.setRightComponent(rightPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 619, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void basicSearchClinicIdTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_basicSearchClinicIdTextFieldKeyTyped
        prepareCard("basicSearchCard");
    }//GEN-LAST:event_basicSearchClinicIdTextFieldKeyTyped

    private void basicSearchClinicNameTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_basicSearchClinicNameTextFieldKeyTyped
        prepareCard("basicSearchCard");
    }//GEN-LAST:event_basicSearchClinicNameTextFieldKeyTyped

    @Action
    public void goHome() {
        showCard("homeCard", true);
    }

    @Action
    public void goBack() {
        if (!currentCardName.equalsIgnoreCase("homeCard")) {
            if (currentCardName.equalsIgnoreCase("clinicIdCard")) {
                showCard("homeCard");
            } else if (currentCardName.equalsIgnoreCase("basicSearchCard")) {
                showCard("clinicIdCard");
            } else if (currentCardName.equalsIgnoreCase("extendedSearchCard")) {
                if (session.getClientType() == Session.CLIENT_TYPE.NEW) {
                    showCard("homeCard");
                } else {
                    if (session.hasKnownClinicId()) {
                        showCard("basicSearchCard");
                    } else {
                        showCard("clinicIdCard");
                    }
                }
            } else if (currentCardName.equalsIgnoreCase("searchResultsCard")) {
                showCard("extendedSearchCard");
            } else if (currentCardName.equalsIgnoreCase("reviewCard1")) {
                showCard("searchResultsCard");
            }
        }
    }

    public void showCard(String cardName) {
        showCard(cardName, false);
    }

    public void showCard(String cardName, boolean home) {
        if (home && !currentCardName.equalsIgnoreCase("homeCard")) {
            if (!showConfirmMessage("Are you sure you want to go back to the home page and"
                    + " start a new session?", this.getFrame())) {
                return;
            }
        }
        cardLayout.show(wizardPanel, cardName);
        previousCardName = currentCardName;
        currentCardName = cardName;
        prepareCard(cardName);
    }

    private void prepareCard(String cardName) {
        if (cardName.equalsIgnoreCase("basicSearchCard")) {
            if (session.getClientType() == Session.CLIENT_TYPE.ENROLLED) {
                basicSearchButton.setEnabled((!session.getImagedFingerprintList().isEmpty()
                        || session.isNonFingerprint()) && !basicSearchClinicIdTextField.getText().isEmpty());
            } else if (session.getClientType() == Session.CLIENT_TYPE.VISITOR) {
                basicSearchButton.setEnabled((!session.getImagedFingerprintList().isEmpty()
                        || session.isNonFingerprint())
                        && !basicSearchClinicIdTextField.getText().isEmpty()
                        && !basicSearchClinicNameTextField.getText().isEmpty());
            }
            basicSearchClinicNameLabel.setVisible((session.getClientType() == Session.CLIENT_TYPE.VISITOR)
                    || (session.getClientType() == Session.CLIENT_TYPE.TRANSFER_IN));
            basicSearchClinicNameTextField.setVisible(session.getClientType() == Session.CLIENT_TYPE.VISITOR);
        } else if (cardName.equalsIgnoreCase("extendedSearchCard")) {
            if (session.getClientType() == Session.CLIENT_TYPE.ENROLLED) {
                basicSearchButton.setEnabled(!basicSearchClinicIdTextField.getText().isEmpty()
                        && (!session.getImagedFingerprintList().isEmpty()
                        || session.isNonFingerprint()));
                extendedSearchClinicIdLabel.setVisible(session.hasKnownClinicId());
                extendedSearchClinicIdTextField.setVisible(session.hasKnownClinicId());
                extendedSearchClinicNameLabel.setVisible(false);
                extendedSearchClinicNameTextField.setVisible(false);
            } else if (session.getClientType() == Session.CLIENT_TYPE.VISITOR) {
                basicSearchButton.setEnabled(!basicSearchClinicIdTextField.getText().isEmpty()
                        && (!session.getImagedFingerprintList().isEmpty()
                        || session.isNonFingerprint()
                        && !basicSearchClinicNameTextField.getText().isEmpty()));
                extendedSearchClinicIdLabel.setVisible(session.hasKnownClinicId());
                extendedSearchClinicIdTextField.setVisible(session.hasKnownClinicId());
                extendedSearchClinicNameLabel.setVisible(true);
                extendedSearchClinicNameTextField.setVisible(true);
            } else if (session.getClientType() == Session.CLIENT_TYPE.NEW) {
                extendedSearchClinicIdLabel.setVisible(false);
                extendedSearchClinicIdTextField.setVisible(false);
                extendedSearchClinicNameLabel.setVisible(false);
                extendedSearchClinicNameTextField.setVisible(false);
            } else if (session.getClientType() == Session.CLIENT_TYPE.TRANSFER_IN) {
                basicSearchButton.setEnabled(!basicSearchClinicIdTextField.getText().isEmpty()
                        && (!session.getImagedFingerprintList().isEmpty()
                        || session.isNonFingerprint()
                        && !basicSearchClinicNameTextField.getText().isEmpty()));
                extendedSearchClinicIdLabel.setVisible(session.hasKnownClinicId());
                extendedSearchClinicIdTextField.setVisible(session.hasKnownClinicId());
                extendedSearchClinicNameLabel.setVisible(true);
                extendedSearchClinicNameTextField.setVisible(true);
            }
        }
    }

    @Action
    public void showFingerprintDialogBasic() {
        try {
            FingerprintDialog fingerprintDialog = new FingerprintDialog(this.getFrame(), true);
            fingerprintDialog.setLocationRelativeTo(this.getFrame());
            fingerprintDialog.setSession(session);
            fingerprintDialog.setVisible(true);
            showFingerprintImageExtended(session.getCurrentImagedFingerprint().getImage());
            prepareCard("basicSearchCard");
        } catch (GrFingerJavaException ex) {
            showWarningMessage("Fingerprinting is currently unavailable because of the following"
                    + " reason: " + ex.getMessage() + ".", this.getFrame(), basicSearchTakeButton);
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    @Action
    public void showFingerprintDialogExtended() {
        try {
            FingerprintDialog fingerprintDialog = new FingerprintDialog(this.getFrame(), true);
            fingerprintDialog.setLocationRelativeTo(this.getFrame());
            fingerprintDialog.setSession(session);
            fingerprintDialog.setVisible(true);
            showFingerprintImageExtended(session.getCurrentImagedFingerprint().getImage());
            prepareCard("extendedSearchCard");
        } catch (GrFingerJavaException ex) {
            showWarningMessage("Fingerprinting functionality is unavailable for the following"
                    + " reason: " + ex.getMessage() + ".", this.getFrame(), basicSearchTakeButton);
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    public void showFingerprintImageBasic(BufferedImage fingerprintImage) {
        if (fingerprintImage != null) {
            basicSearchFingerprintImagePanel.setImage(fingerprintImage);
            basicSearchPanel.repaint();
        }
    }

    public void showFingerprintImageExtended(BufferedImage fingerprintImage) {
        if (fingerprintImage != null) {
            extendedSearchFingerprintImagePanel.setImage(fingerprintImage);
            extendedSearchPanel.repaint();
        }
    }

    private void showWarningMessage(String message, Component parent, JComponent toFocus) {
        JOptionPane.showMessageDialog(parent, message, Session.getApplicationName(), JOptionPane.WARNING_MESSAGE);
        toFocus.requestFocus();
    }

    public boolean showConfirmMessage(String message, Component parent) {
        return JOptionPane.showConfirmDialog(this.getFrame(), message, Session.getApplicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private ProcessResult doBasicSearch(int targetIndex) {
        List<Person> mpiPersonList = null;
        List<Person> lpiPersonList = null;
        if (targetIndex == TargetIndex.BOTH || targetIndex == TargetIndex.MPI) {
            mpiRequestResult = new RequestResult();
        }
        if (targetIndex == TargetIndex.BOTH || targetIndex == TargetIndex.LPI) {
            lpiRequestResult = new RequestResult();
        }
        RequestDispatcher.findCandidates(session.getBasicRequestParameters(),
                mpiRequestResult, lpiRequestResult, targetIndex);
        if (mpiRequestResult.isSuccessful()
                && lpiRequestResult.isSuccessful()) {
            mpiPersonList = (List<Person>) mpiRequestResult.getData();
            lpiPersonList = (List<Person>) lpiRequestResult.getData();
            if (Session.checkPersonListForLinkedCandidates(lpiPersonList)) {
                return new ProcessResult(ProcessResult.Type.LIST, lpiPersonList);
            } else {
                if (Session.checkPersonListForFingerprintCandidates(mpiPersonList)) {
                    return new ProcessResult(ProcessResult.Type.LIST, lpiPersonList);
                } else {
                    if (!session.hasAllFingerprintsTaken()) {
                        return new ProcessResult(ProcessResult.Type.NEXT, null);
                    } else {
                        if (!session.getAnyUnsentFingerprints().isEmpty()) {
                            for (ImagedFingerprint imagedFingerprint : session.getAnyUnsentFingerprints()) {
                                session.setCurrentImagedFingerprint(imagedFingerprint);
                                session.getBasicRequestParameters().setFingerprint(imagedFingerprint.getFingerprint());
                                imagedFingerprint.setSent(true);
                                break;
                            }
                            return doBasicSearch(TargetIndex.BOTH);
                        } else {
                            if (!lpiPersonList.isEmpty()) {
                                return new ProcessResult(ProcessResult.Type.LIST, lpiPersonList);
                            } else {
                                if (!mpiPersonList.isEmpty()) {
                                    return new ProcessResult(ProcessResult.Type.LIST, mpiPersonList);
                                } else {
                                    return new ProcessResult(ProcessResult.Type.EXIT, null);
                                }
                            }
                        }
                    }

                }
            }
        } else {
            if (!mpiRequestResult.isSuccessful()
                    && !lpiRequestResult.isSuccessful()) {
                if (showConfirmMessage("Both the Master and the Local Person Indices could not be contacted. "
                        + "Would you like to try contacting them again?", this.getFrame())) {
                    return doBasicSearch(TargetIndex.BOTH);
                }
            } else {
                if (!mpiRequestResult.isSuccessful()) {
                    if (showConfirmMessage("The Master Person Index could not be contacted. "
                            + "Would you like to try contacting it again?", this.getFrame())) {
                        return doBasicSearch(TargetIndex.MPI);
                    }
                }
                if (!lpiRequestResult.isSuccessful()) {
                    if (showConfirmMessage("The Local Person Index could not be contacted. "
                            + "Would you like to try contacting it again?", this.getFrame())) {
                        return doBasicSearch(TargetIndex.LPI);
                    }
                }
            }
            return new ProcessResult(ProcessResult.Type.ERROR, null);
        }
    }

    private ProcessResult doExtendedSearch(int targetIndex) {
        List<Person> mpiPersonList = null;
        List<Person> lpiPersonList = null;
        if (targetIndex == TargetIndex.BOTH || targetIndex == TargetIndex.MPI) {
            mpiRequestResult = new RequestResult();
        }
        if (targetIndex == TargetIndex.BOTH || targetIndex == TargetIndex.LPI) {
            lpiRequestResult = new RequestResult();
        }
        RequestDispatcher.findCandidates(session.getExtendedRequestParameters(),
                mpiRequestResult, lpiRequestResult, targetIndex);
        if (mpiRequestResult.isSuccessful()
                && lpiRequestResult.isSuccessful()) {
            mpiPersonList = (List<Person>) mpiRequestResult.getData();
            lpiPersonList = (List<Person>) lpiRequestResult.getData();
            if (Session.checkPersonListForLinkedCandidates(lpiPersonList)) {
                return new ProcessResult(ProcessResult.Type.LIST, lpiPersonList);
            } else {
                if (Session.checkPersonListForFingerprintCandidates(mpiPersonList)) {
                    return new ProcessResult(ProcessResult.Type.LIST, lpiPersonList);
                } else {
                    if (!session.hasAllFingerprintsTaken()) {
                        return new ProcessResult(ProcessResult.Type.NEXT, null);
                    } else {
                        if (!session.getAnyUnsentFingerprints().isEmpty()) {
                            for (ImagedFingerprint imagedFingerprint : session.getAnyUnsentFingerprints()) {
                                session.setCurrentImagedFingerprint(imagedFingerprint);
                                session.getExtendedRequestParameters().getBasicRequestParameters().setFingerprint(imagedFingerprint.getFingerprint());
                                imagedFingerprint.setSent(true);
                                break;
                            }
                            return doExtendedSearch(TargetIndex.BOTH);
                        } else {
                            if (!lpiPersonList.isEmpty()) {
                                return new ProcessResult(ProcessResult.Type.LIST, lpiPersonList);
                            } else {
                                if (!mpiPersonList.isEmpty()) {
                                    return new ProcessResult(ProcessResult.Type.LIST, mpiPersonList);
                                } else {
                                    return new ProcessResult(ProcessResult.Type.EXIT, null);
                                }
                            }
                        }
                    }

                }
            }
        } else {
            if (!mpiRequestResult.isSuccessful()
                    && !lpiRequestResult.isSuccessful()) {
                if (showConfirmMessage("Both the Master and the Local Person Indices could not be contacted. "
                        + "Would you like to try contacting them again?", this.getFrame())) {
                    return doBasicSearch(TargetIndex.BOTH);
                }
            } else {
                if (!mpiRequestResult.isSuccessful()) {
                    if (showConfirmMessage("The Master Person Index could not be contacted. "
                            + "Would you like to try contacting it again?", this.getFrame())) {
                        return doBasicSearch(TargetIndex.MPI);
                    }
                }
                if (!lpiRequestResult.isSuccessful()) {
                    if (showConfirmMessage("The Local Person Index could not be contacted. "
                            + "Would you like to try contacting it again?", this.getFrame())) {
                        return doBasicSearch(TargetIndex.LPI);
                    }
                }
            }
            return new ProcessResult(ProcessResult.Type.ERROR, null);
        }
    }

    @Action
    public void refuseFingerprintingBasic() {
        if (basicSearchClientRefusesCheckBox.isSelected()) {
            showFingerprintImageBasic(refusedFingerprint);
            session.setNonFingerprint(true);
        } else {
            showFingerprintImageBasic(fingerprintNotTaken);
            session.setNonFingerprint(false);
        }
        prepareCard("basicSearchCard");
    }

    @Action
    public void refuseFingerprintingExtended() {
        if (extendedSearchClientRefusesCheckBox.isSelected()) {
            showFingerprintImageExtended(refusedFingerprint);
            session.setNonFingerprint(true);
        } else {
            showFingerprintImageExtended(fingerprintNotTaken);
            session.setNonFingerprint(false);
        }
        prepareCard("extendedSearchCard");
    }

    @Action
    public Task searchBasic() {
        return new SearchBasicTask(getApplication());
    }

    private class SearchBasicTask extends org.jdesktop.application.Task<Object, Void> {

        SearchBasicTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            String clinicId = basicSearchClinicIdTextField.getText();
            if (session.hasKnownClinicId() && !Session.validateClinicId(clinicId)) {
                showWarningMessage("The Clinic ID: '" + clinicId + "' you entered is in the wrong format. "
                        + "Please use the format '12345-00001' for Universal Clinic IDs and '00001/2005' "
                        + "for Local Clinic IDs", basicSearchButton, basicSearchClinicIdTextField);
                return new ProcessResult(ProcessResult.Type.ABORT, null);
            } else {
                session.getBasicRequestParameters().setClinicId(clinicId);
                if (session.getCurrentImagedFingerprint() != null) {
                    session.getBasicRequestParameters().setFingerprint(session.getCurrentImagedFingerprint().getFingerprint());
                    session.getCurrentImagedFingerprint().setSent(true);
                }
                if (session.getClientType() == Session.CLIENT_TYPE.VISITOR
                        || session.getClientType() == Session.CLIENT_TYPE.TRANSFER_IN) {
                    if (basicSearchClinicNameTextField.getText().isEmpty()) {
                        showWarningMessage("Please enter Clinic name before proceeding.", basicSearchButton, basicSearchClinicNameTextField);
                        return new ProcessResult(ProcessResult.Type.ABORT, null);
                    } else {
                        session.getBasicRequestParameters().setClinicName(basicSearchClinicNameTextField.getText());
                    }
                }
                return doBasicSearch(TargetIndex.BOTH);
            }
        }

        @Override
        protected void succeeded(Object result) {
            ProcessResult processResult = (ProcessResult) result;
            if (processResult.getType() == ProcessResult.Type.LIST) {
                cardLayout.show(wizardPanel, "searchResultsCard");
                bindingGroup.unbind();
                searchResultsList.clear();
                searchResultsList.addAll((List<Person>) processResult.getData());
                bindingGroup.bind();
                searchResultsTable.repaint();
            } else if (processResult.getType() == ProcessResult.Type.NEXT) {
                showFingerprintDialogBasic();
            } else if (processResult.getType() == ProcessResult.Type.EXIT) {
                if (!showConfirmMessage("Your basic search returned no candidates. Would you like"
                        + " to repeat it? Choose Yes to repeat a basic search or No to proceed to"
                        + " an extended search.", extendedSearchButton)) {
                    showCard("extendedSearchCard");
                }
            }
        }
    }

    @Action
    public Task searchExtended() {
        return new SearchExtendedTask(getApplication());
    }

    private class SearchExtendedTask extends org.jdesktop.application.Task<Object, Void> {

        SearchExtendedTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            String clinicId = extendedSearchClinicIdTextField.getText();
            if (session.hasKnownClinicId() && !Session.validateClinicId(clinicId)) {
                showWarningMessage("The Clinic ID: '" + clinicId + "' you entered is in the wrong format. "
                        + "Please use the format '12345-00001' for Universal Clinic IDs and '00001/2005' "
                        + "for Local Clinic IDs", basicSearchButton, basicSearchClinicIdTextField);
                return new ProcessResult(ProcessResult.Type.ABORT, null);
            } else {
                session.getExtendedRequestParameters().getBasicRequestParameters().setClinicId(clinicId);
                if (session.getCurrentImagedFingerprint() != null) {
                    session.getExtendedRequestParameters().getBasicRequestParameters().setFingerprint(session.getCurrentImagedFingerprint().getFingerprint());
                    session.getCurrentImagedFingerprint().setSent(true);
                }
                if (session.getClientType() == Session.CLIENT_TYPE.VISITOR
                        || session.getClientType() == Session.CLIENT_TYPE.TRANSFER_IN) {
                    if (extendedSearchClinicNameTextField.getText().isEmpty()) {
                        showWarningMessage("Please enter Clinic name before proceeding.", extendedSearchButton, extendedSearchClinicNameTextField);
                        return new ProcessResult(ProcessResult.Type.ABORT, null);
                    } else {
                        session.getExtendedRequestParameters().getBasicRequestParameters().setClinicName(extendedSearchClinicNameTextField.getText());
                    }
                }
                session.getExtendedRequestParameters().setFirstName(extendedSearchFirstNameTextField.getText());
                session.getExtendedRequestParameters().setMiddleName(extendedSearchMiddleNameTextField.getText());
                session.getExtendedRequestParameters().setLastName(extendedSearchLastNameTextField.getText());
                if (extendedSearchMaleRadioButton.isSelected()) {
                    session.getExtendedRequestParameters().setSex(Person.Sex.M);
                } else if (extendedSearchFemaleRadioButton.isSelected()) {
                    session.getExtendedRequestParameters().setSex(Person.Sex.F);
                }
                session.getExtendedRequestParameters().setBirthdate(extendedSearchBirthdateChooser.getDate());
                session.getExtendedRequestParameters().setVillageName(basicSearchClinicIdTextField.getText());
                return doExtendedSearch(TargetIndex.BOTH);
            }
        }

        @Override
        protected void succeeded(Object result) {
            ProcessResult processResult = (ProcessResult) result;
            if (processResult.getType() == ProcessResult.Type.LIST) {
                showCard("searchResultsCard");
                bindingGroup.unbind();
                searchResultsList.clear();
                searchResultsList.addAll((List<Person>) processResult.getData());
                bindingGroup.bind();
                searchResultsTable.repaint();
            } else if (processResult.getType() == ProcessResult.Type.NEXT) {
                showFingerprintDialogExtended();
            } else if (processResult.getType() == ProcessResult.Type.EXIT) {
                if (!showConfirmMessage("Your extended search returned no candidates. Would you like"
                        + " to repeat it? Choose Yes to repeat an extended search or No to proceed to"
                        + " register a new client.", extendedSearchButton)) {
                    showCard("reviewCard1");
                }
            }
        }
  
    }

    @Action
    public void startEnrolledClientSession() {
        session = new Session(Session.CLIENT_TYPE.ENROLLED);
        showCard("clinicIdCard");
    }

    @Action
    public void startVisitorClientSession() {
        session = new Session(Session.CLIENT_TYPE.VISITOR);
        showCard("clinicIdCard");
    }

    @Action
    public void startNewClientSession() {
        session = new Session(Session.CLIENT_TYPE.NEW);
        showCard("extendedSearchCard");
    }

    @Action
    public void startTransferInClientSession() {
        session = new Session(Session.CLIENT_TYPE.TRANSFER_IN);
        showCard("clinicIdCard");
    }

    @Action
    public void setKnownClinicIdToYes() {
        session.setKnownClinicId(true);
        showCard("basicSearchCard");
    }

    @Action
    public void setKnownClinicIdToNo() {
        session.setKnownClinicId(false);
        showCard("extendedSearchCard");
    }

    @Action
    public void acceptMatch() {
        int selectedRow = searchResultsTable.getSelectedRow();
        if (selectedRow > -1) {
            Person p = searchResultsList.get(selectedRow);
            showWarningMessage(p.getFirstName(), this.getFrame(), acceptButton);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton ClinicIdToggleButton;
    private javax.swing.JButton acceptButton;
    private javax.swing.JList alertsList;
    private javax.swing.JPanel alertsListPanel;
    private javax.swing.JScrollPane alertsScrollPane;
    private javax.swing.JTextField alrVillageTextField;
    private javax.swing.JTextField altBirthDateTextField;
    private javax.swing.JTextField altClinicIdTextField;
    private javax.swing.JTextField altCompoundHeadsFirstNameTextField;
    private javax.swing.JTextField altCompoundHeadsLastNameTextField;
    private javax.swing.JTextField altCompoundHeadsMiddleNameTextField;
    private javax.swing.JTextField altFathersFirstNameTextField;
    private javax.swing.JTextField altFathersLastNameTextField;
    private javax.swing.JTextField altFathersMiddleNameTextField;
    private javax.swing.JTextField altFirstNameTextField;
    private javax.swing.JTextField altLastNameTextField;
    private javax.swing.JTextField altMaritalStatusTextField;
    private javax.swing.JTextField altMiddleNameTextField;
    private javax.swing.JTextField altMothersFirstNameTextField;
    private javax.swing.JTextField altMothersLastNameTextField;
    private javax.swing.JTextField altMothersMiddleNameTextField;
    private javax.swing.JTextField altSexTextField;
    private javax.swing.JToggleButton altVillageToggleButton;
    private javax.swing.JButton backButton;
    private javax.swing.JButton basicSearchButton;
    private javax.swing.JPanel basicSearchCard;
    private javax.swing.JCheckBox basicSearchClientRefusesCheckBox;
    private javax.swing.JLabel basicSearchClinicIdLabel;
    private javax.swing.JTextField basicSearchClinicIdTextField;
    private javax.swing.JLabel basicSearchClinicNameLabel;
    private javax.swing.JTextField basicSearchClinicNameTextField;
    private ke.go.moh.oec.reception.gui.custom.ImagePanel basicSearchFingerprintImagePanel;
    private javax.swing.JLabel basicSearchFingerprintLabel;
    private javax.swing.JPanel basicSearchPanel;
    private javax.swing.JButton basicSearchTakeButton;
    private com.toedter.calendar.JDateChooser birthDateChooser;
    private javax.swing.JLabel birthDateLabel;
    private javax.swing.JToggleButton birthDateToggleButton;
    private javax.swing.JPanel clientIdPanel;
    private javax.swing.JCheckBox clientRefusesCheckBox;
    private javax.swing.JPanel clinicIdCard;
    private javax.swing.JLabel clinicIdLabel;
    private javax.swing.JButton clinicIdNoButton;
    private javax.swing.JTextField clinicIdTextField;
    private javax.swing.JButton clinicIdYesButton;
    private javax.swing.JLabel compoundHeadsFirstNameLabel;
    private javax.swing.JTextField compoundHeadsFirstNameTextField;
    private javax.swing.JToggleButton compoundHeadsFirstNameToggleButton;
    private javax.swing.JLabel compoundHeadsLastNameLabel;
    private javax.swing.JTextField compoundHeadsLastNameTextField;
    private javax.swing.JToggleButton compoundHeadsLastNameToggleButton;
    private javax.swing.JButton compoundHeadsMiddleNameButton;
    private javax.swing.JLabel compoundHeadsMiddleNameLabel;
    private javax.swing.JTextField compoundHeadsMiddleNameTextField;
    private javax.swing.JToggleButton compoundHeadsMiddleNameToggleButton;
    private javax.swing.JButton enrolledButton;
    private com.toedter.calendar.JDateChooser extendedSearchBirthdateChooser;
    private javax.swing.JLabel extendedSearchBirthdateLabel;
    private javax.swing.JButton extendedSearchButton;
    private javax.swing.JPanel extendedSearchCard;
    private javax.swing.JCheckBox extendedSearchClientRefusesCheckBox;
    private javax.swing.JLabel extendedSearchClinicIdLabel;
    private javax.swing.JTextField extendedSearchClinicIdTextField;
    private javax.swing.JLabel extendedSearchClinicNameLabel;
    private javax.swing.JTextField extendedSearchClinicNameTextField;
    private javax.swing.JRadioButton extendedSearchFemaleRadioButton;
    private ke.go.moh.oec.reception.gui.custom.ImagePanel extendedSearchFingerprintImagePanel;
    private javax.swing.JLabel extendedSearchFingerprintLabel;
    private javax.swing.JLabel extendedSearchFirstNameLabel;
    private javax.swing.JTextField extendedSearchFirstNameTextField;
    private javax.swing.JLabel extendedSearchLastNameLabel;
    private javax.swing.JTextField extendedSearchLastNameTextField;
    private javax.swing.JRadioButton extendedSearchMaleRadioButton;
    private javax.swing.JLabel extendedSearchMiddleNameLabel;
    private javax.swing.JTextField extendedSearchMiddleNameTextField;
    private javax.swing.JPanel extendedSearchPanel;
    private javax.swing.JLabel extendedSearchSexLabel;
    private javax.swing.JButton extendedSearchTakeButton;
    private javax.swing.JLabel extendedSearchVillageLabel;
    private javax.swing.JTextField extendedSearchVillageTextField;
    private javax.swing.JLabel fathersFirstNameLabel;
    private javax.swing.JTextField fathersFirstNameTextField;
    private javax.swing.JToggleButton fathersFirstNameToggleButton;
    private javax.swing.JLabel fathersLastNameLabel;
    private javax.swing.JTextField fathersLastNameTextField;
    private javax.swing.JToggleButton fathersLastNameToggleButton;
    private javax.swing.JLabel fathersMiddleNameLabel;
    private javax.swing.JTextField fathersMiddleNameTextField;
    private javax.swing.JToggleButton fathersMiddleNameToggleButton;
    private javax.swing.JRadioButton femaleRadioButton;
    private ke.go.moh.oec.reception.gui.custom.ImagePanel fingerprintImagePanel;
    private javax.swing.JLabel fingerprintLabel;
    private javax.swing.JButton finishButton;
    private javax.swing.JLabel firstNameLabel;
    private javax.swing.JTextField firstNameTextField;
    private javax.swing.JToggleButton firstNameToggleButton;
    private javax.swing.JButton homeButton;
    private javax.swing.JPanel homeCard;
    private javax.swing.JPanel homePanel;
    private javax.swing.JLabel lastNameLabel;
    private javax.swing.JTextField lastNameTextField;
    private javax.swing.JToggleButton lastNameToggleButton;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JRadioButton maleRadioButton;
    private javax.swing.JComboBox maritalStatusComboBox;
    private javax.swing.JLabel maritalStatusLabel;
    private javax.swing.JToggleButton maritalStatusToggleButton;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel middleNameLabel;
    private javax.swing.JTextField middleNameTextField;
    private javax.swing.JToggleButton middleNameToggleButton;
    private javax.swing.JLabel mothersFirstNameLabel;
    private javax.swing.JTextField mothersFirstNameTextField;
    private javax.swing.JToggleButton mothersFirstNameToggleButton;
    private javax.swing.JLabel mothersLastNameLabel;
    private javax.swing.JTextField mothersLastNameTextField;
    private javax.swing.JToggleButton mothersLastNameToggleButton;
    private javax.swing.JLabel mothersMiddleNameLabel;
    private javax.swing.JTextField mothersMiddleNameTextField;
    private javax.swing.JToggleButton mothersMiddleNameToggleButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton notFoundButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel reviewCard1;
    private javax.swing.JButton reviewCard1NextButton;
    private javax.swing.JPanel reviewCard2;
    private javax.swing.JPanel reviewCard3;
    private javax.swing.JPanel reviewPanel1;
    private javax.swing.JPanel reviewPanel2;
    private javax.swing.JPanel reviewPanel3;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel searchResultsCard;
    private java.util.List<ke.go.moh.oec.Person> searchResultsList;
    private javax.swing.JPanel searchResultsPanel;
    private javax.swing.JScrollPane searchResultsScrollPane;
    private javax.swing.JTable searchResultsTable;
    private ke.go.moh.oec.reception.controller.Session session;
    private javax.swing.ButtonGroup sexButtonGroup;
    private javax.swing.JLabel sexLabel;
    private javax.swing.JToggleButton sexToggleButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton takeButton;
    private javax.swing.JButton transferInButton;
    private javax.swing.JLabel villageLabel;
    private javax.swing.JTextField villageTextField;
    private javax.swing.JButton visitorButton;
    private javax.swing.JPanel wizardPanel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
