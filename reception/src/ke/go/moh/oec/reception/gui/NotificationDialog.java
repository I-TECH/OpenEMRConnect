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
 * NotificationDialog.java
 *
 * Created on Jul 4, 2011, 2:39:49 PM
 */
package ke.go.moh.oec.reception.gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import ke.go.moh.oec.Person.Sex;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.controller.PersonWrapper;
import ke.go.moh.oec.reception.controller.exceptions.PersistenceManagerException;
import ke.go.moh.oec.reception.data.Department;
import ke.go.moh.oec.reception.data.Notification;
import ke.go.moh.oec.reception.gui.helper.NotificationDialogHelper;
import org.jdesktop.application.Action;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class NotificationDialog extends javax.swing.JDialog {

    private final Notification notification;
    private final NotificationDialogHelper notificationDialogHelper;

    /** Creates new form NotificationDialog */
    public NotificationDialog(java.awt.Frame parent, boolean modal, Notification notification) {
        super(parent, modal);
        this.notification = notification;
        initComponents();
        notificationDialogHelper = new NotificationDialogHelper(this);
        showNotification();
        this.setIconImage(OECReception.applicationIcon());
    }

    private void showNotification() {
        if (notification != null) {
            PersonWrapper subject = notification.getPersonWrapper();
            clinicIdTextField.setText(subject.getClinicId());
            firstNameTextField.setText(subject.getFirstName());
            middleNameTextField.setText(subject.getMiddleName());
            lastNameTextField.setText(subject.getLastName());
            Sex sex = subject.getSex();
            sexTextField.setText(sex != null ? sex.toString() : "Unavailable");
            Date birthDate = subject.getBirthdate();
            birthdateTextField.setText(birthDate != null ? new SimpleDateFormat("dd/MM/yyyy").format(subject.getBirthdate())
                    : "Unavailable");
            notificationTypeTextField.setText(notification.getType().toString());
            Date occurenceDate = notification.getOccurenceDate();
            occurenceDateTextField.setText(occurenceDate != null ? new SimpleDateFormat("dd/MM/yyyy").format(notification.getOccurenceDate())
                    : "Unavailable");
            occurenceDateLabel.setText(notification.getType().occurenceDateId());
            additionalInformationTextArea.setText(notification.getAdditionalInformation());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        notificationPanel = new javax.swing.JPanel();
        clinicIdLabel = new javax.swing.JLabel();
        clinicIdTextField = new javax.swing.JTextField();
        firstNameLabel = new javax.swing.JLabel();
        firstNameTextField = new javax.swing.JTextField();
        middleNameLabel = new javax.swing.JLabel();
        middleNameTextField = new javax.swing.JTextField();
        lastNameLabel = new javax.swing.JLabel();
        lastNameTextField = new javax.swing.JTextField();
        sexLabel = new javax.swing.JLabel();
        sexTextField = new javax.swing.JTextField();
        birthdateLabel = new javax.swing.JLabel();
        birthdateTextField = new javax.swing.JTextField();
        notificationTypeLabel = new javax.swing.JLabel();
        notificationTypeTextField = new javax.swing.JTextField();
        occurenceDateLabel = new javax.swing.JLabel();
        occurenceDateTextField = new javax.swing.JTextField();
        additionalInformationLabel = new javax.swing.JLabel();
        additionalInformationScrollPane = new javax.swing.JScrollPane();
        additionalInformationTextArea = new javax.swing.JTextArea();
        doneButton = new javax.swing.JButton();
        reassignButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getResourceMap(NotificationDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        setResizable(false);

        notificationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("notificationPanel.border.title"))); // NOI18N
        notificationPanel.setName("notificationPanel"); // NOI18N

        clinicIdLabel.setText(resourceMap.getString("clinicIdLabel.text")); // NOI18N
        clinicIdLabel.setName("clinicIdLabel"); // NOI18N

        clinicIdTextField.setEditable(false);
        clinicIdTextField.setText(resourceMap.getString("clinicIdTextField.text")); // NOI18N
        clinicIdTextField.setName("clinicIdTextField"); // NOI18N

        firstNameLabel.setText(resourceMap.getString("firstNameLabel.text")); // NOI18N
        firstNameLabel.setName("firstNameLabel"); // NOI18N

        firstNameTextField.setEditable(false);
        firstNameTextField.setName("firstNameTextField"); // NOI18N

        middleNameLabel.setText(resourceMap.getString("middleNameLabel.text")); // NOI18N
        middleNameLabel.setName("middleNameLabel"); // NOI18N

        middleNameTextField.setEditable(false);
        middleNameTextField.setName("middleNameTextField"); // NOI18N

        lastNameLabel.setText(resourceMap.getString("lastNameLabel.text")); // NOI18N
        lastNameLabel.setName("lastNameLabel"); // NOI18N

        lastNameTextField.setEditable(false);
        lastNameTextField.setName("lastNameTextField"); // NOI18N

        sexLabel.setText(resourceMap.getString("sexLabel.text")); // NOI18N
        sexLabel.setName("sexLabel"); // NOI18N

        sexTextField.setEditable(false);
        sexTextField.setText(resourceMap.getString("sexTextField.text")); // NOI18N
        sexTextField.setName("sexTextField"); // NOI18N

        birthdateLabel.setText(resourceMap.getString("birthdateLabel.text")); // NOI18N
        birthdateLabel.setName("birthdateLabel"); // NOI18N

        birthdateTextField.setEditable(false);
        birthdateTextField.setFont(resourceMap.getFont("birthdateTextField.font")); // NOI18N
        birthdateTextField.setText(resourceMap.getString("birthdateTextField.text")); // NOI18N
        birthdateTextField.setName("birthdateTextField"); // NOI18N

        notificationTypeLabel.setText(resourceMap.getString("notificationTypeLabel.text")); // NOI18N
        notificationTypeLabel.setName("notificationTypeLabel"); // NOI18N

        notificationTypeTextField.setEditable(false);
        notificationTypeTextField.setName("notificationTypeTextField"); // NOI18N

        occurenceDateLabel.setText(resourceMap.getString("occurenceDateLabel.text")); // NOI18N
        occurenceDateLabel.setName("occurenceDateLabel"); // NOI18N

        occurenceDateTextField.setEditable(false);
        occurenceDateTextField.setText(resourceMap.getString("occurenceDateTextField.text")); // NOI18N
        occurenceDateTextField.setName("occurenceDateTextField"); // NOI18N

        additionalInformationLabel.setText(resourceMap.getString("additionalInformationLabel.text")); // NOI18N
        additionalInformationLabel.setName("additionalInformationLabel"); // NOI18N

        additionalInformationScrollPane.setName("additionalInformationScrollPane"); // NOI18N

        additionalInformationTextArea.setColumns(20);
        additionalInformationTextArea.setEditable(false);
        additionalInformationTextArea.setLineWrap(true);
        additionalInformationTextArea.setRows(5);
        additionalInformationTextArea.setWrapStyleWord(true);
        additionalInformationTextArea.setName("additionalInformationTextArea"); // NOI18N
        additionalInformationScrollPane.setViewportView(additionalInformationTextArea);

        javax.swing.GroupLayout notificationPanelLayout = new javax.swing.GroupLayout(notificationPanel);
        notificationPanel.setLayout(notificationPanelLayout);
        notificationPanelLayout.setHorizontalGroup(
            notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notificationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(additionalInformationLabel)
                    .addComponent(notificationTypeLabel)
                    .addComponent(lastNameLabel)
                    .addComponent(sexLabel)
                    .addComponent(middleNameLabel)
                    .addComponent(firstNameLabel)
                    .addComponent(clinicIdLabel)
                    .addComponent(birthdateLabel)
                    .addComponent(occurenceDateLabel))
                .addGap(4, 4, 4)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clinicIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addComponent(firstNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addComponent(middleNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addComponent(lastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addComponent(notificationTypeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addComponent(birthdateTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addComponent(occurenceDateTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addComponent(sexTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addComponent(additionalInformationScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE))
                .addContainerGap())
        );
        notificationPanelLayout.setVerticalGroup(
            notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notificationPanelLayout.createSequentialGroup()
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clinicIdLabel)
                    .addComponent(clinicIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firstNameLabel)
                    .addComponent(firstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(middleNameLabel)
                    .addComponent(middleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastNameLabel)
                    .addComponent(lastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sexLabel)
                    .addComponent(sexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(birthdateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(birthdateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(notificationTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(notificationTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(occurenceDateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(occurenceDateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(additionalInformationLabel)
                    .addComponent(additionalInformationScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getActionMap(NotificationDialog.class, this);
        doneButton.setAction(actionMap.get("markAsDone")); // NOI18N
        doneButton.setText(resourceMap.getString("doneButton.text")); // NOI18N
        doneButton.setName("doneButton"); // NOI18N

        reassignButton.setAction(actionMap.get("reassign")); // NOI18N
        reassignButton.setText(resourceMap.getString("reassignButton.text")); // NOI18N
        reassignButton.setName("reassignButton"); // NOI18N

        closeButton.setAction(actionMap.get("close")); // NOI18N
        closeButton.setText(resourceMap.getString("closeButton.text")); // NOI18N
        closeButton.setName("closeButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(notificationPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(doneButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reassignButton)
                        .addGap(5, 5, 5)
                        .addComponent(closeButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeButton, doneButton, reassignButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(notificationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(reassignButton)
                    .addComponent(doneButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    @Action
    public void close() {
        notification.setFlaggedOff(false);
        dispose();
    }

    @Action
    public void reassign() {
        try {
            Department department = new Department();
            ReassignDialog rd = new ReassignDialog(null, true, notificationDialogHelper.getClinicList(), department);
            rd.setLocationRelativeTo(this);
            rd.setVisible(true);
            if (department.isSelected()) {
                if (showConfirmMessage("Are you sure you want to reassign '" + notification.toString() + "' to "
                        + department.getName() + "?")) {
                    notification.setReassignAggress(department.getCode());
                    notificationDialogHelper.reassignWork(notification);
                    notification.setFlaggedOff(true);
                    dispose();
                }
            }
        } catch (PersistenceManagerException ex) {
            showErrorMessage("The following error occurred: " + ex.getMessage()
                    + " Please contact your administrator.", rootPane);
        }
    }

    @Action
    public void markAsDone() {
        if (showConfirmMessage("Are you sure you want to mark '" + notification.toString()
                + "' as done?")) {
            notification.setReassignAggress(OECReception.applicationAddress());
            notificationDialogHelper.flagWorkAsDone(notification);
            notification.setFlaggedOff(true);
            dispose();
        }
    }

    public void showWarningMessage(String message, JComponent toFocus) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void showErrorMessage(String message, JComponent toFocus) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean showConfirmMessage(String message) {
        return JOptionPane.showConfirmDialog(this, message, OECReception.applicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel additionalInformationLabel;
    private javax.swing.JScrollPane additionalInformationScrollPane;
    private javax.swing.JTextArea additionalInformationTextArea;
    private javax.swing.JLabel birthdateLabel;
    private javax.swing.JTextField birthdateTextField;
    private javax.swing.JLabel clinicIdLabel;
    private javax.swing.JTextField clinicIdTextField;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel firstNameLabel;
    private javax.swing.JTextField firstNameTextField;
    private javax.swing.JLabel lastNameLabel;
    private javax.swing.JTextField lastNameTextField;
    private javax.swing.JLabel middleNameLabel;
    private javax.swing.JTextField middleNameTextField;
    private javax.swing.JPanel notificationPanel;
    private javax.swing.JLabel notificationTypeLabel;
    private javax.swing.JTextField notificationTypeTextField;
    private javax.swing.JLabel occurenceDateLabel;
    private javax.swing.JTextField occurenceDateTextField;
    private javax.swing.JButton reassignButton;
    private javax.swing.JLabel sexLabel;
    private javax.swing.JTextField sexTextField;
    // End of variables declaration//GEN-END:variables
}
