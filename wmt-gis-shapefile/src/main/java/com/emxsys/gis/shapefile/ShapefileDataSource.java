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
package com.emxsys.gis.shapefile;

import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.data.GisDataSource;
import com.emxsys.gis.api.data.GisResultSet;
import com.emxsys.gis.shapefile.format.RandomAccessShapefile;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * This GIS data source represents an ESRI shapefile. The {@link Shapefile} implementation object is
 * contained in the {@link GisDataSource}'s lookup.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapefileDataSource implements GisDataSource {

    private final FileObject shpFile;
    private RandomAccessShapefile shapefile = null;
    private ShapefileResultSetMetaData metaData = null;
    private Lookup lookup = null;
    private final static Logger LOG = Logger.getLogger(ShapefileDataSource.class.getName());

    public ShapefileDataSource(FileObject primaryFile) {
        this.shpFile = primaryFile;
    }

    @Override
    public String getName() {
        return this.shpFile.getName();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Get this {@link GisDataSource}'s lookup.
     * @return the {@link Lookup} containing the {@link GisDataSource}'s {@link Shapefile}.
     */
    @Override
    public Lookup getLookup() {
        if (this.lookup == null) {
            this.shapefile = new RandomAccessShapefile(this.shpFile);
            this.metaData = new ShapefileResultSetMetaData(this.shapefile);
            this.lookup = Lookups.fixed(this.shapefile, this.metaData);
        }
        return this.lookup;
    }

    @Override
    public GisResultSet getResultSet() {
        return new ShapefileResultSet(getShapefile());
    }

    @Override
    public GisResultSet getResultSet(Box boundingBox) {
        return new ShapefileResultSet(getShapefile(), boundingBox);
    }

    @Override
    public GisResultSet getResultSet(String columnLabel, String queryValue, boolean ignoreCase) {
        return new ShapefileResultSet(getShapefile(), columnLabel, queryValue, ignoreCase);
    }

    /**
     * Get the Shapefile implementation object.
     * @return the Shapefile that is backing the result set.
     */
    public RandomAccessShapefile getShapefile() {
        return getLookup().lookup(RandomAccessShapefile.class);
    }

}
