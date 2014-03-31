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
package com.emxsys.wmt.gis.api.marker;

import com.emxsys.wmt.gis.api.EntityCatalog;
import com.emxsys.wmt.gis.api.marker.Marker.Renderer;
import com.emxsys.wmt.gis.api.viewer.GisViewer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;

@Messages(
        {
            "err.marker.null=Marker argument cannot be null.",
            "err.markers.null=Markers collection cannot be null.",
            "# {0} - marker type",
            "err.marker.incompatible=The marker type is incompatable [{0}]. The marker was not added.",
            "# {0} - marker id",
            "err.marker.already.exists=The marker ID ({0}) already exists.",
            "# {0} - marker id",
            "err.marker.renderer.not.found=A renderer for the marker was not found. The marker {0} will not be displayed.",
            "# {0} - marker id",
            "info.adding.marker.to.renderer=Adding the {0} marker to a renderer.",
            "# {0} - marker id",
            "info.removing.marker.from.renderer=Removing the {0} marker from the renderer."
        })
/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: MarkerCatalog.java 441 2012-12-12 13:11:18Z bdschubert $
 */
public class MarkerCatalog extends EntityCatalog<Marker> {

    private FileObject folder;
    private Lookup.Result<Renderer> rendererResults;
    private Marker.Renderer markerRenderer;
    private final ArrayList<Marker> pendingAdds = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(MarkerCatalog.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }
    /**
     * Constructs a EntityCatalog who's contents are backed by a folder.
     *
     * @param folder the folder where Markers are saved
     */
    public MarkerCatalog(FileObject folder) {
        setFolder(folder);
    }

    /**
     * @return the folder who's contents this catalog represents.
     */
    public FileObject getFolder() {
        return folder;
    }

    /**
     * Sets the folder that this catalog represents.
     *
     * @param folder the folder where Markers are saved.
     */
    private void setFolder(FileObject folder) {
        if (folder != null && !folder.isFolder()) {
            throw new IllegalArgumentException("setFolder: " + folder.getName() + " is not a folder.");
        }
        this.folder = folder;
    }

    @Override
    protected void doAddItem(Marker item) {
        super.doAddItem(item);
        addMarkerToRenderer(item);
    }

    protected void addMarkerToRenderer(Marker marker) {
        // Get the renderer (map layer) associated with Markers
        Marker.Renderer renderer = getMarkerRenderer();
        if (renderer != null) {
            logger.fine(Bundle.info_adding_marker_to_renderer(marker.getName()));
            renderer.addMarker(marker);
        }
        else {
            logger.warning(Bundle.err_marker_renderer_not_found(marker.getName()));
            pendingAdds.add(marker);
        }
    }

    @Override
    protected void doRemoveItem(Marker item) {
        super.doRemoveItem(item);

        // Remove from the renderer
        Marker.Renderer renderer = getMarkerRenderer();
        if (renderer != null) {
            logger.fine(Bundle.info_removing_marker_from_renderer(item.getName()));
            renderer.removeMarker(item);
        } 
    }

    /**
     * Dispose of this catalog. Release listeners, release renderables.
     */
    @Override
    public void dispose() {
        Renderer markerRenderer = getMarkerRenderer();
        if (markerRenderer != null) {
            // The renderer may force the removal of the item from the catalog, so copy
            // the markers to array that won't be modified by a subsequent nested call 
            // to removeItem invoked the the Renderer
            Marker[] array = this.getItems().toArray(new Marker[0]);
            for (Marker marker : array) {
                markerRenderer.removeMarker(marker);
            }
        }
        pendingAdds.clear();
        super.dispose();
    }

    private Marker.Renderer getMarkerRenderer() {
        if (markerRenderer == null) {
            GisViewer viewer = Lookup.getDefault().lookup(GisViewer.class);
            if (viewer != null && rendererResults == null) {
                // Create lookup listener to watch for the arrival or disposal of a Marker.Renderer
                this.rendererResults = viewer.getLookup().lookupResult(Marker.Renderer.class);
                this.rendererResults.addLookupListener((LookupEvent ev) -> {
                    Collection<? extends Renderer> allInstances = rendererResults.allInstances();
                    if (allInstances.isEmpty()) {
                        markerRenderer = null;
                    }
                    else {
                        // Update the renderer and process any pending Markers
                        markerRenderer = allInstances.iterator().next();
                        pendingAdds.stream().forEach((marker) -> {
                            addMarkerToRenderer(marker);
                        });
                        pendingAdds.clear();
                    }
                });
                if (!rendererResults.allInstances().isEmpty()) {
                    markerRenderer = rendererResults.allInstances().iterator().next();
                }
            }
        }
        return markerRenderer;
    }
}
