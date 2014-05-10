/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.cache;

import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwindx.examples.util.FileStoreDataSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * @author tag
 * @version $Id: DataCacheViewer.java 395 2012-12-08 21:12:52Z bdschubert $
 */
public class DataCacheViewer
{

    private JPanel panel;
    private CacheTable table;
    private JButton delBtn;
    private JSpinner ageSpinner;
    private JComboBox ageUnit;
    private JLabel deleteSizeLabel;



    public DataCacheViewer()
    {

        FileStore store = new BasicDataFileStore();
        File cacheRoot = store.getWriteLocation();

        this.panel = new JPanel(new BorderLayout(5, 5));

        JLabel rootLabel = new JLabel("Cache Root: " + cacheRoot.getPath());
        rootLabel.setBorder(new EmptyBorder(10, 15, 10, 10));
        this.panel.add(rootLabel, BorderLayout.NORTH);

        this.table = new CacheTable();
        this.table.setDataSets(cacheRoot.getPath(), FileStoreDataSet.getDataSets(cacheRoot));
        JScrollPane sp = new JScrollPane(table);
        this.panel.add(sp, BorderLayout.CENTER);

        JPanel pa = new JPanel(new BorderLayout(10, 10));
        pa.add(new JLabel("Delete selected data older than"), BorderLayout.WEST);
        this.ageSpinner = new JSpinner(new SpinnerNumberModel(6, 0, 10000, 1));
        this.ageSpinner.setToolTipText("0 selects the entire dataset regardless of age");
        JPanel pas = new JPanel();
        pas.add(this.ageSpinner);
        pa.add(pas, BorderLayout.CENTER);
        this.ageUnit = new JComboBox(new String[]
                {
                    "Hours", "Days", "Weeks", "Months", "Years"
                });
        this.ageUnit.setSelectedItem("Months");
        this.ageUnit.setEditable(false);
        pa.add(this.ageUnit, BorderLayout.EAST);

        JPanel pb = new JPanel(new BorderLayout(5, 10));
        this.deleteSizeLabel = new JLabel("Total to delete: 0 MB");
        pb.add(this.deleteSizeLabel, BorderLayout.WEST);
        this.delBtn = new JButton("Delete");
        this.delBtn.setEnabled(false);
        JPanel pbb = new JPanel();
        pbb.add(this.delBtn);
        pb.add(pbb, BorderLayout.CENTER);

        JPanel pc = new JPanel(new BorderLayout(5, 10));
        pc.add(pa, BorderLayout.WEST);
        pc.add(pb, BorderLayout.EAST);

        JPanel ctlPanel = new JPanel(new BorderLayout(10, 10));
        ctlPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
        ctlPanel.add(pc, BorderLayout.CENTER);

        this.panel.add(ctlPanel, BorderLayout.SOUTH);

        this.ageUnit.addItemListener(new ItemListener()
        {

            @Override
            public void itemStateChanged(ItemEvent e)
            {
                update();
            }
        });

        this.ageSpinner.addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                update();
            }
        });

        this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {

            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                update();
            }
        });

        this.delBtn.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Thread t = new Thread(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        try
                        {
                            List<FileStoreDataSet> dataSets = table.getSelectedDataSets();
                            int age = Integer.parseInt(ageSpinner.getValue().toString());
                            String unit = getUnitKey();

                            for (FileStoreDataSet ds : dataSets)
                            {
                                ds.deleteOutOfScopeFiles(unit, age, false);
                                if (ds.getSize() == 0)
                                {
                                    table.deleteDataSet(ds);
                                    ds.delete(false);
                                }
                            }
                        }
                        finally
                        {
                            update();
                            SwingUtilities.invokeLater(new Runnable()
                            {

                                @Override
                                public void run()
                                {
                                    panel.setCursor(Cursor.getDefaultCursor());
                                }
                            });
                        }
                    }
                });
                t.start();
            }
        });

    }



    private void update()
    {
        java.util.List<FileStoreDataSet> dataSets = this.table.getSelectedDataSets();
        int age = Integer.parseInt(this.ageSpinner.getValue().toString());

        if (dataSets.size() == 0)
        {
            this.deleteSizeLabel.setText("Total to delete: 0 MB");
            this.delBtn.setEnabled(false);
            return;
        }

        String unit = this.getUnitKey();

        long totalSize = 0;
        for (FileStoreDataSet ds : dataSets)
        {
            totalSize += ds.getOutOfScopeSize(unit, age);
        }

        Formatter formatter = new Formatter();
        formatter.format("%5.1f", ((float) totalSize) / 1e6);
        this.deleteSizeLabel.setText("Total to delete: " + formatter.toString() + " MB");

        this.delBtn.setEnabled(true);
    }



    private String getUnitKey()
    {
        String unit = null;
        String unitString = (String) this.ageUnit.getSelectedItem();
        if (unitString.equals("Hours"))
        {
            unit = FileStoreDataSet.HOUR;
        }
        else if (unitString.equals("Days"))
        {
            unit = FileStoreDataSet.DAY;
        }
        else if (unitString.equals("Weeks"))
        {
            unit = FileStoreDataSet.WEEK;
        }
        else if (unitString.equals("Months"))
        {
            unit = FileStoreDataSet.MONTH;
        }
        else if (unitString.equals("Years"))
        {
            unit = FileStoreDataSet.YEAR;
        }

        return unit;
    }



    public void displayModal(String dialogTitle)
    {
        Object[] options =
        {
            new JButton("Close")
        };
        // Wrap the panel in a standard dialog
        DialogDescriptor descriptor = new DialogDescriptor(
                this.panel,
                dialogTitle,
                true, // Modal?
                options,
                null,
                DialogDescriptor.DEFAULT_ALIGN, 
                null,
                null);

        DialogDisplayer.getDefault().notify(descriptor);
    }
}
