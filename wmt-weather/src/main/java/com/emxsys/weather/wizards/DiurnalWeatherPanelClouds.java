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

import com.emxsys.util.DateUtil;
import com.emxsys.util.TimeUtil;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.WeatherType;
import static com.emxsys.weather.api.WeatherType.*;
import com.emxsys.weather.panels.RelativeHumiditySkyCoverChart;
import com.emxsys.weather.panels.SkyCoverGauge;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DateFormatter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.FlatField;
import visad.Real;
import visad.VisADException;

@Messages({"CTL_DiurnalCloudCover=Hourly Cloud Cover"})
public final class DiurnalWeatherPanelClouds extends JPanel {

    private final CloudsTableModel tableModel = new CloudsTableModel();
    private final SkyCoverGauge gauge;
    private final RelativeHumiditySkyCoverChart chart;

    /** Creates new form DiurnalWeatherVisualPanel5 */
    public DiurnalWeatherPanelClouds() {
        initComponents();
        // Customize Date spinner to only show/edit time
        JSpinner.DateEditor editor = new JSpinner.DateEditor(timeSpinField, "HH:mm");
        DateFormatter formatter = (DateFormatter) editor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false); // this makes what you want
        formatter.setOverwriteMode(true);
        timeSpinField.setEditor(editor);

        // Add the wind direction and speed dials....
        // Add property change listeners that will sync the controls to the dial sliders
        gauge = new SkyCoverGauge("Cloud Cover", new Real(CLOUD_COVER, 0));
        gauge.addPropertyChangeListener(SkyCoverGauge.PROP_SKY_COVER, (evt) -> {
            percentSpinField.setValue(((Real) evt.getNewValue()).getValue());
        });
        dialsPanel.add(gauge);

        // Create the weather chart used to plot the winds
        chart = new RelativeHumiditySkyCoverChart();
        chartPanel.add(chart);

        // Add a listener to sync the controls when the table selection changes
        cloudsTable.getSelectionModel().addListSelectionListener((event) -> {
            int row = cloudsTable.getSelectedRow();
            if (row < 0) {
                return;
            }
            LocalTime time = tableModel.getTimeAt(row);
            Real cloudCover = tableModel.getItemAt(row);
            percentSpinField.setValue(cloudCover.getValue());
            timeSpinField.setValue(DateUtil.fromLocalTime(time));

        });

