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

import com.emxsys.gis.api.*;
import static com.emxsys.gis.api.GisType.ANGLE;
import com.emxsys.gis.api.event.ReticuleCoordinateProvider;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.layer.GisLayerList;
import com.emxsys.gis.api.layer.LayerGroup;
import com.emxsys.gis.api.marker.Marker;
import com.emxsys.gis.api.symbology.Graphic;
import com.emxsys.gis.api.symbology.Symbol;
import com.emxsys.gis.api.viewer.GisViewer;
import com.emxsys.gis.spi.ShadedTerrainProviderFactory;
import com.emxsys.solar.spi.SunlightProviderFactory;
import com.emxsys.time.spi.TimeProviderFactory;
import com.emxsys.visad.Reals;
import com.emxsys.wmt.globe.dnd.BasicDropTargetListener;
import com.emxsys.wmt.globe.layers.*;
import com.emxsys.wmt.globe.render.*;
import com.emxsys.wmt.globe.ribbons.MarkerTools;
import com.emxsys.wmt.globe.ribbons.SceneTools;
import com.emxsys.wmt.globe.ribbons.SymbolTools;
import com.emxsys.wmt.globe.symbology.palette.GraphicDragAndDropHandler;
import com.emxsys.wmt.globe.symbology.palette.PaletteSupport;
import com.emxsys.wmt.globe.symbology.palette.SymbolDragAndDropHandler;
import com.emxsys.wmt.globe.ui.ReticuleStatusLine;
import com.emxsys.wmt.globe.util.Positions;
import com.terramenta.globe.WorldWindManager;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwindx.examples.util.BalloonController;
import gov.nasa.worldwindx.examples.util.HotSpotController;
import java.awt.Component;
import java.awt.dnd.DropTarget;
import static java.lang.Math.toDegrees;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import visad.CommonUnit;
import static visad.CommonUnit.degree;
import static visad.CommonUnit.meter;
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
    private static Globe INSTANCE;

    /**
     * Get the singleton instance.
     * @return the singleton Globe .
     */
    public static Globe getInstance() {
        if (INSTANCE == null) {
            logger.finest("getInstance() looking up Globe.class");
            return Lookup.getDefault().lookup(Globe.class);
        } else {
            return INSTANCE;
        }
    }

    /**
     * Gets the network status.
     * @return True if WorldWind is in offline mode.
     */
    public static boolean isOfflineMode() {
        return WorldWind.isOfflineMode();
    }

    public static boolean isNetworkUnavailable() {
        return WorldWind.getNetworkStatus().isNetworkUnavailable();
    }

    /**
     * Computes the great circle angular distance (degrees).
     * @param distance [meters]
     * @return A Real containing the distance in degrees.
     */
    public static Real computeAngularDistance(Real distance) {
        try {
            WorldWindManager wwm = Globe.getInstance().getWorldWindManager();
            double radius = wwm.getWorldWindow().getModel().getGlobe().getRadius();
            double radians = distance.getValue(meter) / radius;
            return new Real(ANGLE, toDegrees(radians));
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Computes the great circle distance between two coordinates.
     * @param coord1
     * @param coord2
     * @return A Real containing the distance in meters.
     */
    public static Real computeGreatCircleDistance(Coord2D coord1, Coord2D coord2) {
        Position pos1 = Positions.fromCoord2D(coord1);
        Position pos2 = Positions.fromCoord2D(coord2);
        Angle angle = LatLon.greatCircleDistance(pos2, pos1);

        WorldWindManager wwm = Globe.getInstance().getWorldWindManager();
        // Use average earth radius between the two positions.  
        // Should be OK unless route goes over the poles or across equator.
        double r1 = wwm.getWorldWindow().getModel().getGlobe().getRadiusAt(pos1);
        double r2 = wwm.getWorldWindow().getModel().getGlobe().getRadiusAt(pos2);
        double radius = (r1 + r2) / 2.0;
        double distance = angle.radians * radius;
        return Reals.newDistance(distance, meter);
    }

    /**
     * Computes the azimuth angle from coord1 to coord2.
     * @param coord1
     * @param coord2
     * @return
     */
    public static double computeGreatCircleAzimuth(Coord2D coord1, Coord2D coord2) {
        Position pos1 = Positions.fromCoord2D(coord1);
        Position pos2 = Positions.fromCoord2D(coord2);
        Angle angle = LatLon.greatCircleAzimuth(pos1, pos2);
        return angle.degrees;
    }

    /**
     * Computes the coordinate at the given distance along the azimuth.
     * @param origin The start position from which to travel.
     * @param azimuth The direction to travel. [degrees]
     * @param distance The linear distance to travel along the azimuth. [meters]
     * @return The end position.
     */
    public static Coord2D computeGreatCircleCoordinate(Coord2D origin, Real azimuth, Real distance) {
        try {
            Position startPosition = Positions.fromCoord2D(origin);
            Angle greatCircleAzimuth = Angle.fromDegrees(azimuth.getValue(degree));
            Angle pathLength = Angle.fromDegrees(computeAngularDistance(distance).getValue(degree));

            LatLon endPosition = LatLon.greatCircleEndPosition(startPosition, greatCircleAzimuth, pathLength);

            return GeoCoord2D.fromDegrees(endPosition.latitude.degrees, endPosition.longitude.degrees);
        } catch (VisADException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private final InstanceContent content = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(content);
    private final GisLayerList gisLayers = new GisLayerList();
    private final ArrayList<GisLayer> startupLayers = new ArrayList<>();
    private final HashMap<Feature, SurfaceShape> featureShapes = new HashMap<>();
    private WorldWindManager worldWindManager;
    private boolean initialized = false;

    /**
     * Do not call! Used by @ServiceProvider. Use getInstance() or
     * Lookup.getDefault().lookup(Globe.class) instead.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Globe() {
        logger.finest("Globe() constructor called");
        if (INSTANCE != null) {
            throw new IllegalStateException("Do not call constructor. Use getInstance().");
        }
        INSTANCE = this;
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
        if (this.worldWindManager == null) {
            logger.fine("getWorldWindManager() looking up WorldWindManager.class");
            this.worldWindManager = Lookup.getDefault().lookup(WorldWindManager.class);
        }
        return this.worldWindManager;
    }

    /**
     * initializeResources() is invoked by the Installer class on the Event thread after the UI is
     * ready. It initializes the WorldWind environment, the Globe's capabilities, and the underlying
     * WorldWindow LayerList--the merged and sorted collection of WorldWind/Layers files are
     * instantiated here via lookupAll. See this module's layer.xml.
     */
    @Override
    public void initializeResources() {
        logger.fine("initializeResources() started");

        // Initialize WorldWind. We've deferred creating the WorldWind environment until now
        // to prevent an application lock-up while loading modules.
        WorldWindManager wwm = getWorldWindManager();

        // Assemble the components of the globe
        this.content.add(new HotSpotController(wwm.getWorldWindow())); // 1) Sends input events to BrowserBalloons.
        this.content.add(new BalloonController(wwm.getWorldWindow())); // 2) Handles link and navigation events in BrowserBalloons.
        this.content.add(new GlobeCapabilities());
        this.content.add(new GlobeCoordinateProvider());
        this.content.add(new GlobeSectorEditor(this.content));
        this.content.add(ShadedTerrainProviderFactory.getInstance());
        this.content.add(SunlightProviderFactory.getInstance());
        this.content.add(TimeProviderFactory.getInstance());
        wwm.addLookup(this.lookup);

        // Disable painting during the initialization
        ((Component) wwm.getWorldWindow()).setVisible(false);
        initializeLayers();
        initializeDragAndDrop();
        initializeRibbon();
        initializeStatusBar();
        ((Component) wwm.getWorldWindow()).setVisible(true);

        // 
        this.initialized = true;
        postInitializeLayers();
    }

    private void initializeLayers() {
        WorldWindManager wwm = getWorldWindManager();

        // Create table-of-contents layers
        for (BasicLayerGroup layerGroup : BasicLayerGroup.values()) {
            DummyLayer dummyLayer = new DummyLayer(layerGroup);
            wwm.getLayers().add(layerGroup.getIndex(), dummyLayer);
            this.gisLayers.add(new GisLayerProxy(dummyLayer));
        }
        // Add all the map layers
        addAll(BackgroundLayers.getLayers());
        addAll(BaseMapLayers.getLayers());
        addAll(OverlayLayers.getLayers());
        addAll(WidgetLayers.getLayers());

        // Some GIS layers expose capabilities used by the GIS Viewer interface. 
        // Find them and add them to the Globe's lookup.
        Marker.Renderer markerRenderer = this.gisLayers.getLookup().lookup(Marker.Renderer.class);
        if (markerRenderer != null) {
            this.content.add(markerRenderer);
        }
        Symbol.Renderer symbolRenderer = this.gisLayers.getLookup().lookup(Symbol.Renderer.class);
        if (symbolRenderer != null) {
            this.content.add(symbolRenderer);
        }
        Graphic.Renderer graphicRenderer = this.gisLayers.getLookup().lookup(Graphic.Renderer.class);
        if (graphicRenderer != null) {
            this.content.add(graphicRenderer);
        }
        Geometry.Renderer geometryRenderer = this.gisLayers.getLookup().lookup(Geometry.Renderer.class);
        if (geometryRenderer != null) {
            this.content.add(geometryRenderer);
        }
    }

    private void postInitializeLayers() {
        // Post-initialization Step: Process any layers that we cached during startup
        // see addGisLayer()
        if (!startupLayers.isEmpty()) {
            logger.log(Level.FINE, "addGisLayer() processing {0} cached layers.", startupLayers.size());
            addAll(startupLayers);
            startupLayers.clear();
        }
    }

    private void initializeDragAndDrop() {
        // Setup WorldWind DnD support:
        // Add drag and drop functionality to the viewer. Only one listener is allowed. However,
        // various DND handlers can be registered with the sole listener.
        BasicDropTargetListener dropTargetListener = new BasicDropTargetListener(this);
        dropTargetListener.addDndHandler(new SymbolDragAndDropHandler(this));
        dropTargetListener.addDndHandler(new GraphicDragAndDropHandler(this));
        this.getRendererComponent().setDropTarget(new DropTarget(this.getRendererComponent(), dropTargetListener));
        // Now add a PaletteController to the lookup to make a DnD Palette available
        this.content.add(PaletteSupport.createPalette());
    }

    private void initializeRibbon() {
        // Update the UI Ribbon
        MarkerTools.getInstance();  // Creates the Marker Tools contextual task pane (see layer.xml)
        SymbolTools.getInstance();  // Creates the Symbol Tools contextual task pane (see layer.xml)
        SceneTools.getInstance();   // Creates the Scene Tools contextual task pane (see layer.xml)
    }

    private void initializeStatusBar() {
        // Update the status bar
        ReticuleStatusLine.getInstance().initialize();  // Adds the cross-hair coordinates to the status bar        
    }

    /**
     * Get the Globe's lookup which actually a proxy for the WorldWindManager's lookup.
     * @return the WorldWindManager's lookup.
     */
    @Override
    public Lookup getLookup() {
        if (!this.initialized) {
            // Return an empty or sparse lookup
            return this.lookup;
        }
        // Return the merged lookup
        return getWorldWindManager().getLookup();
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
        // Special handling reqd for initialization by other map modules before WorldWind is ready.
        // If this method is invoked before we're initialized, then cache the layer for
        // post-initalization processing.
        if (!this.initialized) {
            // Cache the layer until WorldWindManager and Globe are ready.
            logger.log(Level.FINE, "addGisLayer({0}): caching layer at startup", gisLayer.getName());
            startupLayers.add(gisLayer);
            return;
        }
        addGisLayer(gisLayer, false);   // false = last in layer group
    }

    private void addAll(List<GisLayer> list) {
        list.stream().forEach((gisLayer) -> {
            addGisLayer(gisLayer, false);
        });
    }

    /**
     * This method should be called only after WorldWind is initialized.
     */
    private synchronized void addGisLayer(GisLayer gisLayer, boolean insertFirstInGroup) {

        WorldWindManager wwm = getWorldWindManager();
        try {
            LayerGroup group = gisLayer.getLookup().lookup(LayerGroup.class);
            if (group == null) {
                throw new IllegalStateException("addGisLayer() layer " + gisLayer.getName() + " must have a LayerGroup in its lookup.");
            }
            // Get the WorldWind Layer implementation from the GIS layer's lookup or class hierarchy
            Layer layerImpl = gisLayer.getLookup().lookup(Layer.class);
            if (layerImpl == null) {
                if (gisLayer instanceof Layer) {
                    layerImpl = (Layer) gisLayer;
                } else {
                    throw new IllegalStateException("addGisLayer() layer " + gisLayer.getName() + "must have a Layer implemenation in its lookup.");
                }
            }
            // Remove preloaded layers (loaded by SessionState) so we can reload them in the proper position.
            for (Layer l : wwm.getLayers()) {
                // Compare the layer implementation's name with the preloaded layer's name
                if (l.getName().equals(layerImpl.getName())) {
                    // Already loaded, so dispose of it...
                    wwm.getLayers().remove(l);
                    // ... but set the layer impl's enabled setting based on the SessionState's setting
                    layerImpl.setEnabled(l.isEnabled());
                    break;
                }
            }
            // Now find the TOC layer that marks the position where this layer should be inserted in WorldWind
            int groupLayerPosition = -1;
            boolean foundGroupLayer = false;
            for (Layer l : wwm.getLayers()) {
                if (l instanceof DummyLayer) {
                    if (group.equals(((DummyLayer) l).getLayerGroup())) {
                        if (insertFirstInGroup) {
                            // Found the layer that marks the beginning of the group
                            groupLayerPosition = wwm.getLayers().indexOf(l);
                            break;
                        }
                        foundGroupLayer = true;
                        // loop once more to find the next group layer entry
                        continue;
                    }
                    if (foundGroupLayer) {
                        // Found the layer that marks the beginning of the next group
                        groupLayerPosition = wwm.getLayers().indexOf(l);
                        break;
                    }
                }
            }
            // Update the WorldWind model
            if (groupLayerPosition > -1) {
                logger.log(Level.CONFIG, "Added layer {0} to group {1} at {2}", new Object[]{
                    gisLayer.getName(), group.getName(), groupLayerPosition});
                wwm.getLayers().add(insertFirstInGroup ? groupLayerPosition + 1 : groupLayerPosition, layerImpl);

            } else {
                logger.log(Level.INFO, "addGisLayer() : layer group not found; adding layer {0} at an arbitrary position.", gisLayer.getName());
                wwm.getLayers().add(layerImpl);
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
            WorldWindManager wwm = getWorldWindManager();
            wwm.getLayers().remove(layerImpl);
        }
        // Syncronize our internal list 
        this.gisLayers.remove(gisLayer);
    }

    public void addFeature(Feature feature, RenderableGisLayer layer) {
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null.");
        }
        Geometry geometry = feature.getGeometry();
        if (geometry == null) {
            throw new IllegalArgumentException("Feature geometry cannot be null.");
        }
        SurfaceShape shape = null;
        switch (feature.getFeatureClass()) {
            case POLYGON:
                if (geometry instanceof GeoPolygon) {
                    shape = new GlobePolygon((GeoPolygon) feature.getGeometry());
                } else if (geometry instanceof GeoSector) {
                    shape = new GlobeSector((GeoSector) feature.getGeometry());
                } else {
                    throw new IllegalArgumentException("Feature geometry must be a GeoPolygon.");
                }
                break;
            case LINE:
                if (geometry instanceof GeoLineString) {
                    shape = new GlobeLineString((GeoLineString) feature.getGeometry());
                } else {
                    throw new IllegalArgumentException("Feature geometry must be a GeoLineString.");
                }
                break;
            case POINT:
                if (geometry instanceof GeoLineString) {
                    shape = new GlobeLineString((GeoLineString) feature.getGeometry());
                } else {
                    throw new IllegalArgumentException("Feature geometry must be a GeoLineString.");
                }
                break;
            default:
                logger.log(Level.WARNING, "{0} not supported.", feature.getFeatureClass());
                return;
        }
        featureShapes.put(feature, shape);
        layer.addRenderable(shape);
    }

    public void removeFeature(Feature feature, RenderableGisLayer layer) {
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null.");
        }
        SurfaceShape shape = featureShapes.remove(feature);
        if (shape != null) {
            layer.removeRenderable(shape);
        }

    }

    @Override
    public void centerOn(Coord2D latlon) {
        getWorldWindManager().gotoPosition(Positions.fromCoord2D(latlon), true); // true = animate
    }

    /**
     * Gets the reticule coordinates at the center of the screen.
     * @return The coordinates from the ReticuleCoordinateProvider.
     */
    @Override
    public Coord3D getLocationAtCenter() {
        return getLookup().lookup(ReticuleCoordinateProvider.class
        ).getReticuleCoordinate();
    }

    @Override
    public Coord3D
            getLocationAtScreenPoint(double x, double y) {
        throw new UnsupportedOperationException("getLocationAtScreenPoint");
    }

    /**
     * Gets the name of the TopComponent hosting the Globe.
     * @return TopComponent.getName().
     */
    @Override
    public String getName() {
        TopComponent tc = getGlobeTopComponent();
        return tc != null ? tc.getName() : "";

    }

    /**
     * Gets a reference to the WorldWindManager.getWorldWindow() component.
     * @return A WorldWindow instance.
     */
    @Override
    public Component getRendererComponent() {
        return (Component) getWorldWindManager().getWorldWindow();
    }

    /**
     * Redraws the globe.
     */
    @Override
    public void refreshView() {
        getWorldWindManager().getWorldWindow().redraw();
    }

    /**
     * Returns a GeoSector that contains a circle defined by a point and radius.
     * @param point The center of the circle.
     * @param radius The circle's radius.
     * @return A new GeoSector who's sides are 2X radius.
     */
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
        WorldWindManager wwm = getWorldWindManager();
        Position position = Positions.fromCoord2D(point);
        gov.nasa.worldwind.globes.Globe globe = wwm.getWorldWindow().getModel().getGlobe();
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
