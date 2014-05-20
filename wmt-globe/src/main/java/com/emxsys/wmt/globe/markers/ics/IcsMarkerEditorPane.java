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
package com.emxsys.wmt.globe.markers.ics;

import com.emxsys.gis.api.Coord3D;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Component;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.openide.util.NbBundle.Messages;

@Messages(
        {
            "CTL_PositionLocked=Locked",
            "CTL_PositionUnlocked=Unlocked",
            "CTL_LockedDesc=Locked: the marker's position is locked.",
            "CTL_UnlockedDesc=Unlocked: the marker can be moved."
        })
/**
 * ICS Editor dialog.
 * @author Bruce Schubert
 */
public class IcsMarkerEditorPane extends javax.swing.JPanel {

    private enum State {

        NEW, INITIALIZING, EDITING
    };
    private State state = State.NEW;
    private PointPlacemarkAttributes renderingAttributes;
    private ImageIcon[] images;
    private final String[] names
            = {
                "Aerial Hazard",
                "Aerial Ignition",
                "Airport 0",
                "Airport 1",
                "Archaeological Site 0",
                "Archaeological Site 1",
                "Branch Break",
                "Camp",
                "Division Break",
                "Drop Point",
                "Fire Location",
                "Fire Origin",
                "Fire Station",
                "First Aid 0",
                "First Aid 1",
                "Heat Source",
                "Helibase",
                "Helispot",
                "Historical Site",
                "Incident Base",
                "Incident Command Post",
                "MediVac Site",
                "Mobile Weather Unit",
                "Repeater",
                "Retardant Pickup",
                "Safety Zone 0",
                "Staging Area",
                "Water Source"
            };
    private int currSelectionIndex;
    private static int lastSelectionIndex = 0;
    private static final Logger LOG = Logger.getLogger(IcsMarkerEditorPane.class.getName());

