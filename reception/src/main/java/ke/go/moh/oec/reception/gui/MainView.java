/*
 * MainView.java
 */
package ke.go.moh.oec.reception.gui;
import com.toedter.calendar.JDateChooser;
import java.text.ParseException;
import javax.swing.UnsupportedLookAndFeelException;
import ke.go.moh.oec.reception.gui.helper.SearchProcessResult;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.reception.controller.exceptions.PersistenceManagerException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.Visit;
import ke.go.moh.oec.fingerprintmanager.FingerprintManager;
import ke.go.moh.oec.fingerprintmanager.FingerprintManagerException;
import ke.go.moh.oec.fingerprintmanager.FingerprintingComponent;
import ke.go.moh.oec.fingerprintmanager.MissingFingerprintManagerImpException;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.data.Session;
import ke.go.moh.oec.reception.controller.PersonWrapper;
import ke.go.moh.oec.reception.data.Server;
import ke.go.moh.oec.reception.controller.exceptions.MalformedCliniIdException;
import ke.go.moh.oec.reception.data.DisplayableMaritalStatus;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import ke.go.moh.oec.reception.data.Notification;
import ke.go.moh.oec.reception.gui.custom.ImagePanel;
import ke.go.moh.oec.reception.gui.helper.MainViewHelper;
import ke.go.moh.oec.reception.gui.helper.NotificationSoundPlayer;
import ke.go.moh.oec.reception.gui.helper.SearchServerResponse;
import ke.go.moh.oec.reception.gui.helper.SuccessSignal;
import ke.go.moh.oec.reception.reader.ReaderManager;
import org.jdesktop.beansbinding.Binding;

/**
 * The application's main frame.
 */
public class MainView extends FrameView implements FingerprintingComponent {

    private CardLayout cardLayout;
    private MainViewHelper mainViewHelper;
    private String currentCardName = "homeCard";
    private List<String> visitedCardList = new ArrayList<String>();
    private FingerprintManager fingerprintManager;
    private QuickSearchManager quickSearchManager = null;
    private final SearchStatus searchStatus = new SearchStatus(false);
    private boolean readerAvailable = false;
    private static final int MIN_WIDTH = 670;
    private static final int MIN_HEIGHT = 670;
    private List<Notification> pregnancyOutcomeNotificationList = new ArrayList<Notification>();
    private List<Notification> pregnancyNotificationList = new ArrayList<Notification>();
    private List<Notification> deathNotificationList = new ArrayList<Notification>();
    private List<Notification> migrationNotificationList = new ArrayList<Notification>();
    private DefaultMutableTreeNode notificationRootNode = new DefaultMutableTreeNode("Notifications (0)");
    private DefaultMutableTreeNode pregnancyOutcomeNode = new DefaultMutableTreeNode("Pregnancy outcome (0)");
    private DefaultMutableTreeNode pregnancyNode = new DefaultMutableTreeNode("Pregnancy (0)");
    private DefaultMutableTreeNode deathNode = new DefaultMutableTreeNode("Death (0)");
    private DefaultMutableTreeNode migrationNode = new DefaultMutableTreeNode("Migration (0)");

