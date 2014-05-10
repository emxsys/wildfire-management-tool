/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.analytics;

import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.layers.GisLayerProxy;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwindx.examples.analytics.AnalyticSurface;
import gov.nasa.worldwindx.examples.analytics.AnalyticSurfaceAttributes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.openide.util.Exceptions;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.LinearLatLonSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.VisADException;


/**
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public class TemporalSpatialAnalytics
{
    private AnalyticSurface surface;
    private FunctionType temporalFunction;
    private FunctionType spatialFunction;
    private FieldImpl field;
    private int numTimeValues;
    private int componentIndex;
    private double minValue;
    private double maxValue;
    protected static final double HUE_BLUE = 240d / 360d;
    protected static final double HUE_RED = 0d / 360d;
    protected RenderableLayer analyticSurfaceLayer;
    private static final Logger logger = Logger.getLogger(TemporalSpatialAnalytics.class.getName());


    /**
     *
     * @param data
     * @param type
     */
    public TemporalSpatialAnalytics(DataImpl data, MathType type)
    {
        if (data == null)
        {
            throw new IllegalArgumentException("data cannot be null!");
        }
        logger.log(Level.INFO, "Intializing TemporalSpatialAnalytics for : {0}", data.getType().prettyString());

        try
        {

            // Validate temporal requirements
            this.temporalFunction = (FunctionType) data.getType();
            if (this.temporalFunction == null
                || !(this.temporalFunction.getDomain().getComponent(0).equals(RealType.Time)))
            {
                throw new IllegalArgumentException("data domain type must be RealType.Time : "
                    + data.getType().prettyString());
            }

            // Validate spatial requirements
            this.spatialFunction = (FunctionType) this.temporalFunction.getRange();
            if (this.spatialFunction == null
                || !(this.spatialFunction.getDomain().equals(RealTupleType.LatitudeLongitudeTuple)
                || this.spatialFunction.getDomain().equals(GeoCoord2D.DEFAULT_TUPLE_TYPE)))
            {
                throw new IllegalArgumentException("data range's domain type must be [RealType.Latitude, RealType.Longitude] : "
                    + data.getType().prettyString());
            }
            
            // Validate spatial range data contains given type
            this.componentIndex = this.spatialFunction.getFlatRange().getIndex(type);
            if (componentIndex < 0)
            {
                throw new IllegalArgumentException("data range does not have supplied type : "
                    + type.prettyString() + " : " + data.getType().prettyString());
            }

            this.field = (FieldImpl) data;
            Set timeSet = this.field.getDomainSet();
            this.numTimeValues = timeSet.getLength();
            
            // Intialize the surface
            initialize();

        }
        catch (VisADException ex)
        {
            throw new IllegalStateException(ex.toString());
        }
    }


    /**
     *
     * @param field
     * @param type
     */
    private void initialize()
    {
        // Create Layer
        this.analyticSurfaceLayer = new RenderableLayer();
        this.analyticSurfaceLayer.setPickEnabled(false);
        this.analyticSurfaceLayer.setName("Analytics");
        Globe.getInstance().addGisLayer(new GisLayerProxy(analyticSurfaceLayer, "Analytics", BasicLayerGroup.Analytic));

        // Create Surface
        try
        {
            // Get the data dimensions to size the surface
            FlatField spatialField = (FlatField) field.getSample(0);
            LinearLatLonSet spatialSet = (LinearLatLonSet) spatialField.getDomainSet();
            
            int rows = spatialSet.getLength(0); // latitudes
            int cols = spatialSet.getLength(1); // longitudes
            
            float[] low = spatialSet.getLow();
            float[] hi = spatialSet.getHi();
            
            float minLat = low[0]; 
            float minLon = low[1];
            float maxLat = hi[0];
            float maxLon = hi[1];

            // Initialize the a surface to the data dimensions
            this.surface = new AnalyticSurface();
            this.surface.setSector(Sector.fromDegrees(minLat, maxLat, minLon, maxLon));
            this.surface.setDimensions(cols, rows);
            this.surface.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            this.surface.setClientLayer(this.analyticSurfaceLayer);
            this.analyticSurfaceLayer.addRenderable(this.surface);

            AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
            attr.setDrawShadow(false);
            attr.setInteriorOpacity(0.6);
            attr.setOutlineWidth(3);
            this.surface.setSurfaceAttributes(attr);

        }
        catch (VisADException | RemoteException ex)
        {
            String msg = String.format("initialize() failed! %1s", ex);
            logger.severe(msg);
            throw new RuntimeException(msg, ex);
        }

    }


    /**
     *
     * @param intervalMs
     */
    public final void animate(final int intervalMs)
    {
        Timer timer = new Timer(intervalMs, new ActionListener()
        {
            protected int timeIndex = 0;


            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    if (timeIndex == numTimeValues)
                    {
                        timeIndex = 0;
                    }
                    FlatField spatialField = (FlatField) field.getSample(timeIndex++);
                    
                    surface.setValues(createColorGradientGridValues(spatialField, componentIndex, 0, 5, HUE_BLUE, HUE_RED));

                    if (surface.getClientLayer() != null)
                    {
                        surface.getClientLayer().firePropertyChange(AVKey.LAYER, null, surface.getClientLayer());
                    }
                }
                catch (VisADException | RemoteException ex)
                {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        timer.start();
    }


    /**
     * 
     * @param spatialField
     * @param componentIndex
     * @param minValue
     * @param maxValue
     * @param minHue
     * @param maxHue
     * @return 
     */
    protected static Iterable<? extends AnalyticSurface.GridPointAttributes> createColorGradientGridValues(
        FlatField spatialField, int componentIndex, double minValue, double maxValue,
        double minHue, double maxHue)
    {
        ArrayList<AnalyticSurface.GridPointAttributes> attributesList = new ArrayList<>();

        try
        {
            LinearLatLonSet spatialSet = (LinearLatLonSet) spatialField.getDomainSet();
            int numRows = spatialSet.getLength(0); // latitudes
            int numCols = spatialSet.getLength(1); // longitudes
            
            float[][] spatialSamples = spatialSet.getSamples(false); // false: get actual values--don't copy
            double[][] values = spatialField.getValues(false);
            
            // The spatial field is ordered as follows with six Lat samples, 5 Lon samples:
            //          Lon (second) component
            //  Lat     5  11  17  23  29
            //          4  10  16  22  28
            // (first)  3   9  15  21  27
            //          2   8  14  20  26
            //component 1   7  13  19  25
            //          0   6  12  18  24
            // 
            // I.e., the first grid dimension changes the fastest
            
            // Grid points are assigned attributes starting at the upper left hand corner, 
            // and proceeding in row-first order across the grid.
            for (int row = numRows-1; row >= 0; row--)
            {
                for (int col = 0; col < numCols; col++)
                {
                    int n = (col * numRows) + row;
                    
                    float lat = spatialSamples[0][n];
                    float lon = spatialSamples[1][n];
                    double value = values[componentIndex][n];
                    
                    //String msg = String.format("row: %1$d, col:%2$d, lat: %3$f, lon:%4$f, value: %5$f",row,col,lat,lon,value);
                    //System.out.println(msg);
                    
                    attributesList.add(AnalyticSurface.createColorGradientAttributes(
                        value, minValue, maxValue, minHue, maxHue));
                    
                }
            }
        }
        catch (VisADException  ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return attributesList;
    }
}
