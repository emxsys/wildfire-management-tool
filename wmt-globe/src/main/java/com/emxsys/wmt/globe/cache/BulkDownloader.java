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

import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.layer.GisLayerList;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.layers.GisLayerProxy;
import com.emxsys.wmt.globe.layers.WorldWindElevationModelAdaptor;
import gov.nasa.worldwind.event.BulkRetrievalEvent;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.retrieve.BulkRetrievable;
import gov.nasa.worldwind.retrieve.BulkRetrievalThread;
import gov.nasa.worldwind.retrieve.Progress;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class BulkDownloader implements PropertyChangeListener {

    private static final ArrayList<BulkDownloadMonitor> activeMonitors = new ArrayList<>();

    public BulkDownloader() {
    }

    /**
     * Discovers the maps and elevation models that are capable of bulk downloads.
     * @return a list of downloadable map layers.
     */
    public List<GisLayer> getBulkDownloadableLayers() {
        List<GisLayer> downloadableLayers = new ArrayList<>();

        // Discover the BulkRetrievable layers
        GisLayerList gisLayerList = Globe.getInstance().getGisLayerList();
        for (GisLayer gisLayer : gisLayerList.getLayers()) {
            Layer layer = null;
            if (gisLayer instanceof GisLayerProxy) {
                layer = ((GisLayerProxy) gisLayer).getLookup().lookup(Layer.class);
            } else if (gisLayer instanceof Layer) {
                layer = (Layer) gisLayer;
            }

            if (layer != null && layer instanceof BulkRetrievable) {
                downloadableLayers.add(gisLayer);
            }
        }
        // Elevation models
        CompoundElevationModel cem = (CompoundElevationModel) Globe.getInstance().getWorldWindManager().getWorldWindow().getModel().getGlobe().getElevationModel();
        for (ElevationModel elevationModel : cem.getElevationModels()) {
            if (elevationModel instanceof BulkRetrievable) {
                downloadableLayers.add(new WorldWindElevationModelAdaptor(elevationModel));
            }
        }
        return downloadableLayers;
    }

    public void startDownload(final GisLayer downloadableLayer, GeoSector sector) {
        BulkRetrievable retrievable = downloadableLayer.getLookup().lookup(BulkRetrievable.class);
        if (retrievable == null) {
            return;
        }
        if (sector.isMissing()) {
            return;
        }
        // Convert from a GeoSector to a WW Sector
        Sector currentSector = new Sector(
                Angle.fromDegreesLatitude(sector.getSouthwest().getLatitude().getValue()),
                Angle.fromDegreesLatitude(sector.getNortheast().getLatitude().getValue()),
                Angle.fromDegreesLongitude(sector.getSouthwest().getLongitude().getValue()),
                Angle.fromDegreesLongitude(sector.getNortheast().getLongitude().getValue()));

        // Will download to the current filestore (cache)
        BulkRetrievalThread thread = retrievable.makeLocal(currentSector, 0, (BulkRetrievalEvent event) -> {
            {
//                            System.out.printf("%s: item %s\n",
//                                    event.getEventType().equals(BulkRetrievalEvent.RETRIEVAL_SUCCEEDED) ? "Succeeded"
//                                    : event.getEventType().equals(BulkRetrievalEvent.RETRIEVAL_FAILED) ? "Failed"
//                                    : "Unknown event type", event.getItem());
            }
        });

        if (thread != null) {
            BulkDownloader.activeMonitors.add(new BulkDownloadMonitor(thread));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class BulkDownloadMonitor implements Cancellable {

        private final BulkRetrievalThread thread;
        private final Progress progress;
        private final Timer updateTimer;
        private final ProgressHandle handle;

        public BulkDownloadMonitor(BulkRetrievalThread thread) {
            this.thread = thread;
            this.progress = thread.getProgress();
            this.handle = ProgressHandleFactory.createHandle(
                    "Downloading " + thread.getRetrievable().getName(),
                    this);

            this.updateTimer = new Timer(1000, (ActionEvent event) -> {
                updateStatus();
            });

            this.handle.start(100);
            this.updateTimer.start();

        }

        private void updateStatus() {
            // Update progress bar
            int percent = 0;
            if (this.progress.getTotalCount() > 0) {
                percent = (int) ((float) this.progress.getCurrentCount() / this.progress.getTotalCount() * 100f);
            }
            this.handle.progress(percent);

            // Check for end of thread
            if (!this.thread.isAlive()) {
                // Thread is done
                this.updateTimer.stop();
                this.handle.finish();
            }
        }

        @Override
        public boolean cancel() {
            if (this.thread.isAlive()) {
                // Cancel thread
                this.thread.interrupt();
                this.updateTimer.stop();
                this.handle.finish();
            }
            // Release the monitor
            BulkDownloader.activeMonitors.remove(this);

            // Return true on success; false if unable.
            return true;
        }
    }

    public void abortDownload(GisLayer downloadableLayer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
