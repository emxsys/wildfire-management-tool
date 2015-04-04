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
import com.emxsys.util.MathUtil;
import com.emxsys.util.TimeUtil;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.DiurnalWeatherProvider;
import com.emxsys.weather.api.WeatherPreferences;
import com.emxsys.weather.api.WeatherType;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_INDEX;
import static com.emxsys.weather.api.WeatherType.CLOUD_COVER_INDEX;
import static com.emxsys.weather.api.WeatherType.FIRE_WEATHER;
import static com.emxsys.weather.api.WeatherType.REL_HUMIDITY_INDEX;
import static com.emxsys.weather.api.WeatherType.WIND_DIR;
import static com.emxsys.weather.api.WeatherType.WIND_DIR_INDEX;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_INDEX;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_KTS;
import com.emxsys.weather.panels.WindForcePanel;
import com.emxsys.weather.panels.WindSpeedDirectionChart;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DateFormatter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import visad.FlatField;
import visad.Real;
import visad.Unit;
import visad.VisADException;

@Messages({"CTL_DiurnalWinds=Daily Winds"})
public final class DiurnalWeatherPanelWinds extends JPanel {

    private final WindsTableModel tableModel = new WindsTableModel();
    private final WindForcePanel windDials;
    private final WindSpeedDirectionChart chart;
    private Unit speedUom = WeatherPreferences.getWindSpeedUnit();

    /**
     * Creates new form DiurnalWeatherPanelWinds.
     * @param provider
     */
    public DiurnalWeatherPanelWinds(DiurnalWeatherProvider provider) {
        initComponents();

        // Customize Date spinner to only show/edit time
        JSpinner.DateEditor editor = new JSpinner.DateEditor(timeSpinField, "HH:mm");
        DateFormatter formatter = (DateFormatter) editor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false); // this makes what you want
        formatter.setOverwriteMode(true);
        timeSpinField.setEditor(editor);

