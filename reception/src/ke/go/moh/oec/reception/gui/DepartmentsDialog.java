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
 * DepartmentsDialog.java
 *
 * Created on Jul 20, 2011, 3:59:05 PM
 */
package ke.go.moh.oec.reception.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.controller.PersistenceManager;
import ke.go.moh.oec.reception.controller.exceptions.PersistenceManagerException;
import ke.go.moh.oec.reception.data.Department;
import ke.go.moh.oec.reception.gui.helper.DialogEscaper;
import org.jdesktop.application.Action;
import org.jdesktop.beansbinding.Binding;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class DepartmentsDialog extends javax.swing.JDialog {

    private final PersistenceManager persistenceManager;

    /** Creates new form DepartmentsDialog */
    public DepartmentsDialog(java.awt.Frame parent, boolean modal) throws PersistenceManagerException {
        super(parent, modal);
        initComponents();
        this.setIconImage(OECReception.applicationIcon());
        persistenceManager = PersistenceManager.getInstance();
        persistenceManager.createDepartmentTable();
        refreshDepartmentsTable(persistenceManager.getDepartmentList());
        this.getRootPane().setDefaultButton(saveButton);
        addEscapeListener();
    }

    private void addEscapeListener() {
        DialogEscaper.addEscapeListener(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        departmentList = new ArrayList<Department>();
        departmentsPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        codeLabel = new javax.swing.JLabel();
        codeTextField = new javax.swing.JTextField();
        saveButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        departmentsScrollPanel = new javax.swing.JScrollPane();
        departmentsTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getResourceMap(DepartmentsDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        departmentsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        departmentsPanel.setName("departmentsPanel"); // NOI18N

        nameLabel.setText(resourceMap.getString("nameLabel.text")); // NOI18N
        nameLabel.setName("nameLabel"); // NOI18N

        nameTextField.setText(resourceMap.getString("nameTextField.text")); // NOI18N
        nameTextField.setName("nameTextField"); // NOI18N

        codeLabel.setText(resourceMap.getString("codeLabel.text")); // NOI18N
        codeLabel.setName("codeLabel"); // NOI18N

        codeTextField.setText(resourceMap.getString("codeTextField.text")); // NOI18N
        codeTextField.setName("codeTextField"); // NOI18N

        javax.swing.GroupLayout departmentsPanelLayout = new javax.swing.GroupLayout(departmentsPanel);
        departmentsPanel.setLayout(departmentsPanelLayout);
        departmentsPanelLayout.setHorizontalGroup(
            departmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(departmentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(departmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(codeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(departmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(codeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                .addContainerGap())
        );
        departmentsPanelLayout.setVerticalGroup(
            departmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(departmentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(departmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(departmentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(codeLabel)
                    .addComponent(codeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getActionMap(DepartmentsDialog.class, this);
        saveButton.setAction(actionMap.get("save")); // NOI18N
        saveButton.setText(resourceMap.getString("saveButton.text")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N

        deleteButton.setAction(actionMap.get("delete")); // NOI18N
        deleteButton.setText(resourceMap.getString("deleteButton.text")); // NOI18N
        deleteButton.setName("deleteButton"); // NOI18N

        closeButton.setAction(actionMap.get("close")); // NOI18N
        closeButton.setText(resourceMap.getString("closeButton.text")); // NOI18N
        closeButton.setName("closeButton"); // NOI18N

        departmentsScrollPanel.setName("departmentsScrollPanel"); // NOI18N

        departmentsTable.setName("departmentsTable"); // NOI18N
        departmentsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, departmentList, departmentsTable, "departmentListBinding");
        org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${name}"));
        columnBinding.setColumnName("Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${code}"));
        columnBinding.setColumnName("Code");
        columnBinding.setColumnClass(String.class);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        departmentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                departmentsTableMouseClicked(evt);
            }
        });
        departmentsScrollPanel.setViewportView(departmentsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(departmentsScrollPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addComponent(departmentsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeButton, deleteButton, saveButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(departmentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(deleteButton)
                    .addComponent(closeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(departmentsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void departmentsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_departmentsTableMouseClicked
        int selectedRow = departmentsTable.getSelectedRow();
        if (selectedRow != -1) {
            showDepartment(departmentList.get(selectedRow));
        }
    }//GEN-LAST:event_departmentsTableMouseClicked

    private void showDepartment(Department department) {
        nameTextField.setText(department.getName());
        codeTextField.setText(department.getCode());
    }

    @Action
    public void close() {
        dispose();
    }

    @Action
    public void delete() {
        int selectedRow = departmentsTable.getSelectedRow();
        if (selectedRow != -1) {
            Department department = departmentList.get(selectedRow);
            if (showConfirmMessage("Are you sure you want to delete " + department.getName() + "?")) {
                try {
                    persistenceManager.deleteDepartment(department);
                    refreshDepartmentsTable(department, false);
                } catch (PersistenceManagerException ex) {
                    showErrorMessage("The following error occurred: " + ex.getMessage()
                            + " Please contact your administrator.", rootPane);
                }
            }
        } else {
            showWarningMessage("Pease selecte the department you want to delete.", rootPane);
        }
    }

    @Action
    public void save() {
        if (nameTextField.getText().isEmpty()) {
            showWarningMessage("Please type in the department name.", nameTextField);
            return;
        }
        if (codeTextField.getText().isEmpty()) {
            showWarningMessage("Please type in the department code.", codeTextField);
            return;
        }
        Department department = new Department(nameTextField.getText(), codeTextField.getText());
        try {
            if (departmentList.contains(department)) {
                if (showConfirmMessage("Would you like to modify " + department.getName() + "?")) {
                    persistenceManager.modifyDepartment(department);
                }
            } else {
                persistenceManager.createDepartment(department);
                refreshDepartmentsTable(department, true);
            }
        } catch (PersistenceManagerException ex) {
            showErrorMessage("The following error occurred: " + ex.getMessage()
                    + " Please contact your administrator.", rootPane);
        }
    }

    private void refreshDepartmentsTable(List<Department> departmentList) {
        Binding binding = bindingGroup.getBinding("departmentListBinding");
        binding.unbind();
        this.departmentList.clear();
        this.departmentList.addAll(departmentList);
        binding.bind();
        departmentsTable.repaint();
    }

    private void refreshDepartmentsTable(Department department, boolean add) {
        Binding binding = bindingGroup.getBinding("departmentListBinding");
        binding.unbind();
        if (add) {
            this.departmentList.add(department);
        } else {
            this.departmentList.remove(department);
        }
        binding.bind();
        departmentsTable.repaint();
    }

    private void showWarningMessage(String message, JComponent toFocus) {
        showMessage(message, toFocus, JOptionPane.WARNING_MESSAGE);
    }

    public boolean showConfirmMessage(String message) {
        return JOptionPane.showConfirmDialog(this, message, OECReception.applicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void showErrorMessage(String message, JComponent toFocus) {
        showMessage(message, toFocus, JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message, JComponent toFocus, int messageType) {
        JOptionPane.showMessageDialog(this, message, OECReception.applicationName(), messageType);
        toFocus.requestFocus();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel codeLabel;
    private javax.swing.JTextField codeTextField;
    private javax.swing.JButton deleteButton;
    private java.util.List<Department> departmentList;
    private javax.swing.JPanel departmentsPanel;
    private javax.swing.JScrollPane departmentsScrollPanel;
    private javax.swing.JTable departmentsTable;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton saveButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
