/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
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
package com.emxsys.weather.wizards;

import com.emxsys.util.AngleUtil;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.panels.WindChartPanel;
import com.emxsys.weather.panels.WindForcePanel;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DateFormatter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.Real;

@Messages({"CTL_DiurnalWinds=Daily Winds"})
public final class DiurnalWeatherPanelWinds extends JPanel {

    private static final String[] PTS = new String[]{
        "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N"
    };
    private final WindsTableModel tableModel = new WindsTableModel();
    private WindForcePanel windPanel;

    /** Creates new form DiurnalWeatherVisualPanel4 */
    public DiurnalWeatherPanelWinds(DiurnalWeatherProvider provider) {
        initComponents();

        // Customize Date spinner to only show/edit time
        JSpinner.DateEditor editor = new JSpinner.DateEditor(timeSpinField, "HH:mm");
        DateFormatter formatter = (DateFormatter) editor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false); // this makes what you want
        formatter.setOverwriteMode(true);
        timeSpinField.setEditor(editor);

        // Add the wind direction and speed dials.
        windPanel = new WindForcePanel();
        jPanel1.add(windPanel);

        // Populate the table model (assumes dirs and speeds have cooincident times)
        TreeMap<LocalTime, Real> dirs = provider.getWindDirs();
        TreeMap<LocalTime, Real> spds = provider.getWindSpeeds();
        Iterator<LocalTime> times = spds.keySet().iterator();
        while (times.hasNext()) {
            LocalTime time = times.next();
            tableModel.add(time, dirs.get(time), spds.get(time));
        }
    }

    @Override
    public String getName() {
        return Bundle.CTL_DiurnalWinds();
    }

    /**
     * Sorted table for winds
     */
    public class WindsTableModel extends AbstractTableModel //implements TableModel
    {

        public class Item {

            Real windDir;
            Real windSpd;

            public Item(Real windSpd, Real windDir) {
                this.windDir = windDir;
                this.windSpd = windSpd;
            }

        }
        private static final int NUM_COLS = 3;
        private final TreeMap<LocalTime, Item> map = new TreeMap<>();

        public void add(LocalTime time, Real dir, Real spd) {
            try {
                map.put(time, new Item(dir, spd));
                this.fireTableDataChanged();
                int row = getRow(time);
                if (row >= 0) {
                    windsTable.setRowSelectionInterval(row, row);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void clear() {
            map.clear();
            this.fireTableDataChanged();
        }

        public void remove(int row) {
            LocalTime key = (LocalTime) map.navigableKeySet().toArray()[row];
            map.remove(key);
            this.fireTableDataChanged();

        }

        @Override
        public int getColumnCount() {
            return NUM_COLS;
        }

        @Override
        public int getRowCount() {
            return this.map.size();
        }

        public int getRow(LocalTime time) {
            return Arrays.binarySearch(map.navigableKeySet().toArray(), time);
        }

        @Override
        public Object getValueAt(int row, int column) {
            LocalTime key = (LocalTime) map.navigableKeySet().toArray()[row];
            Item item = map.get(key);
            try {
                if (column == 0) {
                    return key;
                } else if (column == 1) {
                    return item.windSpd;
                } else {
                    return AngleUtil.degreesToCardinalPoint16(item.windDir.getValue());
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Time";
                case 1:
                    return "Speed";
                case 2:
                    return "Dir.";
            }
            return null;
        }
    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        windChartPanel = new WindChartPanel();
        controlsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        compassSpinner = new javax.swing.JSpinner();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        speedSpinField = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        timeSpinField = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        windsTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();

        windChartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout windChartPanelLayout = new javax.swing.GroupLayout(windChartPanel);
        windChartPanel.setLayout(windChartPanelLayout);
        windChartPanelLayout.setHorizontalGroup(
            windChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        windChartPanelLayout.setVerticalGroup(
            windChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 174, Short.MAX_VALUE)
        );

        controlsPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelWinds.class, "DiurnalWeatherPanelWinds.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelWinds.class, "DiurnalWeatherPanelWinds.jLabel3.text")); // NOI18N

        compassSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"}));
        compassSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                compassSpinnerStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelWinds.class, "DiurnalWeatherPanelWinds.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelWinds.class, "DiurnalWeatherPanelWinds.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeAllButton, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelWinds.class, "DiurnalWeatherPanelWinds.removeAllButton.text")); // NOI18N
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });

        speedSpinField.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(50.0f), Float.valueOf(1.0f)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelWinds.class, "DiurnalWeatherPanelWinds.jLabel4.text")); // NOI18N

        timeSpinField.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1388584800000L), null, null, java.util.Calendar.DAY_OF_MONTH));

        javax.swing.GroupLayout controlsPanelLayout = new javax.swing.GroupLayout(controlsPanel);
        controlsPanel.setLayout(controlsPanelLayout);
        controlsPanelLayout.setHorizontalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlsPanelLayout.createSequentialGroup()
                        .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4))
                        .addGap(19, 19, 19))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(compassSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(speedSpinField, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(timeSpinField, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)))
        );
        controlsPanelLayout.setVerticalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(timeSpinField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compassSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speedSpinField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeAllButton))
                .addGap(17, 17, 17))
        );

        windsTable.setModel(tableModel);
        jScrollPane1.setViewportView(windsTable);

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                    .addComponent(windChartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(controlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(windChartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(controlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        // TODO addWindParam your handling code here:
        this.tableModel.clear();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int selectedRow = windsTable.getSelectedRow();
        if (selectedRow >= 0) {
            this.tableModel.remove(selectedRow);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // TODO addWindParam your handling code here:

        // Use a LocalTime to get second-of-day for the map ky
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) timeSpinField.getValue());
        LocalTime time = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        Real direction = windPanel.getWindDirection();
        Real windspeed = windPanel.getWindSpeed();

        tableModel.add(time, windspeed, direction);
    }//GEN-LAST:event_addButtonActionPerformed

    private void compassSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_compassSpinnerStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_compassSpinnerStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JSpinner compassSpinner;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JSpinner speedSpinField;
    private javax.swing.JSpinner timeSpinField;
    private javax.swing.JPanel windChartPanel;
    private javax.swing.JTable windsTable;
    // End of variables declaration//GEN-END:variables
}
