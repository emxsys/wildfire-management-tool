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
package com.emxsys.wmt.globe;

import com.emxsys.wmt.gis.api.GeoSector;
import com.emxsys.wmt.gis.api.viewer.SectorEditor;
import com.emxsys.wmt.gis.api.viewer.SelectedSector;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.util.Sectors;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwindx.examples.util.SectorSelector;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.lookup.InstanceContent;

/**
 * An interactive editor for defining and editing sectors on the globe that places the selected
 * sector into a supplied Lookup's InstanceContent.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class GlobeSectorEditor extends SectorSelector implements SectorEditor {

    private final InstanceContent content;
    private final CurrentSectorProxy selectedSector;
    private GeoSector currentSector;

    /**
     * Creates an instance that adds and removes the selected sector to/from the supplied
     * InstanceContent.
     *
     * @param viewer that displays the sector
     * @param content to which the GeoSector is added or removed.
     */
    public GlobeSectorEditor(InstanceContent content) {
        // Using our custimized RegionBorderShape (see end of file)
        super(Globe.getInstance().getWorldWindManager().getWorldWindow(), new RegionBorderShape(), new RenderableLayer());

        this.content = content;
        this.currentSector = new GeoSector();
        this.selectedSector = new CurrentSectorProxy();

        // Init WorldWind based sector selector
        this.setInteriorColor(new Color(1f, 1f, 1f, 0.1f)); // light shading
        this.setBorderColor(new Color(1f, 0f, 0f, 0.5f));   // red border
        this.setBorderWidth(3);
        this.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateSector();
            }
        });
    }

    @Override
    public void addSectorPropertyChangeListener(PropertyChangeListener pcl) {
        super.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, pcl);
    }

    @Override
    public void removeSectorPropertyChangeListener(PropertyChangeListener pcl) {
        super.removePropertyChangeListener(SectorSelector.SECTOR_PROPERTY, pcl);
    }

    /**
     * Update the current sector in the lookup.
     */
    private void updateSector() {
        if (this.selectedSector != null) {
            this.content.remove(this.selectedSector);
        }
        Sector wwSector = this.getSector();
        if (wwSector != null) {
            // Get a new sector in GIS/Visad units
            this.currentSector = Sectors.toGeoSector(wwSector);

            // Publish the availablity of the selected sector via the public lookup
            this.content.add(this.selectedSector);
        }

    }

    @Override
    public void disableSectorSelector() {
        // Disable the functionality
        this.disable();
        // Remove the current selection
        if (this.selectedSector != null) {
            this.content.remove(this.selectedSector);
        }
    }

    @Override
    public void enableSectorSelector() {
        this.enable();
    }

    public GeoSector getCurrentSector() {
        return this.currentSector;
    }

    @Override
    public void setSector(GeoSector geoSector) {
        // Update the base class' renderable shape 
        // RegionShape is a composite of a SurfaceSector and a border
        RegionShape shape = super.getShape();
        shape.setSector(geoSector.isMissing() ? Sector.EMPTY_SECTOR : Sectors.fromBox(geoSector));
    }

    /**
     * A capability class which is published on the public lookup mechanism.
     */
    private class CurrentSectorProxy implements SelectedSector {

        @Override
        public GeoSector getSelectedSector() {
            return GlobeSectorEditor.this.getCurrentSector();
        }

        @Override
        public SectorEditor getSectorEditor() {
            return GlobeSectorEditor.this;
        }
    }

    /**
     * This specialized class fixes a problem in the WW RegionShape where the entire border moves
     * during a resize operation. This class sync's the border to the region before rendering.
     */
    private static class RegionBorderShape extends RegionShape {

        RegionBorderShape() {
            super(Sector.EMPTY_SECTOR);
        }

        @Override
        public void render(DrawContext dc) {
            // HACK: Sync the border with to the region. During a resize, the opposite 
            // side of the border moves when it should be anchored.  Dont' know why.
            super.getBorder().setSector(super.getSector());

            super.render(dc);
        }
    }
}
