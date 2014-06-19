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

import com.emxsys.gis.api.AbstractFeature;
import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.FeatureClass;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.Part;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordMultiPoint;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordNull;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPoint;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPolygon;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPolyline;
import gov.nasa.worldwind.util.CompoundVecBuffer;
import gov.nasa.worldwind.util.VecBuffer;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * ShapefileFeature.java
 *
 * Encapsulates details of ESRI Shapefile format, documented at
 * http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf
 *
 * @author Russ Rew
 * @author Bruce Schubert
 */
public class ShapefileFeature extends AbstractFeature implements Geometry {

    private Lookup lookup;
    protected InstanceContent content = new InstanceContent();
    /**
     * bounds of this feature
     */
    protected GeoSector bounds;
    /**
     * the list of parts
     */
    protected List<Part> partsList;
    /**
     * the shapefile feature
     */
    protected ShapefileRecord record;
    /**
     * the feature spatial type
     */
    private final FeatureClass geometryType;
    private static final Logger logger = Logger.getLogger(ShapefileFeature.class.getName());

    public ShapefileFeature(ShapefileRecordNull record) {
        this(record, new GeoSector());
    }

    public ShapefileFeature(ShapefileRecordPoint record) {
        this(record, new GeoSector(
                record.getPoint()[1], record.getPoint()[0], // south, west
                record.getPoint()[1], record.getPoint()[0]));   // north, east
    }

    public ShapefileFeature(ShapefileRecordMultiPoint record) {
        this(record, boundsArrayToRect(record.getBoundingRectangle()));
    }

    public ShapefileFeature(ShapefileRecordPolyline record) {
        this(record, boundsArrayToRect(record.getBoundingRectangle()));
    }

    public ShapefileFeature(ShapefileRecordPolygon record) {
        this(record, boundsArrayToRect(record.getBoundingRectangle()));
    }

    private ShapefileFeature(ShapefileRecord record, GeoSector bounds) {
        this.record = record;
        this.bounds = bounds;
        this.geometryType = findFeatureClass(record);
    }

    @Override
    public Geometry getGeometry() {
        return this;
    }

    @Override
    public Lookup getLookup() {
        if (this.lookup == null) {
            this.lookup = new AbstractLookup(content);
        }
        return this.lookup;
    }

    /**
     * Get bounding rectangle for this feature.
     *
     * @return bounding rectangle for this feature.
     */
    @Override
    public Box getExtents() {
        return bounds;
    }

    /**
     * Get total number of points in all parts of this feature.
     *
     * @return total number of points in all parts of this feature.
     */
    @Override
    public int getNumPoints() {
        return record.getNumberOfPoints();
    }

    /**
     * Get number of parts comprising this feature.
     *
     * @return number of parts comprising this feature.
     */
    @Override
    public int getNumParts() {
        return record.getNumberOfParts();
    }

    /**
     * Get the parts of this feature, in the form of an iterator.
     *
     * @return an iterator over the parts of this feature. Each part is a BasicPart.
     */
    @Override
    public Iterable<Part> getParts() {
        if (this.partsList == null) {
            this.partsList = new ArrayList<>();
            CompoundVecBuffer cpb = this.record.getCompoundPointBuffer();
            for (int i = 0; i < cpb.size(); i++) {
                partsList.add(new ShapefileFeature.BasicPart(cpb.subBuffer(i)));
            }
        }
        return partsList;
    }

