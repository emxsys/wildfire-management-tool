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

import com.emxsys.gis.api.data.GisResultSetMetaData;
import com.emxsys.gis.shapefile.format.RandomAccessShapefile;
import gov.nasa.worldwind.formats.shapefile.DBaseField;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;

@Messages(
        {
            "# {0} - column index",
            "# {1} - max column",
            "error.invalid.column=The column index ({0}) is not within the range of 1 and {1}."
        })

/**
 * Provides meta data about an ESRI Shapefile.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapefileResultSetMetaData extends GisResultSetMetaData {

    private final RandomAccessShapefile shapefile;
    private static final Logger LOG = Logger.getLogger(ShapefileResultSetMetaData.class.getName());

    public ShapefileResultSetMetaData(RandomAccessShapefile shapefile) {
        this.shapefile = shapefile;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return this.shapefile.getFields().size();
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        validateColumnIndex(column);
        return this.shapefile.getFieldNames().get(column - 1);
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        validateColumnIndex(column);
        return this.shapefile.getFieldNames().get(column - 1);
    }

    @Override
    public String getTableName(int column) throws SQLException {
        validateColumnIndex(column);
        return this.shapefile.getName();
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        validateColumnIndex(column);
        String columnTypeName = getColumnTypeName(column);

        switch (columnTypeName) {
            case DBaseField.TYPE_BOOLEAN:
                return java.sql.Types.BOOLEAN;
            case DBaseField.TYPE_CHAR:
                return java.sql.Types.CHAR;
            case DBaseField.TYPE_NUMBER:
                int scale = getScale(column);
                return scale > 0 ? java.sql.Types.DOUBLE : java.sql.Types.INTEGER;
            case DBaseField.TYPE_DATE:
                return java.sql.Types.DATE;
            default:
                return java.sql.Types.OTHER;
        }
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        validateColumnIndex(column);
        return this.shapefile.getFields().get(column - 1).getType();
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        validateColumnIndex(column);
        int columnType = getColumnType(column);
        switch (columnType) {
            case java.sql.Types.BOOLEAN:
                return Boolean.class.getName();
            case java.sql.Types.CHAR:
                return String.class.getName();
            case java.sql.Types.DOUBLE:
                return Double.class.getName();
            case java.sql.Types.INTEGER:
                return Long.class.getName();
            case java.sql.Types.DATE:
                return java.sql.Date.class.getName();
        }
        throw new SQLException("Unknown column type!");
    }

    @Override
    public String getShapeTypeName() throws SQLException {
        return this.shapefile.getShapeType();
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        validateColumnIndex(column);
        return true;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        validateColumnIndex(column);
        return false;
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        validateColumnIndex(column);
        return this.shapefile.getFields().get(column - 1).getLength();
    }

    @Override
    public int getScale(int column) throws SQLException {
        validateColumnIndex(column);
        return this.shapefile.getFields().get(column - 1).getDecimals();
    }

    /**
     * Validate that the column index is within the one-based column range
     * @param column the one-based column index
     * @throws SQLException
     */
    private void validateColumnIndex(int column) throws SQLException {
        if (column < 1 || column > this.getColumnCount()) {
            String message = Bundle.error_invalid_column(column, getColumnCount());
            LOG.severe(message);
            throw new SQLException(message);
        }
    }
}