        // Add the wind direction and speed dials.  
        // These dials have sliders that will generate property change events when manipulated
        windDials = new WindForcePanel();
        windDials.addPropertyChangeListener(WindForcePanel.PROP_WIND_SPD, (evt) -> {
            try {
                Real newValue = (Real) evt.getNewValue();
                speedSpinField.setValue(MathUtil.round(newValue.getValue(speedUom), 1));
            } catch (VisADException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        windDials.addPropertyChangeListener(WindForcePanel.PROP_WIND_DIR, (evt) -> {
            Real newValue = (Real) evt.getNewValue();
            dirSpinField.setValue(newValue.getValue());
        });
        dialsPanel.add(windDials);

        // Create the weather chart used to plot the winds
        chart = new WindSpeedDirectionChart();
        chartPanel.add(chart);

        // Populate the table model (assumes dirs and speeds have cooincident times)
        TreeMap<LocalTime, Real> dirs = provider.getWindDirs();
        TreeMap<LocalTime, Real> spds = provider.getWindSpeeds();
        Iterator<LocalTime> times = spds.keySet().iterator();
        while (times.hasNext()) {
            LocalTime time = times.next();
            tableModel.add(time, dirs.get(time), spds.get(time));
        }

        // Update the weather chart from the table model
        updateChartFromTable();
    }

    @Override
    public String getName() {
        return Bundle.CTL_DiurnalWinds();
    }

    public TreeMap<LocalTime, Real> getWindSpeeds() {
        TreeMap<LocalTime, Real> spds = new TreeMap<>();
        java.util.Set<Map.Entry<LocalTime, WindsTableModel.Item>> entrySet = tableModel.getItems().entrySet();
        for (Map.Entry<LocalTime, WindsTableModel.Item> entry : entrySet) {
            spds.put(entry.getKey(), entry.getValue().windSpd);
        }
        return spds;
    }

    public TreeMap<LocalTime, Real> getWindDirections() {
        TreeMap<LocalTime, Real> dirs = new TreeMap<>();
        java.util.Set<Map.Entry<LocalTime, WindsTableModel.Item>> entrySet = tableModel.getItems().entrySet();
        for (Map.Entry<LocalTime, WindsTableModel.Item> entry : entrySet) {
            dirs.put(entry.getKey(), entry.getValue().windDir);
        }
        return dirs;
    }

    void setSpeedUom(Unit uom) {
        speedUom = uom;
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
            TreeMap<LocalTime, WindsTableModel.Item> items = tableModel.getItems();
            for (int i = 0; i < wxField.getLength(); i++) {

                LocalTime time = domain.getZonedDateTimeAt(i).toLocalTime();
                Map.Entry<LocalTime, WindsTableModel.Item> entry = items.floorEntry(time);
                Real spd = (entry == null) ? new Real(WIND_SPEED_KTS, 0) : entry.getValue().windSpd;
                Real dir = (entry == null) ? new Real(WIND_DIR, 0) : entry.getValue().windDir;

                wxSamples[AIR_TEMP_INDEX][i] = Double.NaN;      // ignored
                wxSamples[REL_HUMIDITY_INDEX][i] = Double.NaN;  // ignored
                wxSamples[WIND_SPEED_INDEX][i] = spd.getValue(speedUom);
                wxSamples[WIND_DIR_INDEX][i] = dir.getValue();
                wxSamples[CLOUD_COVER_INDEX][i] = Double.NaN;   // ignored
            }
            // ...and put the weather values above into it
            wxField.setSamples(wxSamples);
            chart.setWinds(wxField);

        } catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * Sorted table for winds
     */
    public class WindsTableModel extends AbstractTableModel //implements TableModel
    {

        public class Item {

            Real windSpd;
            Real windDir;

            public Item(Real windSpd, Real windDir) {
                this.windSpd = windSpd;
                this.windDir = windDir;
            }

        }
        private static final int NUM_COLS = 3;
        private final TreeMap<LocalTime, Item> map = new TreeMap<>();

        public TreeMap<LocalTime, Item> getItems() {
            return map;
        }

        public void add(LocalTime time, Real spd, Real dir) {
            try {
                map.put(time, new Item(spd, dir));
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

        chartPanel = new javax.swing.JPanel();
        controlsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        dirSpinField = new javax.swing.JSpinner();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        speedSpinField = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        timeSpinField = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        windsTable = new javax.swing.JTable();
        dialsPanel = new javax.swing.JPanel();

        chartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        chartPanel.setPreferredSize(new java.awt.Dimension(200, 100));
        chartPanel.setLayout(new java.awt.BorderLayout());

        controlsPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        controlsPanel.setPreferredSize(new java.awt.Dimension(60, 150));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelWinds.class, "DiurnalWeatherPanelWinds.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DiurnalWeatherPanelWinds.class, "DiurnalWeatherPanelWinds.jLabel3.text")); // NOI18N

        dirSpinField.setModel(new javax.swing.SpinnerNumberModel(0, 0, 360, 1));
        dirSpinField.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dirSpinFieldStateChanged(evt);
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
        speedSpinField.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                speedSpinFieldStateChanged(evt);
            }
        });

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
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(timeSpinField, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(speedSpinField)
                    .addComponent(dirSpinField))
                .addGap(18, 18, 18)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)))
        );
        controlsPanelLayout.setVerticalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeSpinField, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                    .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dirSpinField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speedSpinField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeAllButton))
                .addGap(17, 17, 17))
        );

        jScrollPane1.setPreferredSize(new java.awt.Dimension(60, 100));

        windsTable.setModel(tableModel);
        jScrollPane1.setViewportView(windsTable);

        dialsPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        dialsPanel.setPreferredSize(new java.awt.Dimension(200, 150));
        dialsPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dialsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                    .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(controlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(controlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(dialsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        // TODO addWindParam your handling code here:
        this.tableModel.clear();
        updateChartFromTable();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int selectedRow = windsTable.getSelectedRow();
        if (selectedRow >= 0) {
            this.tableModel.remove(selectedRow);
            updateChartFromTable();
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed

        // Use a LocalTime to get second-of-day for the map ky
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) timeSpinField.getValue());
        LocalTime time = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        Real direction = windDials.getWindDirection();
        Real windspeed = windDials.getWindSpeed();

        tableModel.add(time, windspeed, direction);
        updateChartFromTable();


    }//GEN-LAST:event_addButtonActionPerformed

    private void dirSpinFieldStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dirSpinFieldStateChanged

        // Sync the wind direction dial to the spinner
        // Note, setting the new value will not generate a property change event. :)
        Number value = (Number) dirSpinField.getValue();
        windDials.setWindDirection(new Real(WeatherType.WIND_DIR, value.doubleValue()));

    }//GEN-LAST:event_dirSpinFieldStateChanged

    private void speedSpinFieldStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSpinFieldStateChanged

        // Sync the wind speed dial to the spinner
        // Note, setting the new value will not generate a property change event. :)
        try {
            Number value = (Number) speedSpinField.getValue();
            windDials.setWindSpeed(new Real(WeatherType.WIND_SPEED, value.doubleValue(), speedUom));
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_speedSpinFieldStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JPanel dialsPanel;
    private javax.swing.JSpinner dirSpinField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JSpinner speedSpinField;
    private javax.swing.JSpinner timeSpinField;
    private javax.swing.JTable windsTable;
    // End of variables declaration//GEN-END:variables
}
