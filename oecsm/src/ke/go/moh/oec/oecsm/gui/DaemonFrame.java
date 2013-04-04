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
package ke.go.moh.oec.oecsm.gui;

import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import ke.go.moh.oec.oecsm.daemon.DaemonManager;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;

/**
 *@date Sep 13, 2010
 *
 * @author jgitahi
 */
public class DaemonFrame extends javax.swing.JFrame {

    private class Driver {

        private String displayName;
        private String name;
        private String urlTemplate;

        public Driver(String name) {
            this.name = name;
        }

        public Driver(String displayName, String name, String urlTemplate) {
            this.displayName = displayName;
            this.name = name;
            this.urlTemplate = urlTemplate;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrlTemplate() {
            return urlTemplate;
        }

        public void setUrlTemplate(String urlTemplate) {
            this.urlTemplate = urlTemplate;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Driver other = (Driver) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return this.displayName;
        }
    }

    public javax.swing.JTextArea getOutputTextArea() {
        return outputTextArea;
    }

    public DaemonFrame() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());

            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            SwingUtilities.updateComponentTreeUI(this);

            initComponents();
            generateDriverList();
            getSourceProperties();
            getShadowProperties();
        } catch (ClassNotFoundException ex) {
            Logger logger = Logger.getLogger(DaemonFrame.class.getName());
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        driverList = generateDriverList();
        primaryPanel = new javax.swing.JPanel();
        sourceDatabasePanel = new javax.swing.JPanel();
        sourceDatabaseLabel = new javax.swing.JLabel();
        sourceDatabaseTextField = new javax.swing.JTextField();
        sourceDriverLabel = new javax.swing.JLabel();
        sourceDriverComboBox = new javax.swing.JComboBox();
        sourceUrlTemplateButton = new javax.swing.JButton();
        sourceUrlLabel = new javax.swing.JLabel();
        sourceUrlTextField = new javax.swing.JTextField();
        sourceUsernameLabel = new javax.swing.JLabel();
        sourceUsernameTextField = new javax.swing.JTextField();
        sourcePasswordLabel = new javax.swing.JLabel();
        sourcePasswordField = new javax.swing.JPasswordField();
        sourceDatabaseTestButton = new javax.swing.JButton();
        sourceDatabaseResetButton = new javax.swing.JButton();
        sourceDatabaseSaveButton = new javax.swing.JButton();
        shadowDatabasePanel = new javax.swing.JPanel();
        shadowDatabaseLabel = new javax.swing.JLabel();
        shadowDatabaseTextField = new javax.swing.JTextField();
        shadowDriverLabel = new javax.swing.JLabel();
        shadowDriverComboBox = new javax.swing.JComboBox();
        shadowUrlTemplateButton = new javax.swing.JButton();
        shadowUrlLabel = new javax.swing.JLabel();
        shadowUrlTextField = new javax.swing.JTextField();
        shadowUsernameLabel = new javax.swing.JLabel();
        shadowUsernameTextField = new javax.swing.JTextField();
        shadowPasswordLabel = new javax.swing.JLabel();
        shadowPasswordField = new javax.swing.JPasswordField();
        shadowSourceDatabaseTestButton = new javax.swing.JButton();
        shadowDatabaseResetButton = new javax.swing.JButton();
        shadowDatabaseSaveButton = new javax.swing.JButton();
        pollingFrequencyLabel = new javax.swing.JLabel();
        pollingFrequencyTextField = new javax.swing.JTextField();
        restartButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        outputPanel = new javax.swing.JPanel();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();

        setTitle("OECSM Daemon");
        setIconImage(Toolkit.getDefaultToolkit().getImage("tray.gif"));

        primaryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("OECSM Daemon Configuration"));

        sourceDatabasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Source Database"));

        sourceDatabaseLabel.setText("Database");

        sourceDriverLabel.setText("Driver");

        org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, driverList, sourceDriverComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        sourceUrlTemplateButton.setText("Insert Url Template");
        sourceUrlTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceUrlTemplateButtonActionPerformed(evt);
            }
        });

        sourceUrlLabel.setText("Url");

        sourceUsernameLabel.setText("Username");

        sourcePasswordLabel.setText("Password");

        sourceDatabaseTestButton.setText("Test");
        sourceDatabaseTestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceDatabaseTestButtonActionPerformed(evt);
            }
        });

        sourceDatabaseResetButton.setText("Reset");
        sourceDatabaseResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceDatabaseResetButtonActionPerformed(evt);
            }
        });

        sourceDatabaseSaveButton.setText("Save");
        sourceDatabaseSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceDatabaseSaveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sourceDatabasePanelLayout = new javax.swing.GroupLayout(sourceDatabasePanel);
        sourceDatabasePanel.setLayout(sourceDatabasePanelLayout);
        sourceDatabasePanelLayout.setHorizontalGroup(
            sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceDatabasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sourceDatabasePanelLayout.createSequentialGroup()
                        .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceUsernameLabel)
                            .addComponent(sourcePasswordLabel)
                            .addComponent(sourceUrlLabel)
                            .addComponent(sourceDatabaseLabel)
                            .addComponent(sourceDriverLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceDatabaseTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .addComponent(sourceUrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .addComponent(sourcePasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .addComponent(sourceUsernameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourceDatabasePanelLayout.createSequentialGroup()
                                .addComponent(sourceDriverComboBox, 0, 365, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sourceUrlTemplateButton))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourceDatabasePanelLayout.createSequentialGroup()
                        .addComponent(sourceDatabaseTestButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceDatabaseResetButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceDatabaseSaveButton)))
                .addContainerGap())
        );
        sourceDatabasePanelLayout.setVerticalGroup(
            sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceDatabasePanelLayout.createSequentialGroup()
                .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceDatabaseLabel)
                    .addComponent(sourceDatabaseTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceDriverLabel)
                    .addComponent(sourceUrlTemplateButton)
                    .addComponent(sourceDriverComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceUrlLabel)
                    .addComponent(sourceUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceUsernameLabel)
                    .addComponent(sourceUsernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourcePasswordLabel)
                    .addComponent(sourcePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourceDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceDatabaseSaveButton)
                    .addComponent(sourceDatabaseResetButton)
                    .addComponent(sourceDatabaseTestButton)))
        );

        shadowDatabasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Shadow Database"));

        shadowDatabaseLabel.setText("Database");

        shadowDriverLabel.setText("Driver");

        jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, driverList, shadowDriverComboBox);
        bindingGroup.addBinding(jComboBoxBinding);

        shadowUrlTemplateButton.setText("Insert Url Template");
        shadowUrlTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shadowUrlTemplateButtonActionPerformed(evt);
            }
        });

        shadowUrlLabel.setText("Url");

        shadowUsernameLabel.setText("Username");

        shadowPasswordLabel.setText("Password");

        shadowSourceDatabaseTestButton.setText("Test");
        shadowSourceDatabaseTestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shadowSourceDatabaseTestButtonActionPerformed(evt);
            }
        });

        shadowDatabaseResetButton.setText("Reset");
        shadowDatabaseResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shadowDatabaseResetButtonActionPerformed(evt);
            }
        });

        shadowDatabaseSaveButton.setText("Save");
        shadowDatabaseSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shadowDatabaseSaveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout shadowDatabasePanelLayout = new javax.swing.GroupLayout(shadowDatabasePanel);
        shadowDatabasePanel.setLayout(shadowDatabasePanelLayout);
        shadowDatabasePanelLayout.setHorizontalGroup(
            shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowDatabasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(shadowDatabasePanelLayout.createSequentialGroup()
                        .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(shadowUsernameLabel)
                            .addComponent(shadowPasswordLabel)
                            .addComponent(shadowUrlLabel)
                            .addComponent(shadowDatabaseLabel)
                            .addComponent(shadowDriverLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(shadowDatabaseTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .addComponent(shadowUrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .addComponent(shadowPasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .addComponent(shadowUsernameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowDatabasePanelLayout.createSequentialGroup()
                                .addComponent(shadowDriverComboBox, 0, 0, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(shadowUrlTemplateButton))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowDatabasePanelLayout.createSequentialGroup()
                        .addComponent(shadowSourceDatabaseTestButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(shadowDatabaseResetButton)
                        .addGap(5, 5, 5)
                        .addComponent(shadowDatabaseSaveButton)))
                .addContainerGap())
        );
        shadowDatabasePanelLayout.setVerticalGroup(
            shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowDatabasePanelLayout.createSequentialGroup()
                .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shadowDatabaseLabel)
                    .addComponent(shadowDatabaseTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shadowDriverLabel)
                    .addComponent(shadowUrlTemplateButton)
                    .addComponent(shadowDriverComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shadowUrlLabel)
                    .addComponent(shadowUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shadowUsernameLabel)
                    .addComponent(shadowUsernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shadowPasswordLabel)
                    .addComponent(shadowPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shadowDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shadowSourceDatabaseTestButton)
                    .addComponent(shadowDatabaseResetButton)
                    .addComponent(shadowDatabaseSaveButton)))
        );

        javax.swing.GroupLayout primaryPanelLayout = new javax.swing.GroupLayout(primaryPanel);
        primaryPanel.setLayout(primaryPanelLayout);
        primaryPanelLayout.setHorizontalGroup(
            primaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, primaryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(primaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sourceDatabasePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowDatabasePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        primaryPanelLayout.setVerticalGroup(
            primaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(primaryPanelLayout.createSequentialGroup()
                .addComponent(sourceDatabasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowDatabasePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pollingFrequencyLabel.setText("Polling Frequency (Seconds)");

        pollingFrequencyTextField.setText("10");

        restartButton.setText("Restart");
        restartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restartButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        outputTextArea.setColumns(20);
        outputTextArea.setRows(5);
        outputScrollPane.setViewportView(outputTextArea);

        javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(outputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
        );
        outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(outputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outputPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(primaryPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pollingFrequencyLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pollingFrequencyTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(restartButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exitButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(primaryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pollingFrequencyLabel)
                    .addComponent(pollingFrequencyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitButton)
                    .addComponent(stopButton)
                    .addComponent(restartButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void restartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restartButtonActionPerformed
        try {
            DaemonManager.restartDaemon(Integer.parseInt(pollingFrequencyTextField.getText()) * 1000);
        } catch (Exception ex) {
            DaemonManager.restartDaemon(10000);
        }
        outputTextArea.append("OECSM Daemon restarted.\n");
    }//GEN-LAST:event_restartButtonActionPerformed

    private void sourceDatabaseTestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceDatabaseTestButtonActionPerformed
        try {
            new DatabaseConnector().testConnection(((Driver) sourceDriverComboBox.getSelectedItem()).getName(), sourceUrlTextField.getText(), sourceUsernameTextField.getText(), sourcePasswordField.getText());
            JOptionPane.showMessageDialog(rootPane, "Successful!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e.toString());
        }
    }//GEN-LAST:event_sourceDatabaseTestButtonActionPerformed

    private void shadowSourceDatabaseTestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shadowSourceDatabaseTestButtonActionPerformed
        try {
            new DatabaseConnector().testConnection(((Driver) shadowDriverComboBox.getSelectedItem()).getName(), shadowUrlTextField.getText(), shadowUsernameTextField.getText(), shadowPasswordField.getText());
            JOptionPane.showMessageDialog(rootPane, "Successful!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e.toString());
        }
    }//GEN-LAST:event_shadowSourceDatabaseTestButtonActionPerformed

    private void sourceDatabaseSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceDatabaseSaveButtonActionPerformed
        setSourceProperties();
    }//GEN-LAST:event_sourceDatabaseSaveButtonActionPerformed

    private void shadowDatabaseSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shadowDatabaseSaveButtonActionPerformed
        setShadowProperties();
    }//GEN-LAST:event_shadowDatabaseSaveButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitButtonActionPerformed

    private void sourceUrlTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceUrlTemplateButtonActionPerformed
        sourceUrlTextField.setText(((Driver) sourceDriverComboBox.getSelectedItem()).getUrlTemplate());
    }//GEN-LAST:event_sourceUrlTemplateButtonActionPerformed

    private void shadowUrlTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shadowUrlTemplateButtonActionPerformed
        shadowUrlTextField.setText(((Driver) shadowDriverComboBox.getSelectedItem()).getUrlTemplate());
    }//GEN-LAST:event_shadowUrlTemplateButtonActionPerformed

    private void sourceDatabaseResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceDatabaseResetButtonActionPerformed
        getSourceProperties();
    }//GEN-LAST:event_sourceDatabaseResetButtonActionPerformed

    private void shadowDatabaseResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shadowDatabaseResetButtonActionPerformed
        getShadowProperties();
    }//GEN-LAST:event_shadowDatabaseResetButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        DaemonManager.stopDaemon();
        outputTextArea.append("OECSM Daemon stopped.\n");
    }//GEN-LAST:event_stopButtonActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DaemonFrame().setVisible(true);
            }
        });
    }

    private void getSourceProperties() {
        try {
            Properties sourceProperties = new Properties();
            sourceProperties.load(new FileInputStream("source_database.properties"));
            sourceDatabaseTextField.setText(sourceProperties.getProperty("database"));
            sourceUrlTextField.setText(sourceProperties.getProperty("url"));
            sourceDriverComboBox.setSelectedItem(driverList.get(driverList.indexOf(new Driver(sourceProperties.getProperty("driver")))));
            sourceUsernameTextField.setText(sourceProperties.getProperty("username"));
            sourcePasswordField.setText(sourceProperties.getProperty("password"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getShadowProperties() {
        try {
            Properties shadowProperties = new Properties();
            shadowProperties.load(new FileInputStream("shadow_database.properties"));
            shadowDatabaseTextField.setText(shadowProperties.getProperty("database"));
            shadowUrlTextField.setText(shadowProperties.getProperty("url"));
            shadowDriverComboBox.setSelectedItem(driverList.get(driverList.indexOf(new Driver(shadowProperties.getProperty("driver")))));
            shadowUsernameTextField.setText(shadowProperties.getProperty("username"));
            shadowPasswordField.setText(shadowProperties.getProperty("password"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setSourceProperties() {
        try {
            Properties sourceProperties = new Properties();
            sourceProperties.setProperty("database", sourceDatabaseTextField.getText());
            sourceProperties.setProperty("driver", ((Driver) sourceDriverComboBox.getSelectedItem()).getName());
            sourceProperties.setProperty("url", sourceUrlTextField.getText());
            sourceProperties.setProperty("username", sourceUsernameTextField.getText());
            sourceProperties.setProperty("password", sourcePasswordField.getText());
            sourceProperties.store(new FileOutputStream("source_database.properties"), "");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setShadowProperties() {
        try {
            Properties sourceProperties = new Properties();
            sourceProperties.setProperty("database", shadowDatabaseTextField.getText());
            sourceProperties.setProperty("driver", ((Driver) shadowDriverComboBox.getSelectedItem()).getName());
            sourceProperties.setProperty("url", shadowUrlTextField.getText());
            sourceProperties.setProperty("username", shadowUsernameTextField.getText());
            sourceProperties.setProperty("password", shadowPasswordField.getText());
            sourceProperties.store(new FileOutputStream("shadow_database.properties"), "");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DaemonFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<Driver> generateDriverList() {
        List<Driver> drivers = new ArrayList<Driver>();
        drivers.add(new Driver("MySQL JDBC Driver", "com.mysql.jdbc.Driver", "jdbc:mysql://host:port/database"));
        drivers.add(new Driver("MSSQL Server JDBC Driver", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://host:port/database"));
        drivers.add(new Driver("PostgreSQL JDBC Driver", "org.postgresql.Driver", "jdbc:postgresql://host:port/database"));
        drivers.add(new Driver("MS Access JDBC-ODBC Bridge", "sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};DBQ=C:\\database folder\\database.mdb"));
        drivers.add(new Driver("MS Access JDBC Driver", "com.hxtt.sql.access.AccessDriver", "jdbc:Access:///database folder"));
        return drivers;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.util.List<Driver> driverList;
    private javax.swing.JButton exitButton;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JLabel pollingFrequencyLabel;
    private javax.swing.JTextField pollingFrequencyTextField;
    private javax.swing.JPanel primaryPanel;
    private javax.swing.JButton restartButton;
    private javax.swing.JLabel shadowDatabaseLabel;
    private javax.swing.JPanel shadowDatabasePanel;
    private javax.swing.JButton shadowDatabaseResetButton;
    private javax.swing.JButton shadowDatabaseSaveButton;
    private javax.swing.JTextField shadowDatabaseTextField;
    private javax.swing.JComboBox shadowDriverComboBox;
    private javax.swing.JLabel shadowDriverLabel;
    private javax.swing.JPasswordField shadowPasswordField;
    private javax.swing.JLabel shadowPasswordLabel;
    private javax.swing.JButton shadowSourceDatabaseTestButton;
    private javax.swing.JLabel shadowUrlLabel;
    private javax.swing.JButton shadowUrlTemplateButton;
    private javax.swing.JTextField shadowUrlTextField;
    private javax.swing.JLabel shadowUsernameLabel;
    private javax.swing.JTextField shadowUsernameTextField;
    private javax.swing.JLabel sourceDatabaseLabel;
    private javax.swing.JPanel sourceDatabasePanel;
    private javax.swing.JButton sourceDatabaseResetButton;
    private javax.swing.JButton sourceDatabaseSaveButton;
    private javax.swing.JButton sourceDatabaseTestButton;
    private javax.swing.JTextField sourceDatabaseTextField;
    private javax.swing.JComboBox sourceDriverComboBox;
    private javax.swing.JLabel sourceDriverLabel;
    private javax.swing.JPasswordField sourcePasswordField;
    private javax.swing.JLabel sourcePasswordLabel;
    private javax.swing.JLabel sourceUrlLabel;
    private javax.swing.JButton sourceUrlTemplateButton;
    private javax.swing.JTextField sourceUrlTextField;
    private javax.swing.JLabel sourceUsernameLabel;
    private javax.swing.JTextField sourceUsernameTextField;
    private javax.swing.JButton stopButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
