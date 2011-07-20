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
 * Contributor(fillClinicList):
 *
 * ***** END LICENSE BLOCK ***** */

/*
 * ReassignDialog.java
 *
 * Created on Jul 11, 2011, 4:16:53 PM
 */
package ke.go.moh.oec.reception.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.data.Clinic;
import org.jdesktop.application.Action;
import org.jdesktop.beansbinding.Binding;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class ReassignDialog extends javax.swing.JDialog {

    private final List<Clinic> allClinics;
    private final Clinic selectedClinic;

    /** Creates new form ReassignDialog */
    public ReassignDialog(java.awt.Frame parent, boolean modal,
            List<Clinic> allClinics, Clinic selectedClinic) {
        super(parent, modal);
        initComponents();
        this.allClinics = allClinics;
        this.selectedClinic = selectedClinic;
        fillClinicList();
    }

    private void fillClinicList() {
        Binding binding = bindingGroup.getBinding("clinicListBinding");
        binding.unbind();
        clinicList.addAll(allClinics);
        binding.bind();
        clinicNameComboBox.repaint();
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

        clinicList = new ArrayList<Clinic>();
        reassignPanel = new javax.swing.JPanel();
        clinicNameLabel = new javax.swing.JLabel();
        clinicNameComboBox = new javax.swing.JComboBox();
        reassignButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getResourceMap(ReassignDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        setResizable(false);

        reassignPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        reassignPanel.setName("reassignPanel"); // NOI18N

        clinicNameLabel.setText(resourceMap.getString("clinicNameLabel.text")); // NOI18N
        clinicNameLabel.setName("clinicNameLabel"); // NOI18N

        clinicNameComboBox.setName("clinicNameComboBox"); // NOI18N

        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, clinicList, clinicNameComboBox, "clinicListBinding");
        bindingGroup.addBinding(jComboBoxBinding);

        javax.swing.GroupLayout reassignPanelLayout = new javax.swing.GroupLayout(reassignPanel);
        reassignPanel.setLayout(reassignPanelLayout);
        reassignPanelLayout.setHorizontalGroup(
            reassignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reassignPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clinicNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clinicNameComboBox, 0, 296, Short.MAX_VALUE)
                .addContainerGap())
        );
        reassignPanelLayout.setVerticalGroup(
            reassignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reassignPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reassignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clinicNameLabel)
                    .addComponent(clinicNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ke.go.moh.oec.reception.gui.App.class).getContext().getActionMap(ReassignDialog.class, this);
        reassignButton.setAction(actionMap.get("reassign")); // NOI18N
        reassignButton.setText(resourceMap.getString("reassignButton.text")); // NOI18N
        reassignButton.setName("reassignButton"); // NOI18N

        cancelButton.setAction(actionMap.get("cancel")); // NOI18N
        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reassignPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(reassignButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, reassignButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reassignPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(reassignButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void cancel() {
        selectedClinic.setSelected(false);
        dispose();
    }

    @Action
    public void reassign() {
        Object selectedItem = clinicNameComboBox.getSelectedItem();
        if (selectedItem != null) {
            Clinic reassignClinic = (Clinic) selectedItem;
            selectedClinic.setCode(reassignClinic.getCode());
            selectedClinic.setName(reassignClinic.getName());
            selectedClinic.setSelected(true);
            dispose();
        } else {
            showWarningMessage("Please select the clinic to which you want to reassign "
                    + "this notification.");
        }
    }

    public boolean showConfirmMessage(String message) {
        return JOptionPane.showConfirmDialog(this, message, OECReception.applicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void showWarningMessage(String message) {
        JOptionPane.showConfirmDialog(this, message, OECReception.applicationName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private java.util.List<Clinic> clinicList;
    private javax.swing.JComboBox clinicNameComboBox;
    private javax.swing.JLabel clinicNameLabel;
    private javax.swing.JButton reassignButton;
    private javax.swing.JPanel reassignPanel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