        // Update the weather chart fromLocalTime the table model
        updateChartFromTable();
    }

    @Override
    public String getName() {
        return Bundle.CTL_DiurnalCloudCover();
    }

    public void setClouds(TreeMap<LocalTime, Real> clouds) {
        Iterator<LocalTime> times = clouds.keySet().iterator();
        while (times.hasNext()) {
            LocalTime time = times.next();
            tableModel.add(time, clouds.get(time));
        }
        updateChartFromTable();
    }

    public TreeMap<LocalTime, Real> getClouds() {
        TreeMap<LocalTime, Real> clouds = new TreeMap<>();
        Set<Entry<LocalTime, Real>> entrySet = tableModel.getItems().entrySet();
        for (Map.Entry<LocalTime, Real> entry : entrySet) {
            clouds.put(entry.getKey(), entry.getValue());
        }
        return clouds;
    }

    void updateChartFromTable() {
        // Create a 24 hour temporal domain
        ZonedDateTime startOfDay = TimeUtil.toStartOfDay(ZonedDateTime.now());
        TemporalDomain domain = TemporalDomain.from(startOfDay, 24);
        try {
            // Create a FieldImpl for the chart and the samples for the hourly weather range
            // The chart uses FIRE_WEATHER--we'll just populate the wind fields
            FlatField wxField = domain.createSimpleTemporalField(FIRE_WEATHER); // FunctionType: time -> fire weather)
            double[][] wxSamples = new double[FIRE_WEATHER.getDimension()][wxField.getLength()];

            // Create the wx range samples...
            TreeMap<LocalTime, Real> items = tableModel.getItems();
            for (int i = 0; i < wxField.getLength(); i++) {

                LocalTime time = domain.getZonedDateTimeAt(i).toLocalTime();
                Map.Entry<LocalTime, Real> entry = items.floorEntry(time);
                Real cloudCover = (entry == null) ? new Real(CLOUD_COVER, 0) : entry.getValue();

                wxSamples[WIND_SPEED_INDEX][i] = Double.NaN;    // ignored
                wxSamples[WIND_DIR_INDEX][i] = Double.NaN;      // ignored
                wxSamples[AIR_TEMP_INDEX][i] = Double.NaN;      // ignored
                wxSamples[REL_HUMIDITY_INDEX][i] = Double.NaN;  // ignored
                wxSamples[CLOUD_COVER_INDEX][i] = cloudCover.getValue();
            }
            // ...and put the weather values above into it
            wxField.setSamples(wxSamples);
            chart.setCloudCoverForecasts(wxField);

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * Sorted table for winds
     */
    public class CloudsTableModel extends AbstractTableModel //implements TableModel
    {

        private static final int NUM_COLS = 3;
        private final TreeMap<LocalTime, Real> map = new TreeMap<>();

        public TreeMap<LocalTime, Real> getItems() {
            return map;
        }

        public void add(LocalTime time, Real cloudCover) {
            try {
                map.put(time, cloudCover);
                this.fireTableDataChanged();
                int row = getRow(time);
                if (row >= 0) {
                    cloudsTable.setRowSelectionInterval(row, row);
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

        public LocalTime getTimeAt(int row) {
            return (LocalTime) map.navigableKeySet().toArray()[row];
        }

        public Real getItemAt(int row) {
            return map.get(getTimeAt(row));
        }

        @Override
        public Object getValueAt(int row, int column) {
            try {
                if (column == 0) {
                    return getTimeAt(row);
                } else {
                    return getItemAt(row).getValue();
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
                    return "Cloud Cover";
            }
            return null;
        }
    }

    /** This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dialsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cloudsTable = new javax.swing.JTable();
        controlsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        percentSpinField = new javax.swing.JSpinner();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        timeSpinField = new javax.swing.JSpinner();
        chartPanel = new javax.swing.JPanel();

        dialsPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        dialsPanel.setPreferredSize(new java.awt.Dimension(200, 150));
        dialsPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(60, 100));

        cloudsTable.setModel(tableModel);
        jScrollPane1.setViewportView(cloudsTable);

        controlsPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        controlsPanel.setPreferredSize(new java.awt.Dimension(60, 150));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelClouds.class, "DiurnalWeatherPanelClouds.jLabel3.text")); // NOI18N

        percentSpinField.setModel(new javax.swing.SpinnerNumberModel(0, 0, 360, 1));
        percentSpinField.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                percentSpinFieldStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelClouds.class, "DiurnalWeatherPanelClouds.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelClouds.class, "DiurnalWeatherPanelClouds.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeAllButton, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelClouds.class, "DiurnalWeatherPanelClouds.removeAllButton.text")); // NOI18N
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelClouds.class, "DiurnalWeatherPanelClouds.jLabel4.text")); // NOI18N

        timeSpinField.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1388584800000L), null, null, java.util.Calendar.DAY_OF_MONTH));

        javax.swing.GroupLayout controlsPanelLayout = new javax.swing.GroupLayout(controlsPanel);
        controlsPanel.setLayout(controlsPanelLayout);
        controlsPanelLayout.setHorizontalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(27, 27, 27))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(timeSpinField, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(percentSpinField))
                .addGap(18, 18, 18)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)))
        );
        controlsPanelLayout.setVerticalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeSpinField, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(addButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(percentSpinField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(removeButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeAllButton)
                .addGap(21, 21, 21))
        );

        chartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        chartPanel.setPreferredSize(new java.awt.Dimension(200, 100));
        chartPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dialsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(controlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(controlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(dialsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void percentSpinFieldStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_percentSpinFieldStateChanged

        // Sync the wind direction dial to the spinner
        // Note, setting the new value will not generate a property change event. :)
        Number value = (Number) percentSpinField.getValue();
        gauge.setSkyCover(new Real(WeatherType.CLOUD_COVER, value.doubleValue()));
    }//GEN-LAST:event_percentSpinFieldStateChanged

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed

        // Use a LocalTime to get second-of-day for the map key
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) timeSpinField.getValue());

        LocalTime time = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        Real cloudCover = gauge.getCloudCover();

        tableModel.add(time, cloudCover);
        updateChartFromTable();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int selectedRow = cloudsTable.getSelectedRow();
        if (selectedRow >= 0) {
            this.tableModel.remove(selectedRow);
            updateChartFromTable();
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        // TODO addWindParam your handling code here:
        this.tableModel.clear();
        updateChartFromTable();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JTable cloudsTable;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JPanel dialsPanel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner percentSpinField;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JSpinner timeSpinField;
    // End of variables declaration//GEN-END:variables
}
