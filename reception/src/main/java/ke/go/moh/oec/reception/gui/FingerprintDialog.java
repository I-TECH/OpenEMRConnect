/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OpenEMRConnect.
 *
 * The Initial Developer of the Original Code is International Training &
 * Education Center for Health (I-TECH) <http://www.go2itech.org/>
 *
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */
/*
 * FingerprintDialog.java
 *
 * Created on May 26, 2011, 9:30:59 AM
 */
package ke.go.moh.oec.reception.gui;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Fingerprint.TechnologyType;
import ke.go.moh.oec.fingerprintmanager.FingerprintManager;
import ke.go.moh.oec.fingerprintmanager.FingerprintManagerException;
import ke.go.moh.oec.fingerprintmanager.FingerprintingComponent;
import ke.go.moh.oec.fingerprintmanager.MissingFingerprintManagerImpException;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import ke.go.moh.oec.reception.data.Session;
import ke.go.moh.oec.reception.gui.helper.DialogEscaper;
import ke.go.moh.oec.reception.reader.ReaderManager;
import org.jdesktop.application.Action;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class FingerprintDialog extends javax.swing.JDialog implements FingerprintingComponent {

    private static final long serialVersionUID = 1L;
    private FingerprintManager fingerprintManager;
    private Session session;
    private final ImagedFingerprint missingFingerprint;
    private final static List<ImagedFingerprint> imagedFingerprintCache = new ArrayList<ImagedFingerprint>();
    private boolean readerAvailable = false;

    public void setSession(Session session) {
        this.session = session;
        switch (session.getImagedFingerprintList().size() - 1) {
            case -1:
                rightIndexRadioButton.setSelected(true);
                break;
            case 0:
                leftIndexRadioButton.setSelected(true);
                break;
            case 1:
                rightMiddleRadioButton.setSelected(true);
                break;
            case 2:
                leftMiddleRadioButton.setSelected(true);
                break;
            case 3:
                rightRingRadioButton.setSelected(true);
                break;
            case 4:
                leftRingRadioButton.setSelected(true);
                break;
            default:
                rightIndexRadioButton.setSelected(true);
        }
        imagedFingerprintCache.addAll(session.getImagedFingerprintList());
        showTakenFingerprint();
    }

    /** Creates new form FingerprintDialog
     * @param parent
     * @param modal 
     * @param missingFingerprint  
     */
    public FingerprintDialog(java.awt.Frame parent, boolean modal,
            ImagedFingerprint missingFingerprint) {
        super(parent, modal);
        initComponents();
        this.setIconImage(OECReception.applicationIcon());
        this.missingFingerprint = missingFingerprint;
        this.getRootPane().setDefaultButton(okButton);
        addEscapeListener();
        initReaderManager();
    }

    public static void clearImagedFingerprintCache() {
        imagedFingerprintCache.clear();
    }

    private void addEscapeListener() {
        DialogEscaper.addEscapeListener(this);
    }

    private void initReaderManager() {
        initializeReaderManager(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fingerButtonGroup = new javax.swing.ButtonGroup();
        fingerprintPanel = new javax.swing.JPanel();
        fingerPanel = new javax.swing.JPanel();
        rightIndexRadioButton = new javax.swing.JRadioButton();
        leftIndexRadioButton = new javax.swing.JRadioButton();
        rightMiddleRadioButton = new javax.swing.JRadioButton();
        leftMiddleRadioButton = new javax.swing.JRadioButton();
        rightRingRadioButton = new javax.swing.JRadioButton();
        leftRingRadioButton = new javax.swing.JRadioButton();
        fingerprintImagePanel = new ke.go.moh.oec.reception.gui.custom.ImagePanel();
        qualityTextField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        clearAllButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Take Fingerprint");
        setResizable(false);

        fingerprintPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Fingerprint"));

        fingerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Finger"));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getActionMap(FingerprintDialog.class, this);
        rightIndexRadioButton.setAction(actionMap.get("showTakenFingerprint")); // NOI18N
        fingerButtonGroup.add(rightIndexRadioButton);
        rightIndexRadioButton.setText("Right index");

        leftIndexRadioButton.setAction(actionMap.get("showTakenFingerprint")); // NOI18N
        fingerButtonGroup.add(leftIndexRadioButton);
        leftIndexRadioButton.setText("Left index");

        rightMiddleRadioButton.setAction(actionMap.get("showTakenFingerprint")); // NOI18N
        fingerButtonGroup.add(rightMiddleRadioButton);
        rightMiddleRadioButton.setText("Right middle");

        leftMiddleRadioButton.setAction(actionMap.get("showTakenFingerprint")); // NOI18N
        fingerButtonGroup.add(leftMiddleRadioButton);
        leftMiddleRadioButton.setText("Left middle");

        rightRingRadioButton.setAction(actionMap.get("showTakenFingerprint")); // NOI18N
        fingerButtonGroup.add(rightRingRadioButton);
        rightRingRadioButton.setText("Right ring");

        leftRingRadioButton.setAction(actionMap.get("showTakenFingerprint")); // NOI18N
        fingerButtonGroup.add(leftRingRadioButton);
        leftRingRadioButton.setText("Left ring");

        org.jdesktop.layout.GroupLayout fingerPanelLayout = new org.jdesktop.layout.GroupLayout(fingerPanel);
        fingerPanel.setLayout(fingerPanelLayout);
        fingerPanelLayout.setHorizontalGroup(
            fingerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fingerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fingerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rightIndexRadioButton)
                    .add(leftIndexRadioButton)
                    .add(rightMiddleRadioButton)
                    .add(leftMiddleRadioButton)
                    .add(rightRingRadioButton)
                    .add(leftRingRadioButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fingerPanelLayout.setVerticalGroup(
            fingerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fingerPanelLayout.createSequentialGroup()
                .add(rightIndexRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(leftIndexRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rightMiddleRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(leftMiddleRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rightRingRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(leftRingRadioButton))
        );

        fingerprintImagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.jdesktop.layout.GroupLayout fingerprintImagePanelLayout = new org.jdesktop.layout.GroupLayout(fingerprintImagePanel);
        fingerprintImagePanel.setLayout(fingerprintImagePanelLayout);
        fingerprintImagePanelLayout.setHorizontalGroup(
            fingerprintImagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 93, Short.MAX_VALUE)
        );
        fingerprintImagePanelLayout.setVerticalGroup(
            fingerprintImagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 97, Short.MAX_VALUE)
        );

        qualityTextField.setEditable(false);

        org.jdesktop.layout.GroupLayout fingerprintPanelLayout = new org.jdesktop.layout.GroupLayout(fingerprintPanel);
        fingerprintPanel.setLayout(fingerprintPanelLayout);
        fingerprintPanelLayout.setHorizontalGroup(
            fingerprintPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fingerprintPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fingerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fingerprintPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(fingerprintImagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(qualityTextField))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fingerprintPanelLayout.setVerticalGroup(
            fingerprintPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fingerprintPanelLayout.createSequentialGroup()
                .add(fingerprintPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fingerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fingerprintPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(fingerprintImagePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qualityTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        okButton.setAction(actionMap.get("addFingerprint")); // NOI18N

        cancelButton.setAction(actionMap.get("cancel")); // NOI18N

        clearAllButton.setAction(actionMap.get("clearAllFingerprints")); // NOI18N
        clearAllButton.setText("Clear all");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(okButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cancelButton)))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(clearAllButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(28, 28, 28))))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(fingerprintPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(11, Short.MAX_VALUE)))
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(177, Short.MAX_VALUE)
                .add(clearAllButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(fingerprintPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(55, Short.MAX_VALUE)))
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, clearAllButton, okButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearAllButton;
    private javax.swing.ButtonGroup fingerButtonGroup;
    private javax.swing.JPanel fingerPanel;
    private ke.go.moh.oec.reception.gui.custom.ImagePanel fingerprintImagePanel;
    private javax.swing.JPanel fingerprintPanel;
    private javax.swing.JRadioButton leftIndexRadioButton;
    private javax.swing.JRadioButton leftMiddleRadioButton;
    private javax.swing.JRadioButton leftRingRadioButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField qualityTextField;
    private javax.swing.JRadioButton rightIndexRadioButton;
    private javax.swing.JRadioButton rightMiddleRadioButton;
    private javax.swing.JRadioButton rightRingRadioButton;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

    public void showMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    public void showImage(BufferedImage fingerprintImage, boolean taken) {
        if (fingerprintImage != null) {
            fingerprintImagePanel.setImage(fingerprintImage);
            if (fingerprintImage != missingFingerprint.getImage()
                    && !taken) {
                cacheImagedFingerprint();
            }
        }
    }

    public void showImage(BufferedImage fingerprintImage) {
        showImage(fingerprintImage, false);
    }

    public void showQuality(int quality) {
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
        qualityTextField.setText(message);
    }

    @Action
    public void cancel() {
        this.dispose();
    }

    @Override
    public void dispose() {
        this.setVisible(false);
        destroyReaderManager();
        super.dispose();
    }

    @Action
    public void addFingerprint() {
        Fingerprint fingerPrint = new Fingerprint();
        if (rightIndexRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.rightIndexFinger);
        } else if (leftIndexRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.leftIndexFinger);
        } else if (rightMiddleRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.rightMiddleFinger);
        } else if (leftMiddleRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.leftMiddleFinger);
        } else if (rightRingRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.rightRingFinger);
        } else if (leftRingRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.leftRingFinger);
        }
        for (ImagedFingerprint imagedFingerprint : imagedFingerprintCache) {
            if (imagedFingerprint.getFingerprint().getFingerprintType() == fingerPrint.getFingerprintType()) {
                List<ImagedFingerprint> imagedFingerprintList = session.getImagedFingerprintList();
                if (imagedFingerprintList.contains(imagedFingerprint)) {
                    imagedFingerprintList.remove(imagedFingerprintList.indexOf(imagedFingerprint));
                }
                int validate = validateFingerprint(imagedFingerprint);
                if (validate == 0) {
                    imagedFingerprintList.add(imagedFingerprint);
                    session.setActiveImagedFingerprint(imagedFingerprint);
                } else if (validate == -1) {
                    showWarningMessage("Bad fingerprint. Please retake.", this, okButton);
                    return;
                } else if (validate == 1) {
                    if (showConfirmMessage("You captured a medium quality fingerprint. Would you like to try"
                            + " for High Quality? Choose 'Yes' to try for High Quality and 'No' to accept Medium Quality.", this)) {
                        return;
                    } else {
                        imagedFingerprintList.add(imagedFingerprint);
                        session.setActiveImagedFingerprint(imagedFingerprint);
                    }
                }
            }
        }
        dispose();
    }

    private int validateFingerprint(ImagedFingerprint imagedFingerprint) {
        if (imagedFingerprint == null || imagedFingerprint.getFingerprint() == null) {
            return -1;
        }
        if (imagedFingerprint.getFingerprint().getTemplate() == null
                || imagedFingerprint.getQuality().equalsIgnoreCase("Bad quality.")) {
            return -1;
        }
        if (imagedFingerprint.getQuality().equalsIgnoreCase("Medium quality.")) {
            return 1;
        }
        return 0;
    }

    private void showWarningMessage(String message, Component parent, JComponent toFocus) {
        JOptionPane.showMessageDialog(parent, message, OECReception.applicationName(), JOptionPane.WARNING_MESSAGE);
        toFocus.requestFocus();
    }

    public boolean showConfirmMessage(String message, Component parent) {
        return JOptionPane.showConfirmDialog(this, message, OECReception.applicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void cacheImagedFingerprint() {
        Fingerprint fingerPrint = new Fingerprint();
        if (rightIndexRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.rightIndexFinger);
        } else if (leftIndexRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.leftIndexFinger);
        } else if (rightMiddleRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.rightMiddleFinger);
        } else if (leftMiddleRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.leftMiddleFinger);
        } else if (rightRingRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.rightRingFinger);
        } else if (leftRingRadioButton.isSelected()) {
            fingerPrint.setFingerprintType(Fingerprint.Type.leftRingFinger);
        }
        fingerPrint.setTechnologyType(TechnologyType.griauleTemplate);
        ImagedFingerprint imagedFingerprint = new ImagedFingerprint(fingerPrint, fingerprintImagePanel.getImage(), qualityTextField.getText(), false);
        if (fingerprintManager != null && fingerprintManager.getData() != null) {
            fingerPrint.setTemplate(fingerprintManager.getData());
        } else {
            return;
        }
        if (!imagedFingerprintCache.contains(imagedFingerprint)) {
            imagedFingerprintCache.add(imagedFingerprint);
        } else {
            ImagedFingerprint oldImagedFingerprint = imagedFingerprintCache.get(imagedFingerprintCache.indexOf(imagedFingerprint));
            if (showConfirmMessage("A print has already been taken from the finger you just"
                    + " took. Would you like to overwite it?", this)) {
                imagedFingerprintCache.remove(oldImagedFingerprint);
                imagedFingerprintCache.add(imagedFingerprint);
                if (session.getImagedFingerprintList().contains(oldImagedFingerprint)) {
                    session.getImagedFingerprintList().remove(oldImagedFingerprint);
                }
                showImage(imagedFingerprint.getImage(), true);
                showQuality(imagedFingerprint.getQuality());
            } else {
                showImage(oldImagedFingerprint.getImage(), true);
                showQuality(oldImagedFingerprint.getQuality());
            }
        }
    }

    private void showQuality(String quality) {
        qualityTextField.setText(quality);
    }

    @Action
    public void showTakenFingerprint() {
        List<ImagedFingerprint> imagedFingerprintList = session.getImagedFingerprintList();
        Fingerprint fingerprint = new Fingerprint();
        if (rightIndexRadioButton.isSelected()) {
            fingerprint.setFingerprintType(Fingerprint.Type.rightIndexFinger);
        } else if (leftIndexRadioButton.isSelected()) {
            fingerprint.setFingerprintType(Fingerprint.Type.leftIndexFinger);
        } else if (rightMiddleRadioButton.isSelected()) {
            fingerprint.setFingerprintType(Fingerprint.Type.rightMiddleFinger);
        } else if (leftMiddleRadioButton.isSelected()) {
            fingerprint.setFingerprintType(Fingerprint.Type.leftMiddleFinger);
        } else if (rightRingRadioButton.isSelected()) {
            fingerprint.setFingerprintType(Fingerprint.Type.rightRingFinger);
        } else if (leftRingRadioButton.isSelected()) {
            fingerprint.setFingerprintType(Fingerprint.Type.leftRingFinger);
        }
        ImagedFingerprint dummy = new ImagedFingerprint(fingerprint);
        if (imagedFingerprintList.contains(dummy)) {
            ImagedFingerprint imagedFingerprint = imagedFingerprintList.get(imagedFingerprintList.indexOf(dummy));
            showImage(imagedFingerprint.getImage(), true);
            showQuality(imagedFingerprint.getQuality());
        } else {
            if (imagedFingerprintCache.contains(dummy)) {
                ImagedFingerprint imagedFingerprint = imagedFingerprintCache.get(imagedFingerprintCache.indexOf(dummy));
                showImage(imagedFingerprint.getImage(), true);
                showQuality(imagedFingerprint.getQuality());
            } else {
                showImage(missingFingerprint.getImage(), true);
                showQuality("");
            }
        }
    }

    private void initializeReaderManager(final FingerprintingComponent fingerprintingComponent) {
        //initialize reader in a new thread to prevent gui from hanging.
        Runnable readerInitializer = new Runnable() {

            public void run() {
                showMessage("Preparing fingerprinting software");
                try {
                    fingerprintManager = ReaderManager.getFingerprintManager(OECReception.fingerprintManager());
                    fingerprintManager.setFingerprintingComponent(fingerprintingComponent);
                    readerAvailable = true;
                    showMessage("Waiting for device");
                } catch (MissingFingerprintManagerImpException ex) {
                    Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                    showMessage("Fingerprinting is not available. See log for details.");
                }
            }
        };
        new Thread(readerInitializer).start();
    }

    private void destroyReaderManager() {
        //destroy reader in a new thread to prevent gui from hanging.
        Runnable readerDestroyer = new Runnable() {

            public void run() {
                showMessage("Disconneting from device");
                try {
                    if (fingerprintManager != null) {
                        fingerprintManager.destroy();
                        showMessage("Disconneted from device");
                    }
                } catch (FingerprintManagerException ex) {
                    Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                }
                fingerprintManager = null;
                readerAvailable = false;
            }
        };
        new Thread(readerDestroyer).start();
    }

    @Action
    public void clearAllFingerprints() {
        if (showConfirmMessage("Are you sure you want to clear all the fingerprints collected in this session?", this)) {
            session.getImagedFingerprintList().clear();
            imagedFingerprintCache.clear();
            fingerprintImagePanel.setImage(missingFingerprint.getImage());
            rightIndexRadioButton.setSelected(true);
        }
    }
}
