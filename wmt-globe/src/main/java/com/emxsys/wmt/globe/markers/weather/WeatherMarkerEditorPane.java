/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.emxsys.wmt.globe.markers.weather;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.globe.markers.weather.*;
import com.emxsys.wmt.weather.api.WeatherProvider;
import com.emxsys.wmt.weather.spi.DefaultWeatherProvider;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.openide.util.NbBundle.Messages;

@Messages({
    "CTL_PositionLocked=Locked",
    "CTL_PositionUnlocked=Unlocked",
    "CTL_LockedDesc=The weather position is locked.",
    "CTL_UnlockedDesc=The weather can be moved."
})
/**
 * Weather Editor dialog.
 * @author Bruce Schubert
 */
public class WeatherMarkerEditorPane extends javax.swing.JPanel {

    private WeatherProvider provider;
    private WeatherProvider[] providers;
    private static int lastSelectionIndex = 0;

    /**
     * Creates the weather editor panel.
     * @param markerName
     * @param location
     * @param isMovable
     * @param attributes
     */
    public WeatherMarkerEditorPane(String markerName, Coord3D location, boolean isMovable,
                                   final PointPlacemarkAttributes attributes) {
        initComponents();

        // Find all the Weather service providers that support point forecasts
        List<WeatherProvider> pointForecasters = DefaultWeatherProvider.getPointForecasters();
        Integer[] intArray = new Integer[pointForecasters.size()];
        providers = new WeatherProvider[pointForecasters.size()];
        int i = 0;
        for (WeatherProvider p : pointForecasters) {
            intArray[i] = i;
            providers[i] = p;
            ++i;
        }

        // Create the name field -- autoselect the text to ease the editing of default names
        nameTextField.setText(markerName);
        nameTextField.setSelectionStart(0);
        nameTextField.setSelectionEnd(markerName.length());

        // Create the combo box for the icons
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        iconsComboBox.setModel(new javax.swing.DefaultComboBoxModel(intArray));
        iconsComboBox.setRenderer(renderer);
        iconsComboBox.setSelectedIndex(lastSelectionIndex);

        // Preselect the current image in the combo box
        if (attributes != null) {
//            String imageUrl = attributes.getImageAddress();
//            if (imageUrl != null) {
//                String filename = new File(imageUrl).getName();
//                for (int i = 0; i < images.length; i++) {
//                    String url = images[i].getDescription();
//                    if (url.contains(filename)) {
//                        iconsComboBox.setSelectedIndex(i);
//                        break;
//                    }
//                }
//            }
        }
        // Position coordinates
        if (location != null) {
            latitudeLabel.setText(location.getLatitude().toValueString());
            longitudeLabel.setText(location.getLongitude().toValueString());
        }

        // Update the "locked" button
        lockToggleButton.setSelected(!isMovable);
        lockToggleButtonStateChanged(null);
    }

    public String getMarkerName() {
        return nameTextField.getText().trim();
    }

    public boolean isMovable() {
        return !lockToggleButton.isSelected();
    }

    public WeatherProvider getWeatherProvider() {
        return provider;
    }

    private void updateProvider(int selectedIndex) {
        if (selectedIndex > -1) {
            provider = providers[selectedIndex];
        }
    }

    class ComboBoxRenderer extends JLabel implements ListCellRenderer {

        private Font uhOhFont;

        ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /**
         * This method finds the image and text corresponding to the selected value and returns the
         * label, set up to display the text and image.
         */
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            //Get the selected index. (The index param isn't
            //always valid, so just use the value.)
            int selectedIndex = ((Integer) value);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            //Set the icon and text.  If icon was null, say so.
            ImageIcon icon = providers[selectedIndex].getImageIcon();
            String name = providers[selectedIndex].getClass().getSimpleName();
            setIcon(icon);
            if (icon != null) {
                setText(name);
                setFont(list.getFont());
            } else {
                setUhOhText(name + " (no image available)",
                        list.getFont());
            }

            return this;
        }

        //Set the font and text when no image was found.
        protected void setUhOhText(String uhOhText, Font normalFont) {
            if (uhOhFont == null) { //lazily create this font
                uhOhFont = normalFont.deriveFont(Font.ITALIC);
            }
            setFont(uhOhFont);
            setText(uhOhText);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        iconLabel = new javax.swing.JLabel();
        iconsComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        latLabel = new javax.swing.JLabel();
        lonLabel = new javax.swing.JLabel();
        latitudeLabel = new javax.swing.JLabel();
        longitudeLabel = new javax.swing.JLabel();
        lockToggleButton = new javax.swing.JToggleButton();
        lockLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        nameLabel.setText("Name:");

        iconLabel.setText("Provider:");

        iconsComboBox.setMinimumSize(new java.awt.Dimension(23, 32));
        iconsComboBox.setPreferredSize(new java.awt.Dimension(28, 32));
        iconsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconsComboBoxActionPerformed(evt);
            }
        });

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        latLabel.setText("Latitude:");

        lonLabel.setText("Longitude:");

        lockToggleButton.setText("Unlocked");
        lockToggleButton.setToolTipText("Lock the position to prevent the pushpin from being moved.");
        lockToggleButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lockToggleButtonStateChanged(evt);
            }
        });
        lockToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lockToggleButtonActionPerformed(evt);
            }
        });

        lockLabel.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lockToggleButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lockLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(latLabel)
                            .addComponent(lonLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(longitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                            .addComponent(latitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(latLabel)
                    .addComponent(latitudeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lonLabel)
                    .addComponent(longitudeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lockToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lockLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabel)
                            .addComponent(iconLabel))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iconsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iconsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconLabel))
                .addGap(28, 28, 28)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(89, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void iconsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconsComboBoxActionPerformed
    // Remember the last selection so we can preselect this icon the next time the dialog is displayed
    int selectedIndex = iconsComboBox.getSelectedIndex();
    updateProvider(selectedIndex);
    lastSelectionIndex = selectedIndex;
}//GEN-LAST:event_iconsComboBoxActionPerformed

private void lockToggleButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lockToggleButtonStateChanged
    // Update context sensitive controls here:
    if (lockToggleButton.getModel().isSelected()) {
        lockToggleButton.setIcon(new ImageIcon(getClass().getResource("lock.png")));
        lockToggleButton.setText(Bundle.CTL_PositionLocked());
        lockLabel.setText(Bundle.CTL_LockedDesc());
    } else {
        lockToggleButton.setIcon(new ImageIcon(getClass().getResource("unlock.png")));
        lockToggleButton.setText(Bundle.CTL_PositionUnlocked());
        lockLabel.setText(Bundle.CTL_UnlockedDesc());
    }
}//GEN-LAST:event_lockToggleButtonStateChanged

private void lockToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lockToggleButtonActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_lockToggleButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iconLabel;
    private javax.swing.JComboBox iconsComboBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel latLabel;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JLabel lockLabel;
    private javax.swing.JToggleButton lockToggleButton;
    private javax.swing.JLabel lonLabel;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    // End of variables declaration//GEN-END:variables
}
