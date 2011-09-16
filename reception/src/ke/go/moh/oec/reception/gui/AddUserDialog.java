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
 * LoginDialog.java
 *
 * Created on Jun 30, 2011, 8:49:09 AM
 */
package ke.go.moh.oec.reception.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.data.User;
import ke.go.moh.oec.reception.controller.PersistenceManager;
import ke.go.moh.oec.reception.controller.exceptions.ExistingUserException;
import ke.go.moh.oec.reception.controller.exceptions.PersistenceManagerException;
import ke.go.moh.oec.reception.gui.helper.DialogEscaper;
import org.jdesktop.application.Action;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class AddUserDialog extends javax.swing.JDialog {

    private final PersistenceManager userManager;

    public AddUserDialog(java.awt.Frame parent, boolean modal) throws PersistenceManagerException {
        super(parent, modal);
        userManager = PersistenceManager.getInstance();
        initComponents();
        this.setIconImage(OECReception.applicationIcon());
        this.getRootPane().setDefaultButton(addButton);
        addEscapeListener();
    }

    private void addEscapeListener() {
        DialogEscaper.addEscapeListener(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loginPanel = new javax.swing.JPanel();
        usernameLabel = new javax.swing.JLabel();
        usernameTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        adminCheckBox = new javax.swing.JCheckBox();
        addButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getResourceMap(AddUserDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        setResizable(false);

        loginPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        loginPanel.setName("loginPanel"); // NOI18N

        usernameLabel.setText(resourceMap.getString("usernameLabel.text")); // NOI18N
        usernameLabel.setName("usernameLabel"); // NOI18N

        usernameTextField.setText(resourceMap.getString("usernameTextField.text")); // NOI18N
        usernameTextField.setName("usernameTextField"); // NOI18N

        passwordLabel.setText(resourceMap.getString("passwordLabel.text")); // NOI18N
        passwordLabel.setName("passwordLabel"); // NOI18N

        passwordField.setText(resourceMap.getString("passwordField.text")); // NOI18N
        passwordField.setName("passwordField"); // NOI18N

        adminCheckBox.setText(resourceMap.getString("adminCheckBox.text")); // NOI18N
        adminCheckBox.setName("adminCheckBox"); // NOI18N

        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordLabel)
                    .addComponent(usernameLabel))
                .addGap(18, 18, 18)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(adminCheckBox)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                    .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
                .addContainerGap())
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(adminCheckBox))
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getActionMap(AddUserDialog.class, this);
        addButton.setAction(actionMap.get("add")); // NOI18N
        addButton.setText(resourceMap.getString("addButton.text")); // NOI18N
        addButton.setName("addButton"); // NOI18N

        closeButton.setAction(actionMap.get("close")); // NOI18N
        closeButton.setText(resourceMap.getString("closeButton.text")); // NOI18N
        closeButton.setName("closeButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loginPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, closeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(addButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void add() {
        try {
            if (usernameTextField.getText().isEmpty()) {
                showWarningMessage("You must enter a username before proceeding!", usernameTextField);
                return;
            }
            try {
                userManager.createUser(new User(usernameTextField.getText(), passwordField.getPassword(), adminCheckBox.isSelected()));
                showInformationMessage("User created successfully!", addButton);
            } catch (ExistingUserException ex) {
                Logger.getLogger(AddUserDialog.class.getName()).log(Level.SEVERE, null, ex);
                showWarningMessage(ex.getMessage(), addButton);
            }
        } catch (PersistenceManagerException ex) {
            Logger.getLogger(AddUserDialog.class.getName()).log(Level.SEVERE, null, ex);
            showErrorMessage(ex.getMessage(), addButton);
        }
    }

    private void showWarningMessage(String message, JComponent toFocus) {
        showMessage(message, toFocus, JOptionPane.WARNING_MESSAGE);
    }

    private void showInformationMessage(String message, JComponent toFocus) {
        showMessage(message, toFocus, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message, JComponent toFocus) {
        showMessage(message, toFocus, JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message, JComponent toFocus, int messageType) {
        JOptionPane.showMessageDialog(this, message, OECReception.applicationName(), messageType);
        toFocus.requestFocus();
    }

    @Action
    public void close() {
        dispose();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JCheckBox adminCheckBox;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables
}
