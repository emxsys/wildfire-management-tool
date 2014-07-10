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
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.BasicLayerLegend;
import com.emxsys.gis.api.layer.BasicLayerType;
import com.emxsys.util.ModuleUtil;
import com.emxsys.wildfire.api.FuelModel;
import com.emxsys.wildfire.api.StdFuelModel;
import com.emxsys.wildfire.api.StdFuelModelParams13;
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
 * Fire Behavior Fuel Model 13 original models.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: FBFM13Layer.java 766 2013-06-20 18:02:18Z bdschubert $
 */
public class FBFM13Layer extends LandfireTiledImageLayer {

    private static final Logger logger = Logger.getLogger(FBFM13Layer.class.getName());
    private final BasicLayerLegend legend = new BasicLayerLegend();

    public FBFM13Layer(Element domElement, AVList params) {
        super(domElement, params);
        // Add an instance of the StdFuelModelParams13 to indicate that this
        // layer has support/capabilities for the std's.  Any instance will do.
        // Clients can test layers for this capability.
        super.content.add(StdFuelModelParams13.FBFM01);
        super.content.add(BasicLayerType.Raster);
        super.content.add(BasicLayerGroup.Overlay);
        super.content.add(BasicLayerCategory.Thematic);
        super.content.add(LandfireLayerCategory.Fuels);
        super.content.add(this.legend);
        logger.config("Created FBFM13Layer with StdFuelModelParams13 capabilities.");
    }

    /**
     * Initialize the color key/value map and the LayerLegend from a LANDFIRE csv file. The csv
     * files were originally obtained from the CSV folder on the LANDFIRE distribution DVDs. The
     * color values closely match the image colors returned by the LANDFIRE WMS servers. Example CSV
     * file content: </br>
     * <pre>
     * Value,   FBFM13, RED,    GREEN,  BLUE</br>
     * 1,       FBFM1,  1,      1,      0.745098039</br>
     * 2,       FBFM2,  1,      1,      0</br>
     * 3,       FBFM3,  0.9019, 0.7725, 0.043137255</br>
     * ...</br>
     * -9999,   NoData, 0,      0,      0</br>
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
            // Note: header names are case-sensitive
            csv.readHeaders();
            int red = csv.getIndex("RED");
            int green = csv.getIndex("GREEN");
            int blue = csv.getIndex("BLUE");
            int value = csv.getIndex("Value");
            int fbfm = csv.getIndex("FBFM13");
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
                    this.legend.add(color, fm.getModelName());
                    logger.log(Level.FINE, "Created Fuel Model: {0}", fm);
                }
            }
            csv.close();

        }
        catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
