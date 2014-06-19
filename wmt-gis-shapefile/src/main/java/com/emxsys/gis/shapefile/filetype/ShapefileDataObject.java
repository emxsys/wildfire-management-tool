/*
 * Copyright (c) 2011-2014, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.gis.shapefile.filetype;

import com.emxsys.gis.api.capabilities.AddableGisLayer;
import com.emxsys.gis.api.capabilities.RemovableGisLayer;
import com.emxsys.gis.api.layer.BasicLayerCategory;
import com.emxsys.gis.api.layer.BasicLayerGroup;
import com.emxsys.gis.api.layer.BasicLayerType;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.shapefile.ShapefileDataSource;
import com.emxsys.gis.shapefile.ShapefileLayerFactory;
import com.emxsys.gis.api.viewer.GisViewer;
import com.emxsys.util.TimeUtil;
import com.emxsys.wmt.globe.layers.GisLayerProxy;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.layers.Layer;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;

/**
 * A ShapefileDataObject represents an ESRI Shapefile. A Shapefile's primary file is a *.shp file.
 * This class constructs a {@link ShapefileDataSource} from the primary file and stores the data
 * source in the {@link DataObject}'s {@link Lookup}. The {@link ShapefileDataSource} can be used to
 * retrieve a result set from the Shapefile contents.
 *
 * @see ShapefileDataSource
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapefileDataObject extends MultiDataObject {

    private final ShapefileDataSource dataSource;
    protected Lookup lookup;
    protected InstanceContent content = new InstanceContent();
    private final static Logger LOG = Logger.getLogger(ShapefileDataObject.class.getName());

    @SuppressWarnings("LeakingThisInConstructor")
    public ShapefileDataObject(FileObject pf, MultiFileLoader mfLoader) throws
            DataObjectExistsException, IOException {
        super(pf, mfLoader);
        LOG.log(Level.INFO, "Constructing DataObject from {0}", pf.getPath());

        // Create a data source that can return a result set.
        this.dataSource = new ShapefileDataSource(pf);

        this.content.add(this);
        this.content.add(dataSource);
        this.content.add(new AddGisLayerCapability());
        this.lookup = new AbstractLookup(content);
    }

    @Override
    protected Node createNodeDelegate() {
        ShapefileDataNode node = new ShapefileDataNode(this, lookup);
        //node.setDisplayName(this.getName());
        node.setIconBaseWithExtension("com/emxsys/gis/shapefile/filetype/layer-vector.png");
        return node;
    }

    /**
     * DataShadow object nodes call this method (versus getLookup()) so simply alias it to getLookup
     * to keep the two in unison.
     *
     * @param <T>
     * @param type
     * @return the item returned by this data objects' lookup.
     */
    @Override
    public <T extends Cookie> T getCookie(Class<T> type) {
//        return super.getCookie(type);
        return super.getLookup().lookup(type);
    }

    @Override
    public Lookup getLookup() {
        //return super.getLookup();
        return getCookieSet().getLookup();
    }

    /**
     * This capability class, if added to the DataObject's lookup, provides the ability to add the
     * shapefile to the gis viewer as a layer.
     *
     * This capability is automatically invoked when the DataObject is opened as a result of the
     * AddMapLayerAction being the default action associated with the Loader. AddMapLayerAction is a
     * context sensitive action that acts on the AddableGisLayer interface. See
     * Loaders/application/x-shapefile/Actions in the XML layer. The first action is the default
     * action.
     */
    protected class AddGisLayerCapability implements AddableGisLayer, Node.Cookie {

        @Override
        public void addGisLayerToViewer() {
            // Get the gis viewer
            GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
            final String LAYER_NAME = ShapefileDataObject.this.getName();

            // Prevent this layer from being added multiple times to the viewer
            Collection<? extends GisLayer> layers = viewer.getLookup().lookupAll(GisLayer.class);
            for (GisLayer layer : layers) {
                if (layer.getName().equals(LAYER_NAME)) {
                    return;
                }
            }
            content.remove(this);

            // Now, open the shapefile and create the map layer in a background thread
            Thread t = new Thread(() -> {
                LOG.log(Level.INFO, "Loading shapefile {0} ...", dataSource.getName());
                long startTimeMillis = System.currentTimeMillis();
                ShapefileLayerFactory shapefileLoader = new ShapefileLayerFactory();
                Shapefile shapefile = dataSource.getLookup().lookup(Shapefile.class);
                LOG.log(Level.INFO, "  Type: {0}", shapefile.getShapeType());
                LOG.log(Level.INFO, "  Num records: {0}", shapefile.getNumberOfRecords());
                List<Layer> layers1 = shapefileLoader.createLayersFromShapefile(shapefile);
                for (int i = 0; i < layers1.size(); i++) {
                    Layer layer = layers1.get(i);
                    if (layers1.size() > 1) {
                        String suffix = "(" + (i + 1) + ")";
                        layer.setName(LAYER_NAME + suffix);
                    }
                    else {
                        layer.setName(LAYER_NAME);
                    }
                    GisLayerProxy layerAdaptor = new GisLayerProxy(layer, BasicLayerType.Vector, BasicLayerGroup.Overlay, BasicLayerCategory.Unknown);
                    GisViewer viewer1 = Lookup.getDefault().lookup(GisViewer.class);
                    viewer1.addGisLayer(layerAdaptor);
                    content.add(new RemoveGisLayerCapability(layerAdaptor));
                }
                LOG.log(Level.INFO, "  Load time: {0}", TimeUtil.msToSecs(System.currentTimeMillis() - startTimeMillis));
            }, "Shapefile GisLayer Loader");
            t.start();
        }
    }

    /**
     * This capability class, if added to the DataObject's lookup, provides the ability to remove
     * the layer representing this shapefile from the GIS viewer.
     */
    protected class RemoveGisLayerCapability implements RemovableGisLayer, Node.Cookie {

        private final GisLayer gisLayer;

        public RemoveGisLayerCapability(GisLayer gisLayer) {
            this.gisLayer = gisLayer;
        }

        @Override
        public void removeGisLayerFromViewer() {
            GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
            viewer.removeGisLayer(gisLayer);

            // Update our lookup
            content.remove(this);
            content.add(new AddGisLayerCapability());
        }
    }

//    /**
//     * Adapts a WorldWind Layer to a GIS GisLayer so that it can be added to a GIS GisViewer.
//     */
//    protected class WorldWindMapLayerAdaptor implements GisLayer
//    {
//
//        private final Layer layer;
//        private final Lookup lookup;
//
//
//        public WorldWindMapLayerAdaptor(Layer layer)
//        {
//            this.layer = layer;
//            this.lookup = Lookups.singleton(layer);
//        }
//
//
//        @Override
//        public Lookup getLookup()
//        {
//            return this.lookup;
//        }
//
//
//        @Override
//        public String getName()
//        {
//            return layer.getName();
//        }
//
//
//        @Override
//        public boolean isEnabled()
//        {
//            return layer.isEnabled();
//        }
//
//
//        @Override
//        public void setEnabled(boolean enabled)
//        {
//            layer.setEnabled(enabled);
//            layer.firePropertyChange(AVKey.LAYER, null, layer);
//            GisSupport.refreshViewersContaining(this);
//        }
//
//
//        @Override
//        public void addPropertyChangeListener(PropertyChangeListener listener)
//        {
//            this.layer.addPropertyChangeListener(listener);
//        }
//
//
//        @Override
//        public void removePropertyChangeListener(PropertyChangeListener listener)
//        {
//            this.layer.removePropertyChangeListener(listener);
//        }
//    }
}
