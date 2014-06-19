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
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.data.GisResultSet;
import com.emxsys.gis.shapefile.format.RandomAccessShapefile;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordMultiPoint;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordNull;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPoint;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPolygon;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPolyline;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages(
        {
            "# {0} - column name",
            "# {1} - class type",
            "error.cannot.convert.long=Column '{0}' ({1}} cannot be returned as a Long",
            "# {0} - column name",
            "# {1} - class type",
            "error.cannot.convert.double=Column '{0}' ({1}} cannot be returned as a Double",
            "# {0} - column name",
            "# {1} - class type",
            "error.cannot.convert.string=Column '{0}' ({1}} cannot be returned as a String",
            "error.cannot.convert.null=Could not convert a null value to the appropriate default."
        })
public class ShapefileResultSet extends GisResultSet {

    public static final String GIS_FEATURE_LABEL = "GIS_FEATURE";
    public static final int GIS_FEATURE_INDEX = 0;
    private static final int FIRST_ATTRIBUTE_INDEX = GIS_FEATURE_INDEX + 1;
    private RandomAccessShapefile shapefile;
    private ShapefileRecord cursor = null;
    private ShapefileResultSetMetaData metaData = null;
    private Box queryBounds = null;
    private List<Integer> queryResults = null;
    private boolean beforeFirst = true;
    private boolean afterLast = false;
    private int currentRow = 0;
    private static final Logger LOG = Logger.getLogger(ShapefileResultSet.class.getName());

    /**
     * Creates an unfiltered (all rows) result set.
     *
     * @param shapefile the Shapefile providing the results
     */
    public ShapefileResultSet(RandomAccessShapefile shapefile) {
        this(shapefile, null);
    }

    /**
     * Creates a result filtered to include only those rows who's feature's bounds intersect the
     * query bound bounding box
     * @param shapefile the shapefile providing the results
     * @param queryBounds the query bounding box
     */
    public ShapefileResultSet(RandomAccessShapefile shapefile, Box queryBounds) {
        this.shapefile = shapefile;
        this.queryResults = queryForIntersectingBounds(queryBounds);
    }

    /**
     * Creates a result filtered to include only rows that match a simple query.
     * @param shapefile the shapefile providing the results
     * @param columnLabel the column to query
     * @param queryValue the value to match
     * @param ignoreCase if true, a case insensitive search is performed.
     */
    public ShapefileResultSet(RandomAccessShapefile shapefile, String columnLabel, String queryValue,
                              boolean ignoreCase) {
        this.shapefile = shapefile;
        this.queryResults = queryForMatchingValues(columnLabel, queryValue, ignoreCase);
    }

