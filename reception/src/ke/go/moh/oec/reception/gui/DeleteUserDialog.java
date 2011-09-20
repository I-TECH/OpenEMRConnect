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
 * DeleteUserDialog.java
 *
 * Created on Sep 16, 2011, 12:16:32 PM
 */
package ke.go.moh.oec.reception.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.controller.PersistenceManager;
import ke.go.moh.oec.reception.controller.exceptions.PersistenceManagerException;
import ke.go.moh.oec.reception.data.User;
import ke.go.moh.oec.reception.gui.helper.DialogEscaper;
import org.jdesktop.application.Action;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class DeleteUserDialog extends javax.swing.JDialog {

    private final PersistenceManager persistenceManager;

    /** Creates new form DeleteUserDialog */
    public DeleteUserDialog(java.awt.Frame parent, boolean modal) throws PersistenceManagerException {
        super(parent, modal);
        persistenceManager = PersistenceManager.getInstance();
        initComponents();
        this.getRootPane().setDefaultButton(deleteButton);
        addEscapeListener();
    }

    private void addEscapeListener() {
        DialogEscaper.addEscapeListener(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        permissionsPanel = new javax.swing.JPanel();
        usernameLabel = new javax.swing.JLabel();
        userComboBox = new javax.swing.JComboBox();
        deleteButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getResourceMap(DeleteUserDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        setResizable(false);

        permissionsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        permissionsPanel.setName("permissionsPanel"); // NOI18N

        usernameLabel.setText(resourceMap.getString("usernameLabel.text")); // NOI18N
        usernameLabel.setName("usernameLabel"); // NOI18N

        userComboBox.setName("userComboBox"); // NOI18N

        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, userList, userComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        userComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout permissionsPanelLayout = new javax.swing.GroupLayout(permissionsPanel);
        permissionsPanel.setLayout(permissionsPanelLayout);
        permissionsPanelLayout.setHorizontalGroup(
            permissionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(permissionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(usernameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userComboBox, 0, 304, Short.MAX_VALUE)
                .addContainerGap())
        );
        permissionsPanelLayout.setVerticalGroup(
            permissionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(permissionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(permissionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(userComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getActionMap(DeleteUserDialog.class, this);
        deleteButton.setAction(actionMap.get("delete")); // NOI18N
        deleteButton.setText(resourceMap.getString("deleteButton.text")); // NOI18N
        deleteButton.setName("deleteButton"); // NOI18N

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
                    .addComponent(permissionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeButton, deleteButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(permissionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {closeButton, deleteButton});

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void userComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userComboBoxActionPerformed
}//GEN-LAST:event_userComboBoxActionPerformed

    @Action
    public void delete() {
        Object selectedItem = userComboBox.getSelectedItem();
        if (selectedItem == null) {
            showWarningMessage("Select the user you want to delete.", deleteButton);
            return;
        }
        User user = (User) selectedItem;
        if (user.equals(OECReception.getUser())) {
            showWarningMessage("You are not allowed to delete your own user account!", deleteButton);
            return;
        }
        if (showConfirmMessage("Are you sure you want to delete '" + user.getUsername() + "'?")) {
            try {
                persistenceManager.deleteUser(user);
                userList.remove(user);
                userComboBox.setSelectedItem(null);
                showInformationMessage("Done!", deleteButton);
            } catch (PersistenceManagerException ex) {
                Logger.getLogger(ManagePermissionsDialog.class.getName()).log(Level.SEVERE, null, ex);
                showErrorMessage(ex.getMessage(), deleteButton);
            }
        }
    }

    private void showInformationMessage(String message, JComponent toFocus) {
        showMessage(message, toFocus, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message, JComponent toFocus) {
        showMessage(message, toFocus, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningMessage(String message, JComponent toFocus) {
        showMessage(message, toFocus, JOptionPane.WARNING_MESSAGE);
    }

    public boolean showConfirmMessage(String message) {
        return JOptionPane.showConfirmDialog(this, message, OECReception.applicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
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
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JPanel permissionsPanel;
    private javax.swing.JComboBox userComboBox;
    private final java.util.List<User> userList = ke.go.moh.oec.reception.controller.PersistenceManager.getInstance().getUserList();
    private javax.swing.JLabel usernameLabel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
