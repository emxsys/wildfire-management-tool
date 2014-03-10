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
package com.emxsys.wmt.globe;

import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.api.layer.GisLayerList;
import com.emxsys.wmt.gis.api.layer.GisLayer;
import com.emxsys.wmt.gis.api.layer.LayerGroup;
import com.emxsys.wmt.gis.api.viewer.GisViewer;
import com.terramenta.globe.WorldWindManager;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import com.emxsys.wmt.globe.layers.BackgroundLayers;
import com.emxsys.wmt.globe.layers.BaseMapLayers;
import com.emxsys.wmt.globe.layers.DummyLayer;
import com.emxsys.wmt.globe.layers.GisLayerAdaptor;
import com.emxsys.wmt.globe.layers.OverlayLayers;
import com.emxsys.wmt.globe.layers.WidgetLayers;
import com.emxsys.wmt.globe.ui.ReticuleStatusLine;
import com.emxsys.wmt.globe.util.Positions;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import java.util.List;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import visad.CommonUnit;
import visad.Real;
import visad.VisADException;

/**
 * The Globe is a GisViewer instance that provides access to the WorldWind virtual globe. The
 * instance can be obtained through the getInstance() method or via by querying the global lookup
 * for GisViewer.class.
 * <p>
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages({
    "# {0} - top component id",
    "ERR_GlobeWindowNotFound=The Globe window could not be using the \"{0}\" ID."
})
@ServiceProvider(service = GisViewer.class)
public class Globe implements GisViewer {

    public static final String GLOBE_TOP_COMPONENT_ID = "GlobeTopComponent";
    private static final Logger logger = Logger.getLogger(Globe.class.getName());
    private final WorldWindManager wwm = Lookup.getDefault().lookup(WorldWindManager.class);
    private final InstanceContent content = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(content);
    private final GisLayerList gisLayers = new GisLayerList();
    private boolean initialized = false;
    private static Globe INSTANCE;

    /**
     * Do not call! Used by @ServiceProvider.
     * Use getInstance() or Lookup.getDefault().lookup(Globe.class) instead.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Globe() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Do not call constructor. Use getInstance().");
        }
        INSTANCE = this;
    }

    /**
     * Get the singleton instance.
     * @return the singleton Globe .
     */
    public static Globe getInstance() {
        if (INSTANCE == null) {
            return Lookup.getDefault().lookup(Globe.class);
        } else {
            return INSTANCE;
        }
    }

    /**
     * Get the Globe window.
     * @return the Globe window; may be null.
     */
    public TopComponent getGlobeTopComponent() {
        TopComponent tc = WindowManager.getDefault().findTopComponent(GLOBE_TOP_COMPONENT_ID);
        if (tc == null) {
            logger.log(Level.WARNING, Bundle.ERR_GlobeWindowNotFound(GLOBE_TOP_COMPONENT_ID));
        }
        return tc;
    }

    /**
     * Convenience method returns the registered WorldWindManager.
     * @return the WorldWindManager found on the global lookup.
     */
    public WorldWindManager getWorldWindManager() {
        return this.wwm;
    }

    /**
     * Initializes the Globe's capabilities, and the underlying WorldWindow LayerList via the merged
     * and sorted collection of WorldWind/Layers files are instantiated here via lookupAll. See this
     * module's layer.xml.
     */
    @Override
    public void initializeResources() {
        this.content.add(new GlobeCapabilities(this.wwm));
        this.content.add(new GlobeCoordinateProvider());
        this.wwm.addLookup(this.lookup);

        // Disable painting during the initialization
        this.wwm.getWorldWindow().setVisible(false);

        // Insert group layers
        for (BasicLayerGroup layerGroup : BasicLayerGroup.values()) {
            DummyLayer dummyLayer = new DummyLayer(layerGroup);
            this.wwm.getLayers().add(layerGroup.getIndex(), dummyLayer);
            this.gisLayers.add(new GisLayerAdaptor(dummyLayer));
        }
        addAll(BackgroundLayers.getLayers());
        addAll(BaseMapLayers.getLayers());
        addAll(OverlayLayers.getLayers());
        addAll(WidgetLayers.getLayers());


        // Update the UI
        ReticuleStatusLine.getInstance().initialize();
        this.wwm.getWorldWindow().setVisible(true);
        
        this.initialized = true;
    }

    /**
     * Get the Globe's lookup which actually a proxy for the WorldWindManager's lookup.
     * @return the WorldWindManager's lookup.
     */
    @Override
    public Lookup getLookup() {
        return this.wwm.getLookup();
    }

    @Override
    public void setVisible(boolean show) {
        TopComponent tc = getGlobeTopComponent();
        if (tc == null) {
            return;
        }
        tc.setVisible(show);
    }

    /**
     * Get the visibility of the Globe Window.
     * @return visibility of the GlobeTopComponent
     */
    @Override
    public boolean isVisible() {
        TopComponent tc = getGlobeTopComponent();
        return tc != null ? tc.isVisible() : false;
    }

    @Override
    public GisLayerList getGisLayerList() {
        return this.gisLayers;
    }

    @Override
    public void addGisLayer(GisLayer gisLayer) {
        addGisLayer(gisLayer, false);   // false = last in layer group
    }

    private void addAll(List<GisLayer> list) {
        for (GisLayer gisLayer : list) {
            addGisLayer(gisLayer, false);   // false = position last in layer group
        }
    }

    public synchronized void addGisLayer(GisLayer gisLayer, boolean insertFirstInGroup) {
        try {
            LayerGroup group = gisLayer.getLookup().lookup(LayerGroup.class);
            if (group == null) {
                throw new IllegalStateException("addGisLayer() layer " + gisLayer.getName() + " must have a LayerRole in its lookup.");
            }
            Layer layerImpl = gisLayer.getLookup().lookup(Layer.class);
            if (layerImpl == null) {
                throw new IllegalStateException("addGisLayer() layer " + gisLayer.getName() + "must have a Layer implemenation in its lookup.");
            }
            // Don't re-add the layer to WorldWind, just add it to our internal list
            for (Layer l : this.wwm.getLayers()) {
                if (l.equals(layerImpl)) {
                    this.gisLayers.add(gisLayer);
                    return;
                }
            }
            // Find the layer that marks the position where this layer should be inserted in WorldWind
            int groupLayerPosition = -1;
            boolean foundGroupLayer = false;
            for (Layer l : this.wwm.getLayers()) {
                if (l instanceof DummyLayer) {
                    if (group.equals(((DummyLayer) l).getLayerGroup())) {
                        if (insertFirstInGroup) {
                            // Found the layer that marks the beginning of the group
                            groupLayerPosition = this.wwm.getLayers().indexOf(l);
                            break;
                        }
                        foundGroupLayer = true;
                        // loop once more to find the next group layer entry
                        continue;
                    }
                    if (foundGroupLayer) {
                        // Found the layer that marks the beginning of the next group
                        groupLayerPosition = this.wwm.getLayers().indexOf(l);
                        break;
                    }
                }
            }
            // Update the WorldWind model
            if (groupLayerPosition > -1) {
                logger.log(Level.INFO, "addGisLayer() : added {0} to group {1} at {2}", new Object[]{
                    gisLayer.getName(), group.getName(), groupLayerPosition
                });
                this.wwm.getLayers().add(insertFirstInGroup ? groupLayerPosition + 1 : groupLayerPosition, layerImpl);
            } else {
                logger.log(Level.WARNING, "addGisLayer() : layer group not found; adding {0} at an arbitrary position.", gisLayer.getName());
                this.wwm.getLayers().add(layerImpl);
            }
            // Sync our internal list
            this.gisLayers.add(gisLayer);
        } catch (IllegalStateException e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    public void removeGisLayer(GisLayer gisLayer) {
        // Update the WW Model
        Layer layerImpl = gisLayer.getLookup().lookup(Layer.class);
        if (layerImpl != null) {
            this.wwm.getLayers().remove(layerImpl);
        }
        // Syncronize our internal list 
        this.gisLayers.remove(gisLayer);
    }

    @Override
    public void centerOn(Coord2D latlon) {
        throw new UnsupportedOperationException("centerOn");
    }

    @Override
    public Coord3D getLocationAtCenter() {
        throw new UnsupportedOperationException("getLocationAtCenter");
    }

    @Override
    public Coord3D getLocationAtScreenPoint(double x, double y) {
        throw new UnsupportedOperationException("getLocationAtScreenPoint");
    }

    @Override
    public String getName() {
        TopComponent tc = getGlobeTopComponent();
        return tc != null ? tc.getName() : "";

    }

    @Override
    public Component getRendererComponent() {
        return this.wwm.getWorldWindow();
    }

    @Override
    public void refreshView() {
        this.wwm.getWorldWindow().redraw();
    }

    @Override
    public GeoSector computeSector(Coord2D point, Real radius) {
        double meters;
        try {
            // Convert from an arbitrary unit to meters
            meters = radius.getValue(CommonUnit.meter);
        } catch (VisADException ex) {
            logger.log(Level.SEVERE, "computeSector: Cannot convert radius value to meters", ex);
            // Return a GIS sector with "missing" values
            return new GeoSector();
        }

        // Compute N/S and E/W distances in radians
        Position position = Positions.fromCoord3D(point);
        gov.nasa.worldwind.globes.Globe globe = this.wwm.getWorldWindow().getModel().getGlobe();
        double deltaLatRadians = meters / globe.getEquatorialRadius();
        double deltaLonRadians = deltaLatRadians / Math.cos(position.getLatitude().radians);

        // Create a sector in WW using radians...
        Sector sector = new Sector(
                position.getLatitude().subtractRadians(deltaLatRadians),
                position.getLatitude().addRadians(deltaLatRadians),
                position.getLongitude().subtractRadians(deltaLonRadians),
                position.getLongitude().addRadians(deltaLonRadians));

        //... and convert it to VisAD based sector.
        Coord2D southwest = GeoCoord2D.fromDegrees(sector.getMinLatitude().degrees, sector.getMinLongitude().degrees);
        Coord2D northeast = GeoCoord2D.fromDegrees(sector.getMaxLatitude().degrees, sector.getMaxLongitude().degrees);
        return new GeoSector(southwest, northeast);
    }
}
