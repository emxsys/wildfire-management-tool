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
package com.emxsys.wmt.globe.actions;

import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.layer.GisLayer;
import com.emxsys.gis.api.viewer.SelectedSector;
import com.emxsys.wmt.globe.cache.BulkDownloader;
import com.emxsys.wmt.globe.cache.LayerSelectionPanel;
import com.terramenta.ribbon.RibbonActionReference;
import gov.nasa.worldwind.WorldWind;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * A context aware action that listens for a SelectSelected object in the lookup.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@ActionID(id = "com.emxsys.wmt.globe.actions.DownloadMaps", category = "Tools")
@ActionRegistration(displayName = "#CTL_DownloadMaps",
        iconBase = "com/emxsys/wmt/globe/images/mapset-save.png",
        iconInMenu = true)
@ActionReference(path = "Toolbars/Map Tools", position = 1300)
@RibbonActionReference(path = "Menu/Tools/Map Cache", position = 300,
        description = "#CTL_DownloadMaps_Hint",
        priority = "top",
        tooltipTitle = "#CTL_DownloadMaps_TooltipTitle",
        tooltipBody = "#CTL_DownloadMaps_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/globe/images/mapset-save.png")

@Messages({
    "CTL_DownloadMaps=Download Maps",
    "CTL_DownloadMaps_Hint=Download maps for offline use.",
    "CTL_DownloadMaps_TooltipTitle=Download Maps in Sector",
    "CTL_DownloadMaps_TooltipBody=Downloads maps within the selected sector into your system's map cache.\n"
    + "This action will open a dialog where you can select the map types to download.",
    "networkIsOffline=Bulk Download is disabled because the system is offline or the network is not avialable.",
    "downloadSupportUnavailable=Bulk Download support is not installed or is not avialable.",
    "dialogTitle=Select Maps and Layers to Download"
})
public class DownloadMaps extends AbstractAction {

    SelectedSector context;
    static final Logger LOG = Logger.getLogger(DownloadMaps.class.getName());

    public DownloadMaps(SelectedSector context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (WorldWind.isOfflineMode() || WorldWind.getNetworkStatus().isNetworkUnavailable()) {
            LOG.warning(Bundle.networkIsOffline());
            return;
        }
        BulkDownloader downloader = new BulkDownloader();

        // Get the sector to be downloaded
        GeoSector selectedSector = context.getSelectedSector();

        // Create the dialog content panel
        LayerSelectionPanel layerSelection = new LayerSelectionPanel(downloader.getBulkDownloadableLayers());

        if (layerSelection.displayModal(Bundle.dialogTitle())) {
            List<GisLayer> selectedLayers = layerSelection.getSelectedLayers();

            for (GisLayer gisLayer : selectedLayers) {
                downloader.startDownload(gisLayer, selectedSector);
            }
        }
    }
}