    /**
     * Convert this Feature to a java.awt.Shape.
     *
     * @return a java.awt.Shape corresponding to this feature.
     * @see Shape
     */
    public Shape getShape() {
        if (this.geometryType != FeatureClass.LINE
                || this.geometryType != FeatureClass.POLYGON) {
            throw new UnsupportedOperationException("getShape() does not support the "
                    + this.record.getShapeType() + " shape type.");
        }

        Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD, getNumPoints());
        for (Part part : getParts()) {
            double[] firstPoint = null;
            for (double[] point : part.getPoints()) {
                if (firstPoint == null) {
                    firstPoint = new double[]{
                        point[0], point[1]
                    };
                    path.moveTo(point[0], point[1]);
                }
                else {
                    path.lineTo(point[0], point[1]);
                }
            }
            // Must "close" polygon parts... 
            if (this.geometryType == FeatureClass.POLYGON) {
                path.lineTo(firstPoint[0], firstPoint[1]);
            }
        }
        return path;
    }

    /**
     * Convert this GisFeature to a java.awt.Point2D.
     *
     * @return point corresponding to this feature.
     */
    public Point2D getPoint2D() {

        if (this.geometryType != FeatureClass.POINT) {
            throw new UnsupportedOperationException("getPoint() does not support the "
                    + this.record.getShapeType() + " shape type. Use getShape() instead.");
        }
        for (Part part : getParts()) {
            for (double[] point : part.getPoints()) {
                return new Point.Double(point[0], point[1]);
            }
        }
        return null;
    }

    @Override
    public FeatureClass getFeatureClass() {
        return this.geometryType;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Gets the ID for this feature.
     *
     * @return the record number.
     */
    @Override
    public String getUniqueID() {
        return Long.toString(this.record.getRecordNumber());
    }

    /**
     * Not supported.
     *
     * @param uniqueID ignored.
     */
    @Override
    public void setUniqueID(String uniqueID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Gets the name for this feature. return text for a label.
     */
    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not supported.
     *
     * @param name ignored.
     */
    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    static GeoSector boundsArrayToRect(double[] boundsArray) {
        double yMin = boundsArray[0];
        double yMax = boundsArray[1];
        double xMin = boundsArray[2];
        double xMax = boundsArray[3];
        //double width = xMax - xMin;
        //double height = yMax - yMin;
        return new GeoSector(yMin, xMin, yMax, xMax);   // order: south, west, north, east
    }

    static FeatureClass findFeatureClass(ShapefileRecord record) {
        final String shapeType = record.getShapeType();
        // Comparing intern'd Strings
        switch (shapeType) {
            case Shapefile.SHAPE_POINT:
            case Shapefile.SHAPE_POINT_M:
            case Shapefile.SHAPE_POINT_Z:
                return FeatureClass.POINT;
            case Shapefile.SHAPE_POLYLINE:
            case Shapefile.SHAPE_POLYLINE_M:
            case Shapefile.SHAPE_POLYLINE_Z:
                return FeatureClass.LINE;
            case Shapefile.SHAPE_POLYGON:
            case Shapefile.SHAPE_POLYGON_M:
            case Shapefile.SHAPE_POLYGON_Z:
                return FeatureClass.POLYGON;
            case Shapefile.SHAPE_MULTI_POINT:
            case Shapefile.SHAPE_MULTI_POINT_M:
            case Shapefile.SHAPE_MULTI_POINT_Z:
                return FeatureClass.MULTIPOINT;
            case Shapefile.SHAPE_MULTI_PATCH:
                return FeatureClass.MULTIPATCH;
            default:
                return FeatureClass.UNDEFINED;
        }
    }

    /**
     * Implementation of BasicPart for Esri specific features, x and y are converted to lon/lat if a
     * ProjFile is available
     */
    class BasicPart implements Part {

        private final VecBuffer buffer;
        private double[] xx;
        private double[] yy;
        private double[] zz;

        /**
         * Construct an Shapefile BasicPart from a VecBuffer.
         *
         * @param buffer the Shapefile part to use
         */
        public BasicPart(VecBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public int getNumDimensions() {
            return this.buffer.getCoordsPerVec();
        }

        /**
         * Get the number of points.
         *
         * @return the number of points
         */
        @Override
        public int getNumPoints() {
            return this.buffer.getSize();
        }

        /**
         * Get this part's collection of points.
         *
         * @return this part's collection of points as double[] arrays.
         */
        @Override
        public Iterable<double[]> getPoints() {
            return this.buffer.getCoords();
        }

        @Override
        public double[] getX() {
            if (this.xx == null) {
                initXYZ();
            }
            return xx;
        }

        @Override
        public double[] getY() {
            if (this.yy == null) {
                initXYZ();
            }
            return yy;
        }

        @Override
        public double[] getZ() {
            if (this.zz == null) {
                initXYZ();
            }
            return zz;
        }

        private void initXYZ() {
            xx = new double[this.getNumPoints()];
            yy = new double[this.getNumPoints()];
            zz = new double[this.getNumPoints()];

            int i = 0;
            for (double[] point : this.buffer.getCoords()) {
                xx[i] = point[0];
                yy[i] = point[1];
                if (this.buffer.getCoordsPerVec() == 3) {
                    zz[i] = point[2];
                }
                ++i;

            }
        }
    }
}
