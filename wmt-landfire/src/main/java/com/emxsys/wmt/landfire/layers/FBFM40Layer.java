/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.landfire.layers;

import com.csvreader.CsvReader;
import com.emxsys.gis.api.layer.BasicLayerCategory;
import com.emxsys.gis.api.layer.BasicLayerLegend;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.BasicLayerType;
import com.emxsys.util.ModuleUtil;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.StdFuelModelParams40;
import gov.nasa.worldwind.avlist.AVList;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;

/**
 * Fire Behavior Fuel Model 40 standard models.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class FBFM40Layer extends LandfireTiledImageLayer {

    /** The legend displayed in the layer manager */
    private final BasicLayerLegend legend = new BasicLayerLegend();
    private static final Logger LOG = Logger.getLogger(FBFM40Layer.class.getName());

    public FBFM40Layer(Element domElement, AVList params) {
        super(domElement, params);
        // Add an instance of the StdFuelModelParams40 to indicate that this
        // layer has support/capabilities for the 40's.  Any instance will do.
        // Clients can test layers for this capability.
        super.content.add(StdFuelModelParams40.FBFM101);
        super.content.add(BasicLayerType.Raster);
        super.content.add(BasicLayerGroup.Overlay);
        super.content.add(BasicLayerCategory.Thematic);
        super.content.add(LandfireLayerCategory.Fuels);
        // Add a LayerLegend instance to manifest a legend for the layer
        super.content.add(this.legend);

    }

    /**
     * Initialize the color key/value map from a LANDFIRE csv file. The csv
     * files were originally obtained from the CSV folder on the LANDFIRE
     * distribution DVDs. The color values closely match the image colors
     * returned by the LANDFIRE WMS servers. Example CSV file content: </br>
     * <pre>
     * </br> 
     * VALUE,   FBFM40,  Red,       Green,      Blue</br>
     * 91,      NB1,    0.407843137,0.407843137,0.407843137</br>
     * 92,      NB2,    0.882352941,0.882352941,0.882352941</br>
     * 93,      NB3,    1,          0.929411765,0.929411765</br>
     * 98,      NB8,    0,          0.054901961,0.839215686</br>
     * 99,      NB9,    0.301960784,0.431372549,0.439215686</br>
     * 101,     GR1,    1,          0.921568627,0.745098039</br>
     * 102,     GR2,    1,          0.82745098, 0.450980392</br>
     * 103,     GR3,    1,          0.925490196,0.545098039</br> 
     * 104,     GR4,    1,          1,          0.450980392</br>
     * ...</br>
     * -9999,   NoData, 0,          0,          0</br>
     * </pre>
     */
    @Override
    protected void initColorMap(URL csvUrl) {
        // Create the color key a csv file in the found in the modules/ext folder hierarchy
        try {
            if (csvUrl == null) {
                return;
            }
            File csvFile = ModuleUtil.createFileFromUrl(csvUrl);
            CsvReader csv = new CsvReader(csvFile.getPath());
            // Parse the header and get the column indices
            csv.readHeaders();
            int red = csv.getIndex("Red");
            int green = csv.getIndex("Green");
            int blue = csv.getIndex("Blue");
            int value = csv.getIndex("VALUE");
            int fbfm = csv.getIndex("FBFM40");
            // Parse the records and generate color/value mappings
            while (csv.readRecord()) {
                float r = Float.parseFloat(csv.get(red));
                float g = Float.parseFloat(csv.get(green));
                float b = Float.parseFloat(csv.get(blue));
                int val = Integer.parseInt(csv.get(value));
                FuelModel fm = StdFuelModel.from(val);
                if (fm != null) {
                    Color color = new Color(r, g, b);
                    addColorEntry(color, fm);

                    // Add this time to the legend.
                    this.legend.add(color, fm.getModelName());

                    LOG.log(Level.FINE, "Created Fuel Model: {0}", fm);
                }
            }
            csv.close();

        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