    public MainView(SingleFrameApplication app) {
        super(app);
        initComponents();
        initializeNotificationTree();
        processNotificationButton.setEnabled(false);
        setUpThemes();
        discriminateUser();
        final JFrame frame = this.getFrame();
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                //Hide the window and then hang on a while to allow the fp sdk to be destroyed.
                //This seems to raise the chances of the fp sdk being destroyed successfully
                frame.setVisible(false);
                destroyReaderManager(true);
                try {
                    Thread.sleep(5000);
                    System.exit(0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
        });
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int width = evt.getComponent().getWidth();
                int height = evt.getComponent().getHeight();
                boolean resize = false;
                if (width < MIN_WIDTH) {
                    resize = true;
                    width = MIN_WIDTH;
                }
                if (height < MIN_HEIGHT) {
                    resize = true;
                    height = MIN_HEIGHT;
                }
                if (resize) {
                    setSize(width, height);
                }
            }
        });
        this.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cardLayout = (CardLayout) wizardPanel.getLayout();
        this.getFrame().setTitle(OECReception.applicationName());
        this.getFrame().setIconImage(OECReception.applicationIcon());
        this.mainViewHelper = new MainViewHelper(this);
        loggedInLabel.setText("Logged in as: " + OECReception.getUser().getUsername());
        showCard("homeCard", true, false);
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

    public void setSize(int width, int height) {
        this.getFrame().setSize(width, height);
    }

    private void setUpThemes() {
        UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(lookAndFeelInfo.getName(),
                    UIManager.getLookAndFeel().getName().equalsIgnoreCase(lookAndFeelInfo.getName()));
            menuItem.setActionCommand(lookAndFeelInfo.getClassName());
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    try {
                        UIManager.setLookAndFeel(e.getActionCommand());
                        updateUI(e.getActionCommand());
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InstantiationException ex) {
                        Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedLookAndFeelException ex) {
                        Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });
            themesMenu.add(menuItem);
        }
    }

    private void updateUI(String actionCommand) {
        SwingUtilities.updateComponentTreeUI(this.getFrame());
        for (Component c : themesMenu.getMenuComponents()) {
            JCheckBoxMenuItem d = ((JCheckBoxMenuItem) c);
            d.setSelected(actionCommand.equalsIgnoreCase(d.getActionCommand()));
        }
    }

    private void save(String looknfeel) {
        getResourceMap().getString("App.busyAnimationRate");
    }

    private void discriminateUser() {
        manageUsersMenu.setVisible(OECReception.getUser().isAdmin());
    }

    public final void initializeNotificationTree() {
        notificationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        notificationTree.setModel(new DefaultTreeModel(notificationRootNode));
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
        notificationListPanel = new javax.swing.JPanel();
        processNotificationButton = new javax.swing.JButton();
        notificationScrollPane = new javax.swing.JScrollPane();
        notificationTree = new javax.swing.JTree();
        rightPanel = new javax.swing.JPanel();
        homeButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        loggedInLabel = new javax.swing.JLabel();
        wizardPanel = new javax.swing.JPanel();
        homeCard = new javax.swing.JPanel();
        quickSearchPanel = new javax.swing.JPanel();
        readerStatusLabel = new javax.swing.JLabel();
        quickSearchFingerprintImagePanel = new ke.go.moh.oec.reception.gui.custom.ImagePanel();
        quickSearchQualityTextField = new javax.swing.JTextField();
        clearRightIndexButton = new javax.swing.JButton();
        clearLeftIndexButton = new javax.swing.JButton();
        resetQuickSearchButton = new javax.swing.JButton();
        fingerToBeTakenLabel = new javax.swing.JLabel();
        quickSearchMessageLabel = new javax.swing.JLabel();
        quickSearchButton = new javax.swing.JButton();
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
        basicSearchFingerprintImagePanel = new ke.go.moh.oec.reception.gui.custom.ImagePanel();
        basicSearchClientRefusesCheckBox = new javax.swing.JCheckBox();
        basicSearchTakeButton = new javax.swing.JButton();
        basicSearchButton = new javax.swing.JButton();
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
        extendedSearchUnknownBirthdateCheckBox = new javax.swing.JCheckBox();
        extendedSearchOtherNameLabel = new javax.swing.JLabel();
        extendedSearchOtherNameTextField = new javax.swing.JTextField();
        extendedSearchClanNameLabel = new javax.swing.JLabel();
        extendedSearchClanNameTextField = new javax.swing.JTextField();
        extendedSearchVillageLabel = new javax.swing.JLabel();
        extendedSearchVillageTextField = new javax.swing.JTextField();
        extendedSearchFingerprintLabel = new javax.swing.JLabel();
        extendedSearchFingerprintImagePanel = new ke.go.moh.oec.reception.gui.custom.ImagePanel();
        extendedSearchClientRefusesCheckBox = new javax.swing.JCheckBox();
        extendedSearchTakeButton = new javax.swing.JButton();
        extendedSearchButton = new javax.swing.JButton();
        mpiResultsCard = new javax.swing.JPanel();
        mpiResultsPanel = new javax.swing.JPanel();
        mpiResultsScrollPane = new javax.swing.JScrollPane();
        mpiResultsTable = new javax.swing.JTable();
        mpiConfirmButton = new javax.swing.JButton();
        mpiNotFoundButton = new javax.swing.JButton();
        lpiResultsCard = new javax.swing.JPanel();
        lpiResultsPanel = new javax.swing.JPanel();
        lpiResultsScrollPane = new javax.swing.JScrollPane();
        lpiResultsTable = new javax.swing.JTable();
        lpiConfirmButton = new javax.swing.JButton();
        lpiNotFoundButton = new javax.swing.JButton();
        reviewCard1 = new javax.swing.JPanel();
        reviewPanel1 = new javax.swing.JPanel();
        clinicIdLabel = new javax.swing.JLabel();
        clinicIdTextField = new javax.swing.JTextField();
        altClinicIdTextField = new javax.swing.JTextField();
        clinicIdAcceptRadioButton = new javax.swing.JRadioButton();
        clinicIdRejectRadioButton = new javax.swing.JRadioButton();
        firstNameLabel = new javax.swing.JLabel();
        firstNameTextField = new javax.swing.JTextField();
        altFirstNameTextField = new javax.swing.JTextField();
        firstNameAcceptRadioButton = new javax.swing.JRadioButton();
        firstNameRejectRadioButton = new javax.swing.JRadioButton();
        middleNameLabel = new javax.swing.JLabel();
        middleNameTextField = new javax.swing.JTextField();
        altMiddleNameTextField = new javax.swing.JTextField();
        middleNameAcceptRadioButton = new javax.swing.JRadioButton();
        middleNameRejectRadioButton = new javax.swing.JRadioButton();
        lastNameLabel = new javax.swing.JLabel();
        lastNameTextField = new javax.swing.JTextField();
        altLastNameTextField = new javax.swing.JTextField();
        lastNameAcceptRadioButton = new javax.swing.JRadioButton();
        lastNameRejectRadioButton = new javax.swing.JRadioButton();
        sexLabel = new javax.swing.JLabel();
        maleRadioButton = new javax.swing.JRadioButton();
        femaleRadioButton = new javax.swing.JRadioButton();
        altSexTextField = new javax.swing.JTextField();
        sexAcceptRadioButton = new javax.swing.JRadioButton();
        sexRejectRadioButton = new javax.swing.JRadioButton();
        birthDateLabel = new javax.swing.JLabel();
        birthDateChooser = new com.toedter.calendar.JDateChooser();
        unknownBirthdateCheckBox = new javax.swing.JCheckBox();
        altBirthDateTextField = new javax.swing.JTextField();
        birthDateAcceptRadioButton = new javax.swing.JRadioButton();
        birthDateRejectRadioButton = new javax.swing.JRadioButton();
        maritalStatusLabel = new javax.swing.JLabel();
        maritalStatusComboBox = new javax.swing.JComboBox();
        altMaritalStatusTextField = new javax.swing.JTextField();
        maritalStatusAcceptRadioButton = new javax.swing.JRadioButton();
        maritalStatusRejectRadioButton = new javax.swing.JRadioButton();
        reviewCard1NextButton = new javax.swing.JButton();
        otherNameLabel = new javax.swing.JLabel();
        otherNameTextField = new javax.swing.JTextField();
        altOtherNameTextField = new javax.swing.JTextField();
        otherNameAcceptRadioButton = new javax.swing.JRadioButton();
        otherNameRejectRadioButton = new javax.swing.JRadioButton();
        reviewCard2 = new javax.swing.JPanel();
        reviewPanel2 = new javax.swing.JPanel();
        clanLabel = new javax.swing.JLabel();
        clanTextField = new javax.swing.JTextField();
        altClanTextField = new javax.swing.JTextField();
        clanAcceptRadioButton = new javax.swing.JRadioButton();
        clanRejectRadioButton = new javax.swing.JRadioButton();
        villageLabel = new javax.swing.JLabel();
        villageTextField = new javax.swing.JTextField();
        altVillageTextField = new javax.swing.JTextField();
        villageAcceptRadioButton = new javax.swing.JRadioButton();
        villageRejectRadioButton = new javax.swing.JRadioButton();
        fathersFirstNameLabel = new javax.swing.JLabel();
        fathersFirstNameTextField = new javax.swing.JTextField();
        altFathersFirstNameTextField = new javax.swing.JTextField();
        fathersFirstNameAcceptRadioButton = new javax.swing.JRadioButton();
        fathersFirstNameRejectRadioButton = new javax.swing.JRadioButton();
        fathersMiddleNameLabel = new javax.swing.JLabel();
        fathersMiddleNameTextField = new javax.swing.JTextField();
        altFathersMiddleNameTextField = new javax.swing.JTextField();
        fathersMiddleNameAcceptRadioButton = new javax.swing.JRadioButton();
        fathersMiddleNameRejectRadioButton = new javax.swing.JRadioButton();
        fathersLastNameLabel = new javax.swing.JLabel();
        fathersLastNameTextField = new javax.swing.JTextField();
        altFathersLastNameTextField = new javax.swing.JTextField();
        fathersLastNameAcceptRadioButton = new javax.swing.JRadioButton();
        fathersLastNameRejectRadioButton = new javax.swing.JRadioButton();
        mothersFirstNameLabel = new javax.swing.JLabel();
        mothersFirstNameTextField = new javax.swing.JTextField();
        altMothersFirstNameTextField = new javax.swing.JTextField();
        mothersFirstNameAcceptRadioButton = new javax.swing.JRadioButton();
        mothersFirstNameRejectRadioButton = new javax.swing.JRadioButton();
        mothersMiddleNameLabel = new javax.swing.JLabel();
        mothersMiddleNameTextField = new javax.swing.JTextField();
        altMothersMiddleNameTextField = new javax.swing.JTextField();
        mothersMiddleNameAcceptRadioButton = new javax.swing.JRadioButton();
        mothersMiddleNameRejectRadioButton = new javax.swing.JRadioButton();
        mothersLastNameLabel = new javax.swing.JLabel();
        mothersLastNameTextField = new javax.swing.JTextField();
        altMothersLastNameTextField = new javax.swing.JTextField();
        mothersLastNameAcceptRadioButton = new javax.swing.JRadioButton();
        mothersLastNameRejectRadioButton = new javax.swing.JRadioButton();
        review2NextButton = new javax.swing.JButton();
        reviewCard3 = new javax.swing.JPanel();
        reviewPanel3 = new javax.swing.JPanel();
        compoundHeadsFirstNameLabel = new javax.swing.JLabel();
        compoundHeadsFirstNameTextField = new javax.swing.JTextField();
        altCompoundHeadsFirstNameTextField = new javax.swing.JTextField();
        compoundHeadsFirstNameAcceptRadioButton = new javax.swing.JRadioButton();
        compoundHeadsFirstNameRejectRadioButton = new javax.swing.JRadioButton();
        compoundHeadsMiddleNameLabel = new javax.swing.JLabel();
        compoundHeadsMiddleNameTextField = new javax.swing.JTextField();
        altCompoundHeadsMiddleNameTextField = new javax.swing.JTextField();
        compoundHeadsMiddleNameAcceptRadioButton = new javax.swing.JRadioButton();
        compoundHeadsMiddleNameRejectRadioButton = new javax.swing.JRadioButton();
        compoundHeadsLastNameLabel = new javax.swing.JLabel();
        compoundHeadsLastNameTextField = new javax.swing.JTextField();
        altCompoundHeadsLastNameTextField = new javax.swing.JTextField();
        compoundHeadsLastNameAcceptRadioButton = new javax.swing.JRadioButton();
        compoundHeadsLastNameRejectRadioButton = new javax.swing.JRadioButton();
        hdssDataConsentLabel = new javax.swing.JLabel();
        hdssDataConsentYesRadioButton = new javax.swing.JRadioButton();
        hdssDataConsentNoRadioButton = new javax.swing.JRadioButton();
        hdssDataConsentNoAnswerRadioButton = new javax.swing.JRadioButton();
        altHdssDataConsentTextField = new javax.swing.JTextField();
        hdssDataConsentAcceptRadioButton = new javax.swing.JRadioButton();
        hdssDataConsentRejectRadioButton = new javax.swing.JRadioButton();
        clientTypeLabel = new javax.swing.JLabel();
        enrolledRadioButton = new javax.swing.JRadioButton();
        visitorRadioButton = new javax.swing.JRadioButton();
        newRadioButton = new javax.swing.JRadioButton();
        transferInRadioButton = new javax.swing.JRadioButton();
        fingerprintLabel = new javax.swing.JLabel();
        fingerprintImagePanel = new ke.go.moh.oec.reception.gui.custom.ImagePanel();
        clientRefusesCheckBox = new javax.swing.JCheckBox();
        takeButton = new javax.swing.JButton();
        viewHouseholdButton = new javax.swing.JButton();
        finishButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        departmentsMenuItem = new javax.swing.JMenuItem();
        usersMenu = new javax.swing.JMenu();
        changePasswordMenuItem = new javax.swing.JMenuItem();
        manageUsersMenu = new javax.swing.JMenu();
        addUsersMenuItem = new javax.swing.JMenuItem();
        managePermissionsMenuItem = new javax.swing.JMenuItem();
        reserPasswordMenuItem = new javax.swing.JMenuItem();
        deleteUserMenuItem = new javax.swing.JMenuItem();
        themesMenu = new javax.swing.JMenu();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        extendedSearchSexButtonGroup = new javax.swing.ButtonGroup();
        mpiSearchResultList = new ArrayList<Person>();
        lpiSearchResultList = new ArrayList<Person>();
        altHdssDataConsentButtonGroup = new javax.swing.ButtonGroup();
        compoundHeadsLastNameButtonGroup = new javax.swing.ButtonGroup();
        clinicIdButtonGroup = new javax.swing.ButtonGroup();
        firstNameButtonGroup = new javax.swing.ButtonGroup();
        middleNameButtonGroup = new javax.swing.ButtonGroup();
        lastNameButtonGroup = new javax.swing.ButtonGroup();
        altReviewSexButtonGroup = new javax.swing.ButtonGroup();
        reviewSexButtonGroup = new javax.swing.ButtonGroup();
        birthDateButtonGroup = new javax.swing.ButtonGroup();
        maritalStatusButtonGroup = new javax.swing.ButtonGroup();
        villageButtonGroup = new javax.swing.ButtonGroup();
        fathersFirstNameButtonGroup = new javax.swing.ButtonGroup();
        fathersMiddleNameButtonGroup = new javax.swing.ButtonGroup();
        fathersLastNameButtonGroup = new javax.swing.ButtonGroup();
        mothersFirstNameButtonGroup = new javax.swing.ButtonGroup();
        mothersMiddleNameButtonGroup = new javax.swing.ButtonGroup();
        mothersLastNameButtonGroup = new javax.swing.ButtonGroup();
        compoundHeadsFirstNameButtonGroup = new javax.swing.ButtonGroup();
        compoundHeadsMiddleNameButtonGroup = new javax.swing.ButtonGroup();
        maritalStatusList = DisplayableMaritalStatus.getList();
        hdssDataConsentButtonGroup = new javax.swing.ButtonGroup();
        clanButtonGroup = new javax.swing.ButtonGroup();
        otherNameButtonGroup = new javax.swing.ButtonGroup();
        clientTypeButtonGroup = new javax.swing.ButtonGroup();

        mainPanel.setName("mainPanel"); // NOI18N

        mainSplitPane.setDividerLocation(250);
        mainSplitPane.setName("mainSplitPane"); // NOI18N

        leftPanel.setName("leftPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getResourceMap(MainView.class);
        notificationListPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("notificationListPanel.border.title"))); // NOI18N
        notificationListPanel.setName("notificationListPanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getActionMap(MainView.class, this);
        processNotificationButton.setAction(actionMap.get("processNotification")); // NOI18N
        processNotificationButton.setText(resourceMap.getString("processNotificationButton.text")); // NOI18N
        processNotificationButton.setName("processNotificationButton"); // NOI18N

        notificationScrollPane.setName("notificationScrollPane"); // NOI18N

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Notifications");
        notificationTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        notificationTree.setName("notificationTree"); // NOI18N
        notificationTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                notificationTreeMouseClicked(evt);
            }
        });
        notificationScrollPane.setViewportView(notificationTree);

        javax.swing.GroupLayout notificationListPanelLayout = new javax.swing.GroupLayout(notificationListPanel);
        notificationListPanel.setLayout(notificationListPanelLayout);
        notificationListPanelLayout.setHorizontalGroup(
            notificationListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notificationListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(notificationListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notificationScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                    .addComponent(processNotificationButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
                .addContainerGap())
        );
        notificationListPanelLayout.setVerticalGroup(
            notificationListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notificationListPanelLayout.createSequentialGroup()
                .addComponent(notificationScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processNotificationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(notificationListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(notificationListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainSplitPane.setLeftComponent(leftPanel);

        rightPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        rightPanel.setName("rightPanel"); // NOI18N

        homeButton.setAction(actionMap.get("goHome")); // NOI18N
        homeButton.setBackground(resourceMap.getColor("backButton.background")); // NOI18N
        homeButton.setText(resourceMap.getString("homeButton.text")); // NOI18N
        homeButton.setName("homeButton"); // NOI18N

        backButton.setAction(actionMap.get("goBack")); // NOI18N
        backButton.setBackground(resourceMap.getColor("backButton.background")); // NOI18N
        backButton.setText(resourceMap.getString("backButton.text")); // NOI18N
        backButton.setName("backButton"); // NOI18N

        loggedInLabel.setFont(resourceMap.getFont("loggedInLabel.font")); // NOI18N
        loggedInLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        loggedInLabel.setText(resourceMap.getString("loggedInLabel.text")); // NOI18N
        loggedInLabel.setName("loggedInLabel"); // NOI18N

        wizardPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        wizardPanel.setName("wizardPanel"); // NOI18N
        wizardPanel.setLayout(new java.awt.CardLayout());

        homeCard.setName("homeCard"); // NOI18N

        quickSearchPanel.setName("quickSearchPanel"); // NOI18N

        readerStatusLabel.setText(resourceMap.getString("readerStatusLabel.text")); // NOI18N
        readerStatusLabel.setName("readerStatusLabel"); // NOI18N

        quickSearchFingerprintImagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        quickSearchFingerprintImagePanel.setName("quickSearchFingerprintImagePanel"); // NOI18N

        javax.swing.GroupLayout quickSearchFingerprintImagePanelLayout = new javax.swing.GroupLayout(quickSearchFingerprintImagePanel);
        quickSearchFingerprintImagePanel.setLayout(quickSearchFingerprintImagePanelLayout);
        quickSearchFingerprintImagePanelLayout.setHorizontalGroup(
            quickSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 77, Short.MAX_VALUE)
        );
        quickSearchFingerprintImagePanelLayout.setVerticalGroup(
            quickSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 79, Short.MAX_VALUE)
        );

        quickSearchQualityTextField.setEditable(false);
        quickSearchQualityTextField.setText(resourceMap.getString("quickSearchQualityTextField.text")); // NOI18N
        quickSearchQualityTextField.setName("quickSearchQualityTextField"); // NOI18N

        clearRightIndexButton.setAction(actionMap.get("clearRightIndex")); // NOI18N
        clearRightIndexButton.setText(resourceMap.getString("clearRightIndexButton.text")); // NOI18N
        clearRightIndexButton.setName("clearRightIndexButton"); // NOI18N

        clearLeftIndexButton.setAction(actionMap.get("clearLeftIndex")); // NOI18N
        clearLeftIndexButton.setText(resourceMap.getString("clearLeftIndexButton.text")); // NOI18N
        clearLeftIndexButton.setName("clearLeftIndexButton"); // NOI18N

        resetQuickSearchButton.setAction(actionMap.get("resertQuickSearch")); // NOI18N
        resetQuickSearchButton.setText(resourceMap.getString("resetQuickSearchButton.text")); // NOI18N
        resetQuickSearchButton.setName("resetQuickSearchButton"); // NOI18N

        fingerToBeTakenLabel.setText(resourceMap.getString("fingerToBeTakenLabel.text")); // NOI18N
        fingerToBeTakenLabel.setName("fingerToBeTakenLabel"); // NOI18N

        quickSearchMessageLabel.setText(resourceMap.getString("quickSearchMessageLabel.text")); // NOI18N
        quickSearchMessageLabel.setName("quickSearchMessageLabel"); // NOI18N

        quickSearchButton.setAction(actionMap.get("quickSearch")); // NOI18N
        quickSearchButton.setText(resourceMap.getString("quickSearchButton.text")); // NOI18N
        quickSearchButton.setName("quickSearchButton"); // NOI18N

        javax.swing.GroupLayout quickSearchPanelLayout = new javax.swing.GroupLayout(quickSearchPanel);
        quickSearchPanel.setLayout(quickSearchPanelLayout);
        quickSearchPanelLayout.setHorizontalGroup(
            quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(readerStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                    .addGroup(quickSearchPanelLayout.createSequentialGroup()
                        .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(quickSearchQualityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(quickSearchFingerprintImagePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(quickSearchMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                            .addGroup(quickSearchPanelLayout.createSequentialGroup()
                                .addComponent(resetQuickSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(quickSearchButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, quickSearchPanelLayout.createSequentialGroup()
                                .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(clearLeftIndexButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                                    .addComponent(clearRightIndexButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fingerToBeTakenLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        quickSearchPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clearLeftIndexButton, clearRightIndexButton, resetQuickSearchButton});

        quickSearchPanelLayout.setVerticalGroup(
            quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(readerStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(quickSearchPanelLayout.createSequentialGroup()
                        .addComponent(clearRightIndexButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fingerToBeTakenLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                            .addComponent(clearLeftIndexButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(resetQuickSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(quickSearchButton)))
                    .addComponent(quickSearchFingerprintImagePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(quickSearchMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .addComponent(quickSearchQualityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        quickSearchPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {clearLeftIndexButton, clearRightIndexButton, resetQuickSearchButton});

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
                    .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(visitorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(enrolledButton, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(transferInButton, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
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
                .addContainerGap(103, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout homeCardLayout = new javax.swing.GroupLayout(homeCard);
        homeCard.setLayout(homeCardLayout);
        homeCardLayout.setHorizontalGroup(
            homeCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, homeCardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(homeCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(quickSearchPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(homePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        homeCardLayout.setVerticalGroup(
            homeCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homeCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(quickSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(homePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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
                    .addComponent(clinicIdNoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(clinicIdYesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
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
                .addContainerGap(359, Short.MAX_VALUE))
        );

        wizardPanel.add(clinicIdCard, "clinicIdCard");

        basicSearchCard.setName("basicSearchCard"); // NOI18N

        basicSearchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("basicSearchPanel.border.title"))); // NOI18N
        basicSearchPanel.setName("basicSearchPanel"); // NOI18N

        basicSearchClinicIdLabel.setText(resourceMap.getString("basicSearchClinicIdLabel.text")); // NOI18N
        basicSearchClinicIdLabel.setName("basicSearchClinicIdLabel"); // NOI18N

        basicSearchClinicIdTextField.setName("basicSearchClinicIdTextField"); // NOI18N
        basicSearchClinicIdTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                basicSearchClinicIdTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                basicSearchClinicIdTextFieldFocusLost(evt);
            }
        });
        basicSearchClinicIdTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                basicSearchClinicIdTextFieldKeyTyped(evt);
            }
        });

        basicSearchClinicNameLabel.setText(resourceMap.getString("basicSearchClinicNameLabel.text")); // NOI18N
        basicSearchClinicNameLabel.setName("basicSearchClinicNameLabel"); // NOI18N

        basicSearchClinicNameTextField.setName("basicSearchClinicNameTextField"); // NOI18N
        basicSearchClinicNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                basicSearchClinicNameTextFieldKeyTyped(evt);
            }
        });

        basicSearchFingerprintLabel.setText(resourceMap.getString("basicSearchFingerprintLabel.text")); // NOI18N
        basicSearchFingerprintLabel.setName("basicSearchFingerprintLabel"); // NOI18N

        basicSearchFingerprintImagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        basicSearchFingerprintImagePanel.setName("basicSearchFingerprintImagePanel"); // NOI18N

        javax.swing.GroupLayout basicSearchFingerprintImagePanelLayout = new javax.swing.GroupLayout(basicSearchFingerprintImagePanel);
        basicSearchFingerprintImagePanel.setLayout(basicSearchFingerprintImagePanelLayout);
        basicSearchFingerprintImagePanelLayout.setHorizontalGroup(
            basicSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 75, Short.MAX_VALUE)
        );
        basicSearchFingerprintImagePanelLayout.setVerticalGroup(
            basicSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 74, Short.MAX_VALUE)
        );

        basicSearchClientRefusesCheckBox.setAction(actionMap.get("refuseFingerprintingBasic")); // NOI18N
        basicSearchClientRefusesCheckBox.setText(resourceMap.getString("basicSearchClientRefusesCheckBox.text")); // NOI18N
        basicSearchClientRefusesCheckBox.setName("basicSearchClientRefusesCheckBox"); // NOI18N

        basicSearchTakeButton.setAction(actionMap.get("showFingerprintDialogBasic")); // NOI18N
        basicSearchTakeButton.setText(resourceMap.getString("basicSearchTakeButton.text")); // NOI18N
        basicSearchTakeButton.setName("basicSearchTakeButton"); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, basicSearchClientRefusesCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), basicSearchTakeButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        basicSearchButton.setAction(actionMap.get("searchBasic")); // NOI18N
        basicSearchButton.setText(resourceMap.getString("basicSearchButton.text")); // NOI18N
        basicSearchButton.setName("basicSearchButton"); // NOI18N

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(basicSearchClinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                            .addComponent(basicSearchClinicNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                            .addGroup(basicSearchPanelLayout.createSequentialGroup()
                                .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(basicSearchTakeButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(basicSearchFingerprintImagePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(basicSearchClientRefusesCheckBox))))
                    .addComponent(basicSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
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
                    .addGroup(basicSearchPanelLayout.createSequentialGroup()
                        .addGroup(basicSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(basicSearchFingerprintLabel)
                            .addComponent(basicSearchFingerprintImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(basicSearchTakeButton))
                    .addComponent(basicSearchClientRefusesCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addContainerGap(252, Short.MAX_VALUE))
        );

        wizardPanel.add(basicSearchCard, "basicSearchCard");

        extendedSearchCard.setName("extendedSearchCard"); // NOI18N

        extendedSearchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("extendedSearchPanel.border.title"))); // NOI18N
        extendedSearchPanel.setName("extendedSearchPanel"); // NOI18N

        extendedSearchClinicIdLabel.setText(resourceMap.getString("extendedSearchClinicIdLabel.text")); // NOI18N
        extendedSearchClinicIdLabel.setName("extendedSearchClinicIdLabel"); // NOI18N

        extendedSearchClinicIdTextField.setName("extendedSearchClinicIdTextField"); // NOI18N
        extendedSearchClinicIdTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                extendedSearchClinicIdTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                extendedSearchClinicIdTextFieldFocusLost(evt);
            }
        });
        extendedSearchClinicIdTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                extendedSearchClinicIdTextFieldKeyTyped(evt);
            }
        });

        extendedSearchClinicNameLabel.setText(resourceMap.getString("extendedSearchClinicNameLabel.text")); // NOI18N
        extendedSearchClinicNameLabel.setName("extendedSearchClinicNameLabel"); // NOI18N

        extendedSearchClinicNameTextField.setName("extendedSearchClinicNameTextField"); // NOI18N
        extendedSearchClinicNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                extendedSearchClinicNameTextFieldKeyTyped(evt);
            }
        });

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

        extendedSearchSexButtonGroup.add(extendedSearchMaleRadioButton);
        extendedSearchMaleRadioButton.setText(resourceMap.getString("extendedSearchMaleRadioButton.text")); // NOI18N
        extendedSearchMaleRadioButton.setName("extendedSearchMaleRadioButton"); // NOI18N

        extendedSearchSexButtonGroup.add(extendedSearchFemaleRadioButton);
        extendedSearchFemaleRadioButton.setText(resourceMap.getString("extendedSearchFemaleRadioButton.text")); // NOI18N
        extendedSearchFemaleRadioButton.setName("extendedSearchFemaleRadioButton"); // NOI18N

        extendedSearchBirthdateLabel.setText(resourceMap.getString("extendedSearchBirthdateLabel.text")); // NOI18N
        extendedSearchBirthdateLabel.setName("extendedSearchBirthdateLabel"); // NOI18N

        extendedSearchBirthdateChooser.setName("extendedSearchBirthdateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, extendedSearchUnknownBirthdateCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), extendedSearchBirthdateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        extendedSearchUnknownBirthdateCheckBox.setText(resourceMap.getString("extendedSearchUnknownBirthdateCheckBox.text")); // NOI18N
        extendedSearchUnknownBirthdateCheckBox.setName("extendedSearchUnknownBirthdateCheckBox"); // NOI18N

        extendedSearchOtherNameLabel.setText(resourceMap.getString("extendedSearchOtherNameLabel.text")); // NOI18N
        extendedSearchOtherNameLabel.setName("extendedSearchOtherNameLabel"); // NOI18N

        extendedSearchOtherNameTextField.setText(resourceMap.getString("extendedSearchOtherNameTextField.text")); // NOI18N
        extendedSearchOtherNameTextField.setName("extendedSearchOtherNameTextField"); // NOI18N

        extendedSearchClanNameLabel.setText(resourceMap.getString("extendedSearchClanNameLabel.text")); // NOI18N
        extendedSearchClanNameLabel.setName("extendedSearchClanNameLabel"); // NOI18N

        extendedSearchClanNameTextField.setText(resourceMap.getString("extendedSearchClanNameTextField.text")); // NOI18N
        extendedSearchClanNameTextField.setName("extendedSearchClanNameTextField"); // NOI18N

        extendedSearchVillageLabel.setText(resourceMap.getString("extendedSearchVillageLabel.text")); // NOI18N
        extendedSearchVillageLabel.setName("extendedSearchVillageLabel"); // NOI18N

        extendedSearchVillageTextField.setText(resourceMap.getString("extendedSearchVillageTextField.text")); // NOI18N
        extendedSearchVillageTextField.setName("extendedSearchVillageTextField"); // NOI18N

        extendedSearchFingerprintLabel.setText(resourceMap.getString("extendedSearchFingerprintLabel.text")); // NOI18N
        extendedSearchFingerprintLabel.setName("extendedSearchFingerprintLabel"); // NOI18N

        extendedSearchFingerprintImagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        extendedSearchFingerprintImagePanel.setName("extendedSearchFingerprintImagePanel"); // NOI18N

        javax.swing.GroupLayout extendedSearchFingerprintImagePanelLayout = new javax.swing.GroupLayout(extendedSearchFingerprintImagePanel);
        extendedSearchFingerprintImagePanel.setLayout(extendedSearchFingerprintImagePanelLayout);
        extendedSearchFingerprintImagePanelLayout.setHorizontalGroup(
            extendedSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 75, Short.MAX_VALUE)
        );
        extendedSearchFingerprintImagePanelLayout.setVerticalGroup(
            extendedSearchFingerprintImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 74, Short.MAX_VALUE)
        );

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

        javax.swing.GroupLayout extendedSearchPanelLayout = new javax.swing.GroupLayout(extendedSearchPanel);
        extendedSearchPanel.setLayout(extendedSearchPanelLayout);
        extendedSearchPanelLayout.setHorizontalGroup(
            extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(extendedSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                    .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                        .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(extendedSearchFingerprintLabel)
                            .addComponent(extendedSearchVillageLabel)
                            .addComponent(extendedSearchClinicNameLabel)
                            .addComponent(extendedSearchClinicIdLabel)
                            .addComponent(extendedSearchFirstNameLabel)
                            .addComponent(extendedSearchMiddleNameLabel)
                            .addComponent(extendedSearchLastNameLabel)
                            .addComponent(extendedSearchSexLabel)
                            .addComponent(extendedSearchBirthdateLabel)
                            .addComponent(extendedSearchOtherNameLabel)
                            .addComponent(extendedSearchClanNameLabel))
                        .addGap(5, 5, 5)
                        .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(extendedSearchLastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addComponent(extendedSearchMiddleNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addComponent(extendedSearchFirstNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addComponent(extendedSearchClinicNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addComponent(extendedSearchClinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addComponent(extendedSearchOtherNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                                .addComponent(extendedSearchMaleRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(extendedSearchFemaleRadioButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, extendedSearchPanelLayout.createSequentialGroup()
                                .addComponent(extendedSearchBirthdateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(extendedSearchUnknownBirthdateCheckBox))
                            .addComponent(extendedSearchClanNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addComponent(extendedSearchVillageTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                            .addGroup(extendedSearchPanelLayout.createSequentialGroup()
                                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(extendedSearchTakeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(extendedSearchFingerprintImagePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(extendedSearchClientRefusesCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE)))))
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
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(extendedSearchBirthdateLabel)
                    .addComponent(extendedSearchBirthdateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(extendedSearchUnknownBirthdateCheckBox, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(extendedSearchOtherNameLabel)
                    .addComponent(extendedSearchOtherNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchClanNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(extendedSearchClanNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extendedSearchVillageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(extendedSearchVillageLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extendedSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(extendedSearchFingerprintImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(extendedSearchClientRefusesCheckBox)
                    .addComponent(extendedSearchFingerprintLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(extendedSearchTakeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(extendedSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
                .addContainerGap(49, Short.MAX_VALUE))
        );

        wizardPanel.add(extendedSearchCard, "extendedSearchCard");

        mpiResultsCard.setName("mpiResultsCard"); // NOI18N

        mpiResultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("mpiResultsPanel.border.title"))); // NOI18N
        mpiResultsPanel.setName("mpiResultsPanel"); // NOI18N

        mpiResultsScrollPane.setName("mpiResultsScrollPane"); // NOI18N

        mpiResultsTable.setName("mpiResultsTable"); // NOI18N
        mpiResultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, mpiSearchResultList, mpiResultsTable, "mpiBinding");
        org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${matchScore}"));
        columnBinding.setColumnName("Match Score");
        columnBinding.setColumnClass(Integer.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${fingerprintMatched}"));
        columnBinding.setColumnName("Fingerprint Matched");
        columnBinding.setColumnClass(Boolean.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${firstName}"));
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
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        mpiResultsScrollPane.setViewportView(mpiResultsTable);

        mpiConfirmButton.setAction(actionMap.get("confirmMPIMatch")); // NOI18N
        mpiConfirmButton.setText(resourceMap.getString("mpiConfirmButton.text")); // NOI18N
        mpiConfirmButton.setName("mpiConfirmButton"); // NOI18N

        mpiNotFoundButton.setAction(actionMap.get("noMPIMatchFound")); // NOI18N
        mpiNotFoundButton.setText(resourceMap.getString("mpiNotFoundButton.text")); // NOI18N
        mpiNotFoundButton.setName("mpiNotFoundButton"); // NOI18N

        javax.swing.GroupLayout mpiResultsPanelLayout = new javax.swing.GroupLayout(mpiResultsPanel);
        mpiResultsPanel.setLayout(mpiResultsPanelLayout);
        mpiResultsPanelLayout.setHorizontalGroup(
            mpiResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mpiResultsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mpiResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mpiResultsScrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mpiNotFoundButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(mpiConfirmButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
                .addContainerGap())
        );
        mpiResultsPanelLayout.setVerticalGroup(
            mpiResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mpiResultsPanelLayout.createSequentialGroup()
                .addComponent(mpiResultsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mpiConfirmButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mpiNotFoundButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout mpiResultsCardLayout = new javax.swing.GroupLayout(mpiResultsCard);
        mpiResultsCard.setLayout(mpiResultsCardLayout);
        mpiResultsCardLayout.setHorizontalGroup(
            mpiResultsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mpiResultsCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mpiResultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        mpiResultsCardLayout.setVerticalGroup(
            mpiResultsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mpiResultsCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mpiResultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        wizardPanel.add(mpiResultsCard, "mpiResultsCard");

        lpiResultsCard.setName("lpiResultsCard"); // NOI18N

        lpiResultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("lpiResultsPanel.border.title"))); // NOI18N
        lpiResultsPanel.setName("lpiResultsPanel"); // NOI18N

        lpiResultsScrollPane.setName("lpiResultsScrollPane"); // NOI18N

        lpiResultsTable.setName("lpiResultsTable"); // NOI18N
        lpiResultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, lpiSearchResultList, lpiResultsTable, "lpiBinding");
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${matchScore}"));
        columnBinding.setColumnName("Match Score");
        columnBinding.setColumnClass(Integer.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${fingerprintMatched}"));
        columnBinding.setColumnName("Fingerprint Matched");
        columnBinding.setColumnClass(Boolean.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${firstName}"));
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
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        lpiResultsScrollPane.setViewportView(lpiResultsTable);

        lpiConfirmButton.setAction(actionMap.get("confirmLPIMatch")); // NOI18N
        lpiConfirmButton.setText(resourceMap.getString("lpiConfirmButton.text")); // NOI18N
        lpiConfirmButton.setName("lpiConfirmButton"); // NOI18N

        lpiNotFoundButton.setAction(actionMap.get("noLPIMatchFound")); // NOI18N
        lpiNotFoundButton.setText(resourceMap.getString("lpiNotFoundButton.text")); // NOI18N
        lpiNotFoundButton.setName("lpiNotFoundButton"); // NOI18N

        javax.swing.GroupLayout lpiResultsPanelLayout = new javax.swing.GroupLayout(lpiResultsPanel);
        lpiResultsPanel.setLayout(lpiResultsPanelLayout);
        lpiResultsPanelLayout.setHorizontalGroup(
            lpiResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, lpiResultsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lpiResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lpiResultsScrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lpiNotFoundButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(lpiConfirmButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
                .addContainerGap())
        );
        lpiResultsPanelLayout.setVerticalGroup(
            lpiResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, lpiResultsPanelLayout.createSequentialGroup()
                .addComponent(lpiResultsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lpiConfirmButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lpiNotFoundButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout lpiResultsCardLayout = new javax.swing.GroupLayout(lpiResultsCard);
        lpiResultsCard.setLayout(lpiResultsCardLayout);
        lpiResultsCardLayout.setHorizontalGroup(
            lpiResultsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lpiResultsCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lpiResultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        lpiResultsCardLayout.setVerticalGroup(
            lpiResultsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lpiResultsCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lpiResultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        wizardPanel.add(lpiResultsCard, "lpiResultsCard");

        reviewCard1.setName("reviewCard1"); // NOI18N

        reviewPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        reviewPanel1.setName("reviewPanel1"); // NOI18N

        clinicIdLabel.setText(resourceMap.getString("clinicIdLabel.text")); // NOI18N
        clinicIdLabel.setName("clinicIdLabel"); // NOI18N

        clinicIdTextField.setText(resourceMap.getString("clinicIdTextField.text")); // NOI18N
        clinicIdTextField.setName("clinicIdTextField"); // NOI18N
        clinicIdTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                clinicIdTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                clinicIdTextFieldFocusLost(evt);
            }
        });

        altClinicIdTextField.setEditable(false);
        altClinicIdTextField.setForeground(resourceMap.getColor("altClinicIdTextField.foreground")); // NOI18N
        altClinicIdTextField.setText(resourceMap.getString("altClinicIdTextField.text")); // NOI18N
        altClinicIdTextField.setName("altClinicIdTextField"); // NOI18N

        clinicIdAcceptRadioButton.setAction(actionMap.get("confirmClinicId")); // NOI18N
        clinicIdButtonGroup.add(clinicIdAcceptRadioButton);
        clinicIdAcceptRadioButton.setIcon(resourceMap.getIcon("clinicIdAcceptRadioButton.icon")); // NOI18N
        clinicIdAcceptRadioButton.setName("clinicIdAcceptRadioButton"); // NOI18N
        clinicIdAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("clinicIdAcceptRadioButton.selectedIcon")); // NOI18N

        clinicIdRejectRadioButton.setAction(actionMap.get("confirmClinicId")); // NOI18N
        clinicIdButtonGroup.add(clinicIdRejectRadioButton);
        clinicIdRejectRadioButton.setIcon(resourceMap.getIcon("clinicIdRejectRadioButton.icon")); // NOI18N
        clinicIdRejectRadioButton.setName("clinicIdRejectRadioButton"); // NOI18N
        clinicIdRejectRadioButton.setSelectedIcon(resourceMap.getIcon("clinicIdRejectRadioButton.selectedIcon")); // NOI18N

        firstNameLabel.setText(resourceMap.getString("firstNameLabel.text")); // NOI18N
        firstNameLabel.setName("firstNameLabel"); // NOI18N

        firstNameTextField.setName("firstNameTextField"); // NOI18N

        altFirstNameTextField.setEditable(false);
        altFirstNameTextField.setForeground(resourceMap.getColor("altClinicIdTextField.foreground")); // NOI18N
        altFirstNameTextField.setName("altFirstNameTextField"); // NOI18N

        firstNameAcceptRadioButton.setAction(actionMap.get("confirmFirstName")); // NOI18N
        firstNameButtonGroup.add(firstNameAcceptRadioButton);
        firstNameAcceptRadioButton.setIcon(resourceMap.getIcon("firstNameAcceptRadioButton.icon")); // NOI18N
        firstNameAcceptRadioButton.setName("firstNameAcceptRadioButton"); // NOI18N
        firstNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("firstNameAcceptRadioButton.selectedIcon")); // NOI18N

        firstNameRejectRadioButton.setAction(actionMap.get("confirmFirstName")); // NOI18N
        firstNameButtonGroup.add(firstNameRejectRadioButton);
        firstNameRejectRadioButton.setIcon(resourceMap.getIcon("firstNameRejectRadioButton.icon")); // NOI18N
        firstNameRejectRadioButton.setName("firstNameRejectRadioButton"); // NOI18N
        firstNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("firstNameRejectRadioButton.selectedIcon")); // NOI18N

        middleNameLabel.setText(resourceMap.getString("middleNameLabel.text")); // NOI18N
        middleNameLabel.setName("middleNameLabel"); // NOI18N

        middleNameTextField.setName("middleNameTextField"); // NOI18N

        altMiddleNameTextField.setEditable(false);
        altMiddleNameTextField.setForeground(resourceMap.getColor("altClinicIdTextField.foreground")); // NOI18N
        altMiddleNameTextField.setName("altMiddleNameTextField"); // NOI18N

        middleNameAcceptRadioButton.setAction(actionMap.get("confirmMiddleName")); // NOI18N
        middleNameButtonGroup.add(middleNameAcceptRadioButton);
        middleNameAcceptRadioButton.setIcon(resourceMap.getIcon("middleNameAcceptRadioButton.icon")); // NOI18N
        middleNameAcceptRadioButton.setName("middleNameAcceptRadioButton"); // NOI18N
        middleNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("middleNameAcceptRadioButton.selectedIcon")); // NOI18N

        middleNameRejectRadioButton.setAction(actionMap.get("confirmMiddleName")); // NOI18N
        middleNameButtonGroup.add(middleNameRejectRadioButton);
        middleNameRejectRadioButton.setIcon(resourceMap.getIcon("middleNameRejectRadioButton.icon")); // NOI18N
        middleNameRejectRadioButton.setName("middleNameRejectRadioButton"); // NOI18N
        middleNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("middleNameRejectRadioButton.selectedIcon")); // NOI18N

        lastNameLabel.setText(resourceMap.getString("lastNameLabel.text")); // NOI18N
        lastNameLabel.setName("lastNameLabel"); // NOI18N

        lastNameTextField.setName("lastNameTextField"); // NOI18N

        altLastNameTextField.setEditable(false);
        altLastNameTextField.setForeground(resourceMap.getColor("altClinicIdTextField.foreground")); // NOI18N
        altLastNameTextField.setName("altLastNameTextField"); // NOI18N

        lastNameAcceptRadioButton.setAction(actionMap.get("confirmLastName")); // NOI18N
        lastNameButtonGroup.add(lastNameAcceptRadioButton);
        lastNameAcceptRadioButton.setIcon(resourceMap.getIcon("lastNameAcceptRadioButton.icon")); // NOI18N
        lastNameAcceptRadioButton.setName("lastNameAcceptRadioButton"); // NOI18N
        lastNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("lastNameAcceptRadioButton.selectedIcon")); // NOI18N

        lastNameRejectRadioButton.setAction(actionMap.get("confirmLastName")); // NOI18N
        lastNameButtonGroup.add(lastNameRejectRadioButton);
        lastNameRejectRadioButton.setIcon(resourceMap.getIcon("lastNameRejectRadioButton.icon")); // NOI18N
        lastNameRejectRadioButton.setName("lastNameRejectRadioButton"); // NOI18N
        lastNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("lastNameRejectRadioButton.selectedIcon")); // NOI18N

        sexLabel.setText(resourceMap.getString("sexLabel.text")); // NOI18N
        sexLabel.setName("sexLabel"); // NOI18N

        reviewSexButtonGroup.add(maleRadioButton);
        maleRadioButton.setText(resourceMap.getString("maleRadioButton.text")); // NOI18N
        maleRadioButton.setName("maleRadioButton"); // NOI18N

        reviewSexButtonGroup.add(femaleRadioButton);
        femaleRadioButton.setText(resourceMap.getString("femaleRadioButton.text")); // NOI18N
        femaleRadioButton.setName("femaleRadioButton"); // NOI18N

        altSexTextField.setEditable(false);
        altSexTextField.setForeground(resourceMap.getColor("altClinicIdTextField.foreground")); // NOI18N
        altSexTextField.setText(resourceMap.getString("altSexTextField.text")); // NOI18N
        altSexTextField.setName("altSexTextField"); // NOI18N

        sexAcceptRadioButton.setAction(actionMap.get("confirmSex")); // NOI18N
        altReviewSexButtonGroup.add(sexAcceptRadioButton);
        sexAcceptRadioButton.setIcon(resourceMap.getIcon("sexAcceptRadioButton.icon")); // NOI18N
        sexAcceptRadioButton.setName("sexAcceptRadioButton"); // NOI18N
        sexAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("sexAcceptRadioButton.selectedIcon")); // NOI18N

        sexRejectRadioButton.setAction(actionMap.get("confirmSex")); // NOI18N
        altReviewSexButtonGroup.add(sexRejectRadioButton);
        sexRejectRadioButton.setIcon(resourceMap.getIcon("sexRejectRadioButton.icon")); // NOI18N
        sexRejectRadioButton.setName("sexRejectRadioButton"); // NOI18N
        sexRejectRadioButton.setSelectedIcon(resourceMap.getIcon("sexRejectRadioButton.selectedIcon")); // NOI18N

        birthDateLabel.setText(resourceMap.getString("birthDateLabel.text")); // NOI18N
        birthDateLabel.setName("birthDateLabel"); // NOI18N

        birthDateChooser.setName("birthDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, unknownBirthdateCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), birthDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        unknownBirthdateCheckBox.setText(resourceMap.getString("unknownBirthdateCheckBox.text")); // NOI18N
        unknownBirthdateCheckBox.setName("unknownBirthdateCheckBox"); // NOI18N

        altBirthDateTextField.setEditable(false);
        altBirthDateTextField.setForeground(resourceMap.getColor("altClinicIdTextField.foreground")); // NOI18N
        altBirthDateTextField.setText(resourceMap.getString("altBirthDateTextField.text")); // NOI18N
        altBirthDateTextField.setName("altBirthDateTextField"); // NOI18N

        birthDateAcceptRadioButton.setAction(actionMap.get("confirmBirthdate")); // NOI18N
        birthDateButtonGroup.add(birthDateAcceptRadioButton);
        birthDateAcceptRadioButton.setIcon(resourceMap.getIcon("birthDateAcceptRadioButton.icon")); // NOI18N
        birthDateAcceptRadioButton.setName("birthDateAcceptRadioButton"); // NOI18N
        birthDateAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("birthDateAcceptRadioButton.selectedIcon")); // NOI18N

        birthDateRejectRadioButton.setAction(actionMap.get("confirmBirthdate")); // NOI18N
        birthDateButtonGroup.add(birthDateRejectRadioButton);
        birthDateRejectRadioButton.setIcon(resourceMap.getIcon("birthDateRejectRadioButton.icon")); // NOI18N
        birthDateRejectRadioButton.setName("birthDateRejectRadioButton"); // NOI18N
        birthDateRejectRadioButton.setSelectedIcon(resourceMap.getIcon("birthDateRejectRadioButton.selectedIcon")); // NOI18N

        maritalStatusLabel.setText(resourceMap.getString("maritalStatusLabel.text")); // NOI18N
        maritalStatusLabel.setName("maritalStatusLabel"); // NOI18N

        maritalStatusComboBox.setName("maritalStatusComboBox"); // NOI18N

        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, maritalStatusList, maritalStatusComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        altMaritalStatusTextField.setEditable(false);
        altMaritalStatusTextField.setForeground(resourceMap.getColor("altClinicIdTextField.foreground")); // NOI18N
        altMaritalStatusTextField.setText(resourceMap.getString("altMaritalStatusTextField.text")); // NOI18N
        altMaritalStatusTextField.setName("altMaritalStatusTextField"); // NOI18N

        maritalStatusAcceptRadioButton.setAction(actionMap.get("confirmMaritalStatus")); // NOI18N
        maritalStatusButtonGroup.add(maritalStatusAcceptRadioButton);
        maritalStatusAcceptRadioButton.setIcon(resourceMap.getIcon("maritalStatusAcceptRadioButton.icon")); // NOI18N
        maritalStatusAcceptRadioButton.setName("maritalStatusAcceptRadioButton"); // NOI18N
        maritalStatusAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("maritalStatusAcceptRadioButton.selectedIcon")); // NOI18N

        maritalStatusRejectRadioButton.setAction(actionMap.get("confirmMaritalStatus")); // NOI18N
        maritalStatusButtonGroup.add(maritalStatusRejectRadioButton);
        maritalStatusRejectRadioButton.setIcon(resourceMap.getIcon("maritalStatusRejectRadioButton.icon")); // NOI18N
        maritalStatusRejectRadioButton.setName("maritalStatusRejectRadioButton"); // NOI18N
        maritalStatusRejectRadioButton.setSelectedIcon(resourceMap.getIcon("maritalStatusRejectRadioButton.selectedIcon")); // NOI18N

        reviewCard1NextButton.setAction(actionMap.get("showReviewCard2")); // NOI18N
        reviewCard1NextButton.setText(resourceMap.getString("reviewCard1NextButton.text")); // NOI18N
        reviewCard1NextButton.setName("reviewCard1NextButton"); // NOI18N

        otherNameLabel.setText(resourceMap.getString("otherNameLabel.text")); // NOI18N
        otherNameLabel.setName("otherNameLabel"); // NOI18N

        otherNameTextField.setName("otherNameTextField"); // NOI18N

        altOtherNameTextField.setEditable(false);
        altOtherNameTextField.setForeground(resourceMap.getColor("altOtherNameTextField.foreground")); // NOI18N
        altOtherNameTextField.setName("altOtherNameTextField"); // NOI18N

        otherNameAcceptRadioButton.setAction(actionMap.get("confirmOtherName")); // NOI18N
        otherNameButtonGroup.add(otherNameAcceptRadioButton);
        otherNameAcceptRadioButton.setIcon(resourceMap.getIcon("otherNameAcceptRadioButton.icon")); // NOI18N
        otherNameAcceptRadioButton.setName("otherNameAcceptRadioButton"); // NOI18N
        otherNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("otherNameAcceptRadioButton.selectedIcon")); // NOI18N

        otherNameRejectRadioButton.setAction(actionMap.get("confirmOtherName")); // NOI18N
        otherNameButtonGroup.add(otherNameRejectRadioButton);
        otherNameRejectRadioButton.setIcon(resourceMap.getIcon("otherNameRejectRadioButton.icon")); // NOI18N
        otherNameRejectRadioButton.setName("otherNameRejectRadioButton"); // NOI18N
        otherNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("otherNameRejectRadioButton.selectedIcon")); // NOI18N

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
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(firstNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(firstNameRejectRadioButton))
                            .addGroup(reviewPanel1Layout.createSequentialGroup()
                                .addComponent(maleRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(femaleRadioButton))
                            .addComponent(lastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(middleNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(middleNameRejectRadioButton))
                            .addComponent(middleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addGroup(reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lastNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lastNameRejectRadioButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altSexTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sexAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sexRejectRadioButton))))
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(firstNameLabel)
                            .addComponent(clinicIdLabel))
                        .addGap(18, 18, 18)
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(clinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altClinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clinicIdAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clinicIdRejectRadioButton))
                            .addComponent(firstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)))
                    .addComponent(reviewCard1NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(birthDateLabel)
                            .addComponent(maritalStatusLabel)
                            .addComponent(otherNameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altOtherNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(otherNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(otherNameRejectRadioButton))
                            .addComponent(otherNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altBirthDateTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(birthDateAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(birthDateRejectRadioButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(altMaritalStatusTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maritalStatusAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maritalStatusRejectRadioButton))
                            .addComponent(maritalStatusComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 391, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel1Layout.createSequentialGroup()
                                .addComponent(birthDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(unknownBirthdateCheckBox)))))
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
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(clinicIdRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(clinicIdAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altClinicIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firstNameLabel)
                    .addComponent(firstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(firstNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(firstNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(middleNameLabel)
                    .addComponent(middleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(middleNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(middleNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastNameLabel)
                    .addComponent(lastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lastNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(lastNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maleRadioButton)
                    .addComponent(sexLabel)
                    .addComponent(femaleRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(sexRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(sexAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altSexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(birthDateLabel))
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, reviewPanel1Layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(unknownBirthdateCheckBox, 0, 0, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, reviewPanel1Layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addComponent(birthDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(7, 7, 7)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(birthDateRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(birthDateAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altBirthDateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maritalStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maritalStatusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(maritalStatusRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(maritalStatusAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altMaritalStatusTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(otherNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(otherNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(otherNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(otherNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altOtherNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(reviewCard1NextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout reviewCard1Layout = new javax.swing.GroupLayout(reviewCard1);
        reviewCard1.setLayout(reviewCard1Layout);
        reviewCard1Layout.setHorizontalGroup(
            reviewCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewCard1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reviewPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        reviewCard1Layout.setVerticalGroup(
            reviewCard1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewCard1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reviewPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        wizardPanel.add(reviewCard1, "reviewCard1");

        reviewCard2.setName("reviewCard2"); // NOI18N

        reviewPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        reviewPanel2.setName("reviewPanel2"); // NOI18N

        clanLabel.setText(resourceMap.getString("clanLabel.text")); // NOI18N
        clanLabel.setName("clanLabel"); // NOI18N

        clanTextField.setName("clanTextField"); // NOI18N

        altClanTextField.setEditable(false);
        altClanTextField.setForeground(resourceMap.getColor("altClanTextField.foreground")); // NOI18N
        altClanTextField.setName("altClanTextField"); // NOI18N

        clanAcceptRadioButton.setAction(actionMap.get("confirmClanName")); // NOI18N
        clanButtonGroup.add(clanAcceptRadioButton);
        clanAcceptRadioButton.setIcon(resourceMap.getIcon("clanAcceptRadioButton.icon")); // NOI18N
        clanAcceptRadioButton.setName("clanAcceptRadioButton"); // NOI18N
        clanAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("clanAcceptRadioButton.selectedIcon")); // NOI18N

        clanRejectRadioButton.setAction(actionMap.get("confirmClanName")); // NOI18N
        clanButtonGroup.add(clanRejectRadioButton);
        clanRejectRadioButton.setIcon(resourceMap.getIcon("clanRejectRadioButton.icon")); // NOI18N
        clanRejectRadioButton.setName("clanRejectRadioButton"); // NOI18N
        clanRejectRadioButton.setSelectedIcon(resourceMap.getIcon("clanRejectRadioButton.selectedIcon")); // NOI18N

        villageLabel.setText(resourceMap.getString("villageLabel.text")); // NOI18N
        villageLabel.setName("villageLabel"); // NOI18N

        villageTextField.setName("villageTextField"); // NOI18N

        altVillageTextField.setEditable(false);
        altVillageTextField.setForeground(resourceMap.getColor("altClinicIdTextField.foreground")); // NOI18N
        altVillageTextField.setName("altVillageTextField"); // NOI18N

        villageAcceptRadioButton.setAction(actionMap.get("confirmVillageName")); // NOI18N
        villageButtonGroup.add(villageAcceptRadioButton);
        villageAcceptRadioButton.setIcon(resourceMap.getIcon("villageAcceptRadioButton.icon")); // NOI18N
        villageAcceptRadioButton.setName("villageAcceptRadioButton"); // NOI18N
        villageAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("villageAcceptRadioButton.selectedIcon")); // NOI18N

        villageRejectRadioButton.setAction(actionMap.get("confirmVillageName")); // NOI18N
        villageButtonGroup.add(villageRejectRadioButton);
        villageRejectRadioButton.setIcon(resourceMap.getIcon("villageRejectRadioButton.icon")); // NOI18N
        villageRejectRadioButton.setName("villageRejectRadioButton"); // NOI18N
        villageRejectRadioButton.setSelectedIcon(resourceMap.getIcon("villageRejectRadioButton.selectedIcon")); // NOI18N

        fathersFirstNameLabel.setText(resourceMap.getString("fathersFirstNameLabel.text")); // NOI18N
        fathersFirstNameLabel.setName("fathersFirstNameLabel"); // NOI18N

        fathersFirstNameTextField.setName("fathersFirstNameTextField"); // NOI18N

        altFathersFirstNameTextField.setEditable(false);
        altFathersFirstNameTextField.setForeground(resourceMap.getColor("altFathersFirstNameTextField.foreground")); // NOI18N
        altFathersFirstNameTextField.setName("altFathersFirstNameTextField"); // NOI18N

        fathersFirstNameAcceptRadioButton.setAction(actionMap.get("confirmFathersFirstName")); // NOI18N
        fathersFirstNameButtonGroup.add(fathersFirstNameAcceptRadioButton);
        fathersFirstNameAcceptRadioButton.setIcon(resourceMap.getIcon("fathersFirstNameAcceptRadioButton.icon")); // NOI18N
        fathersFirstNameAcceptRadioButton.setName("fathersFirstNameAcceptRadioButton"); // NOI18N
        fathersFirstNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("fathersFirstNameAcceptRadioButton.selectedIcon")); // NOI18N

        fathersFirstNameRejectRadioButton.setAction(actionMap.get("confirmFathersFirstName")); // NOI18N
        fathersFirstNameButtonGroup.add(fathersFirstNameRejectRadioButton);
        fathersFirstNameRejectRadioButton.setIcon(resourceMap.getIcon("fathersFirstNameRejectRadioButton.icon")); // NOI18N
        fathersFirstNameRejectRadioButton.setName("fathersFirstNameRejectRadioButton"); // NOI18N
        fathersFirstNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("fathersFirstNameRejectRadioButton.selectedIcon")); // NOI18N

        fathersMiddleNameLabel.setText(resourceMap.getString("fathersMiddleNameLabel.text")); // NOI18N
        fathersMiddleNameLabel.setName("fathersMiddleNameLabel"); // NOI18N

        fathersMiddleNameTextField.setName("fathersMiddleNameTextField"); // NOI18N

        altFathersMiddleNameTextField.setEditable(false);
        altFathersMiddleNameTextField.setForeground(resourceMap.getColor("altFathersMiddleNameTextField.foreground")); // NOI18N
        altFathersMiddleNameTextField.setName("altFathersMiddleNameTextField"); // NOI18N

        fathersMiddleNameAcceptRadioButton.setAction(actionMap.get("confirmFathersMiddleName")); // NOI18N
        fathersMiddleNameButtonGroup.add(fathersMiddleNameAcceptRadioButton);
        fathersMiddleNameAcceptRadioButton.setIcon(resourceMap.getIcon("fathersMiddleNameAcceptRadioButton.icon")); // NOI18N
        fathersMiddleNameAcceptRadioButton.setName("fathersMiddleNameAcceptRadioButton"); // NOI18N
        fathersMiddleNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("fathersMiddleNameAcceptRadioButton.selectedIcon")); // NOI18N

        fathersMiddleNameRejectRadioButton.setAction(actionMap.get("confirmFathersMiddleName")); // NOI18N
        fathersMiddleNameButtonGroup.add(fathersMiddleNameRejectRadioButton);
        fathersMiddleNameRejectRadioButton.setIcon(resourceMap.getIcon("fathersMiddleNameRejectRadioButton.icon")); // NOI18N
        fathersMiddleNameRejectRadioButton.setName("fathersMiddleNameRejectRadioButton"); // NOI18N
        fathersMiddleNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("fathersMiddleNameRejectRadioButton.selectedIcon")); // NOI18N

        fathersLastNameLabel.setText(resourceMap.getString("fathersLastNameLabel.text")); // NOI18N
        fathersLastNameLabel.setName("fathersLastNameLabel"); // NOI18N

        fathersLastNameTextField.setName("fathersLastNameTextField"); // NOI18N

        altFathersLastNameTextField.setEditable(false);
        altFathersLastNameTextField.setForeground(resourceMap.getColor("altFathersLastNameTextField.foreground")); // NOI18N
        altFathersLastNameTextField.setName("altFathersLastNameTextField"); // NOI18N

        fathersLastNameAcceptRadioButton.setAction(actionMap.get("confirmFathersLastName")); // NOI18N
        fathersLastNameButtonGroup.add(fathersLastNameAcceptRadioButton);
        fathersLastNameAcceptRadioButton.setIcon(resourceMap.getIcon("fathersLastNameAcceptRadioButton.icon")); // NOI18N
        fathersLastNameAcceptRadioButton.setName("fathersLastNameAcceptRadioButton"); // NOI18N
        fathersLastNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("fathersLastNameAcceptRadioButton.selectedIcon")); // NOI18N

        fathersLastNameRejectRadioButton.setAction(actionMap.get("confirmFathersLastName")); // NOI18N
        fathersLastNameButtonGroup.add(fathersLastNameRejectRadioButton);
        fathersLastNameRejectRadioButton.setIcon(resourceMap.getIcon("fathersLastNameRejectRadioButton.icon")); // NOI18N
        fathersLastNameRejectRadioButton.setName("fathersLastNameRejectRadioButton"); // NOI18N
        fathersLastNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("fathersLastNameRejectRadioButton.selectedIcon")); // NOI18N

        mothersFirstNameLabel.setText(resourceMap.getString("mothersFirstNameLabel.text")); // NOI18N
        mothersFirstNameLabel.setName("mothersFirstNameLabel"); // NOI18N

        mothersFirstNameTextField.setName("mothersFirstNameTextField"); // NOI18N

        altMothersFirstNameTextField.setEditable(false);
        altMothersFirstNameTextField.setForeground(resourceMap.getColor("altMothersFirstNameTextField.foreground")); // NOI18N
        altMothersFirstNameTextField.setName("altMothersFirstNameTextField"); // NOI18N

        mothersFirstNameAcceptRadioButton.setAction(actionMap.get("confirmMothersFirstName")); // NOI18N
        mothersFirstNameButtonGroup.add(mothersFirstNameAcceptRadioButton);
        mothersFirstNameAcceptRadioButton.setIcon(resourceMap.getIcon("mothersFirstNameAcceptRadioButton.icon")); // NOI18N
        mothersFirstNameAcceptRadioButton.setName("mothersFirstNameAcceptRadioButton"); // NOI18N
        mothersFirstNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("mothersFirstNameAcceptRadioButton.selectedIcon")); // NOI18N

        mothersFirstNameRejectRadioButton.setAction(actionMap.get("confirmMothersFirstName")); // NOI18N
        mothersFirstNameButtonGroup.add(mothersFirstNameRejectRadioButton);
        mothersFirstNameRejectRadioButton.setIcon(resourceMap.getIcon("mothersFirstNameRejectRadioButton.icon")); // NOI18N
        mothersFirstNameRejectRadioButton.setName("mothersFirstNameRejectRadioButton"); // NOI18N
        mothersFirstNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("mothersFirstNameRejectRadioButton.selectedIcon")); // NOI18N

        mothersMiddleNameLabel.setText(resourceMap.getString("mothersMiddleNameLabel.text")); // NOI18N
        mothersMiddleNameLabel.setName("mothersMiddleNameLabel"); // NOI18N

        mothersMiddleNameTextField.setName("mothersMiddleNameTextField"); // NOI18N

        altMothersMiddleNameTextField.setEditable(false);
        altMothersMiddleNameTextField.setForeground(resourceMap.getColor("altMothersMiddleNameTextField.foreground")); // NOI18N
        altMothersMiddleNameTextField.setName("altMothersMiddleNameTextField"); // NOI18N

        mothersMiddleNameAcceptRadioButton.setAction(actionMap.get("confirmMothersMiddleName")); // NOI18N
        mothersMiddleNameButtonGroup.add(mothersMiddleNameAcceptRadioButton);
        mothersMiddleNameAcceptRadioButton.setIcon(resourceMap.getIcon("mothersMiddleNameAcceptRadioButton.icon")); // NOI18N
        mothersMiddleNameAcceptRadioButton.setName("mothersMiddleNameAcceptRadioButton"); // NOI18N
        mothersMiddleNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("mothersMiddleNameAcceptRadioButton.selectedIcon")); // NOI18N

        mothersMiddleNameRejectRadioButton.setAction(actionMap.get("confirmMothersMiddleName")); // NOI18N
        mothersMiddleNameButtonGroup.add(mothersMiddleNameRejectRadioButton);
        mothersMiddleNameRejectRadioButton.setIcon(resourceMap.getIcon("mothersMiddleNameRejectRadioButton.icon")); // NOI18N
        mothersMiddleNameRejectRadioButton.setName("mothersMiddleNameRejectRadioButton"); // NOI18N
        mothersMiddleNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("mothersMiddleNameRejectRadioButton.selectedIcon")); // NOI18N

        mothersLastNameLabel.setText(resourceMap.getString("mothersLastNameLabel.text")); // NOI18N
        mothersLastNameLabel.setName("mothersLastNameLabel"); // NOI18N

        mothersLastNameTextField.setName("mothersLastNameTextField"); // NOI18N

        altMothersLastNameTextField.setEditable(false);
        altMothersLastNameTextField.setForeground(resourceMap.getColor("altMothersLastNameTextField.foreground")); // NOI18N
        altMothersLastNameTextField.setName("altMothersLastNameTextField"); // NOI18N

        mothersLastNameAcceptRadioButton.setAction(actionMap.get("confirmMothersLastName")); // NOI18N
        mothersLastNameButtonGroup.add(mothersLastNameAcceptRadioButton);
        mothersLastNameAcceptRadioButton.setIcon(resourceMap.getIcon("mothersLastNameAcceptRadioButton.icon")); // NOI18N
        mothersLastNameAcceptRadioButton.setName("mothersLastNameAcceptRadioButton"); // NOI18N
        mothersLastNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("mothersLastNameAcceptRadioButton.selectedIcon")); // NOI18N

        mothersLastNameRejectRadioButton.setAction(actionMap.get("confirmMothersLastName")); // NOI18N
        mothersLastNameButtonGroup.add(mothersLastNameRejectRadioButton);
        mothersLastNameRejectRadioButton.setIcon(resourceMap.getIcon("mothersLastNameRejectRadioButton.icon")); // NOI18N
        mothersLastNameRejectRadioButton.setName("mothersLastNameRejectRadioButton"); // NOI18N
        mothersLastNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("mothersLastNameRejectRadioButton.selectedIcon")); // NOI18N

        review2NextButton.setAction(actionMap.get("showReviewCard3")); // NOI18N
        review2NextButton.setText(resourceMap.getString("review2NextButton.text")); // NOI18N
        review2NextButton.setName("review2NextButton"); // NOI18N

        javax.swing.GroupLayout reviewPanel2Layout = new javax.swing.GroupLayout(reviewPanel2);
        reviewPanel2.setLayout(reviewPanel2Layout);
        reviewPanel2Layout.setHorizontalGroup(
            reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(review2NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addGroup(reviewPanel2Layout.createSequentialGroup()
                        .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mothersLastNameLabel)
                            .addComponent(mothersMiddleNameLabel)
                            .addComponent(fathersLastNameLabel)
                            .addComponent(mothersFirstNameLabel)
                            .addComponent(fathersMiddleNameLabel)
                            .addComponent(fathersFirstNameLabel)
                            .addComponent(villageLabel)
                            .addComponent(clanLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(villageTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                            .addGroup(reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altClanTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clanAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clanRejectRadioButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altFathersFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersFirstNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersFirstNameRejectRadioButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altMothersFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersFirstNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersFirstNameRejectRadioButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altFathersMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersMiddleNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersMiddleNameRejectRadioButton))
                            .addComponent(fathersLastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altFathersLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersLastNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fathersLastNameRejectRadioButton))
                            .addComponent(mothersFirstNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                            .addComponent(mothersLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altMothersLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersLastNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersLastNameRejectRadioButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altMothersMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersMiddleNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mothersMiddleNameRejectRadioButton))
                            .addComponent(mothersMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                                .addComponent(altVillageTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(villageAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(villageRejectRadioButton))
                            .addComponent(fathersMiddleNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                            .addComponent(fathersFirstNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                            .addComponent(clanTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE))))
                .addContainerGap())
        );
        reviewPanel2Layout.setVerticalGroup(
            reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel2Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clanLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(clanRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(clanAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altClanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(villageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(villageLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(villageRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(villageAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altVillageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fathersFirstNameLabel)
                    .addComponent(fathersFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(fathersFirstNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(fathersFirstNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altFathersFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fathersMiddleNameLabel)
                    .addComponent(fathersMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(fathersMiddleNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(fathersMiddleNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altFathersMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fathersLastNameLabel)
                    .addComponent(fathersLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(fathersLastNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(fathersLastNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altFathersLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mothersFirstNameLabel)
                    .addComponent(mothersFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mothersFirstNameAcceptRadioButton)
                    .addComponent(altMothersFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mothersFirstNameRejectRadioButton, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mothersMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mothersMiddleNameLabel))
                .addGap(7, 7, 7)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(mothersMiddleNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(mothersMiddleNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altMothersMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mothersLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mothersLastNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(reviewPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(mothersLastNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(mothersLastNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altMothersLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(review2NextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        reviewPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {clanTextField, fathersFirstNameTextField, fathersLastNameTextField, fathersMiddleNameTextField, mothersFirstNameTextField, mothersLastNameTextField, mothersMiddleNameTextField, villageTextField});

        reviewPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {altClanTextField, altFathersFirstNameTextField, altFathersLastNameTextField, altFathersMiddleNameTextField, altMothersFirstNameTextField, altMothersLastNameTextField, altMothersMiddleNameTextField, altVillageTextField});

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
                .addContainerGap(12, Short.MAX_VALUE))
        );

        wizardPanel.add(reviewCard2, "reviewCard2");

        reviewCard3.setName("reviewCard3"); // NOI18N

        reviewPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        reviewPanel3.setName("reviewPanel3"); // NOI18N

        compoundHeadsFirstNameLabel.setText(resourceMap.getString("compoundHeadsFirstNameLabel.text")); // NOI18N
        compoundHeadsFirstNameLabel.setName("compoundHeadsFirstNameLabel"); // NOI18N

        compoundHeadsFirstNameTextField.setName("compoundHeadsFirstNameTextField"); // NOI18N

        altCompoundHeadsFirstNameTextField.setEditable(false);
        altCompoundHeadsFirstNameTextField.setForeground(resourceMap.getColor("altCompoundHeadsFirstNameTextField.foreground")); // NOI18N
        altCompoundHeadsFirstNameTextField.setName("altCompoundHeadsFirstNameTextField"); // NOI18N

        compoundHeadsFirstNameAcceptRadioButton.setAction(actionMap.get("confirmCompoundHeadFirstName")); // NOI18N
        compoundHeadsFirstNameButtonGroup.add(compoundHeadsFirstNameAcceptRadioButton);
        compoundHeadsFirstNameAcceptRadioButton.setIcon(resourceMap.getIcon("compoundHeadsFirstNameAcceptRadioButton.icon")); // NOI18N
        compoundHeadsFirstNameAcceptRadioButton.setName("compoundHeadsFirstNameAcceptRadioButton"); // NOI18N
        compoundHeadsFirstNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("compoundHeadsFirstNameAcceptRadioButton.selectedIcon")); // NOI18N

        compoundHeadsFirstNameRejectRadioButton.setAction(actionMap.get("confirmCompoundHeadFirstName")); // NOI18N
        compoundHeadsFirstNameButtonGroup.add(compoundHeadsFirstNameRejectRadioButton);
        compoundHeadsFirstNameRejectRadioButton.setIcon(resourceMap.getIcon("compoundHeadsFirstNameRejectRadioButton.icon")); // NOI18N
        compoundHeadsFirstNameRejectRadioButton.setName("compoundHeadsFirstNameRejectRadioButton"); // NOI18N
        compoundHeadsFirstNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("compoundHeadsFirstNameRejectRadioButton.selectedIcon")); // NOI18N

        compoundHeadsMiddleNameLabel.setText(resourceMap.getString("compoundHeadsMiddleNameLabel.text")); // NOI18N
        compoundHeadsMiddleNameLabel.setName("compoundHeadsMiddleNameLabel"); // NOI18N

        compoundHeadsMiddleNameTextField.setName("compoundHeadsMiddleNameTextField"); // NOI18N

        altCompoundHeadsMiddleNameTextField.setEditable(false);
        altCompoundHeadsMiddleNameTextField.setForeground(resourceMap.getColor("altCompoundHeadsMiddleNameTextField.foreground")); // NOI18N
        altCompoundHeadsMiddleNameTextField.setName("altCompoundHeadsMiddleNameTextField"); // NOI18N

        compoundHeadsMiddleNameAcceptRadioButton.setAction(actionMap.get("confirmCompoundHeadMiddleName")); // NOI18N
        compoundHeadsMiddleNameButtonGroup.add(compoundHeadsMiddleNameAcceptRadioButton);
        compoundHeadsMiddleNameAcceptRadioButton.setIcon(resourceMap.getIcon("compoundHeadsMiddleNameAcceptRadioButton.icon")); // NOI18N
        compoundHeadsMiddleNameAcceptRadioButton.setName("compoundHeadsMiddleNameAcceptRadioButton"); // NOI18N
        compoundHeadsMiddleNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("compoundHeadsMiddleNameAcceptRadioButton.selectedIcon")); // NOI18N

        compoundHeadsMiddleNameRejectRadioButton.setAction(actionMap.get("confirmCompoundHeadMiddleName")); // NOI18N
        compoundHeadsMiddleNameButtonGroup.add(compoundHeadsMiddleNameRejectRadioButton);
        compoundHeadsMiddleNameRejectRadioButton.setIcon(resourceMap.getIcon("compoundHeadsMiddleNameRejectRadioButton.icon")); // NOI18N
        compoundHeadsMiddleNameRejectRadioButton.setName("compoundHeadsMiddleNameRejectRadioButton"); // NOI18N
        compoundHeadsMiddleNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("compoundHeadsMiddleNameRejectRadioButton.selectedIcon")); // NOI18N

        compoundHeadsLastNameLabel.setText(resourceMap.getString("compoundHeadsLastNameLabel.text")); // NOI18N
        compoundHeadsLastNameLabel.setName("compoundHeadsLastNameLabel"); // NOI18N

        compoundHeadsLastNameTextField.setName("compoundHeadsLastNameTextField"); // NOI18N

        altCompoundHeadsLastNameTextField.setEditable(false);
        altCompoundHeadsLastNameTextField.setName("altCompoundHeadsLastNameTextField"); // NOI18N

        compoundHeadsLastNameAcceptRadioButton.setAction(actionMap.get("confirmCompoundHeadLastName")); // NOI18N
        compoundHeadsLastNameButtonGroup.add(compoundHeadsLastNameAcceptRadioButton);
        compoundHeadsLastNameAcceptRadioButton.setIcon(resourceMap.getIcon("compoundHeadsLastNameAcceptRadioButton.icon")); // NOI18N
        compoundHeadsLastNameAcceptRadioButton.setName("compoundHeadsLastNameAcceptRadioButton"); // NOI18N
        compoundHeadsLastNameAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("compoundHeadsLastNameAcceptRadioButton.selectedIcon")); // NOI18N

        compoundHeadsLastNameRejectRadioButton.setAction(actionMap.get("confirmCompoundHeadLastName")); // NOI18N
        compoundHeadsLastNameButtonGroup.add(compoundHeadsLastNameRejectRadioButton);
        compoundHeadsLastNameRejectRadioButton.setIcon(resourceMap.getIcon("hdssDataConsentRejectRadioButton.icon")); // NOI18N
        compoundHeadsLastNameRejectRadioButton.setName("compoundHeadsLastNameRejectRadioButton"); // NOI18N
        compoundHeadsLastNameRejectRadioButton.setSelectedIcon(resourceMap.getIcon("hdssDataConsentRejectRadioButton.selectedIcon")); // NOI18N

        hdssDataConsentLabel.setText(resourceMap.getString("hdssDataConsentLabel.text")); // NOI18N
        hdssDataConsentLabel.setName("hdssDataConsentLabel"); // NOI18N

        hdssDataConsentButtonGroup.add(hdssDataConsentYesRadioButton);
        hdssDataConsentYesRadioButton.setText(resourceMap.getString("hdssDataConsentYesRadioButton.text")); // NOI18N
        hdssDataConsentYesRadioButton.setName("hdssDataConsentYesRadioButton"); // NOI18N

        hdssDataConsentButtonGroup.add(hdssDataConsentNoRadioButton);
        hdssDataConsentNoRadioButton.setText(resourceMap.getString("hdssDataConsentNoRadioButton.text")); // NOI18N
        hdssDataConsentNoRadioButton.setName("hdssDataConsentNoRadioButton"); // NOI18N

        hdssDataConsentButtonGroup.add(hdssDataConsentNoAnswerRadioButton);
        hdssDataConsentNoAnswerRadioButton.setText(resourceMap.getString("hdssDataConsentNoAnswerRadioButton.text")); // NOI18N
        hdssDataConsentNoAnswerRadioButton.setName("hdssDataConsentNoAnswerRadioButton"); // NOI18N

        altHdssDataConsentTextField.setEditable(false);
        altHdssDataConsentTextField.setName("altHdssDataConsentTextField"); // NOI18N

        hdssDataConsentAcceptRadioButton.setAction(actionMap.get("dummy")); // NOI18N
        altHdssDataConsentButtonGroup.add(hdssDataConsentAcceptRadioButton);
        hdssDataConsentAcceptRadioButton.setIcon(resourceMap.getIcon("hdssDataConsentAcceptRadioButton.icon")); // NOI18N
        hdssDataConsentAcceptRadioButton.setName("hdssDataConsentAcceptRadioButton"); // NOI18N
        hdssDataConsentAcceptRadioButton.setSelectedIcon(resourceMap.getIcon("hdssDataConsentAcceptRadioButton.selectedIcon")); // NOI18N

        altHdssDataConsentButtonGroup.add(hdssDataConsentRejectRadioButton);
        hdssDataConsentRejectRadioButton.setText(resourceMap.getString("hdssDataConsentRejectRadioButton.text")); // NOI18N
        hdssDataConsentRejectRadioButton.setIcon(resourceMap.getIcon("hdssDataConsentRejectRadioButton.icon")); // NOI18N
        hdssDataConsentRejectRadioButton.setName("hdssDataConsentRejectRadioButton"); // NOI18N
        hdssDataConsentRejectRadioButton.setSelectedIcon(resourceMap.getIcon("hdssDataConsentRejectRadioButton.selectedIcon")); // NOI18N

        clientTypeLabel.setText(resourceMap.getString("clientTypeLabel.text")); // NOI18N
        clientTypeLabel.setName("clientTypeLabel"); // NOI18N

        clientTypeButtonGroup.add(enrolledRadioButton);
        enrolledRadioButton.setText(resourceMap.getString("enrolledRadioButton.text")); // NOI18N
        enrolledRadioButton.setName("enrolledRadioButton"); // NOI18N

        clientTypeButtonGroup.add(visitorRadioButton);
        visitorRadioButton.setText(resourceMap.getString("visitorRadioButton.text")); // NOI18N
        visitorRadioButton.setName("visitorRadioButton"); // NOI18N

        clientTypeButtonGroup.add(newRadioButton);
        newRadioButton.setText(resourceMap.getString("newRadioButton.text")); // NOI18N
        newRadioButton.setName("newRadioButton"); // NOI18N

        clientTypeButtonGroup.add(transferInRadioButton);
        transferInRadioButton.setText(resourceMap.getString("transferInRadioButton.text")); // NOI18N
        transferInRadioButton.setName("transferInRadioButton"); // NOI18N

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

        clientRefusesCheckBox.setAction(actionMap.get("refuseFingerprintingReview")); // NOI18N
        clientRefusesCheckBox.setText(resourceMap.getString("clientRefusesCheckBox.text")); // NOI18N
        clientRefusesCheckBox.setName("clientRefusesCheckBox"); // NOI18N

        takeButton.setAction(actionMap.get("showFingerprintDialogReview")); // NOI18N
        takeButton.setText(resourceMap.getString("takeButton.text")); // NOI18N
        takeButton.setName("takeButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, clientRefusesCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), takeButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        viewHouseholdButton.setAction(actionMap.get("viewHouseholdMembers")); // NOI18N
        viewHouseholdButton.setText(resourceMap.getString("viewHouseholdButton.text")); // NOI18N
        viewHouseholdButton.setName("viewHouseholdButton"); // NOI18N

        finishButton.setAction(actionMap.get("finish")); // NOI18N
        finishButton.setText(resourceMap.getString("finishButton.text")); // NOI18N
        finishButton.setName("finishButton"); // NOI18N

        javax.swing.GroupLayout reviewPanel3Layout = new javax.swing.GroupLayout(reviewPanel3);
        reviewPanel3.setLayout(reviewPanel3Layout);
        reviewPanel3Layout.setHorizontalGroup(
            reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel3Layout.createSequentialGroup()
                        .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(compoundHeadsFirstNameLabel)
                            .addComponent(compoundHeadsMiddleNameLabel)
                            .addComponent(compoundHeadsLastNameLabel)
                            .addComponent(hdssDataConsentLabel)
                            .addComponent(clientTypeLabel)
                            .addComponent(fingerprintLabel))
                        .addGap(23, 23, 23)
                        .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel3Layout.createSequentialGroup()
                                .addComponent(altCompoundHeadsLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsLastNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsLastNameRejectRadioButton))
                            .addGroup(reviewPanel3Layout.createSequentialGroup()
                                .addComponent(hdssDataConsentYesRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hdssDataConsentNoRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hdssDataConsentNoAnswerRadioButton))
                            .addComponent(compoundHeadsLastNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel3Layout.createSequentialGroup()
                                .addComponent(altCompoundHeadsMiddleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsMiddleNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsMiddleNameRejectRadioButton))
                            .addComponent(compoundHeadsMiddleNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel3Layout.createSequentialGroup()
                                .addComponent(altCompoundHeadsFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsFirstNameAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compoundHeadsFirstNameRejectRadioButton))
                            .addComponent(compoundHeadsFirstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                            .addGroup(reviewPanel3Layout.createSequentialGroup()
                                .addComponent(enrolledRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(visitorRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(newRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(transferInRadioButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reviewPanel3Layout.createSequentialGroup()
                                .addComponent(altHdssDataConsentTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hdssDataConsentAcceptRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hdssDataConsentRejectRadioButton))
                            .addGroup(reviewPanel3Layout.createSequentialGroup()
                                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(takeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(fingerprintImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clientRefusesCheckBox))))
                    .addComponent(viewHouseholdButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addComponent(finishButton, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                .addContainerGap())
        );
        reviewPanel3Layout.setVerticalGroup(
            reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reviewPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compoundHeadsFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compoundHeadsFirstNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(altCompoundHeadsFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(compoundHeadsFirstNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(compoundHeadsFirstNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(compoundHeadsMiddleNameLabel)
                    .addComponent(compoundHeadsMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(compoundHeadsMiddleNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(compoundHeadsMiddleNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(altCompoundHeadsMiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compoundHeadsLastNameLabel)
                    .addComponent(compoundHeadsLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reviewPanel3Layout.createSequentialGroup()
                        .addComponent(altCompoundHeadsLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hdssDataConsentNoRadioButton)
                            .addComponent(hdssDataConsentNoAnswerRadioButton)
                            .addComponent(hdssDataConsentYesRadioButton)
                            .addComponent(hdssDataConsentLabel)))
                    .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(compoundHeadsLastNameRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(compoundHeadsLastNameAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(altHdssDataConsentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(hdssDataConsentRejectRadioButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(hdssDataConsentAcceptRadioButton, javax.swing.GroupLayout.Alignment.LEADING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enrolledRadioButton)
                    .addComponent(visitorRadioButton)
                    .addComponent(newRadioButton)
                    .addComponent(transferInRadioButton)
                    .addComponent(clientTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reviewPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fingerprintLabel)
                    .addComponent(fingerprintImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clientRefusesCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(takeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewHouseholdButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(finishButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
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
                .addComponent(reviewPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        wizardPanel.add(reviewCard3, "reviewCard3");

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(wizardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(homeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loggedInLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)))
                .addContainerGap())
        );

        rightPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {backButton, homeButton});

        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(backButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(homeButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loggedInLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wizardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                .addContainerGap())
        );

        rightPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {backButton, homeButton});

        mainSplitPane.setRightComponent(rightPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        toolsMenu.setText(resourceMap.getString("toolsMenu.text")); // NOI18N
        toolsMenu.setName("toolsMenu"); // NOI18N

        departmentsMenuItem.setAction(actionMap.get("showDepartmentsDialog")); // NOI18N
        departmentsMenuItem.setText(resourceMap.getString("departmentsMenuItem.text")); // NOI18N
        departmentsMenuItem.setName("departmentsMenuItem"); // NOI18N
        toolsMenu.add(departmentsMenuItem);

        menuBar.add(toolsMenu);

        usersMenu.setText(resourceMap.getString("usersMenu.text")); // NOI18N
        usersMenu.setName("usersMenu"); // NOI18N

        changePasswordMenuItem.setAction(actionMap.get("changePassword")); // NOI18N
        changePasswordMenuItem.setText(resourceMap.getString("changePasswordMenuItem.text")); // NOI18N
        changePasswordMenuItem.setName("changePasswordMenuItem"); // NOI18N
        usersMenu.add(changePasswordMenuItem);

        manageUsersMenu.setText(resourceMap.getString("manageUsersMenu.text")); // NOI18N
        manageUsersMenu.setName("manageUsersMenu"); // NOI18N

        addUsersMenuItem.setAction(actionMap.get("addUsers")); // NOI18N
        addUsersMenuItem.setText(resourceMap.getString("addUsersMenuItem.text")); // NOI18N
        addUsersMenuItem.setName("addUsersMenuItem"); // NOI18N
        manageUsersMenu.add(addUsersMenuItem);

        managePermissionsMenuItem.setAction(actionMap.get("managePermissions")); // NOI18N
        managePermissionsMenuItem.setText(resourceMap.getString("managePermissionsMenuItem.text")); // NOI18N
        managePermissionsMenuItem.setName("managePermissionsMenuItem"); // NOI18N
        manageUsersMenu.add(managePermissionsMenuItem);

        reserPasswordMenuItem.setAction(actionMap.get("resetUserPassword")); // NOI18N
        reserPasswordMenuItem.setText(resourceMap.getString("reserPasswordMenuItem.text")); // NOI18N
        reserPasswordMenuItem.setName("reserPasswordMenuItem"); // NOI18N
        manageUsersMenu.add(reserPasswordMenuItem);

        deleteUserMenuItem.setAction(actionMap.get("deleteUser")); // NOI18N
        deleteUserMenuItem.setText(resourceMap.getString("deleteUserMenuItem.text")); // NOI18N
        deleteUserMenuItem.setName("deleteUserMenuItem"); // NOI18N
        manageUsersMenu.add(deleteUserMenuItem);

        usersMenu.add(manageUsersMenu);

        menuBar.add(usersMenu);

        themesMenu.setText(resourceMap.getString("themesMenu.text")); // NOI18N
        themesMenu.setName("themesMenu"); // NOI18N
        menuBar.add(themesMenu);

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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 615, Short.MAX_VALUE)
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
                .addGap(6, 6, 6))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void basicSearchClinicIdTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_basicSearchClinicIdTextFieldKeyTyped
        enableBasicSearchButton();
    }//GEN-LAST:event_basicSearchClinicIdTextFieldKeyTyped
    private void basicSearchClinicNameTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_basicSearchClinicNameTextFieldKeyTyped
        enableBasicSearchButton();
    }//GEN-LAST:event_basicSearchClinicNameTextFieldKeyTyped

    private void extendedSearchClinicIdTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_extendedSearchClinicIdTextFieldKeyTyped
        enableExtendedSearchButton();
    }//GEN-LAST:event_extendedSearchClinicIdTextFieldKeyTyped

    private void extendedSearchClinicNameTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_extendedSearchClinicNameTextFieldKeyTyped
        enableExtendedSearchButton();
    }//GEN-LAST:event_extendedSearchClinicNameTextFieldKeyTyped

    private void notificationTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_notificationTreeMouseClicked
        if (evt.getClickCount() == 1) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) notificationTree.getLastSelectedPathComponent();
            if (node == null || node.isRoot() || !node.isLeaf()) {
                processNotificationButton.setEnabled(false);
            } else {
                processNotificationButton.setEnabled(true);
            }
        } else if (evt.getClickCount() == 2) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) notificationTree.getLastSelectedPathComponent();
            if (node != null && !node.isRoot() && node.isLeaf()) {
                processNotification();
            }
        }
    }//GEN-LAST:event_notificationTreeMouseClicked

private void basicSearchClinicIdTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_basicSearchClinicIdTextFieldFocusLost
    PersonIdentifier personIdentifier = OECReception.createPersonIdentifier(basicSearchClinicIdTextField.getText());
    if (personIdentifier != null) {
        basicSearchClinicIdTextField.setText(personIdentifier.getIdentifier());
        basicSearchClinicIdTextField.setForeground(Color.BLACK);
    } else {
        basicSearchClinicIdTextField.setForeground(Color.RED);
    }
}//GEN-LAST:event_basicSearchClinicIdTextFieldFocusLost

private void extendedSearchClinicIdTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_extendedSearchClinicIdTextFieldFocusLost
    PersonIdentifier personIdentifier = OECReception.createPersonIdentifier(extendedSearchClinicIdTextField.getText());
    if (personIdentifier != null) {
        extendedSearchClinicIdTextField.setText(personIdentifier.getIdentifier());
        extendedSearchClinicIdTextField.setForeground(Color.BLACK);
    } else {
        extendedSearchClinicIdTextField.setForeground(Color.RED);
    }
}//GEN-LAST:event_extendedSearchClinicIdTextFieldFocusLost

private void clinicIdTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_clinicIdTextFieldFocusLost
    PersonIdentifier personIdentifier = OECReception.createPersonIdentifier(clinicIdTextField.getText());
    if (personIdentifier != null) {
        clinicIdTextField.setText(personIdentifier.getIdentifier());
        clinicIdTextField.setForeground(Color.BLACK);
    } else {
        clinicIdTextField.setForeground(Color.RED);
    }
}//GEN-LAST:event_clinicIdTextFieldFocusLost

private void basicSearchClinicIdTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_basicSearchClinicIdTextFieldFocusGained
    basicSearchClinicIdTextField.setForeground(Color.BLACK);
}//GEN-LAST:event_basicSearchClinicIdTextFieldFocusGained

private void extendedSearchClinicIdTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_extendedSearchClinicIdTextFieldFocusGained
    extendedSearchClinicIdTextField.setForeground(Color.BLACK);
}//GEN-LAST:event_extendedSearchClinicIdTextFieldFocusGained

private void clinicIdTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_clinicIdTextFieldFocusGained
    clinicIdTextField.setForeground(Color.BLACK);
}//GEN-LAST:event_clinicIdTextFieldFocusGained

    @Action
    public void goHome() {
        showCard("homeCard", true, sessionHasData());
    }

    @Action
    public void goBack() {
        int i = visitedCardList.indexOf(currentCardName);
        if (i > 0) {
            if (currentCardName.equalsIgnoreCase("mpiResultsCard")) {
                mainViewHelper.undoMpiResultDisplay();
            }
            if (currentCardName.equalsIgnoreCase("lpiResultsCard")) {
                mainViewHelper.undoLpiResultDisplay();
            }
            visitedCardList.remove(i);
            if (i == 1) {
                showCard(visitedCardList.get(i - 1), true, sessionHasData());
            } else if (i > 1) {
                showCard(visitedCardList.get(i - 1));
            }
        }
    }

    private boolean sessionHasData() {
        boolean hasData = false;
        if (quickSearchManager != null) {
            if (quickSearchManager.getRightIndex().getFingerprint().getTemplate() != null
                    || quickSearchManager.getLeftIndex().getFingerprint().getTemplate() != null) {
                hasData = true;
            }
        }
        if (!extendedSearchClinicIdTextField.getText().isEmpty()
                || !extendedSearchClinicNameTextField.getText().isEmpty()
                || !extendedSearchFirstNameTextField.getText().isEmpty()
                || !extendedSearchMiddleNameTextField.getText().isEmpty()
                || !extendedSearchLastNameTextField.getText().isEmpty()
                || !extendedSearchOtherNameTextField.getText().isEmpty()
                || !extendedSearchClanNameTextField.getText().isEmpty()
                || !extendedSearchVillageTextField.getText().isEmpty()
                || extendedSearchMaleRadioButton.isSelected()
                || extendedSearchFemaleRadioButton.isSelected()) {
            hasData = true;
        }
        if (mainViewHelper != null) {
            if (mainViewHelper.getSession() != null) {
                if (mainViewHelper.getSession().getImagedFingerprintList() != null
                        && !mainViewHelper.getSession().getImagedFingerprintList().isEmpty()) {
                    hasData = true;
                }
            }
        }
        if (extendedSearchBirthdateChooser.getDate() != null) {
            String selectedDate = new SimpleDateFormat("yyyy-MM-dd").format(extendedSearchBirthdateChooser.getDate());
            String dateToday = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            if (!selectedDate.equals(dateToday)) {
                hasData = true;
            }
        }
        if (!basicSearchClinicIdTextField.getText().isEmpty()
                || !basicSearchClinicNameTextField.getText().isEmpty()) {
            hasData = true;
        }
        return hasData;
    }

    private void showCard(String cardName) {
        boolean home = cardName.equalsIgnoreCase("homeCard");
        showCard(cardName, home, false);
    }

    public final void showCard(String cardName, boolean home, boolean confirm) {
        if (home) {
            if ((!currentCardName.equalsIgnoreCase("homeCard"))) {
                if (confirm && !showConfirmMessage("Are you sure you want to go back to the home page and"
                        + " start a new session? You will loose the data collected in the current session.", this.getFrame())) {
                    return;
                }
                flipToCard(cardName);
            } else {
                //avoid destroying and re-initializing fp sdk unnecessarily
                if (!readerAvailable) {
                    flipToCard(cardName);
                }
                return;
            }
        } else {
            flipToCard(cardName);
        }
    }

    private void flipToCard(String cardName) {
        finalizeCard(currentCardName);
        currentCardName = cardName;
        initializeCard(cardName);
        cardLayout.show(wizardPanel, cardName);
        if (!visitedCardList.contains(cardName)) {
            visitedCardList.add(cardName);
        }
    }

    private void initializeCard(String cardName) {
        PersonWrapper personWrapper = null;
        if (mainViewHelper.getSession() != null) {
            personWrapper = mainViewHelper.getSession().getSearchPersonWrapper();
        }
        if (cardName.equalsIgnoreCase("homeCard")) {
            quickSearchButton.setVisible(false);
            initializeReaderManager(this);
            endCurrentSession();
            clearFields(wizardPanel);
            quickSearchManager = new QuickSearchManager();
            quickSearchManager.toggleQuickSearchButtons();
            FingerprintDialog.clearImagedFingerprintCache();

        } else if (cardName.equalsIgnoreCase("clinicIdCard")) {
            TitledBorder clinicIdPanelBorder = (TitledBorder) clientIdPanel.getBorder();
            clinicIdPanelBorder.setTitle("\"" + mainViewHelper.getSession().getClientType().toString() + " client\" "
                    + "knows their Clinic ID?");
        } else if (cardName.equalsIgnoreCase("basicSearchCard")) {
            basicSearchClinicNameLabel.setVisible((mainViewHelper.getSession().getClientType() == Session.ClientType.VISITOR)
                    || (mainViewHelper.getSession().getClientType() == Session.ClientType.TRANSFER_IN));
            basicSearchClinicNameTextField.setVisible((mainViewHelper.getSession().getClientType() == Session.ClientType.VISITOR)
                    || (mainViewHelper.getSession().getClientType() == Session.ClientType.TRANSFER_IN));
            basicSearchClinicIdTextField.setText(personWrapper.getClinicId());
            basicSearchClinicNameTextField.setText(personWrapper.getClinicName());
            if (mainViewHelper.getSession() != null) {
                List<ImagedFingerprint> imagedFingerprintList = mainViewHelper.getSession().getImagedFingerprintList();
                if (imagedFingerprintList != null && !imagedFingerprintList.isEmpty()) {
                    basicSearchFingerprintImagePanel.setImage(imagedFingerprintList.get(imagedFingerprintList.size() - 1).getImage());
                }
            }
            basicSearchClientRefusesCheckBox.setSelected(!mainViewHelper.getSession().isFingerprint());
            enableBasicSearchButton();
        } else if (cardName.equalsIgnoreCase("extendedSearchCard")) {
            if (mainViewHelper.getSession().getClientType() == Session.ClientType.ENROLLED) {
                extendedSearchClinicIdLabel.setVisible(mainViewHelper.getSession().isClinicId());
                extendedSearchClinicIdTextField.setVisible(mainViewHelper.getSession().isClinicId());
                extendedSearchClinicNameLabel.setVisible(false);
                extendedSearchClinicNameTextField.setVisible(false);
            } else if (mainViewHelper.getSession().getClientType() == Session.ClientType.VISITOR) {
                extendedSearchClinicIdLabel.setVisible(mainViewHelper.getSession().isClinicId());
                extendedSearchClinicIdTextField.setVisible(mainViewHelper.getSession().isClinicId());
                extendedSearchClinicNameLabel.setVisible(true);
                extendedSearchClinicNameTextField.setVisible(true);
            } else if (mainViewHelper.getSession().getClientType() == Session.ClientType.NEW) {
                extendedSearchClinicIdLabel.setVisible(false);
                extendedSearchClinicIdTextField.setVisible(false);
                extendedSearchClinicNameLabel.setVisible(false);
                extendedSearchClinicNameTextField.setVisible(false);
            } else if (mainViewHelper.getSession().getClientType() == Session.ClientType.TRANSFER_IN) {
                extendedSearchClinicIdLabel.setVisible(mainViewHelper.getSession().isClinicId());
                extendedSearchClinicIdTextField.setVisible(mainViewHelper.getSession().isClinicId());
                extendedSearchClinicNameLabel.setVisible(true);
                extendedSearchClinicNameTextField.setVisible(true);
            }
            extendedSearchClinicIdTextField.setText(personWrapper.getClinicId());
            extendedSearchClinicNameTextField.setText(personWrapper.getClinicName());
            extendedSearchFirstNameTextField.setText(personWrapper.getFirstName());
            extendedSearchMiddleNameTextField.setText(personWrapper.getMiddleName());
            extendedSearchLastNameTextField.setText(personWrapper.getLastName());
            extendedSearchBirthdateChooser.setDate(personWrapper.getBirthdate());
            extendedSearchUnknownBirthdateCheckBox.setSelected(personWrapper.getBirthdate() == null);
            Person.Sex sex = personWrapper.getSex();
            if (sex != null) {
                extendedSearchMaleRadioButton.setSelected(sex == Person.Sex.M);
                extendedSearchFemaleRadioButton.setSelected(sex == Person.Sex.F);
            } else {
                extendedSearchMaleRadioButton.setSelected(false);
                extendedSearchFemaleRadioButton.setSelected(false);
            }
            if (mainViewHelper.getSession() != null) {
                List<ImagedFingerprint> imagedFingerprintList = mainViewHelper.getSession().getImagedFingerprintList();
                if (imagedFingerprintList != null && !imagedFingerprintList.isEmpty()) {
                    extendedSearchFingerprintImagePanel.setImage(imagedFingerprintList.get(imagedFingerprintList.size() - 1).getImage());
                }
            }
            extendedSearchClientRefusesCheckBox.setSelected(!mainViewHelper.getSession().isFingerprint());
            enableExtendedSearchButton();
        } else if (cardName.equalsIgnoreCase("reviewCard3")) {
            toggleClientTypeOptions(false);
            if (mainViewHelper.getSession().getClientType() == Session.ClientType.UNSPECIFIED) {
                PersonIdentifier personIdentifier = OECReception.createPersonIdentifier(clinicIdTextField.getText());
                if (personIdentifier != null && personIdentifier.getIdentifierType() == PersonIdentifier.Type.cccLocalId) {
                    String clinicCode = OECReception.extractFacilityCode(personIdentifier.getIdentifier());
                    if (clinicCode.equalsIgnoreCase(OECReception.facilityCode())) {
                        mainViewHelper.getSession().changeSessionClientType(Session.ClientType.ENROLLED);
                    } else {
                        toggleClientTypeOptions(true);
                    }
                } else {
                    showErrorMessage("Please key in a valid Clinic ID for this client.", clinicIdTextField);
                    showCard("reviewCard1");
                }
            }

            viewHouseholdButton.setVisible(false);
            PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
            if (mpiMatchPersonWrapper != null) {
                String kisumuHdssId = mpiMatchPersonWrapper.getKisumuHdssId();
                Person.ConsentSigned consentSigned = mpiMatchPersonWrapper.getConsentSigned();
                if (!kisumuHdssId.isEmpty()) {
                    viewHouseholdButton.setVisible(true);
                    viewHouseholdButton.setEnabled(false);
                } else {
                    return;
                }
                if (consentSigned == Person.ConsentSigned.yes) {
                    viewHouseholdButton.setEnabled(true);
                    return;
                } else {
                    return;
                }
            }
        }
    }

    private void finalizeCard(String cardName) {
        if (cardName.equalsIgnoreCase("homeCard")) {
            showMessage("");
            showQuickSearchStatus("");
            destroyReaderManager(false);
        }
    }

    private void toggleClientTypeOptions(boolean visible) {
        clientTypeLabel.setVisible(visible);
        enrolledRadioButton.setVisible(visible);
        visitorRadioButton.setVisible(visible);
        newRadioButton.setVisible(visible);
        transferInRadioButton.setVisible(visible);
    }

    public void enableBasicSearchButton() {
        if (mainViewHelper.getSession().getClientType() == Session.ClientType.ENROLLED) {
            basicSearchButton.setEnabled((!mainViewHelper.getSession().getImagedFingerprintList().isEmpty()
                    || !mainViewHelper.getSession().isFingerprint()) && !basicSearchClinicIdTextField.getText().isEmpty());
        } else if (mainViewHelper.getSession().getClientType() == Session.ClientType.VISITOR
                || mainViewHelper.getSession().getClientType() == Session.ClientType.TRANSFER_IN) {
            basicSearchButton.setEnabled((!mainViewHelper.getSession().getImagedFingerprintList().isEmpty()
                    || !mainViewHelper.getSession().isFingerprint())
                    && !basicSearchClinicIdTextField.getText().isEmpty()
                    && !basicSearchClinicNameTextField.getText().isEmpty());
        }
    }

    public void enableExtendedSearchButton() {
        if (mainViewHelper.getSession().getClientType() == Session.ClientType.ENROLLED
                && mainViewHelper.getSession().isClinicId()) {
            extendedSearchButton.setEnabled(!extendedSearchClinicIdTextField.getText().isEmpty()
                    && (!mainViewHelper.getSession().getImagedFingerprintList().isEmpty()
                    || !mainViewHelper.getSession().isFingerprint()));
        } else if (mainViewHelper.getSession().getClientType() == Session.ClientType.VISITOR
                || mainViewHelper.getSession().getClientType() == Session.ClientType.TRANSFER_IN) {
            if (mainViewHelper.getSession().isClinicId()) {
                extendedSearchButton.setEnabled(!extendedSearchClinicIdTextField.getText().isEmpty()
                        && (!mainViewHelper.getSession().getImagedFingerprintList().isEmpty()
                        || !mainViewHelper.getSession().isFingerprint())
                        && !extendedSearchClinicNameTextField.getText().isEmpty());
            } else {
                extendedSearchButton.setEnabled((!mainViewHelper.getSession().getImagedFingerprintList().isEmpty()
                        || !mainViewHelper.getSession().isFingerprint())
                        && !extendedSearchClinicNameTextField.getText().isEmpty());
            }
        }
    }

    @Action
    public void showFingerprintDialogBasic() {
        try {
            FingerprintDialog fingerprintDialog = new FingerprintDialog(this.getFrame(), true,
                    mainViewHelper.getMissingFingerprint());
            fingerprintDialog.setLocationRelativeTo(this.getFrame());
            fingerprintDialog.setSession(mainViewHelper.getSession());
            fingerprintDialog.setVisible(true);
            if (mainViewHelper.getSession().getActiveImagedFingerprint() != null) {
                showFingerprintImageBasic(mainViewHelper.getSession().getActiveImagedFingerprint().getImage());
            }
            enableBasicSearchButton();
        } catch (Exception ex) {
            showWarningMessage("Fingerprinting is currently unavailable because of the following"
                    + " reason: " + ex.getMessage() + ".", basicSearchTakeButton);
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    @Action
    public void showFingerprintDialogExtended() {
        try {
            FingerprintDialog fingerprintDialog = new FingerprintDialog(this.getFrame(), true,
                    mainViewHelper.getMissingFingerprint());
            fingerprintDialog.setLocationRelativeTo(this.getFrame());
            fingerprintDialog.setSession(mainViewHelper.getSession());
            fingerprintDialog.setVisible(true);
            if (mainViewHelper.getSession().getActiveImagedFingerprint() != null) {
                showFingerprintImageExtended(mainViewHelper.getSession().getActiveImagedFingerprint().getImage());
            }
            enableExtendedSearchButton();
        } catch (Exception ex) {
            showWarningMessage("Fingerprinting functionality is unavailable for the following"
                    + " reason: " + ex.getMessage() + ".", this.getFrame(), basicSearchTakeButton);
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    @Action
    public void showFingerprintDialogReview() {
        try {
            FingerprintDialog fingerprintDialog = new FingerprintDialog(this.getFrame(), true, mainViewHelper.getMissingFingerprint());
            fingerprintDialog.setLocationRelativeTo(this.getFrame());
            fingerprintDialog.setSession(mainViewHelper.getSession());
            fingerprintDialog.setVisible(true);
            if (mainViewHelper.getSession().getActiveImagedFingerprint() != null) {
                showFingerprintImageReview(mainViewHelper.getSession().getActiveImagedFingerprint().getImage());
            }
            initializeCard("reviewCard3");
        } catch (Exception ex) {
            showWarningMessage("Fingerprinting functionality is unavailable for the following"
                    + " reason: " + ex.getMessage() + ".", this.getFrame(), basicSearchTakeButton);
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    public void showFingerprintImageBasic(BufferedImage fingerprintImage) {
        if (fingerprintImage != null) {
            basicSearchFingerprintImagePanel.setImage(fingerprintImage);
        }
    }

    public void showFingerprintImageExtended(BufferedImage fingerprintImage) {
        if (fingerprintImage != null) {
            extendedSearchFingerprintImagePanel.setImage(fingerprintImage);
        }
    }

    public void showFingerprintImageReview(BufferedImage fingerprintImage) {
        if (fingerprintImage != null) {
            fingerprintImagePanel.setImage(fingerprintImage);
        }
    }

    public void showWarningMessage(String message, Component parent, JComponent toFocus) {
        JOptionPane.showMessageDialog(parent, message, OECReception.applicationName(), JOptionPane.WARNING_MESSAGE);
        toFocus.requestFocus();
    }

    public void showErrorMessage(String message, Component parent, JComponent toFocus) {
        JOptionPane.showMessageDialog(parent, message, OECReception.applicationName(), JOptionPane.WARNING_MESSAGE);
        toFocus.requestFocus();
    }

    public boolean showConfirmMessage(String message, Component parent) {
        return JOptionPane.showConfirmDialog(this.getFrame(), message, OECReception.applicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void showWarningMessage(String message, JComponent toFocus) {
        JOptionPane.showMessageDialog(this.getFrame(), message, OECReception.applicationName(), JOptionPane.WARNING_MESSAGE);
        toFocus.requestFocus();
    }

    public void showErrorMessage(String message, JComponent toFocus) {
        JOptionPane.showMessageDialog(this.getFrame(), message, OECReception.applicationName(), JOptionPane.WARNING_MESSAGE);
        toFocus.requestFocus();
    }

    public boolean showConfirmMessage(String message) {
        return JOptionPane.showConfirmDialog(this.getFrame(), message, OECReception.applicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    @Action
    public void refuseFingerprintingBasic() {
        if (basicSearchClientRefusesCheckBox.isSelected()) {
            showFingerprintImageBasic(mainViewHelper.getRefusedFingerprint().getImage());
            mainViewHelper.getSession().setFingerprint(false);
        } else {
            showFingerprintImageBasic(mainViewHelper.getMissingFingerprint().getImage());
            mainViewHelper.getSession().setFingerprint(true);
        }
        enableBasicSearchButton();
    }

    @Action
    public void refuseFingerprintingExtended() {
        if (extendedSearchClientRefusesCheckBox.isSelected()) {
            showFingerprintImageExtended(mainViewHelper.getRefusedFingerprint().getImage());
            mainViewHelper.getSession().setFingerprint(false);
        } else {
            showFingerprintImageExtended(mainViewHelper.getMissingFingerprint().getImage());
            mainViewHelper.getSession().setFingerprint(true);
        }
        enableExtendedSearchButton();
    }

    @Action
    public void refuseFingerprintingReview() {
        if (clientRefusesCheckBox.isSelected()) {
            showFingerprintImageReview(mainViewHelper.getRefusedFingerprint().getImage());
            mainViewHelper.getSession().setFingerprint(false);
        } else {
            showFingerprintImageReview(mainViewHelper.getMissingFingerprint().getImage());
            mainViewHelper.getSession().setFingerprint(true);
        }
        initializeCard("reviewCard3");
    }

    @Action
    public Task searchBasic() {
        disableBusyButton(basicSearchButton);
        return new SearchBasicTask(getApplication());
    }

    public void addNotifications(final List<Notification> notificationList) {
        Runnable notificationAdder = new Runnable() {

            public void run() {
                statusMessageLabel.setText("Receiving " + notificationList.size() + " notification(s).");
                for (Notification notification : notificationList) {
                    addNotificationToTree(notification);
                }
                statusMessageLabel.setText(notificationList.size() + " notification(s) received. You now have "
                        + totalNotifications() + " notification(s) to process.");
                if (NotificationSoundPlayer.getInstance() != null) {
                    //if sound is not due for playing then don't flash a label of
                    // repaint the jtree either
                    if (NotificationSoundPlayer.getInstance().play()) {
                        flash(statusMessageLabel, Color.RED, Font.BOLD, 5);
                        notificationTree.repaint();
                    }
                }
                //notificationTree.repaint();
            }
        };
        new Thread(notificationAdder).start();
    }

    public void flash(final JLabel label, final Color flashColor, final int flashFont, final int noOfTimes) {
        Runnable flasher = new Runnable() {

            private int loopMax = (noOfTimes * 2);

            public void run() {
                Color original = new Color(0, 0, 0);
                boolean flash = false;
                for (int i = 0; i < loopMax; i++) {
                    label.setForeground(flashColor);
                    label.setFont(new Font(label.getFont().getName(), flashFont, label.getFont().getSize()));
                    label.setVisible(flash);
                    flash = !flash;
                    try {
                        if (flash == false) {
                            Thread.sleep(400);
                        } else {
                            Thread.sleep(800);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainView.class.getName()).log(Level.INFO, null, ex);
                    }
                }
                label.setForeground(original);
                label.setFont(new Font(label.getFont().getName(), Font.PLAIN, label.getFont().getSize()));
                label.setVisible(true);
            }
        };
        new Thread(flasher).start();
    }

    private void addNotificationToTree(Notification notification) {
        DefaultTreeModel treeModel = (DefaultTreeModel) notificationTree.getModel();
        DefaultMutableTreeNode newNotificationNode = new DefaultMutableTreeNode(notification);
        boolean reload = false;
        if (notification.getType() == Notification.Type.PREGNANCY_OUTCOME) {
            reload = pregnancyOutcomeNotificationList.isEmpty();
            pregnancyOutcomeNotificationList.add(notification);
            insertNotificationTypeNode(pregnancyOutcomeNode);
            int size = treeModel.getChildCount(pregnancyOutcomeNode);
            int preferredPosition = pregnancyOutcomeNotificationList.indexOf(notification);
            int safePosition = (preferredPosition < size ? preferredPosition : size);
            treeModel.insertNodeInto(newNotificationNode, pregnancyOutcomeNode, safePosition);
            pregnancyOutcomeNode.setUserObject("Pregnancy outcome " + "(" + pregnancyOutcomeNotificationList.size() + ")");
        } else if (notification.getType() == Notification.Type.PREGNANCY) {
            reload = (reload || pregnancyNotificationList.isEmpty());
            pregnancyNotificationList.add(notification);
            insertNotificationTypeNode(pregnancyNode);
            int size = treeModel.getChildCount(pregnancyNode);
            int preferredPosition = pregnancyNotificationList.indexOf(notification);
            int safePosition = (preferredPosition < size ? preferredPosition : size);
            treeModel.insertNodeInto(newNotificationNode, pregnancyNode, safePosition);
            pregnancyNode.setUserObject("Pregnancy " + "(" + pregnancyNotificationList.size() + ")");
        } else if (notification.getType() == Notification.Type.DEATH) {
            reload = (reload || deathNotificationList.isEmpty());
            deathNotificationList.add(notification);
            insertNotificationTypeNode(deathNode);
            int size = treeModel.getChildCount(deathNode);
            int preferredPosition = deathNotificationList.indexOf(notification);
            int safePosition = (preferredPosition < size ? preferredPosition : size);
            treeModel.insertNodeInto(newNotificationNode, deathNode, safePosition);
            deathNode.setUserObject("Death " + "(" + deathNotificationList.size() + ")");
        } else if (notification.getType() == Notification.Type.MIGRATION) {
            reload = (reload || migrationNotificationList.isEmpty());
            migrationNotificationList.add(notification);
            insertNotificationTypeNode(migrationNode);
            int size = treeModel.getChildCount(migrationNode);
            int preferredPosition = migrationNotificationList.indexOf(notification);
            int safePosition = (preferredPosition < size ? preferredPosition : size);
            treeModel.insertNodeInto(newNotificationNode, migrationNode, safePosition);
            migrationNode.setUserObject("Migration " + "(" + migrationNotificationList.size() + ")");
        }
        if (reload) {
            treeModel.reload();
        }
        notificationRootNode.setUserObject("Notifications " + "(" + totalNotifications() + ")");
    }
    /*
     * Inserts a notification type node into the root node if it hasn't already been
     * inserted
     */

    private void insertNotificationTypeNode(DefaultMutableTreeNode notificationTypeNode) {
        if (notificationTypeNode.getParent() == null) {
            int count = notificationRootNode.getChildCount();
            boolean inserted = false;
            if (count > 0) {
                String notificationType = (String) notificationTypeNode.getUserObject();
                for (int i = 0; i < count; i++) {
                    DefaultMutableTreeNode currentNotificationTypeNode = (DefaultMutableTreeNode) notificationRootNode.getChildAt(i);
                    String currentNotificationType = (String) currentNotificationTypeNode.getUserObject();
                    if (notificationType.compareToIgnoreCase(currentNotificationType) < 0) {
                        notificationRootNode.insert(notificationTypeNode, i);
                        inserted = true;
                    }
                }
            }
            if (count < 1 || !inserted) {
                notificationRootNode.add(notificationTypeNode);
            }
        }
    }

    private void removeNotificationFromTree(Notification notification) {
        DefaultTreeModel treeModel = (DefaultTreeModel) notificationTree.getModel();
        if (notification.getType() == Notification.Type.PREGNANCY_OUTCOME) {
            treeModel.removeNodeFromParent(
                    (MutableTreeNode) pregnancyOutcomeNode.getChildAt(pregnancyOutcomeNotificationList.indexOf(notification)));
            pregnancyOutcomeNotificationList.remove(notification);
            pregnancyOutcomeNode.setUserObject("Pregnancy outcome" + "(" + pregnancyOutcomeNotificationList.size() + ")");
            if (pregnancyOutcomeNotificationList.isEmpty()) {
                treeModel.removeNodeFromParent(pregnancyOutcomeNode);
            }
        } else if (notification.getType() == Notification.Type.PREGNANCY) {
            treeModel.removeNodeFromParent(
                    (MutableTreeNode) pregnancyNode.getChildAt(pregnancyNotificationList.indexOf(notification)));
            pregnancyNotificationList.remove(notification);
            pregnancyNode.setUserObject("Pregnancy" + "(" + pregnancyNotificationList.size() + ")");
            if (pregnancyNotificationList.isEmpty()) {
                treeModel.removeNodeFromParent(pregnancyNode);
            }
        } else if (notification.getType() == Notification.Type.DEATH) {
            treeModel.removeNodeFromParent(
                    (MutableTreeNode) deathNode.getChildAt(deathNotificationList.indexOf(notification)));
            deathNotificationList.remove(notification);
            deathNode.setUserObject("Death" + "(" + deathNotificationList.size() + ")");
            if (deathNotificationList.isEmpty()) {
                treeModel.removeNodeFromParent(deathNode);
            }
        } else if (notification.getType() == Notification.Type.MIGRATION) {
            treeModel.removeNodeFromParent(
                    (MutableTreeNode) migrationNode.getChildAt(migrationNotificationList.indexOf(notification)));
            migrationNotificationList.remove(notification);
            migrationNode.setUserObject("Migration" + "(" + migrationNotificationList.size() + ")");
            if (migrationNotificationList.isEmpty()) {
                treeModel.removeNodeFromParent(migrationNode);
            }
        }
        notificationRootNode.setUserObject("Notifications " + "(" + totalNotifications() + ")");
        //notificationTree.repaint();
    }

    private void endCurrentSession() {
        mainViewHelper.endSession();
    }

    private class SearchBasicTask extends org.jdesktop.application.Task<Object, Void> {

        SearchBasicTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            PersonWrapper personWrapper = mainViewHelper.getSearchPersonWrapper();
            try {
                personWrapper.setClinicId(basicSearchClinicIdTextField.getText());
            } catch (MalformedCliniIdException ex) {
                showWarningMessage(ex.getMessage(), basicSearchButton, basicSearchClinicIdTextField);
                return new SearchProcessResult(SearchProcessResult.Type.ABORT, null);
            }
            personWrapper.setClinicName(basicSearchClinicNameTextField.getText());
            personWrapper.setAliveStatus(Person.AliveStatus.yes);
            ImagedFingerprint imagedFingerprint = mainViewHelper.getActiveImagedFingerprint();
            personWrapper.addFingerprint(imagedFingerprint);
            return mainViewHelper.findPerson(Server.MPI_LPI);
        }

        @Override
        protected void succeeded(Object result) {
            SearchProcessResult searchProcessResult = (SearchProcessResult) result;
            if (searchProcessResult.getType() == SearchProcessResult.Type.LIST) {
                showSearchResults(searchProcessResult.getData());
            } else if (searchProcessResult.getType() == SearchProcessResult.Type.NEXT_FINGERPRINT) {
                showFingerprintDialogBasic();
            } else if (searchProcessResult.getType() == SearchProcessResult.Type.EXIT) {
                if (!showConfirmMessage("Your basic search returned no candidates. Would you like"
                        + " to repeat it? Choose Yes to repeat a basic search or No to proceed to"
                        + " an extended search.", extendedSearchButton)) {
                    showCard("extendedSearchCard");
                }
            }
            enableBusyButton(basicSearchButton);
        }
    }

    private void initializeReaderManager(final FingerprintingComponent mainView) {
        //initialize reader in a new thread to prevent gui from hanging.
        Runnable readerInitializer = new Runnable() {

            public void run() {
                showMessage("Preparing fingerprinting software");
                try {
                    System.out.println("Inititializing Griaule");
                    fingerprintManager = ReaderManager.getFingerprintManager(OECReception.fingerprintManager());
                    fingerprintManager.setFingerprintingComponent(mainView);
                    readerAvailable = true;
                    showMessage("Waiting for device");
                    System.out.println("Griaule initialized");
                } catch (MissingFingerprintManagerImpException ex) {
                    Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                    showMessage("Fingerprinting is not available. See log for details.");
                }
            }
        };
        new Thread(readerInitializer).start();
    }

    private void destroyReaderManager() {
        showMessage("Disconneting from device");
        try {
            System.out.println("Destroying Griaule");
            if (fingerprintManager != null) {
                System.out.println("Griaule is not null");
                fingerprintManager.destroy();
                System.out.println("Griaule destroyed");
                showMessage("Disconneted from device");
            } else {
                System.out.println("Griaule is null");
            }
        } catch (FingerprintManagerException ex) {
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
        }
        fingerprintManager = null;
        readerAvailable = false;
    }

    private void destroyReaderManager(boolean synchronously) {
        //destroy reader in a new thread to prevent gui from hanging.
        if (synchronously) {
            destroyReaderManager();
        } else {
            Runnable readerDestroyer = new Runnable() {

                public void run() {
                    destroyReaderManager();
                }
            };
            new Thread(readerDestroyer).start();
        }
    }

    public void showMessage(String message) {
        //do nothing if another search is in progress
        if (!searchStatus.isOn()) {
            if (readerStatusLabel != null) {
                readerStatusLabel.setText(message);
            }
        }
        if (quickSearchManager != null) {
            quickSearchManager.toggleQuickSearchButtons();
        }
    }

    public void showQuickSearchStatus(String message) {
        if (quickSearchMessageLabel != null) {
            quickSearchMessageLabel.setText(message);
        }
    }

    public void showFingerToBeTaken(String message) {
        if (fingerToBeTakenLabel != null) {
            if (!readerAvailable) {
                message = "Fingerprinting is not available.";
            }
            if (searchStatus.isOn()) {
                message = "Search in progress. No fingerprints will be accepted.";
            }
            fingerToBeTakenLabel.setText(message);
        }
    }

    public void showQuality(int quality) {
        //do nothing if another search is in progress
        if (!searchStatus.isOn()) {
            String message = "Unknown quality.";
            switch (quality) {
                case FingerprintManager.HIGH_QUALITY:
                    message = "High quality.";
                    break;
                case FingerprintManager.MEDIUM_QUALITY:
                    message = "Medium quality.";
                    break;
                case FingerprintManager.LOW_QUALITY:
                    message = "Low quality.";
                    break;
            }
            quickSearchQualityTextField.setText(message);
            judgeTemplateQuality(quality);
        }
    }

    private void judgeTemplateQuality(int quality) {
        if (quality == FingerprintManager.HIGH_QUALITY) {
        } else if (quality == FingerprintManager.MEDIUM_QUALITY) {
            if (showConfirmMessage("You captured a medium quality fingerprint. Would you like to try"
                    + " for High Quality? Choose 'Yes' to try for High Quality and 'No' to search using Medium Quality.")) {
                return;
            }
        } else if (quality == FingerprintManager.LOW_QUALITY) {
            showWarningMessage("Low Quality fingerprint captured. Please try for higher quality.", quickSearchFingerprintImagePanel);
            return;
        } else {
            showWarningMessage("Unknown Quality fingerprint captured. Please try for higher quality.", quickSearchFingerprintImagePanel);
            return;
        }
        ImagedFingerprint rightIndex = quickSearchManager.getRightIndex();
        ImagedFingerprint leftIndex = quickSearchManager.getLeftIndex();
        byte[] rightIndexTemplate = rightIndex.getFingerprint().getTemplate();
        byte[] leftIndexTemplate = leftIndex.getFingerprint().getTemplate();
        if (rightIndexTemplate != null && leftIndexTemplate != null) {
            showWarningMessage("No more than two fingerprints are allowed on the quick search"
                    + " page. Please clear or reset to retake.", quickSearchFingerprintImagePanel);
            return;
        } else {
            //simulate an actual gui button click in order to take advamtage of background task
            quickSearchButton.doClick();
        }
    }

    public void showImage(BufferedImage fingerprintImage) {
        //do nothing if another search is in progress
        if (!searchStatus.isOn()) {
            if (fingerprintImage != null) {
                quickSearchFingerprintImagePanel.setImage(fingerprintImage);
            }
        }
    }

    @Action
    public Task searchExtended() {
        disableBusyButton(extendedSearchButton);
        return new SearchExtendedTask(getApplication());






    }

    private class SearchExtendedTask extends org.jdesktop.application.Task<Object, Void> {

        SearchExtendedTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            PersonWrapper personWrapper = mainViewHelper.getSearchPersonWrapper();
            personWrapper.setAliveStatus(Person.AliveStatus.yes);
            try {
                if (mainViewHelper.requiresClinicId()) {
                    personWrapper.setClinicId(extendedSearchClinicIdTextField.getText());
                }
                personWrapper.setFirstName(extendedSearchFirstNameTextField.getText());
                personWrapper.setMiddleName(extendedSearchMiddleNameTextField.getText());
                personWrapper.setLastName(extendedSearchLastNameTextField.getText());
                if (extendedSearchMaleRadioButton.isSelected()) {
                    personWrapper.setSex(Person.Sex.M);
                } else if (extendedSearchFemaleRadioButton.isSelected()) {
                    personWrapper.setSex(Person.Sex.F);
                }
                if (!extendedSearchUnknownBirthdateCheckBox.isSelected()) {
                    String dateToday = new SimpleDateFormat("ddMMyyyy").format(new Date());
                    String selectedDate = new SimpleDateFormat("ddMMyyyy").format(extendedSearchBirthdateChooser.getDate());
                    if (dateToday.equals(selectedDate)) {
                        if (showConfirmMessage("Are you sure you want to set this person's birthdate to today's date? "
                                + "Choose 'Yes' to accept today's date and continue or 'No' to change.")) {
                            personWrapper.setBirthdate(extendedSearchBirthdateChooser.getDate());
                        } else {
                            return new SearchProcessResult(SearchProcessResult.Type.ABORT, null);
                        }
                    } else {
                        personWrapper.setBirthdate(extendedSearchBirthdateChooser.getDate());
                    }
                }
                personWrapper.setOtherName(extendedSearchOtherNameTextField.getText());
                personWrapper.setClanName(extendedSearchClanNameTextField.getText());
                personWrapper.setVillageName(extendedSearchVillageTextField.getText());
                personWrapper.addFingerprint(mainViewHelper.getSession().getActiveImagedFingerprint());
                return mainViewHelper.findPerson(Server.MPI_LPI);
            } catch (MalformedCliniIdException ex) {
                showWarningMessage(ex.getMessage(), extendedSearchButton, extendedSearchClinicIdTextField);
                return new SearchProcessResult(SearchProcessResult.Type.ABORT, null);
            }
        }

        @Override
        protected void succeeded(Object result) {
            SearchProcessResult searchProcessResult = (SearchProcessResult) result;
            if (searchProcessResult.getType() == SearchProcessResult.Type.LIST) {
                showSearchResults((SearchServerResponse) searchProcessResult.getData());
            } else if (searchProcessResult.getType() == SearchProcessResult.Type.NEXT_FINGERPRINT) {
                showFingerprintDialogExtended();
            } else if (searchProcessResult.getType() == SearchProcessResult.Type.EXIT) {
                if (!showConfirmMessage("Your extended search returned no candidates. Would you like"
                        + " to repeat it? Choose Yes to repeat an extended search or No to proceed to"
                        + " register a new client.", extendedSearchButton)) {
                    populateReviewCards(mainViewHelper.getSession().getSearchPersonWrapper());
                    showCard("reviewCard1");
                }
            }
            enableBusyButton(extendedSearchButton);
        }
    }

    public void disableBusyButton(JButton busyButton) {
        busyButton.setEnabled(false);
    }

    public void enableBusyButton(JButton busyButton) {
        busyButton.setEnabled(true);
    }

    @Action
    public void startEnrolledClientSession() {
        if (mainViewHelper.getSession() != null) {
            mainViewHelper.changeSessionClientType(Session.ClientType.ENROLLED);
        } else {
            mainViewHelper.startSession(Session.ClientType.ENROLLED);
        }
        carryQuickPrintsOver(quickSearchManager.getImagedFingerprintList());
        showCard("clinicIdCard");
    }

    @Action
    public void startVisitorClientSession() {
        if (mainViewHelper.getSession() != null) {
            mainViewHelper.changeSessionClientType(Session.ClientType.VISITOR);
        } else {
            mainViewHelper.startSession(Session.ClientType.VISITOR);
        }
        carryQuickPrintsOver(quickSearchManager.getImagedFingerprintList());
        showCard("clinicIdCard");
    }

    @Action
    public void startNewClientSession() {
        if (mainViewHelper.getSession() != null) {
            mainViewHelper.changeSessionClientType(Session.ClientType.NEW);
        } else {
            mainViewHelper.startSession(Session.ClientType.NEW);
        }
        carryQuickPrintsOver(quickSearchManager.getImagedFingerprintList());
        showCard("extendedSearchCard");
    }

    @Action
    public void startTransferInClientSession() {
        if (mainViewHelper.getSession() != null) {
            mainViewHelper.changeSessionClientType(Session.ClientType.TRANSFER_IN);
        } else {
            mainViewHelper.startSession(Session.ClientType.TRANSFER_IN);
        }
        carryQuickPrintsOver(quickSearchManager.getImagedFingerprintList());
        showCard("clinicIdCard");
    }

    public void startUnspecifiedClientSession() {
        if (mainViewHelper.getSession() != null) {
            if (mainViewHelper.getSession().getClientType() != Session.ClientType.UNSPECIFIED) {
                mainViewHelper.changeSessionClientType(Session.ClientType.UNSPECIFIED);
            }
        } else {
            mainViewHelper.startSession(Session.ClientType.UNSPECIFIED);
        }
    }

    private void carryQuickPrintsOver(List<ImagedFingerprint> quickImagedFingerprintList) {
        List<ImagedFingerprint> imagedFingerprintList = new ArrayList<ImagedFingerprint>();
        ImagedFingerprint rightIndex = quickImagedFingerprintList.get(0);
        if (rightIndex.getFingerprint().getTemplate() != null) {
            imagedFingerprintList.add(rightIndex);
        }
        ImagedFingerprint leftIndex = quickImagedFingerprintList.get(1);
        if (leftIndex.getFingerprint().getTemplate() != null) {
            imagedFingerprintList.add(leftIndex);
        }
        mainViewHelper.getSession().getImagedFingerprintList().clear();
        mainViewHelper.getSession().getImagedFingerprintList().addAll(imagedFingerprintList);
    }

    @Action
    public void setKnownClinicIdToYes() {
        mainViewHelper.requireClinicId();
        showCard("basicSearchCard");
    }

    @Action
    public void setKnownClinicIdToNo() {
        mainViewHelper.doNotRequireClinicId();
        showCard("extendedSearchCard");
    }

    private void showSearchResults(SearchServerResponse piListData) {
        showSearchResults(piListData, false);
    }

    private void showSearchResults(SearchServerResponse piListData, boolean lastResort) {
        Binding binding = null;
        List<Person> personList = piListData.getPersonList();
        if (piListData.getServer() == Server.MPI) {
            mainViewHelper.getSession().setMpiResultDisplayed(true);
            if (personList.size() == 1) {
                confirmMatch(new PersonWrapper(personList.get(0)), Server.MPI, true);
            } else {
                binding = bindingGroup.getBinding("mpiBinding");
                binding.unbind();
                mpiSearchResultList.clear();
                mpiSearchResultList.addAll(personList);
                binding.bind();
                mpiResultsTable.repaint();
                TitledBorder mpiResultsPanelBorder = (TitledBorder) mpiResultsPanel.getBorder();
                if (!lastResort) {
                    mpiResultsPanelBorder.setTitle("MPI Results - Regular");
                } else {
                    mpiResultsPanelBorder.setTitle("MPI Results - Last resort");
                }
                mpiResultsPanel.repaint();
                showCard("mpiResultsCard");
            }
        } else if (piListData.getServer() == Server.LPI) {
            mainViewHelper.getSession().setLpiResultDisplayed(true);
            if (personList.size() == 1) {
                confirmMatch(new PersonWrapper(personList.get(0)), Server.LPI, true);
            } else {
                binding = bindingGroup.getBinding("lpiBinding");
                binding.unbind();
                lpiSearchResultList.clear();
                lpiSearchResultList.addAll(personList);
                binding.bind();
                lpiResultsTable.repaint();
                TitledBorder lpiResultsPanelBorder = (TitledBorder) lpiResultsPanel.getBorder();
                if (!lastResort) {
                    lpiResultsPanelBorder.setTitle("LPI Results - Regular");
                } else {
                    lpiResultsPanelBorder.setTitle("LPI Results - Last resort");
                }
                lpiResultsPanel.repaint();
                showCard("lpiResultsCard");
            }
        }
    }

    public void acceptMPIMatch(PersonWrapper personWrapper) {
        mainViewHelper.acceptMatch(Server.MPI, personWrapper);
        List<Person> lpiPersonList = mainViewHelper.getLpiResultList();
        if (!mainViewHelper.isLpiResultDisplayed() && lpiPersonList != null
                && !lpiPersonList.isEmpty()) {
            showSearchResults(new SearchServerResponse(Server.LPI, lpiPersonList));
        } else {
            populateReviewCards(mainViewHelper.getSession().getMpiMatchPersonWrapper(), mainViewHelper.getSession().getLpiMatchPersonWrapper());
            showCard("reviewCard1");
        }
    }

    @Action
    public void confirmMPIMatch() {
        int selectedRow = mpiResultsTable.getSelectedRow();
        if (selectedRow > -1) {
            PersonWrapper personWrapper = new PersonWrapper(mainViewHelper.getMpiResultList().get(selectedRow));
            confirmMatch(personWrapper, Server.MPI);
        } else {
            showWarningMessage("Please select a candidate to confirm.", mpiConfirmButton, mpiResultsTable);
        }
    }

    public void acceptLPIMatch(PersonWrapper personWrapper) {
        mainViewHelper.acceptMatch(Server.LPI, personWrapper);
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        List<Person> mpiPersonList = (List<Person>) mainViewHelper.getSession().getMpiRequestResult().getData();
        String mpiIdentifier = lpiMatchPersonWrapper.getMPIIdentifier();
        if (mpiIdentifier.length() != 0) {
            if (mpiPersonList != null && !mpiPersonList.isEmpty()) {
                for (Person person : mpiPersonList) {
                    if (person.getPersonGuid().equalsIgnoreCase(mpiIdentifier)) {
                        mainViewHelper.getSession().setMpiMatchPersonWrapper(new PersonWrapper(person));
                        break;
                    }
                }
            }
            PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
            if (mpiMatchPersonWrapper != null) {
                //reset mpiIdentifierSearchDone
                //go directly to review screen because you now know who this is in the mpi
                mainViewHelper.getSession().setMpiIdentifierSearchDone(false);
                populateReviewCards(mpiMatchPersonWrapper, lpiMatchPersonWrapper);
                showCard("reviewCard1");
            } else {
                //the person is linked but their mpi data is unavailable
                if (!mainViewHelper.getSession().isMpiIdentifierSearchDone()) {
                    PersonWrapper p = new PersonWrapper(new Person());
                    p.setMPIIdentifier(mpiIdentifier);
                    SearchProcessResult searchProcessResult = mainViewHelper.findPerson(Server.MPI, p);
                    mainViewHelper.getSession().setMpiIdentifierSearchDone(true);
                    if (searchProcessResult.getType() == SearchProcessResult.Type.LIST) {
                        showSearchResults(searchProcessResult.getData());
                    } else {
                        showCard("reviewCard1");
                    }
                } else {
                    populateReviewCards(mpiMatchPersonWrapper, lpiMatchPersonWrapper);
                    showCard("reviewCard1");
                }
            }
        } else {
            if (!mainViewHelper.getSession().isMpiResultDisplayed()) {
                if (mpiPersonList != null
                        && !mpiPersonList.isEmpty()) {
                    showSearchResults(new SearchServerResponse(Server.MPI, mpiPersonList));
                } else {
                    populateReviewCards(mainViewHelper.getSession().getMpiMatchPersonWrapper(), lpiMatchPersonWrapper);
                    showCard("reviewCard1");
                }
            } else {
                populateReviewCards(mainViewHelper.getSession().getMpiMatchPersonWrapper(), lpiMatchPersonWrapper);
                showCard("reviewCard1");
            }
        }
    }

    @Action
    public void confirmLPIMatch() {
        int selectedRow = lpiResultsTable.getSelectedRow();
        if (selectedRow > -1) {
            PersonWrapper personWrapper = new PersonWrapper(mainViewHelper.getLpiResultList().get(selectedRow));
            confirmMatch(personWrapper, Server.LPI);
        } else {
            showWarningMessage("Please select a candidate to confirm.", lpiConfirmButton, lpiResultsTable);
        }
    }

    public void confirmMatch(PersonWrapper personWrapper, int targetIndex) {
        confirmMatch(personWrapper, targetIndex, false);
    }

    public void confirmMatch(PersonWrapper personWrapper, int targetIndex, boolean singleCandidate) {
        String title = "[Score: " + personWrapper.unwrap().getMatchScore()
                + "; Fingerprint: " + (personWrapper.unwrap().isFingerprintMatched() ? "Yes" : "No") + "]";
        if (targetIndex == Server.MPI) {
            title = "Confirm MPI Match - " + title;
        } else if (targetIndex == Server.LPI) {
            title = "Confirm LPI Match - " + title;
        }
        ConfirmationDialog confirmationDialog = new ConfirmationDialog(this.getFrame(), true, personWrapper, title);
        confirmationDialog.setLocationRelativeTo(this.getFrame());
        confirmationDialog.setVisible(true);
        if (personWrapper.isConfirmed()) {
            if (targetIndex == Server.MPI) {
                acceptMPIMatch(personWrapper);
            } else if (targetIndex == Server.LPI) {
                acceptLPIMatch(personWrapper);
            }
        } else {
            if (singleCandidate) {
                if (targetIndex == Server.MPI) {
                    noMPIMatchFound();
                } else if (targetIndex == Server.LPI) {
                    noLPIMatchFound();
                }
            }
        }
    }

    private void populateReviewCards(PersonWrapper personWrapper) {
        populateReviewCards(personWrapper, null);
    }

    private void populateReviewCards(PersonWrapper mpiPersonWrapper, PersonWrapper lpiPersonWrapper) {
        if (mpiPersonWrapper != null
                && lpiPersonWrapper != null) {
            autofilLPIData(lpiPersonWrapper, mpiPersonWrapper);
            populateReviewCardsWithMainData(lpiPersonWrapper);
            populateReviewCardsWithAlternativenData(mpiPersonWrapper);
        } else {
            if (mpiPersonWrapper != null
                    && lpiPersonWrapper == null) {
                populateReviewCardsWithMainData(mpiPersonWrapper);
            } else if (mpiPersonWrapper == null
                    && lpiPersonWrapper != null) {
                populateReviewCardsWithMainData(lpiPersonWrapper);
            }
        }
        hideAllAlternativeFields();
        unhideNecessaryAlternativeFields();
    }

    private void populateReviewCardsWithMainData(PersonWrapper personWrapper) {
        clinicIdTextField.setText(personWrapper.getClinicId());
        firstNameTextField.setText(personWrapper.getFirstName());
        middleNameTextField.setText(personWrapper.getMiddleName());
        lastNameTextField.setText(personWrapper.getLastName());
        maleRadioButton.setSelected(personWrapper.getSex() == Person.Sex.M);
        femaleRadioButton.setSelected(personWrapper.getSex() == Person.Sex.F);
        birthDateChooser.setDate(personWrapper.getBirthdate());
        unknownBirthdateCheckBox.setSelected(personWrapper.getBirthdate() == null);
        maritalStatusComboBox.setSelectedItem(DisplayableMaritalStatus.getDisplayableMaritalStatus(personWrapper.getMaritalStatus()));
        otherNameTextField.setText(personWrapper.getOtherName());
        clanTextField.setText(personWrapper.getClanName());
        villageTextField.setText(personWrapper.getVillageName());
        fathersFirstNameTextField.setText(personWrapper.getFathersFirstName());
        fathersMiddleNameTextField.setText(personWrapper.getFathersMiddleName());
        fathersLastNameTextField.setText(personWrapper.getFathersLastName());
        mothersFirstNameTextField.setText(personWrapper.getMothersFirstName());
        mothersMiddleNameTextField.setText(personWrapper.getMothersMiddleName());
        mothersLastNameTextField.setText(personWrapper.getMothersLastName());
        compoundHeadsFirstNameTextField.setText(personWrapper.getCompoundHeadFirstName());
        compoundHeadsMiddleNameTextField.setText(personWrapper.getCompoundHeadMiddleName());
        compoundHeadsLastNameTextField.setText(personWrapper.getCompoundHeadLastName());
        hdssDataConsentYesRadioButton.setSelected(personWrapper.getConsentSigned() == Person.ConsentSigned.yes);
        hdssDataConsentNoRadioButton.setSelected(personWrapper.getConsentSigned() == Person.ConsentSigned.no);
        hdssDataConsentNoAnswerRadioButton.setSelected(personWrapper.getConsentSigned() == Person.ConsentSigned.notAnswered);
        if (mainViewHelper.getSession() != null) {
            List<ImagedFingerprint> imagedFingerprintList = mainViewHelper.getSession().getImagedFingerprintList();
            if (imagedFingerprintList != null && !imagedFingerprintList.isEmpty()) {
                fingerprintImagePanel.setImage(imagedFingerprintList.get(imagedFingerprintList.size() - 1).getImage());
            }
        }
        clientRefusesCheckBox.setSelected(!mainViewHelper.getSession().isFingerprint());
    }

    private void autofilLPIData(PersonWrapper lpiPersonWrapper, PersonWrapper mpiPersonWrapper) {
        if (lpiPersonWrapper.getClinicId().isEmpty()) {
            try {
                lpiPersonWrapper.setClinicId(mpiPersonWrapper.getClinicId());






            } catch (MalformedCliniIdException ex) {
                //This should not happen at all
                Logger.getLogger(MainView.class.getName()).log(Level.INFO, null, ex);
            }
        }
        if (lpiPersonWrapper.getFirstName().isEmpty()) {
            lpiPersonWrapper.setFirstName(mpiPersonWrapper.getFirstName());
        }
        if (lpiPersonWrapper.getMiddleName().isEmpty()) {
            lpiPersonWrapper.setMiddleName(mpiPersonWrapper.getMiddleName());
        }
        if (lpiPersonWrapper.getLastName().isEmpty()) {
            lpiPersonWrapper.setLastName(mpiPersonWrapper.getLastName());
        }
        if (lpiPersonWrapper.getSex() == null) {
            lpiPersonWrapper.setSex(mpiPersonWrapper.getSex());
        }
        if (lpiPersonWrapper.getBirthdate() == null) {
            lpiPersonWrapper.setBirthdate(mpiPersonWrapper.getBirthdate());
        }
        if (lpiPersonWrapper.getMaritalStatus() == null) {
            lpiPersonWrapper.setMaritalStatus(mpiPersonWrapper.getMaritalStatus());
        }
        if (lpiPersonWrapper.getVillageName().isEmpty()) {
            lpiPersonWrapper.setVillageName(mpiPersonWrapper.getVillageName());
        }
        if (lpiPersonWrapper.getFathersFirstName().isEmpty()) {
            lpiPersonWrapper.setFathersFirstName(mpiPersonWrapper.getFathersFirstName());
        }
        if (lpiPersonWrapper.getFathersMiddleName().isEmpty()) {
            lpiPersonWrapper.setFathersMiddleName(mpiPersonWrapper.getFathersMiddleName());
        }
        if (lpiPersonWrapper.getFathersLastName().isEmpty()) {
            lpiPersonWrapper.setFathersLastName(mpiPersonWrapper.getFathersLastName());
        }
        if (lpiPersonWrapper.getMothersFirstName().isEmpty()) {
            lpiPersonWrapper.setMothersFirstName(mpiPersonWrapper.getMothersFirstName());
        }
        if (lpiPersonWrapper.getMothersMiddleName().isEmpty()) {
            lpiPersonWrapper.setMothersMiddleName(mpiPersonWrapper.getMothersMiddleName());
        }
        if (lpiPersonWrapper.getMothersLastName().isEmpty()) {
            lpiPersonWrapper.setMothersLastName(mpiPersonWrapper.getMothersLastName());
        }
        if (lpiPersonWrapper.getCompoundHeadFirstName().isEmpty()) {
            lpiPersonWrapper.setCompoundHeadsFirstName(mpiPersonWrapper.getCompoundHeadFirstName());
        }
        if (lpiPersonWrapper.getCompoundHeadMiddleName().isEmpty()) {
            lpiPersonWrapper.setCompoundHeadsMiddleName(mpiPersonWrapper.getCompoundHeadMiddleName());
        }
        if (lpiPersonWrapper.getCompoundHeadLastName().isEmpty()) {
            lpiPersonWrapper.setCompoundHeadsLastName(mpiPersonWrapper.getCompoundHeadLastName());
        }
        if (lpiPersonWrapper.getConsentSigned() == null) {
            lpiPersonWrapper.setConsentSigned(mpiPersonWrapper.getConsentSigned());
        }
        if (lpiPersonWrapper.getFingerprintList() == null) {
            lpiPersonWrapper.setFingerprintList(mpiPersonWrapper.getFingerprintList());
        }
    }

    private void populateReviewCardsWithAlternativenData(PersonWrapper personWrapper) {
        altClinicIdTextField.setText(personWrapper.getClinicId());
        altFirstNameTextField.setText(personWrapper.getFirstName());
        altMiddleNameTextField.setText(personWrapper.getMiddleName());
        altLastNameTextField.setText(personWrapper.getLastName());
        altSexTextField.setText(mainViewHelper.getSexString(personWrapper.getSex()));
        if (personWrapper.getBirthdate() != null) {
            altBirthDateTextField.setText(new SimpleDateFormat("dd/MM/yyyy").format(personWrapper.getBirthdate()));
        }
        altMaritalStatusTextField.setText(mainViewHelper.getMaritalStatusString(personWrapper.getMaritalStatus()));
        altOtherNameTextField.setText(personWrapper.getOtherName());
        altClanTextField.setText(personWrapper.getClanName());
        altVillageTextField.setText(personWrapper.getVillageName());
        altFathersFirstNameTextField.setText(personWrapper.getFathersFirstName());
        altFathersMiddleNameTextField.setText(personWrapper.getFathersMiddleName());
        altFathersLastNameTextField.setText(personWrapper.getFathersLastName());
        altMothersFirstNameTextField.setText(personWrapper.getMothersFirstName());
        altMothersMiddleNameTextField.setText(personWrapper.getMothersMiddleName());
        altMothersLastNameTextField.setText(personWrapper.getMothersLastName());
        altCompoundHeadsFirstNameTextField.setText(personWrapper.getCompoundHeadFirstName());
        altCompoundHeadsMiddleNameTextField.setText(personWrapper.getCompoundHeadMiddleName());
        altCompoundHeadsLastNameTextField.setText(personWrapper.getCompoundHeadLastName());
        altHdssDataConsentTextField.setText(mainViewHelper.getConsentSignedString(personWrapper.getConsentSigned()));
    }

    private void unhideNecessaryAlternativeFields() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null
                && lpiMatchPersonWrapper != null) {
            boolean clinicIdVisible = lpiMatchPersonWrapper.getClinicId().equals(mpiMatchPersonWrapper.getClinicId());
            altClinicIdTextField.setVisible(!clinicIdVisible);
            clinicIdAcceptRadioButton.setVisible(!clinicIdVisible);
            clinicIdRejectRadioButton.setVisible(!clinicIdVisible);
            boolean firstNameVisible = lpiMatchPersonWrapper.getFirstName().equals(mpiMatchPersonWrapper.getFirstName());
            altFirstNameTextField.setVisible(!firstNameVisible);
            firstNameAcceptRadioButton.setVisible(!firstNameVisible);
            firstNameRejectRadioButton.setVisible(!firstNameVisible);
            boolean middleNameVisible = lpiMatchPersonWrapper.getMiddleName().equals(mpiMatchPersonWrapper.getMiddleName());
            altMiddleNameTextField.setVisible(!middleNameVisible);
            middleNameAcceptRadioButton.setVisible(!middleNameVisible);
            middleNameRejectRadioButton.setVisible(!middleNameVisible);
            boolean lastNameVisible = lpiMatchPersonWrapper.getLastName().equals(mpiMatchPersonWrapper.getLastName());
            altLastNameTextField.setVisible(!lastNameVisible);
            lastNameAcceptRadioButton.setVisible(!lastNameVisible);
            lastNameRejectRadioButton.setVisible(!lastNameVisible);

            if (lpiMatchPersonWrapper.getSex() != null
                    && mpiMatchPersonWrapper.getSex() != null) {
                boolean sexVisible = lpiMatchPersonWrapper.getSex().equals(mpiMatchPersonWrapper.getSex());
                altSexTextField.setVisible(!sexVisible);
                sexAcceptRadioButton.setVisible(!sexVisible);
                sexRejectRadioButton.setVisible(!sexVisible);
            }

            if (lpiMatchPersonWrapper.getBirthdate() != null
                    && mpiMatchPersonWrapper.getBirthdate() != null) {
                try {
                    Date mainBirthdate = new SimpleDateFormat("dd/MM/yyyy").parse(new SimpleDateFormat("dd/MM/yyyy").format(lpiMatchPersonWrapper.getBirthdate()));
                    Date altBirthdate = new SimpleDateFormat("dd/MM/yyyy").parse(new SimpleDateFormat("dd/MM/yyyy").format(mpiMatchPersonWrapper.getBirthdate()));
                    boolean dateVisible = mainBirthdate.equals(altBirthdate);
                    altBirthDateTextField.setVisible(!dateVisible);
                    birthDateAcceptRadioButton.setVisible(!dateVisible);
                    birthDateRejectRadioButton.setVisible(!dateVisible);






                } catch (ParseException ex) {
                    Logger.getLogger(MainView.class.getName()).log(Level.INFO, null, ex);
                }
            }

            if (lpiMatchPersonWrapper.getMaritalStatus() != null
                    && mpiMatchPersonWrapper.getMaritalStatus() != null) {
                boolean maritalStatusVisible = lpiMatchPersonWrapper.getMaritalStatus().equals(mpiMatchPersonWrapper.getMaritalStatus());
                altMaritalStatusTextField.setVisible(!maritalStatusVisible);
                maritalStatusAcceptRadioButton.setVisible(!maritalStatusVisible);
                maritalStatusRejectRadioButton.setVisible(!maritalStatusVisible);
            }
            boolean otherNameVisible = lpiMatchPersonWrapper.getOtherName().equals(mpiMatchPersonWrapper.getOtherName());
            altOtherNameTextField.setVisible(!otherNameVisible);
            otherNameAcceptRadioButton.setVisible(!otherNameVisible);
            otherNameRejectRadioButton.setVisible(!otherNameVisible);
            boolean clanVisible = lpiMatchPersonWrapper.getClanName().equals(mpiMatchPersonWrapper.getClanName());
            altClanTextField.setVisible(!clanVisible);
            clanAcceptRadioButton.setVisible(!clanVisible);
            clanRejectRadioButton.setVisible(!clanVisible);
            boolean villageVisible = lpiMatchPersonWrapper.getVillageName().equals(mpiMatchPersonWrapper.getVillageName());
            altVillageTextField.setVisible(!villageVisible);
            villageAcceptRadioButton.setVisible(!villageVisible);
            villageRejectRadioButton.setVisible(!villageVisible);
            boolean fathersFirstNameVisible = lpiMatchPersonWrapper.getFathersFirstName().equals(mpiMatchPersonWrapper.getFathersFirstName());
            altFathersFirstNameTextField.setVisible(!fathersFirstNameVisible);
            fathersFirstNameAcceptRadioButton.setVisible(!fathersFirstNameVisible);
            fathersFirstNameRejectRadioButton.setVisible(!fathersFirstNameVisible);
            boolean fathersMiddleNameVisible = lpiMatchPersonWrapper.getFathersMiddleName().equals(mpiMatchPersonWrapper.getFathersMiddleName());
            altFathersMiddleNameTextField.setVisible(!fathersMiddleNameVisible);
            fathersMiddleNameAcceptRadioButton.setVisible(!fathersMiddleNameVisible);
            fathersMiddleNameRejectRadioButton.setVisible(!fathersMiddleNameVisible);
            boolean fathersLastNameVisible = lpiMatchPersonWrapper.getFathersLastName().equals(mpiMatchPersonWrapper.getFathersLastName());
            altFathersLastNameTextField.setVisible(!fathersLastNameVisible);
            fathersLastNameAcceptRadioButton.setVisible(!fathersLastNameVisible);
            fathersLastNameRejectRadioButton.setVisible(!fathersLastNameVisible);
            boolean mothersFirstNameVisible = lpiMatchPersonWrapper.getMothersFirstName().equals(mpiMatchPersonWrapper.getMothersFirstName());
            altMothersFirstNameTextField.setVisible(!mothersFirstNameVisible);
            mothersFirstNameAcceptRadioButton.setVisible(!mothersFirstNameVisible);
            mothersFirstNameRejectRadioButton.setVisible(!mothersFirstNameVisible);
            boolean mothersMiddleNameVisible = lpiMatchPersonWrapper.getMothersMiddleName().equals(mpiMatchPersonWrapper.getMothersMiddleName());
            altMothersMiddleNameTextField.setVisible(!mothersMiddleNameVisible);
            mothersMiddleNameAcceptRadioButton.setVisible(!mothersMiddleNameVisible);
            mothersMiddleNameRejectRadioButton.setVisible(!mothersMiddleNameVisible);
            boolean mothersLastNameVisible = lpiMatchPersonWrapper.getMothersLastName().equals(mpiMatchPersonWrapper.getMothersLastName());
            altMothersLastNameTextField.setVisible(!mothersLastNameVisible);
            mothersLastNameAcceptRadioButton.setVisible(!mothersLastNameVisible);
            mothersLastNameRejectRadioButton.setVisible(!mothersLastNameVisible);
            boolean compoundHeadsFirstNameVisible = lpiMatchPersonWrapper.getCompoundHeadFirstName().equals(mpiMatchPersonWrapper.getCompoundHeadFirstName());
            altCompoundHeadsFirstNameTextField.setVisible(!compoundHeadsFirstNameVisible);
            compoundHeadsFirstNameAcceptRadioButton.setVisible(!compoundHeadsFirstNameVisible);
            compoundHeadsFirstNameRejectRadioButton.setVisible(!compoundHeadsFirstNameVisible);
            boolean compoundHeadsMiddleNameVisible = lpiMatchPersonWrapper.getCompoundHeadMiddleName().equals(mpiMatchPersonWrapper.getCompoundHeadMiddleName());
            altCompoundHeadsMiddleNameTextField.setVisible(!compoundHeadsMiddleNameVisible);
            compoundHeadsMiddleNameAcceptRadioButton.setVisible(!compoundHeadsMiddleNameVisible);
            compoundHeadsMiddleNameRejectRadioButton.setVisible(!compoundHeadsMiddleNameVisible);
            boolean compoundHeadsLastNameVisible = lpiMatchPersonWrapper.getCompoundHeadLastName().equals(mpiMatchPersonWrapper.getCompoundHeadLastName());
            altCompoundHeadsLastNameTextField.setVisible(!compoundHeadsLastNameVisible);
            compoundHeadsLastNameAcceptRadioButton.setVisible(!compoundHeadsLastNameVisible);
            compoundHeadsLastNameRejectRadioButton.setVisible(!compoundHeadsLastNameVisible);

            if (lpiMatchPersonWrapper.getConsentSigned() != null
                    && mpiMatchPersonWrapper.getConsentSigned() != null) {
                boolean hdssDataConsentVisible = lpiMatchPersonWrapper.getConsentSigned().equals(mpiMatchPersonWrapper.getConsentSigned());
                altHdssDataConsentTextField.setVisible(!hdssDataConsentVisible);
                hdssDataConsentAcceptRadioButton.setVisible(!hdssDataConsentVisible);
                hdssDataConsentRejectRadioButton.setVisible(!hdssDataConsentVisible);
            }
        }
    }

    private void hideAllAlternativeFields() {
        altClinicIdTextField.setVisible(false);
        clinicIdAcceptRadioButton.setVisible(false);
        clinicIdRejectRadioButton.setVisible(false);
        altFirstNameTextField.setVisible(false);
        firstNameAcceptRadioButton.setVisible(false);
        firstNameRejectRadioButton.setVisible(false);
        altMiddleNameTextField.setVisible(false);
        middleNameAcceptRadioButton.setVisible(false);
        middleNameRejectRadioButton.setVisible(false);
        altLastNameTextField.setVisible(false);
        lastNameAcceptRadioButton.setVisible(false);
        lastNameRejectRadioButton.setVisible(false);
        altSexTextField.setVisible(false);
        sexAcceptRadioButton.setVisible(false);
        sexRejectRadioButton.setVisible(false);
        altBirthDateTextField.setVisible(false);
        birthDateAcceptRadioButton.setVisible(false);
        birthDateRejectRadioButton.setVisible(false);
        altMaritalStatusTextField.setVisible(false);
        maritalStatusAcceptRadioButton.setVisible(false);
        maritalStatusRejectRadioButton.setVisible(false);
        altOtherNameTextField.setVisible(false);
        otherNameAcceptRadioButton.setVisible(false);
        otherNameRejectRadioButton.setVisible(false);
        altClanTextField.setVisible(false);
        clanAcceptRadioButton.setVisible(false);
        clanRejectRadioButton.setVisible(false);
        altVillageTextField.setVisible(false);
        villageAcceptRadioButton.setVisible(false);
        villageRejectRadioButton.setVisible(false);
        altFathersFirstNameTextField.setVisible(false);
        fathersFirstNameAcceptRadioButton.setVisible(false);
        fathersFirstNameRejectRadioButton.setVisible(false);
        altFathersMiddleNameTextField.setVisible(false);
        fathersMiddleNameAcceptRadioButton.setVisible(false);
        fathersMiddleNameRejectRadioButton.setVisible(false);
        altFathersLastNameTextField.setVisible(false);
        fathersLastNameAcceptRadioButton.setVisible(false);
        fathersLastNameRejectRadioButton.setVisible(false);
        altMothersFirstNameTextField.setVisible(false);
        mothersFirstNameAcceptRadioButton.setVisible(false);
        mothersFirstNameRejectRadioButton.setVisible(false);
        altMothersMiddleNameTextField.setVisible(false);
        mothersMiddleNameAcceptRadioButton.setVisible(false);
        mothersMiddleNameRejectRadioButton.setVisible(false);
        altMothersLastNameTextField.setVisible(false);
        mothersLastNameAcceptRadioButton.setVisible(false);
        mothersLastNameRejectRadioButton.setVisible(false);
        altCompoundHeadsFirstNameTextField.setVisible(false);
        compoundHeadsFirstNameAcceptRadioButton.setVisible(false);
        compoundHeadsFirstNameRejectRadioButton.setVisible(false);
        altCompoundHeadsMiddleNameTextField.setVisible(false);
        compoundHeadsMiddleNameAcceptRadioButton.setVisible(false);
        compoundHeadsMiddleNameRejectRadioButton.setVisible(false);
        altCompoundHeadsLastNameTextField.setVisible(false);
        compoundHeadsLastNameAcceptRadioButton.setVisible(false);
        compoundHeadsLastNameRejectRadioButton.setVisible(false);
        altHdssDataConsentTextField.setVisible(false);
        hdssDataConsentAcceptRadioButton.setVisible(false);
        hdssDataConsentRejectRadioButton.setVisible(false);
    }

    private void clearFields(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                if (component instanceof ImagePanel) {
                    ((ImagePanel) component).setImage(mainViewHelper.getMissingFingerprint().getImage());
                } else {
                    clearFields((Container) component);
                }
            }
            if (component instanceof JTextComponent) {
                ((JTextComponent) component).setText("");
            } else if (component instanceof JToggleButton) {
                ((JToggleButton) component).setSelected(false);
            } else if (component instanceof JDateChooser) {
                ((JDateChooser) component).setDate(new Date());
            } else if (component instanceof JComboBox) {
                ((JComboBox) component).setSelectedItem(null);
            }
        }
        resetState();
    }

    private void resetState() {
        visitedCardList.clear();
        visitedCardList.add("homeCard");
    }

    @Action
    public void showReviewCard2() {
        showCard("reviewCard2");
    }

    @Action
    public void showReviewCard3() {
        showCard("reviewCard3");
    }

    @Action
    public void finish() {
        if (!ensurePreUpdateConfirmation()) {
            return;
        }
        if (!hasSelectedButton(clientTypeButtonGroup)) {
            showWarningMessage("Please specify the type of client whose details you are reviewing.",
                    enrolledRadioButton);
            return;
        } else {
            if (enrolledRadioButton.isSelected()) {
                mainViewHelper.getSession().changeSessionClientType(Session.ClientType.ENROLLED);
            } else if (visitorRadioButton.isSelected()) {
                mainViewHelper.getSession().changeSessionClientType(Session.ClientType.VISITOR);
            } else if (newRadioButton.isSelected()) {
                mainViewHelper.getSession().changeSessionClientType(Session.ClientType.NEW);
            } else if (transferInRadioButton.isSelected()) {
                mainViewHelper.getSession().changeSessionClientType(Session.ClientType.TRANSFER_IN);
            }
        }
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        boolean mpiMatched = mpiMatchPersonWrapper != null;
        boolean lpiMatched = lpiMatchPersonWrapper != null;
        PersonWrapper mpiUpdatePersonWrapper = null;
        PersonWrapper lpiUpdatePersonWrapper = null;
        if (mpiMatched) {
            mpiUpdatePersonWrapper = new PersonWrapper(mpiMatchPersonWrapper.unwrap());
        } else {
            mpiUpdatePersonWrapper = new PersonWrapper(new Person());
        }
        if (lpiMatched) {
            lpiUpdatePersonWrapper = new PersonWrapper(lpiMatchPersonWrapper.unwrap());
        } else {
            lpiUpdatePersonWrapper = new PersonWrapper(new Person());
        }
        List<Fingerprint> updatedFingerprintList = getUpdatedFingerprintList();
        if (updatedFingerprintList != null) {
            if (updatedFingerprintList.isEmpty()) {
                showWarningMessage("This person has no good fingerprints registered. "
                        + "Please take some now.", this.getFrame(), takeButton);
                return;
            }
            int numberOfFingerprintsTaken = updatedFingerprintList.size();
            if (numberOfFingerprintsTaken < OECReception.MINIMUM_FINGERPRINTS_FOR_REGISTRATION) {
                showWarningMessage("This person has taken " + numberOfFingerprintsTaken + " fingerprints instead of the "
                        + "required " + OECReception.MINIMUM_FINGERPRINTS_FOR_REGISTRATION + ". Please take"
                        + " the remaining " + (OECReception.MINIMUM_FINGERPRINTS_FOR_REGISTRATION - numberOfFingerprintsTaken)
                        + ".", this.getFrame(), takeButton);
                return;
            }
        }
        mpiUpdatePersonWrapper.setFingerprintList(updatedFingerprintList);
        lpiUpdatePersonWrapper.setFingerprintList(updatedFingerprintList);
        try {
            setUpUpdatePersonWrapper(mpiUpdatePersonWrapper);
            setUpUpdatePersonWrapper(lpiUpdatePersonWrapper);
        } catch (MalformedCliniIdException ex) {
            showWarningMessage(ex.getMessage(), finishButton, clinicIdTextField);
            showCard("reviewCard1");
            return;
        }
        //we do a last resort search if we have to and haven't done it already
        if (!mainViewHelper.hasLastResortSearchDone()
                && (mainViewHelper.noMPIMatchWasFound()
                || mainViewHelper.noLPIMatchWasFound())) {
            SearchProcessResult searchProcessResult = null;
            if (mainViewHelper.noMPIMatchWasFound()
                    && mainViewHelper.noLPIMatchWasFound()) {
                searchProcessResult = mainViewHelper.findPerson(Server.MPI_LPI, mpiUpdatePersonWrapper, true);
                if (searchProcessResult.getType() == SearchProcessResult.Type.LIST) {
                    showSearchResults(new SearchServerResponse(Server.MPI_LPI, searchProcessResult.getData().getPersonList()), true);
                    return;
                }
            } else if (mainViewHelper.noMPIMatchWasFound()
                    && !mainViewHelper.noLPIMatchWasFound()) {
                searchProcessResult = mainViewHelper.findPerson(Server.MPI, mpiUpdatePersonWrapper, true);
                if (searchProcessResult.getType() == SearchProcessResult.Type.LIST) {
                    showSearchResults(searchProcessResult.getData(), true);
                    return;
                }
            } else if (!mainViewHelper.noMPIMatchWasFound()
                    && mainViewHelper.noLPIMatchWasFound()) {
                searchProcessResult = mainViewHelper.findPerson(Server.LPI, lpiUpdatePersonWrapper, true);
                if (searchProcessResult.getType() == SearchProcessResult.Type.LIST) {
                    showSearchResults(new SearchServerResponse(Server.LPI, searchProcessResult.getData().getPersonList()), true);
                    return;
                }
            }
            mainViewHelper.setLastResortSearchDone(true);
        }
        //update person identifiers
        if (lpiUpdatePersonWrapper.getKisumuHdssId().isEmpty()
                || !lpiUpdatePersonWrapper.getKisumuHdssId().equalsIgnoreCase(mpiUpdatePersonWrapper.getKisumuHdssId())) {
            if (!mpiUpdatePersonWrapper.getKisumuHdssId().isEmpty()) {
                lpiUpdatePersonWrapper.setKisumuHdssId(mpiUpdatePersonWrapper.getKisumuHdssId());
            }
        }
        if (mpiUpdatePersonWrapper.getClinicId().isEmpty()
                || !mpiUpdatePersonWrapper.getClinicId().equalsIgnoreCase(lpiUpdatePersonWrapper.getClinicId())) {
            try {
                mpiUpdatePersonWrapper.setClinicId(mpiUpdatePersonWrapper.getClinicId());
            } catch (MalformedCliniIdException ex) {
                Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!mpiMatched
                && !lpiMatched) {
            mainViewHelper.createPerson(Server.MPI, mpiUpdatePersonWrapper);
            mainViewHelper.createPerson(Server.LPI, lpiUpdatePersonWrapper);
        } else {
            if (!mpiMatched && lpiMatched) {
                mainViewHelper.createPerson(Server.MPI, mpiUpdatePersonWrapper);
                mainViewHelper.modifyPerson(Server.LPI, lpiUpdatePersonWrapper);
            } else if (mpiMatched && !lpiMatched) {
                lpiUpdatePersonWrapper.setMPIIdentifier(mpiUpdatePersonWrapper.getPersonGuid());
                mainViewHelper.modifyPerson(Server.MPI, mpiUpdatePersonWrapper);
                mainViewHelper.createPerson(Server.LPI, lpiUpdatePersonWrapper);
            } else {
                //link mpiMatch with lpiMatch if they have not been linked yet
                if (lpiUpdatePersonWrapper.getMPIIdentifier().isEmpty()) {
                    lpiUpdatePersonWrapper.setMPIIdentifier(mpiUpdatePersonWrapper.getPersonGuid());
                }
                mainViewHelper.modifyPerson(Server.MPI, mpiUpdatePersonWrapper);
                mainViewHelper.modifyPerson(Server.LPI, lpiUpdatePersonWrapper);
            }
        }
        showCard("homeCard", true, false);
    }

    private boolean ensurePreUpdateConfirmation() {
        if (!hasSelectedButton(clinicIdButtonGroup)) {
            showWarningMessage("Please confirm the Clinic ID field before proceeding.",
                    finishButton, clinicIdTextField);
            showCard("reviewCard1");
            return false;
        }
        if (!hasSelectedButton(firstNameButtonGroup)) {
            showWarningMessage("Please confirm the First Name field before proceeding.",
                    finishButton, firstNameTextField);
            showCard("reviewCard1");
            return false;
        }
        if (!hasSelectedButton(middleNameButtonGroup)) {
            showWarningMessage("Please confirm the Middle Name field before proceeding.",
                    finishButton, middleNameTextField);
            showCard("reviewCard1");
            return false;
        }
        if (!hasSelectedButton(lastNameButtonGroup)) {
            showWarningMessage("Please confirm the Last Name field before proceeding.",
                    finishButton, lastNameTextField);
            showCard("reviewCard1");
            return false;
        }
        if (!hasSelectedButton(reviewSexButtonGroup)) {
            showWarningMessage("Please confirm the Sex field before proceeding.",
                    finishButton, maleRadioButton);
            showCard("reviewCard1");
            return false;
        }
        if (!hasSelectedButton(birthDateButtonGroup)) {
            showWarningMessage("Please confirm the Birthdate field before proceeding.",
                    finishButton, birthDateChooser);
            showCard("reviewCard1");
            return false;
        }
        if (!hasSelectedButton(maritalStatusButtonGroup)) {
            showWarningMessage("Please confirm the Marital Status field before proceeding.",
                    finishButton, maritalStatusComboBox);
            showCard("reviewCard1");
            return false;
        }
        if (!hasSelectedButton(otherNameButtonGroup)) {
            showWarningMessage("Please confirm the Other Name field before proceeding.",
                    finishButton, otherNameTextField);
            showCard("reviewCard1");
            return false;
        }
        if (!hasSelectedButton(clanButtonGroup)) {
            showWarningMessage("Please confirm the Clan Name field before proceeding.",
                    finishButton, clanTextField);
            showCard("reviewCard2");
            return false;
        }
        if (!hasSelectedButton(villageButtonGroup)) {
            showWarningMessage("Please confirm the Village Name field before proceeding.",
                    finishButton, villageTextField);
            showCard("reviewCard2");
            return false;
        }
        if (!hasSelectedButton(fathersFirstNameButtonGroup)) {
            showWarningMessage("Please confirm the Father First Name field before proceeding.",
                    finishButton, fathersFirstNameTextField);
            showCard("reviewCard2");
            return false;
        }
        if (!hasSelectedButton(fathersMiddleNameButtonGroup)) {
            showWarningMessage("Please confirm the Father Middle Name field before proceeding.",
                    finishButton, fathersMiddleNameTextField);
            showCard("reviewCard2");
            return false;
        }
        if (!hasSelectedButton(fathersLastNameButtonGroup)) {
            showWarningMessage("Please confirm the Father Last Name field before proceeding.",
                    finishButton, fathersLastNameTextField);
            showCard("reviewCard2");
            return false;
        }
        if (!hasSelectedButton(mothersFirstNameButtonGroup)) {
            showWarningMessage("Please confirm the Mother First Name field before proceeding.",
                    finishButton, mothersFirstNameTextField);
            showCard("reviewCard2");
            return false;
        }
        if (!hasSelectedButton(mothersMiddleNameButtonGroup)) {
            showWarningMessage("Please confirm the Mother Middle Name field before proceeding.",
                    finishButton, mothersMiddleNameTextField);
            showCard("reviewCard2");
            return false;
        }
        if (!hasSelectedButton(mothersLastNameButtonGroup)) {
            showWarningMessage("Please confirm the Mother Last Name field before proceeding.",
                    finishButton, mothersLastNameTextField);
            showCard("reviewCard2");
            return false;
        }
        if (!hasSelectedButton(compoundHeadsFirstNameButtonGroup)) {
            showWarningMessage("Please confirm the Compound Head First Name field before proceeding.",
                    finishButton, compoundHeadsFirstNameTextField);
            showCard("reviewCard3");
            return false;
        }
        if (!hasSelectedButton(compoundHeadsMiddleNameButtonGroup)) {
            showWarningMessage("Please confirm the Compound Head Middle Name field before proceeding.",
                    finishButton, compoundHeadsMiddleNameTextField);
            showCard("reviewCard3");
            return false;
        }
        if (!hasSelectedButton(compoundHeadsLastNameButtonGroup)) {
            showWarningMessage("Please confirm the Compound Head Last Name field before proceeding.",
                    finishButton, compoundHeadsLastNameTextField);
            showCard("reviewCard3");
            return false;
        }
        if (!hasSelectedButton(hdssDataConsentButtonGroup)) {
            showWarningMessage("Please confirm the Consent to share HDSS data field before proceeding.",
                    finishButton, hdssDataConsentYesRadioButton);
            showCard("reviewCard3");
            return false;
        }
        return true;
    }

    private boolean hasSelectedButton(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> enumeration = buttonGroup.getElements(); enumeration.hasMoreElements();) {
            JRadioButton radioButton = (JRadioButton) enumeration.nextElement();
            //check if radio button is available for selection (visible) in the first place
            if (!radioButton.isVisible()) {
                return true;
            }
            if (radioButton.getModel() == buttonGroup.getSelection()) {
                return true;
            }
        }
        return false;
    }

    private void setUpUpdatePersonWrapper(PersonWrapper personWrapper) throws MalformedCliniIdException {
        try {
            personWrapper.setClinicId(clinicIdTextField.getText());
        } catch (MalformedCliniIdException ex) {
            throw ex;
        }
        personWrapper.setFirstName(firstNameTextField.getText());
        personWrapper.setMiddleName(middleNameTextField.getText());
        personWrapper.setLastName(lastNameTextField.getText());
        if (maleRadioButton.isSelected()) {
            personWrapper.setSex(Person.Sex.M);
        } else if (femaleRadioButton.isSelected()) {
            personWrapper.setSex(Person.Sex.F);
        }
        personWrapper.setBirthdate(birthDateChooser.getDate());
        Object displayableMaritalStatus = maritalStatusComboBox.getSelectedItem();
        if (displayableMaritalStatus != null) {
            personWrapper.setMaritalStatus(((DisplayableMaritalStatus) displayableMaritalStatus).getMaritalStatus());
        }
        personWrapper.setOtherName(otherNameTextField.getText());
        personWrapper.setClanName(clanTextField.getText());
        personWrapper.setVillageName(villageTextField.getText());
        personWrapper.setFathersFirstName(fathersFirstNameTextField.getText());
        personWrapper.setFathersMiddleName(fathersMiddleNameTextField.getText());
        personWrapper.setFathersLastName(fathersLastNameTextField.getText());
        personWrapper.setMothersFirstName(mothersFirstNameTextField.getText());
        personWrapper.setMothersMiddleName(mothersMiddleNameTextField.getText());
        personWrapper.setMothersLastName(mothersLastNameTextField.getText());
        personWrapper.setCompoundHeadsFirstName(compoundHeadsFirstNameTextField.getText());
        personWrapper.setCompoundHeadsMiddleName(compoundHeadsMiddleNameTextField.getText());
        personWrapper.setCompoundHeadsLastName(compoundHeadsLastNameTextField.getText());
        if (hdssDataConsentYesRadioButton.isSelected()) {
            personWrapper.setConsentSigned(Person.ConsentSigned.yes);
        } else if (hdssDataConsentNoRadioButton.isSelected()) {
            personWrapper.setConsentSigned(Person.ConsentSigned.no);
        } else if (hdssDataConsentNoAnswerRadioButton.isSelected()) {
            personWrapper.setConsentSigned(Person.ConsentSigned.notAnswered);
        }
        Visit visit = new Visit();
        visit.setAddress(OECReception.applicationAddress());
        visit.setVisitDate(new Date());
        visit.setFacilityName(OECReception.facilityName());
        if (mainViewHelper.getSession().getClientType() == Session.ClientType.ENROLLED
                || mainViewHelper.getSession().getClientType() == Session.ClientType.NEW
                || mainViewHelper.getSession().getClientType() == Session.ClientType.TRANSFER_IN) {
            personWrapper.setLastRegularVisit(visit);
        } else if (mainViewHelper.getSession().getClientType() == Session.ClientType.VISITOR) {
            personWrapper.setLastOneOffVisit(visit);






        } else {
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, "Client type not specified before updating the"
                    + "person index.");
        }
    }

    private List<Fingerprint> getBrandNewFingerprintList() {
        List<Fingerprint> fingerprintList = new ArrayList<Fingerprint>();
        for (ImagedFingerprint imagedFingerprint : mainViewHelper.getSession().getImagedFingerprintList()) {
            Fingerprint fingerprint = imagedFingerprint.getFingerprint();
            if (fingerprint != null) {
                fingerprint.setDateEntered(new Date());
                fingerprintList.add(fingerprint);
            }
        }
        if (mainViewHelper.getSession().isFingerprint()) {
            return fingerprintList;
        } else {
            return null;
        }
    }

    private List<Fingerprint> getMPIFingerprintList() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        List<Fingerprint> fingerprintList = new ArrayList<Fingerprint>();
        if (mpiMatchPersonWrapper != null
                && mpiMatchPersonWrapper.unwrap().getFingerprintList() != null
                && !mpiMatchPersonWrapper.unwrap().getFingerprintList().isEmpty()) {
            fingerprintList = mpiMatchPersonWrapper.unwrap().getFingerprintList();
            for (Fingerprint fingerprint : fingerprintList) {
                fingerprint.setDateChanged(new Date());
            }
        }
        return fingerprintList;
    }

    private List<Fingerprint> getLPIFingerprintList() {
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        List<Fingerprint> fingerprintList = new ArrayList<Fingerprint>();
        if (lpiMatchPersonWrapper != null
                && lpiMatchPersonWrapper.unwrap().getFingerprintList() != null
                && !lpiMatchPersonWrapper.unwrap().getFingerprintList().isEmpty()) {
            fingerprintList = lpiMatchPersonWrapper.unwrap().getFingerprintList();
            for (Fingerprint fingerprint : fingerprintList) {
                fingerprint.setDateChanged(new Date());
            }
        }
        return fingerprintList;
    }

    private List<Fingerprint> getUpdatedFingerprintList() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        boolean mpiMatched = (mpiMatchPersonWrapper != null);
        boolean lpiMatched = (lpiMatchPersonWrapper != null);
        if (!mpiMatched && !lpiMatched) {
            return getBrandNewFingerprintList();
        } else {
            if (mpiMatched && lpiMatched) {
                if (mpiMatchPersonWrapper.unwrap().isFingerprintMatched()
                        && lpiMatchPersonWrapper.unwrap().isFingerprintMatched()) {
                    return null;
                } else {
                    if (!mpiMatchPersonWrapper.unwrap().isFingerprintMatched()
                            && !lpiMatchPersonWrapper.unwrap().isFingerprintMatched()) {
                        return getBrandNewFingerprintList();
                    } else if (mpiMatchPersonWrapper.unwrap().isFingerprintMatched()
                            && !lpiMatchPersonWrapper.unwrap().isFingerprintMatched()) {
                        return getMPIFingerprintList();
                    } else if (!mpiMatchPersonWrapper.unwrap().isFingerprintMatched()
                            && lpiMatchPersonWrapper.unwrap().isFingerprintMatched()) {
                        return getLPIFingerprintList();
                    }
                }
            } else if (mpiMatched && !lpiMatched) {
                if (mpiMatchPersonWrapper.unwrap().isFingerprintMatched()) {
                    return getMPIFingerprintList();
                } else {
                    return getBrandNewFingerprintList();
                }
            } else if (!mpiMatched && lpiMatched) {
                if (lpiMatchPersonWrapper.unwrap().isFingerprintMatched()) {
                    return getLPIFingerprintList();
                } else {
                    return getBrandNewFingerprintList();
                }
            }
        }
        return null;
    }

    @Action
    public void noMPIMatchFound() {
        mainViewHelper.noMatchFound(Server.MPI);
        List<Person> lpiPersonList = mainViewHelper.getLpiResultList();
        if (!mainViewHelper.isLpiResultDisplayed() && lpiPersonList != null && !lpiPersonList.isEmpty()) {
            showSearchResults(new SearchServerResponse(Server.LPI, lpiPersonList));
        } else {
            populateReviewCards(mainViewHelper.getSearchPersonWrapper());
            showCard("reviewCard1");
        }
    }

    @Action
    public void noLPIMatchFound() {
        mainViewHelper.noMatchFound(Server.LPI);
        List<Person> mpiPersonList = mainViewHelper.getMpiResultList();
        if (!mainViewHelper.isMpiResultDisplayed() && mpiPersonList != null && !mpiPersonList.isEmpty()) {
            showSearchResults(new SearchServerResponse(Server.MPI, mpiPersonList));
        } else {
            populateReviewCards(mainViewHelper.getSearchPersonWrapper());
            showCard("reviewCard1");
        }
    }

    @Action
    public void confirmClinicId() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (clinicIdAcceptRadioButton.isSelected()) {
                clinicIdTextField.setText(mpiMatchPersonWrapper.getClinicId());
            } else if (clinicIdRejectRadioButton.isSelected()) {
                clinicIdTextField.setText(lpiMatchPersonWrapper.getClinicId());
            }
        }
    }

    @Action
    public void confirmFirstName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (firstNameAcceptRadioButton.isSelected()) {
                firstNameTextField.setText(mpiMatchPersonWrapper.getFirstName());
            } else if (firstNameRejectRadioButton.isSelected()) {
                firstNameTextField.setText(lpiMatchPersonWrapper.getFirstName());
            }
        }
    }

    @Action
    public void confirmMiddleName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (middleNameAcceptRadioButton.isSelected()) {
                middleNameTextField.setText(mpiMatchPersonWrapper.getMiddleName());
            } else if (middleNameRejectRadioButton.isSelected()) {
                middleNameTextField.setText(lpiMatchPersonWrapper.getMiddleName());
            }
        }
    }

    @Action
    public void confirmLastName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (lastNameAcceptRadioButton.isSelected()) {
                lastNameTextField.setText(mpiMatchPersonWrapper.getLastName());
            } else if (lastNameRejectRadioButton.isSelected()) {
                lastNameTextField.setText(lpiMatchPersonWrapper.getLastName());
            }
        }
    }

    @Action
    public void confirmSex() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (sexAcceptRadioButton.isSelected()) {
                maleRadioButton.setSelected(mpiMatchPersonWrapper.getSex() == Person.Sex.M);
                femaleRadioButton.setSelected(mpiMatchPersonWrapper.getSex() == Person.Sex.F);
            } else if (sexRejectRadioButton.isSelected()) {
                maleRadioButton.setSelected(lpiMatchPersonWrapper.getSex() == Person.Sex.M);
                femaleRadioButton.setSelected(lpiMatchPersonWrapper.getSex() == Person.Sex.F);
            }
        }
    }

    @Action
    public void confirmBirthdate() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (birthDateAcceptRadioButton.isSelected()) {
                birthDateChooser.setDate(mpiMatchPersonWrapper.getBirthdate());
            } else if (birthDateRejectRadioButton.isSelected()) {
                birthDateChooser.setDate(lpiMatchPersonWrapper.getBirthdate());
            }
        }
    }

    @Action
    public void confirmMaritalStatus() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (maritalStatusAcceptRadioButton.isSelected()) {
                maritalStatusComboBox.setSelectedItem(DisplayableMaritalStatus.getDisplayableMaritalStatus(mpiMatchPersonWrapper.getMaritalStatus()));
            } else if (maritalStatusRejectRadioButton.isSelected()) {
                maritalStatusComboBox.setSelectedItem(DisplayableMaritalStatus.getDisplayableMaritalStatus(lpiMatchPersonWrapper.getMaritalStatus()));
            }
        }
    }

    @Action
    public void confirmOtherName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (otherNameAcceptRadioButton.isSelected()) {
                otherNameTextField.setText(mpiMatchPersonWrapper.getOtherName());
            } else if (otherNameRejectRadioButton.isSelected()) {
                otherNameTextField.setText(lpiMatchPersonWrapper.getOtherName());
            }
        }
    }

    @Action
    public void confirmClanName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (clanAcceptRadioButton.isSelected()) {
                clanTextField.setText(mpiMatchPersonWrapper.getClanName());
            } else if (clanRejectRadioButton.isSelected()) {
                clanTextField.setText(lpiMatchPersonWrapper.getClanName());
            }
        }
    }

    @Action
    public void confirmVillageName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (villageAcceptRadioButton.isSelected()) {
                villageTextField.setText(mpiMatchPersonWrapper.getVillageName());
            } else if (villageRejectRadioButton.isSelected()) {
                villageTextField.setText(lpiMatchPersonWrapper.getVillageName());
            }
        }
    }

    @Action
    public void confirmFathersFirstName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (fathersFirstNameAcceptRadioButton.isSelected()) {
                fathersFirstNameTextField.setText(mpiMatchPersonWrapper.getFathersFirstName());
            } else if (fathersFirstNameRejectRadioButton.isSelected()) {
                fathersFirstNameTextField.setText(lpiMatchPersonWrapper.getFathersFirstName());
            }
        }
    }

    @Action
    public void confirmFathersMiddleName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (fathersMiddleNameAcceptRadioButton.isSelected()) {
                fathersMiddleNameTextField.setText(mpiMatchPersonWrapper.getFathersMiddleName());
            } else if (fathersMiddleNameRejectRadioButton.isSelected()) {
                fathersMiddleNameTextField.setText(lpiMatchPersonWrapper.getFathersMiddleName());
            }
        }
    }

    @Action
    public void confirmFathersLastName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (fathersLastNameAcceptRadioButton.isSelected()) {
                fathersLastNameTextField.setText(mpiMatchPersonWrapper.getFathersLastName());
            } else if (fathersLastNameRejectRadioButton.isSelected()) {
                fathersLastNameTextField.setText(lpiMatchPersonWrapper.getFathersLastName());
            }
        }
    }

    @Action
    public void confirmMothersFirstName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (mothersFirstNameAcceptRadioButton.isSelected()) {
                mothersFirstNameTextField.setText(mpiMatchPersonWrapper.getMothersFirstName());
            } else if (mothersFirstNameRejectRadioButton.isSelected()) {
                mothersFirstNameTextField.setText(lpiMatchPersonWrapper.getMothersFirstName());
            }
        }
    }

    @Action
    public void confirmMothersMiddleName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (mothersMiddleNameAcceptRadioButton.isSelected()) {
                mothersMiddleNameTextField.setText(mpiMatchPersonWrapper.getMothersMiddleName());
            } else if (mothersMiddleNameRejectRadioButton.isSelected()) {
                mothersMiddleNameTextField.setText(lpiMatchPersonWrapper.getMothersMiddleName());
            }
        }
    }

    @Action
    public void confirmMothersLastName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (mothersLastNameAcceptRadioButton.isSelected()) {
                mothersLastNameTextField.setText(mpiMatchPersonWrapper.getMothersLastName());
            } else if (mothersLastNameRejectRadioButton.isSelected()) {
                mothersLastNameTextField.setText(lpiMatchPersonWrapper.getMothersLastName());
            }
        }
    }

    @Action
    public void confirmCompoundHeadFirstName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (compoundHeadsFirstNameAcceptRadioButton.isSelected()) {
                compoundHeadsFirstNameTextField.setText(mpiMatchPersonWrapper.getCompoundHeadFirstName());
            } else if (compoundHeadsFirstNameRejectRadioButton.isSelected()) {
                compoundHeadsFirstNameTextField.setText(lpiMatchPersonWrapper.getCompoundHeadFirstName());
            }
        }
    }

    @Action
    public void confirmCompoundHeadMiddleName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (compoundHeadsMiddleNameAcceptRadioButton.isSelected()) {
                compoundHeadsMiddleNameTextField.setText(mpiMatchPersonWrapper.getCompoundHeadMiddleName());
            } else if (compoundHeadsMiddleNameRejectRadioButton.isSelected()) {
                compoundHeadsMiddleNameTextField.setText(lpiMatchPersonWrapper.getCompoundHeadMiddleName());
            }
        }
    }

    @Action
    public void confirmCompoundHeadLastName() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (compoundHeadsLastNameAcceptRadioButton.isSelected()) {
                compoundHeadsLastNameTextField.setText(mpiMatchPersonWrapper.getCompoundHeadLastName());
            } else if (compoundHeadsLastNameRejectRadioButton.isSelected()) {
                compoundHeadsLastNameTextField.setText(lpiMatchPersonWrapper.getCompoundHeadLastName());
            }
        }
    }

    @Action
    public void confirmConsentSigned() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        PersonWrapper lpiMatchPersonWrapper = mainViewHelper.getSession().getLpiMatchPersonWrapper();
        if (mpiMatchPersonWrapper != null) {
            if (sexAcceptRadioButton.isSelected()) {
                hdssDataConsentYesRadioButton.setSelected(mpiMatchPersonWrapper.getConsentSigned() == Person.ConsentSigned.yes);
                hdssDataConsentNoRadioButton.setSelected(mpiMatchPersonWrapper.getConsentSigned() == Person.ConsentSigned.no);
                hdssDataConsentNoAnswerRadioButton.setSelected(mpiMatchPersonWrapper.getConsentSigned() == Person.ConsentSigned.notAnswered);
            } else if (sexRejectRadioButton.isSelected()) {
                hdssDataConsentYesRadioButton.setSelected(lpiMatchPersonWrapper.getConsentSigned() == Person.ConsentSigned.yes);
                hdssDataConsentNoRadioButton.setSelected(lpiMatchPersonWrapper.getConsentSigned() == Person.ConsentSigned.no);
                hdssDataConsentNoAnswerRadioButton.setSelected(lpiMatchPersonWrapper.getConsentSigned() == Person.ConsentSigned.notAnswered);

            }
        }
    }

    @Action
    public void changePassword() {
        try {
            ChangePasswordDialog cpd = new ChangePasswordDialog(this.getFrame(), true,
                    OECReception.getUser());
            cpd.setLocationRelativeTo(this.getFrame());
            cpd.setVisible(true);
        } catch (PersistenceManagerException ex) {
            showErrorMessage(ex.getMessage(), changePasswordMenuItem);
        }
    }

    @Action
    public void managePermissions() {
        if (authenticateAdmin()) {
            ManagePermissionsDialog mpd;
            try {
                mpd = new ManagePermissionsDialog(this.getFrame(), true);
                mpd.setLocationRelativeTo(this.getFrame());
                mpd.setVisible(true);
            } catch (PersistenceManagerException ex) {
                showErrorMessage(ex.getMessage(), managePermissionsMenuItem);
            }
        }
    }

    @Action
    public void addUsers() {
        if (authenticateAdmin()) {
            AddUserDialog aud;
            try {
                aud = new AddUserDialog(this.getFrame(), true);
                aud.setLocationRelativeTo(this.getFrame());
                aud.setVisible(true);
            } catch (PersistenceManagerException ex) {
                showErrorMessage(ex.getMessage(), addUsersMenuItem);
            }
        }
    }

    private boolean authenticateAdmin() {
        SuccessSignal successSignal = new SuccessSignal();
        AdminAuthenticatorDialog aad;
        try {
            aad = new AdminAuthenticatorDialog(this.getFrame(), true, successSignal);
            aad.setLocationRelativeTo(this.getFrame());
            aad.setVisible(true);
        } catch (PersistenceManagerException ex) {
            showErrorMessage(ex.getMessage(), addUsersMenuItem);
        }
        return successSignal.isSuccessful();
    }

    @Action
    public void processNotification() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) notificationTree.getLastSelectedPathComponent();
        if (!node.isRoot()) {
            if (node == null || !node.isLeaf()) {
                showWarningMessage("Please select the notification to process.", processNotificationButton);
            } else {
                Notification notification = (Notification) node.getUserObject();
                NotificationDialog nd = new NotificationDialog(this.getFrame(), true, notification);
                nd.setLocationRelativeTo(this.getFrame());
                nd.setVisible(true);
                if (notification.isFlaggedOff()) {
                    removeNotificationFromTree(notification);
                    statusMessageLabel.setText("1 notification processed. You now have " + totalNotifications()
                            + " notification(s) to process.");
                }
            }
        }
    }

    private int totalNotifications() {
        return pregnancyOutcomeNotificationList.size()
                + pregnancyNotificationList.size()
                + deathNotificationList.size()
                + migrationNotificationList.size();
    }

    @Action
    public void viewHouseholdMembers() {
        PersonWrapper mpiMatchPersonWrapper = mainViewHelper.getSession().getMpiMatchPersonWrapper();
        String kisumuHdssId = mpiMatchPersonWrapper.getKisumuHdssId();
        PersonWrapper personWrapper = new PersonWrapper(new Person());
        personWrapper.setKisumuHdssId(kisumuHdssId);
        SearchProcessResult searchProcessResult = mainViewHelper.findHouseholdMembers(personWrapper);
        if (searchProcessResult.getType() == SearchProcessResult.Type.LIST) {
            HouseholdMembersDialog hmd = new HouseholdMembersDialog(this.getFrame(), true, searchProcessResult.getData().getPersonList());
            hmd.setTitle("Household members of " + mpiMatchPersonWrapper.getLongName());
            hmd.setLocationRelativeTo(this.getFrame());
            hmd.setVisible(true);
        } else if (searchProcessResult.getType() == SearchProcessResult.Type.UNREACHABLE_SERVER) {
            showWarningMessage("Can't contact hdss server!", viewHouseholdButton);
        }
    }

    @Action
    public void showDepartmentsDialog() {
        if (authenticateAdmin()) {
            try {
                DepartmentsDialog dd = new DepartmentsDialog(this.getFrame(), true);
                dd.setLocationRelativeTo(this.getFrame());
                dd.setVisible(true);
            } catch (PersistenceManagerException ex) {
                showWarningMessage("Database malfunction! " + ex.getMessage()
                        + ". Please contact your administrator.", departmentsMenuItem);






            }
        }
    }

    //small class for tracking search status
    private final class SearchStatus {

        private boolean on;

        public SearchStatus(boolean on) {
            this.on = on;
        }

        public boolean isOn() {
            return on;
        }

        public void setOn(boolean on) {
            this.on = on;
        }
    }

    @Action
    public Task quickSearch() {
        return new QuickSearchTask(getApplication());






    }

    private class QuickSearchTask extends org.jdesktop.application.Task<Object, Void> {

        QuickSearchTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            //display search "on" status in own thread
            Runnable searchMessagePlayer = new Runnable() {

                String searchMessage = "Searching. Please wait";
                String dots = "";

                public void run() {
                    int j = 0;
                    //run no more than 50 times even if some funny error preventing searchStatus.setOn(false);
                    //from happening occurs
                    while (searchStatus.isOn()
                            && j < 50) {
                        for (int i = 0; i < 6; i++) {
                            if (searchStatus.isOn()) {
                                showQuickSearchStatus(searchMessage + dots);
                            }
                            dots += ".";
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MainView.class.getName()).log(Level.INFO, null, ex);
                            }
                            j++;
                        }
                        dots = "";
                    }
                }
            };
            new Thread(searchMessagePlayer).start();
            searchStatus.setOn(true);
            toggleSessionStarterButtons();
            startUnspecifiedClientSession();
            PersonWrapper quickSearchPersonWrapper = mainViewHelper.getSession().getSearchPersonWrapper();
            quickSearchPersonWrapper.setAliveStatus(Person.AliveStatus.yes);

            ImagedFingerprint ifp = null;
            ImagedFingerprint rightIndex = quickSearchManager.getRightIndex();
            ImagedFingerprint leftIndex = quickSearchManager.getLeftIndex();
            byte[] rightIndexTemplate = rightIndex.getFingerprint().getTemplate();
            byte[] leftIndexTemplate = leftIndex.getFingerprint().getTemplate();
            if (rightIndexTemplate == null && leftIndexTemplate == null) {
                ifp = rightIndex;
            } else {
                if (rightIndexTemplate == null && leftIndexTemplate != null) {
                    ifp = rightIndex;
                } else if (rightIndexTemplate != null && leftIndexTemplate == null) {
                    ifp = leftIndex;
                } else {
                    showWarningMessage("No more than two fingerprints are allowed on the quick search"
                            + " page. Please clear or reset to retake.", quickSearchFingerprintImagePanel);
                    return new SearchProcessResult(SearchProcessResult.Type.MAX, null);
                }
            }
            try {
                ifp.getFingerprint().setTemplate(fingerprintManager.getData());
                ifp.setImage(quickSearchFingerprintImagePanel.getImage());
                ifp.setQuality(quickSearchQualityTextField.getText());
            } catch (Exception ex) {
                return new SearchProcessResult(SearchProcessResult.Type.ABORT, null);
            }
            mainViewHelper.getSession().getImagedFingerprintList().add(ifp);
            mainViewHelper.getSession().setActiveImagedFingerprint(ifp);
            quickSearchPersonWrapper.addFingerprint(mainViewHelper.getSession().getActiveImagedFingerprint());
            return mainViewHelper.findPerson(Server.MPI_LPI, quickSearchPersonWrapper);
        }

        @Override
        protected void succeeded(Object result) {
            searchStatus.setOn(false);
            toggleSessionStarterButtons();
            SearchProcessResult searchProcessResult = (SearchProcessResult) result;
            if (searchProcessResult.getType() == SearchProcessResult.Type.LIST) {
                showQuickSearchStatus("Candidates found.");
                showSearchResults(searchProcessResult.getData());
            } else if (searchProcessResult.getType() == SearchProcessResult.Type.MAX) {
                showQuickSearchStatus("Meximum quick search fingerprints taken.");
            } else if (searchProcessResult.getType() == SearchProcessResult.Type.ABORT) {
                showQuickSearchStatus("Bad fingerprint. Please retake.");
            } else if (searchProcessResult.getType() == SearchProcessResult.Type.EXIT
                    || searchProcessResult.getType() == SearchProcessResult.Type.NEXT_FINGERPRINT) {
                showQuickSearchStatus("No candidates found.");
            } else if (searchProcessResult.getType() == SearchProcessResult.Type.UNREACHABLE_SERVER) {
                showQuickSearchStatus("Master and Local Person Indices could not be contacted.");
            }
            quickSearchManager.toggleQuickSearchButtons();
        }
    }

    private void toggleSessionStarterButtons() {
        boolean enabled = !searchStatus.isOn();
        enrolledButton.setEnabled(enabled);
        visitorButton.setEnabled(enabled);
        newButton.setEnabled(enabled);
        transferInButton.setEnabled(enabled);
    }

    @Action
    public void clearRightIndex() {
        quickSearchManager.clearRightIndex();
        clearFingerprintImagePanel();
    }

    @Action
    public void clearLeftIndex() {
        quickSearchManager.clearLeftIndex();
        clearFingerprintImagePanel();
    }

    @Action
    public void resertQuickSearch() {
        quickSearchManager.reset();
        clearFingerprintImagePanel();
    }

    private void clearFingerprintImagePanel() {
        quickSearchFingerprintImagePanel.setImage(mainViewHelper.getMissingFingerprint().getImage());
    }

    private class QuickSearchManager {

        private final List<ImagedFingerprint> imagedFingerprintList;

        private QuickSearchManager() {
            imagedFingerprintList = new ArrayList<ImagedFingerprint>();

            Fingerprint fp1 = new Fingerprint();
            fp1.setFingerprintType(Fingerprint.Type.rightIndexFinger);
            fp1.setTechnologyType(Fingerprint.TechnologyType.griauleTemplate);
            fp1.setTemplate(null);
            ImagedFingerprint ifp1 = new ImagedFingerprint(fp1, mainViewHelper.getMissingFingerprint().getImage(), quickSearchQualityTextField.getText());
            imagedFingerprintList.add(ifp1);

            Fingerprint fp2 = new Fingerprint();
            fp2.setFingerprintType(Fingerprint.Type.leftIndexFinger);
            fp2.setTechnologyType(Fingerprint.TechnologyType.griauleTemplate);
            fp2.setTemplate(null);
            ImagedFingerprint ifp2 = new ImagedFingerprint(fp2, mainViewHelper.getMissingFingerprint().getImage(), quickSearchQualityTextField.getText());
            imagedFingerprintList.add(ifp2);
        }

        private List<ImagedFingerprint> getImagedFingerprintList() {
            return imagedFingerprintList;
        }

        private ImagedFingerprint getRightIndex() {
            return imagedFingerprintList.get(0);
        }

        private ImagedFingerprint getLeftIndex() {
            return imagedFingerprintList.get(1);
        }

        private void clearRightIndex() {
            ImagedFingerprint ifp = imagedFingerprintList.get(0);
            ifp.getFingerprint().setTemplate(null);
            ifp.setSent(false);
            ifp.setImage(mainViewHelper.getMissingFingerprint().getImage());
            toggleQuickSearchButtons();
        }

        private void clearLeftIndex() {
            ImagedFingerprint ifp = imagedFingerprintList.get(1);
            ifp.getFingerprint().setTemplate(null);
            ifp.setSent(false);
            ifp.setImage(mainViewHelper.getMissingFingerprint().getImage());
            toggleQuickSearchButtons();
        }

        private void reset() {
            clearRightIndex();
            clearLeftIndex();
        }

        private void toggleQuickSearchButtons() {
            if (imagedFingerprintList.get(0).getFingerprint().getTemplate() != null) {
                clearRightIndexButton.setEnabled(!searchStatus.isOn());
            } else {
                clearRightIndexButton.setEnabled(false);
            }
            if (imagedFingerprintList.get(1).getFingerprint().getTemplate() != null) {
                clearLeftIndexButton.setEnabled(!searchStatus.isOn());
            } else {
                clearLeftIndexButton.setEnabled(false);
            }
            if (imagedFingerprintList.get(0).getFingerprint().getTemplate() != null
                    || imagedFingerprintList.get(1).getFingerprint().getTemplate() != null) {
                resetQuickSearchButton.setEnabled(!searchStatus.isOn());
            } else {
                resetQuickSearchButton.setEnabled(false);
            }
            showFingerToBeTaken("");
            if (!clearRightIndexButton.isEnabled() && !clearLeftIndexButton.isEnabled()) {
                showFingerToBeTaken("Take right-index fingerprint.");
            } else {
                if (clearRightIndexButton.isEnabled() && clearLeftIndexButton.isEnabled()) {
                    showFingerToBeTaken("All required quick search fingerprints have been taken.");
                } else {
                    if (!clearRightIndexButton.isEnabled() && clearLeftIndexButton.isEnabled()) {
                        showFingerToBeTaken("Take right-index fingerprint.");
                    } else if (clearRightIndexButton.isEnabled() && !clearLeftIndexButton.isEnabled()) {
                        showFingerToBeTaken("Take left-index fingerprint.");
                    }
                }
            }
        }
    }

    @Action
    public void deleteUser() {
        if (authenticateAdmin()) {
            DeleteUserDialog dud;
            try {
                dud = new DeleteUserDialog(this.getFrame(), true);
                dud.setLocationRelativeTo(this.getFrame());
                dud.setVisible(true);
            } catch (PersistenceManagerException ex) {
                showErrorMessage(ex.getMessage(), deleteUserMenuItem);
            }
        }
    }

    @Action
    public void resetUserPassword() {
        if (authenticateAdmin()) {
            ResetUserPasswordDialog rupd;
            try {
                rupd = new ResetUserPasswordDialog(this.getFrame(), true);
                rupd.setLocationRelativeTo(this.getFrame());
                rupd.setVisible(true);
            } catch (PersistenceManagerException ex) {
                showErrorMessage(ex.getMessage(), reserPasswordMenuItem);
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addUsersMenuItem;
    private javax.swing.JTextField altBirthDateTextField;
    private javax.swing.JTextField altClanTextField;
    private javax.swing.JTextField altClinicIdTextField;
    private javax.swing.JTextField altCompoundHeadsFirstNameTextField;
    private javax.swing.JTextField altCompoundHeadsLastNameTextField;
    private javax.swing.JTextField altCompoundHeadsMiddleNameTextField;
    private javax.swing.JTextField altFathersFirstNameTextField;
    private javax.swing.JTextField altFathersLastNameTextField;
    private javax.swing.JTextField altFathersMiddleNameTextField;
    private javax.swing.JTextField altFirstNameTextField;
    private javax.swing.ButtonGroup altHdssDataConsentButtonGroup;
    private javax.swing.JTextField altHdssDataConsentTextField;
    private javax.swing.JTextField altLastNameTextField;
    private javax.swing.JTextField altMaritalStatusTextField;
    private javax.swing.JTextField altMiddleNameTextField;
    private javax.swing.JTextField altMothersFirstNameTextField;
    private javax.swing.JTextField altMothersLastNameTextField;
    private javax.swing.JTextField altMothersMiddleNameTextField;
    private javax.swing.JTextField altOtherNameTextField;
    private javax.swing.ButtonGroup altReviewSexButtonGroup;
    private javax.swing.JTextField altSexTextField;
    private javax.swing.JTextField altVillageTextField;
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
    private javax.swing.JRadioButton birthDateAcceptRadioButton;
    private javax.swing.ButtonGroup birthDateButtonGroup;
    private com.toedter.calendar.JDateChooser birthDateChooser;
    private javax.swing.JLabel birthDateLabel;
    private javax.swing.JRadioButton birthDateRejectRadioButton;
    private javax.swing.JMenuItem changePasswordMenuItem;
    private javax.swing.JRadioButton clanAcceptRadioButton;
    private javax.swing.ButtonGroup clanButtonGroup;
    private javax.swing.JLabel clanLabel;
    private javax.swing.JRadioButton clanRejectRadioButton;
    private javax.swing.JTextField clanTextField;
    private javax.swing.JButton clearLeftIndexButton;
    private javax.swing.JButton clearRightIndexButton;
    private javax.swing.JPanel clientIdPanel;
    private javax.swing.JCheckBox clientRefusesCheckBox;
    private javax.swing.ButtonGroup clientTypeButtonGroup;
    private javax.swing.JLabel clientTypeLabel;
    private javax.swing.JRadioButton clinicIdAcceptRadioButton;
    private javax.swing.ButtonGroup clinicIdButtonGroup;
    private javax.swing.JPanel clinicIdCard;
    private javax.swing.JLabel clinicIdLabel;
    private javax.swing.JButton clinicIdNoButton;
    private javax.swing.JRadioButton clinicIdRejectRadioButton;
    private javax.swing.JTextField clinicIdTextField;
    private javax.swing.JButton clinicIdYesButton;
    private javax.swing.JRadioButton compoundHeadsFirstNameAcceptRadioButton;
    private javax.swing.ButtonGroup compoundHeadsFirstNameButtonGroup;
    private javax.swing.JLabel compoundHeadsFirstNameLabel;
    private javax.swing.JRadioButton compoundHeadsFirstNameRejectRadioButton;
    private javax.swing.JTextField compoundHeadsFirstNameTextField;
    private javax.swing.JRadioButton compoundHeadsLastNameAcceptRadioButton;
    private javax.swing.ButtonGroup compoundHeadsLastNameButtonGroup;
    private javax.swing.JLabel compoundHeadsLastNameLabel;
    private javax.swing.JRadioButton compoundHeadsLastNameRejectRadioButton;
    private javax.swing.JTextField compoundHeadsLastNameTextField;
    private javax.swing.JRadioButton compoundHeadsMiddleNameAcceptRadioButton;
    private javax.swing.ButtonGroup compoundHeadsMiddleNameButtonGroup;
    private javax.swing.JLabel compoundHeadsMiddleNameLabel;
    private javax.swing.JRadioButton compoundHeadsMiddleNameRejectRadioButton;
    private javax.swing.JTextField compoundHeadsMiddleNameTextField;
    private javax.swing.JMenuItem deleteUserMenuItem;
    private javax.swing.JMenuItem departmentsMenuItem;
    private javax.swing.JButton enrolledButton;
    private javax.swing.JRadioButton enrolledRadioButton;
    private com.toedter.calendar.JDateChooser extendedSearchBirthdateChooser;
    private javax.swing.JLabel extendedSearchBirthdateLabel;
    private javax.swing.JButton extendedSearchButton;
    private javax.swing.JPanel extendedSearchCard;
    private javax.swing.JLabel extendedSearchClanNameLabel;
    private javax.swing.JTextField extendedSearchClanNameTextField;
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
    private javax.swing.JLabel extendedSearchOtherNameLabel;
    private javax.swing.JTextField extendedSearchOtherNameTextField;
    private javax.swing.JPanel extendedSearchPanel;
    private javax.swing.ButtonGroup extendedSearchSexButtonGroup;
    private javax.swing.JLabel extendedSearchSexLabel;
    private javax.swing.JButton extendedSearchTakeButton;
    private javax.swing.JCheckBox extendedSearchUnknownBirthdateCheckBox;
    private javax.swing.JLabel extendedSearchVillageLabel;
    private javax.swing.JTextField extendedSearchVillageTextField;
    private javax.swing.JRadioButton fathersFirstNameAcceptRadioButton;
    private javax.swing.ButtonGroup fathersFirstNameButtonGroup;
    private javax.swing.JLabel fathersFirstNameLabel;
    private javax.swing.JRadioButton fathersFirstNameRejectRadioButton;
    private javax.swing.JTextField fathersFirstNameTextField;
    private javax.swing.JRadioButton fathersLastNameAcceptRadioButton;
    private javax.swing.ButtonGroup fathersLastNameButtonGroup;
    private javax.swing.JLabel fathersLastNameLabel;
    private javax.swing.JRadioButton fathersLastNameRejectRadioButton;
    private javax.swing.JTextField fathersLastNameTextField;
    private javax.swing.JRadioButton fathersMiddleNameAcceptRadioButton;
    private javax.swing.ButtonGroup fathersMiddleNameButtonGroup;
    private javax.swing.JLabel fathersMiddleNameLabel;
    private javax.swing.JRadioButton fathersMiddleNameRejectRadioButton;
    private javax.swing.JTextField fathersMiddleNameTextField;
    private javax.swing.JRadioButton femaleRadioButton;
    private javax.swing.JLabel fingerToBeTakenLabel;
    private ke.go.moh.oec.reception.gui.custom.ImagePanel fingerprintImagePanel;
    private javax.swing.JLabel fingerprintLabel;
    private javax.swing.JButton finishButton;
    private javax.swing.JRadioButton firstNameAcceptRadioButton;
    private javax.swing.ButtonGroup firstNameButtonGroup;
    private javax.swing.JLabel firstNameLabel;
    private javax.swing.JRadioButton firstNameRejectRadioButton;
    private javax.swing.JTextField firstNameTextField;
    private javax.swing.JRadioButton hdssDataConsentAcceptRadioButton;
    private javax.swing.ButtonGroup hdssDataConsentButtonGroup;
    private javax.swing.JLabel hdssDataConsentLabel;
    private javax.swing.JRadioButton hdssDataConsentNoAnswerRadioButton;
    private javax.swing.JRadioButton hdssDataConsentNoRadioButton;
    private javax.swing.JRadioButton hdssDataConsentRejectRadioButton;
    private javax.swing.JRadioButton hdssDataConsentYesRadioButton;
    private javax.swing.JButton homeButton;
    private javax.swing.JPanel homeCard;
    private javax.swing.JPanel homePanel;
    private javax.swing.JRadioButton lastNameAcceptRadioButton;
    private javax.swing.ButtonGroup lastNameButtonGroup;
    private javax.swing.JLabel lastNameLabel;
    private javax.swing.JRadioButton lastNameRejectRadioButton;
    private javax.swing.JTextField lastNameTextField;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JLabel loggedInLabel;
    private javax.swing.JButton lpiConfirmButton;
    private javax.swing.JButton lpiNotFoundButton;
    private javax.swing.JPanel lpiResultsCard;
    private javax.swing.JPanel lpiResultsPanel;
    private javax.swing.JScrollPane lpiResultsScrollPane;
    private javax.swing.JTable lpiResultsTable;
    private java.util.List<ke.go.moh.oec.Person> lpiSearchResultList;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JRadioButton maleRadioButton;
    private javax.swing.JMenuItem managePermissionsMenuItem;
    private javax.swing.JMenu manageUsersMenu;
    private javax.swing.JRadioButton maritalStatusAcceptRadioButton;
    private javax.swing.ButtonGroup maritalStatusButtonGroup;
    private javax.swing.JComboBox maritalStatusComboBox;
    private javax.swing.JLabel maritalStatusLabel;
    private java.util.List<DisplayableMaritalStatus> maritalStatusList;
    private javax.swing.JRadioButton maritalStatusRejectRadioButton;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButton middleNameAcceptRadioButton;
    private javax.swing.ButtonGroup middleNameButtonGroup;
    private javax.swing.JLabel middleNameLabel;
    private javax.swing.JRadioButton middleNameRejectRadioButton;
    private javax.swing.JTextField middleNameTextField;
    private javax.swing.JRadioButton mothersFirstNameAcceptRadioButton;
    private javax.swing.ButtonGroup mothersFirstNameButtonGroup;
    private javax.swing.JLabel mothersFirstNameLabel;
    private javax.swing.JRadioButton mothersFirstNameRejectRadioButton;
    private javax.swing.JTextField mothersFirstNameTextField;
    private javax.swing.JRadioButton mothersLastNameAcceptRadioButton;
    private javax.swing.ButtonGroup mothersLastNameButtonGroup;
    private javax.swing.JLabel mothersLastNameLabel;
    private javax.swing.JRadioButton mothersLastNameRejectRadioButton;
    private javax.swing.JTextField mothersLastNameTextField;
    private javax.swing.JRadioButton mothersMiddleNameAcceptRadioButton;
    private javax.swing.ButtonGroup mothersMiddleNameButtonGroup;
    private javax.swing.JLabel mothersMiddleNameLabel;
    private javax.swing.JRadioButton mothersMiddleNameRejectRadioButton;
    private javax.swing.JTextField mothersMiddleNameTextField;
    private javax.swing.JButton mpiConfirmButton;
    private javax.swing.JButton mpiNotFoundButton;
    private javax.swing.JPanel mpiResultsCard;
    private javax.swing.JPanel mpiResultsPanel;
    private javax.swing.JScrollPane mpiResultsScrollPane;
    private javax.swing.JTable mpiResultsTable;
    private java.util.List<ke.go.moh.oec.Person> mpiSearchResultList;
    private javax.swing.JButton newButton;
    private javax.swing.JRadioButton newRadioButton;
    private javax.swing.JPanel notificationListPanel;
    private javax.swing.JScrollPane notificationScrollPane;
    private javax.swing.JTree notificationTree;
    private javax.swing.JRadioButton otherNameAcceptRadioButton;
    private javax.swing.ButtonGroup otherNameButtonGroup;
    private javax.swing.JLabel otherNameLabel;
    private javax.swing.JRadioButton otherNameRejectRadioButton;
    private javax.swing.JTextField otherNameTextField;
    private javax.swing.JButton processNotificationButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton quickSearchButton;
    private ke.go.moh.oec.reception.gui.custom.ImagePanel quickSearchFingerprintImagePanel;
    private javax.swing.JLabel quickSearchMessageLabel;
    private javax.swing.JPanel quickSearchPanel;
    private javax.swing.JTextField quickSearchQualityTextField;
    private javax.swing.JLabel readerStatusLabel;
    private javax.swing.JMenuItem reserPasswordMenuItem;
    private javax.swing.JButton resetQuickSearchButton;
    private javax.swing.JButton review2NextButton;
    private javax.swing.JPanel reviewCard1;
    private javax.swing.JButton reviewCard1NextButton;
    private javax.swing.JPanel reviewCard2;
    private javax.swing.JPanel reviewCard3;
    private javax.swing.JPanel reviewPanel1;
    private javax.swing.JPanel reviewPanel2;
    private javax.swing.JPanel reviewPanel3;
    private javax.swing.ButtonGroup reviewSexButtonGroup;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JRadioButton sexAcceptRadioButton;
    private javax.swing.JLabel sexLabel;
    private javax.swing.JRadioButton sexRejectRadioButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton takeButton;
    private javax.swing.JMenu themesMenu;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JButton transferInButton;
    private javax.swing.JRadioButton transferInRadioButton;
    private javax.swing.JCheckBox unknownBirthdateCheckBox;
    private javax.swing.JMenu usersMenu;
    private javax.swing.JButton viewHouseholdButton;
    private javax.swing.JRadioButton villageAcceptRadioButton;
    private javax.swing.ButtonGroup villageButtonGroup;
    private javax.swing.JLabel villageLabel;
    private javax.swing.JRadioButton villageRejectRadioButton;
    private javax.swing.JTextField villageTextField;
    private javax.swing.JButton visitorButton;
    private javax.swing.JRadioButton visitorRadioButton;
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