    /**
     * Creates new form EditMarkerPanel
     * @param markerName
     * @param location
     * @param isMovable
     * @param attributes
     */
    public IcsMarkerEditorPane(String markerName, Coord3D location, boolean isMovable,
                               final PointPlacemarkAttributes attributes) {
        this.state = State.INITIALIZING;
        if (markerName == null) {
            String msg = "markerName argument cannot be null.";
            LOG.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (location == null) {
            String msg = "location argument cannot be null.";
            LOG.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        initComponents();

        // Initialize the rendering attributes that will be used for the Pushpin
        renderingAttributes = new PointPlacemarkAttributes(attributes);

        //Load the marker images and create an array of indexes.
        images = new ImageIcon[names.length];
        Integer[] intArray = new Integer[names.length];
        for (int i = 0; i < names.length; i++) {
            intArray[i] = i;
            String filename = names[i].replace(" ", "_");
            images[i] = createImageIcon(filename + "24.png");
        }
        // Create the name field -- autoselect the text to ease the editing of default names
        nameTextField.setText(markerName);
        nameTextField.setSelectionStart(0);
        nameTextField.setSelectionEnd(markerName.length());

        // Create the combo box for the icons
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        iconsComboBox.setModel(new javax.swing.DefaultComboBoxModel(intArray));
        iconsComboBox.setRenderer(renderer);

        // Preselect the current icon in the combo box
        String imageAddress = renderingAttributes.getImageAddress();
        if (imageAddress == null) {
            currSelectionIndex = lastSelectionIndex;
        } else {
            currSelectionIndex = findIconSelectionIndex(imageAddress);
        }
        iconsComboBox.setSelectedIndex(currSelectionIndex);

        // Position coordinates
        latitudeLabel.setText(location.getLatitude().toValueString());
        longitudeLabel.setText(location.getLongitude().toValueString());

        // Update the "locked" button
        lockToggleButton.setSelected(!isMovable);
        lockToggleButtonStateChanged(null);

        this.state = State.EDITING;
    }

    private int findIconSelectionIndex(String imageAddress) {
        if (imageAddress != null) {
            String filename = imageAddress.substring(imageAddress.lastIndexOf('/'));
            for (int i = 0; i < names.length; i++) {
                String s = names[i].replace(' ', '_');
                if (filename.contains(s)) {
                    return i;
                }
            }
            LOG.log(Level.WARNING, "findIconSelectionIndex could not locate {0}", filename);
        }
        return -1;
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = IcsMarkerEditorPane.class.getResource(path);
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            // Convienently store the image address property in the description field
            icon.setDescription(imgURL.toString());
            return icon;
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public String getMarkerName() {
        return nameTextField.getText().trim();
    }

    public boolean isMovable() {
        return !lockToggleButton.isSelected();
    }

    public PointPlacemarkAttributes getMarkerRenderingAttributes() {
        return renderingAttributes;
    }

    private void updateRenderingAttributes(int selectedIndex) {
        if (selectedIndex > -1) {
            // Note: we stored the image address/url in the description field
            ImageIcon icon = images[selectedIndex];
            renderingAttributes.setImageAddress(icon.getDescription());

            // As a convienience, prepend the icon name the text
            if (state == State.EDITING) {
                // Replace an existing icon name with the new icon name
                StringBuilder sb = new StringBuilder();
                final String DELIM = " - ";
                String[] split = nameTextField.getText().split(DELIM);

                // Replace previous icon.
                int i = split[0].equals(names[currSelectionIndex]) ? 1 : 0;
                sb.append(names[selectedIndex]);
                // Append the original text following the delimeter
                for (; i < split.length; i++) {
                    sb.append(DELIM);
                    sb.append(split[i]);
                }
                nameTextField.setText(sb.toString());
            }
            currSelectionIndex = selectedIndex;
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
            int selectedIndex = ((Integer) value).intValue();

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            //Set the icon and text.  If icon was null, say so.
            ImageIcon icon = images[selectedIndex];
            String pushpin = names[selectedIndex];
            setIcon(icon);
            if (icon != null) {
                setText(pushpin);
                setFont(list.getFont());
            } else {
                setUhOhText(pushpin + " (no image available)",
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

        iconLabel = new javax.swing.JLabel();
        iconsComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        latLabel = new javax.swing.JLabel();
        lonLabel = new javax.swing.JLabel();
        latitudeLabel = new javax.swing.JLabel();
        longitudeLabel = new javax.swing.JLabel();
        lockToggleButton = new javax.swing.JToggleButton();
        lockLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setMaximumSize(new java.awt.Dimension(343, 211));

        iconLabel.setText("Type:");

        iconsComboBox.setMinimumSize(new java.awt.Dimension(23, 32));
        iconsComboBox.setPreferredSize(new java.awt.Dimension(28, 32));
        iconsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconsComboBoxActionPerformed(evt);
            }
        });

        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));

        latLabel.setText("Latitude:");

        lonLabel.setText("Longitude:");

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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(latLabel)
                    .addComponent(lonLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lockToggleButton, 0, 0, Short.MAX_VALUE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(longitudeLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                            .addComponent(latitudeLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lockLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)))
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
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lockLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lockToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        nameLabel.setText("Name:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iconLabel)
                            .addComponent(nameLabel))
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                                .addGap(4, 4, 4))
                            .addComponent(iconsComboBox, 0, 264, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(iconLabel)
                        .addGap(10, 10, 10))
                    .addComponent(iconsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void iconsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconsComboBoxActionPerformed
    // Remember the last selection so we can preselect this icon the next time the dialog is displayed
    int selectedIndex = iconsComboBox.getSelectedIndex();
    updateRenderingAttributes(selectedIndex);
    lastSelectionIndex = selectedIndex;
}//GEN-LAST:event_iconsComboBoxActionPerformed

    private void lockToggleButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_lockToggleButtonActionPerformed
    {//GEN-HEADEREND:event_lockToggleButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lockToggleButtonActionPerformed

    private void lockToggleButtonStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_lockToggleButtonStateChanged
    {//GEN-HEADEREND:event_lockToggleButtonStateChanged
        // Update context sensitive controls here:
        if (lockToggleButton.getModel().isSelected()) {
            lockToggleButton.setIcon(new ImageIcon(getClass().getResource("lock24.png")));
            //lockToggleButton.setText(Bundle.CTL_PositionLocked());
            lockLabel.setText(Bundle.CTL_LockedDesc());
        } else {
            lockToggleButton.setIcon(new ImageIcon(getClass().getResource("unlock24.png")));
            //lockToggleButton.setText(Bundle.CTL_PositionUnlocked());
            lockLabel.setText(Bundle.CTL_UnlockedDesc());
        }
    }//GEN-LAST:event_lockToggleButtonStateChanged
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