    /**
     * Gets a list of record numbers who features intersect the query bounding box.
     * @param queryBounds the query criteria
     * @return a list of qualifying record numbers
     */
    private List<Integer> queryForIntersectingBounds(Box queryBounds) {
        this.queryBounds = queryBounds;
        if (this.queryBounds == null || this.queryBounds.contains(getShapefileBounds(this.shapefile))) {
            return null;
        }
        // Filter the result set rows based on the bounding box
        List<Integer> results = new ArrayList<>();
        try {
            while (next()) {
                Geometry feature = this.getFeature();
                if (feature.getExtents().intersects(this.queryBounds)) {
                    results.add(getRow());
                }
            }
            setBeforeFirst();
        }
        catch (SQLException ex) {
            LOG.warning(ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    /**
     * Gets a list of record numbers where a column matches a query value.
     * @param queryBounds the query criteria
     * @return a list of qualifying record numbers
     */
    private List<Integer> queryForMatchingValues(String columnLabel, String queryValue,
                                                 boolean ignoreCase) {
        // Filter the result set rows based on simple text equality
        List<Integer> results = new ArrayList<>();
        try {
            while (next()) {
                String valueAsString = this.getString(columnLabel);
                if (ignoreCase ? valueAsString.equalsIgnoreCase(queryValue) : valueAsString.equals(queryValue)) {
                    results.add(getRow());
                }
            }
            setBeforeFirst();
        }
        catch (SQLException ex) {
            LOG.warning(ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    private int getRecordNumberForRow(int row) {
        int recordNumber = row;
        if (this.queryResults != null) {
            recordNumber = this.queryResults.get(row - 1);
        }
        return recordNumber;
    }

    private void setRowIsValid() {
        beforeFirst = false;
        afterLast = false;
    }

    private void setBeforeFirst() {
        beforeFirst = true;
        afterLast = false;
    }

    private void setAfterLast() {
        beforeFirst = false;
        afterLast = true;
    }

    private boolean isEmpty() {
        return shapefile.getNumberOfRecords() < 1;
    }

    private void validateCursor() throws SQLException {
        if (cursor == null) {
            String message = "Access denied. The result set is empty (null).";
            LOG.severe(message);
            throw new SQLException(message);
        }
        else if (beforeFirst) {
            String message = "Access denied. The cursor is before the first record.";
            LOG.severe(message);
            throw new SQLException(message);
        }
        else if (afterLast) {
            String message = "Access denied. The cursor is after the last record.";
            LOG.severe(message);
            throw new SQLException(message);
        }
    }

    public int getRowCount() {
        if (this.queryResults != null) {
            return this.queryResults.size();
        }
        else {
            return this.shapefile.getNumberOfRecords();
        }
    }

    @Override
    public String getCursorName() throws SQLException {
        return shapefile.getName();
    }

    @Override
    public Box getBounds() {
        if (this.queryBounds != null) {
            return this.queryBounds;
        }
        return getShapefileBounds(this.shapefile);
    }

    public static GeoSector getShapefileBounds(Shapefile shapefile) {
        double[] boundingRectangle = shapefile.getBoundingRectangle();
        double yMin = boundingRectangle[0];
        double yMax = boundingRectangle[1];
        double xMin = boundingRectangle[2];
        double xMax = boundingRectangle[3];
//        double width = xMax - xMin;
//        double height = yMax - yMin;
        return new GeoSector(yMin, xMin, yMax, xMax);   // south, west, north, east
    }

    @Override
    public boolean isFirst() throws SQLException {
        if (beforeFirst || afterLast) {
            return false;
        }
        return cursor.getRecordNumber() == getRecordNumberForRow(1);
    }

    @Override
    public boolean isLast() throws SQLException {
        if (beforeFirst || afterLast) {
            return false;
        }
        return cursor.getRecordNumber() == getRecordNumberForRow(getRowCount());
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return beforeFirst;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return afterLast;
    }

    @Override
    public boolean first() throws SQLException {
        return absolute(1);
    }

    @Override
    public boolean last() throws SQLException {
        return absolute(getRowCount());
    }

    @Override
    public void beforeFirst() throws SQLException {
        setBeforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        // afterLast is not allowed on an empty result set.
        // So if no action is taken we should remain in a beforeFirst state.
        if (getRowCount() > 0) {
            setAfterLast();
        }
    }

    @Override
    public boolean next() throws SQLException {
        // Special handling for the first call of next so as to mimic the prescribed behavior for
        // the first call of next to place the record pointer on the first row.
        if (beforeFirst) {
            return first();
        }
        else if (afterLast) {
            return false;
        }
        return absolute(getRow() + 1);

    }

    @Override
    public boolean previous() throws SQLException {
        if (beforeFirst) {
            return false;
        }
        else if (afterLast) {
            return last();
        }
        return absolute(getRow() - 1);
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        if (rows == 1) {
            return next();
        }
        else if (rows == -1) {
            return previous();
        }
        return absolute(getRow() + rows);

    }

    @Override
    public boolean absolute(int row) throws SQLException {
        if (row < 1) {
            LOG.log(Level.INFO, "Attempting to move before first row via absolute({0}). Setting isBeforeFirst true.", row);
            setBeforeFirst();
            return false;
        }
        else if (row > getRowCount()) {
            if (getRowCount() == 0) {
                LOG.log(Level.INFO, "Attempting navigate an empty result set via absolute({0}). Setting isBeforeFirst true.", row);
                setBeforeFirst();
            }
            else {
                LOG.log(Level.INFO, "Attempting to move past last row via absolute({0}). Setting isAfterLast true.", row);
                setAfterLast();
            }
            return false;
        }

        // Position the shapefile on the specific record number
        int recordNumber = getRecordNumberForRow(row);
        if (this.shapefile.absolute(recordNumber)) {
            // Read the record contents into our "cursor" 
            if (updateCursor(recordNumber)) {
                this.currentRow = row;
                setRowIsValid();
            }
            else {
                String message = "Failed to update the cursor to row via updateCursor(" + row + ").";
                LOG.severe(message);
                throw new SQLException(message);
            }
        }
        else {
            String message = "Failed to move the cursor to row via absolute(" + row + ").";
            LOG.severe(message);
            throw new SQLException(message);
        }
        return true;
    }

    /**
     * Refresh the cached record contents if required to sync with the record pointer.
     */
    private boolean updateCursor(int recordNumber) {
        if (cursor != null && cursor.getRecordNumber() == recordNumber) {
            return true;
        }
        else if (readRecord()) {
            return cursor.getRecordNumber() == recordNumber;
        }
        return false;
    }

    private boolean readRecord() {
        if (shapefile.hasNext()) {
            // IMPORTANT: this method will actually move the Shapefile record pointer 
            // to the beginning of the next record, thus the cursor record number is one less
            // than the current shapefile record number.
            cursor = shapefile.nextRecord();
            return true;
        }
        else {
            LOG.warning("An attempt was made to read past the end of the result set by readRecord.");
            cursor = null;
            return false;
        }
    }

    @Override
    public int getRow() throws SQLException {
        if (beforeFirst || afterLast) {
            return 0;
        }
        validateCursor();
        return currentRow;
        //return cursor.getRecordNumber();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return getString(findColumn(columnIndex));
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        validateCursor();
        try {
            Object value = this.cursor.getAttributes().getValue(columnLabel);
            if (value instanceof String) {
                return (String) value;
            }
            else if (value == null) {
                return new String();
            }
            else {
                return value.toString();
            }
        }
        catch (RuntimeException e) {
            throw new SQLException(e.toString());
        }
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return getBoolean(findColumn(columnIndex));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        validateCursor();
        try {
            Object value = this.cursor.getAttributes().getValue(columnLabel);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            else {
                throw new SQLException();
            }
        }
        catch (Exception e) {
            throw new SQLException(e.toString());
        }
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return getLong(findColumn(columnIndex));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        validateCursor();
        try {
            Object value = this.cursor.getAttributes().getValue(columnLabel);
            if (value instanceof Long) {
                return (Long) value;
            }
            if (value instanceof Double) {
                return Math.round((Double) value);
            }
            else if (value == null) {
                // Return zero if its permissible to convert this column to a long
                int columnIndex = findColumn(columnLabel);
                int columnType = getMetaData().getColumnType(columnIndex);
                if (columnType == Types.INTEGER || columnType == Types.DOUBLE) {
                    return 0;
                }
                throw new SQLException(Bundle.error_cannot_convert_long(columnLabel, getMetaData().getColumnClassName(columnIndex)));
            }
            else {
                throw new SQLException(Bundle.error_cannot_convert_long(columnLabel, value.getClass()));
            }
        }
        catch (WWRuntimeException e) {
            throw new SQLException(e.toString());
        }
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return getDouble(findColumn(columnIndex));
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        validateCursor();
        try {
            Object value = this.cursor.getAttributes().getValue(columnLabel);
            if (value instanceof Double) {
                return (Double) value;
            }
            if (value instanceof Long) {
                return ((Long) value).doubleValue();
            }
            else if (value == null) {
                // Return zero if its permissible to convert this column to a double
                int columnIndex = findColumn(columnLabel);
                int columnType = getMetaData().getColumnType(columnIndex);
                if (columnType == Types.INTEGER || columnType == Types.DOUBLE) {
                    return 0.0;
                }
                throw new SQLException(Bundle.error_cannot_convert_double(columnLabel, getMetaData().getColumnClassName(columnIndex)));
            }
            else {
                throw new SQLException(Bundle.error_cannot_convert_double(columnLabel, value.getClass()));
            }
        }
        catch (WWRuntimeException e) {
            throw new SQLException(e.toString());
        }
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return getObject(findColumn(columnIndex));
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        validateCursor();
        try {
            if (GIS_FEATURE_LABEL.equalsIgnoreCase(columnLabel)) {
                return getFeature();
            }
            Object value = this.cursor.getAttributes().getValue(columnLabel);
            if (value != null) {
                return value;
            }
            else {
                // When null, return an appropriate default value
                int columnIndex = findColumn(columnLabel);
                int columnType = getMetaData().getColumnType(columnIndex);
                switch (columnType) {
                    case Types.INTEGER:
                        return (long) 0;
                    case Types.DOUBLE:
                        return 0.0;
                    case Types.CHAR:
                        return new String();
                    case Types.BOOLEAN:
                        return false;
                }
                throw new SQLException(Bundle.error_cannot_convert_null());
            }
        }
        catch (Exception e) {
            throw new SQLException(e.toString());
        }
    }

    @Override
    public Geometry getFeature() throws SQLException {
        validateCursor();

        if (cursor instanceof ShapefileRecordPoint) {
            return new ShapefileFeature((ShapefileRecordPoint) this.cursor);
        }
        else if (cursor instanceof ShapefileRecordPolyline) {
            return new ShapefileFeature((ShapefileRecordPolyline) this.cursor);
        }
        else if (cursor instanceof ShapefileRecordPolygon) {
            return new ShapefileFeature((ShapefileRecordPolygon) this.cursor);
        }
        else if (cursor instanceof ShapefileRecordMultiPoint) {
            return new ShapefileFeature((ShapefileRecordMultiPoint) this.cursor);
        }
        else if (cursor instanceof ShapefileRecordNull) {
            return new ShapefileFeature((ShapefileRecordNull) this.cursor);
        }
        else {
            throw new SQLException();
        }

    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        if (GIS_FEATURE_LABEL.equalsIgnoreCase(columnLabel)) {
            return GIS_FEATURE_INDEX;
        }
        int columnIndex = this.shapefile.getFieldNames().indexOf(columnLabel);
        if (columnIndex == -1) {
            throw new SQLException("A column named '" + columnLabel + "' was not found in the ResultSet");
        }
        else {
            // Convert to one-based index
            return columnIndex + FIRST_ATTRIBUTE_INDEX;
        }

    }

    public String findColumn(int columnIndex) throws SQLException {
        if (columnIndex == GIS_FEATURE_INDEX) {
            return GIS_FEATURE_LABEL;
        }
        int columnCount = getMetaData().getColumnCount();
        if (columnIndex < 1 || columnIndex > columnCount) {
            throw new SQLException(Bundle.error_invalid_column(columnIndex, columnCount));
        }
        return this.shapefile.getFieldNames().get(columnIndex - FIRST_ATTRIBUTE_INDEX);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        if (this.metaData == null) {
            this.metaData = new ShapefileResultSetMetaData(this.shapefile);
        }
        return this.metaData;
    }

    @Override
    public int getType() {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

}
