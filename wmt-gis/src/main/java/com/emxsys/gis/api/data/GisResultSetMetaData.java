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
package com.emxsys.gis.api.data;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class GisResultSetMetaData implements ResultSetMetaData {

    abstract public String getShapeTypeName() throws SQLException;

    @Override
    abstract public String getTableName(int column) throws SQLException;

    @Override
    abstract public int getColumnCount() throws SQLException;

    @Override
    abstract public int getColumnType(int column) throws SQLException;

    @Override
    abstract public String getColumnTypeName(int column) throws SQLException;

    @Override
    abstract public String getColumnLabel(int column) throws SQLException;

    @Override
    abstract public String getColumnName(int column) throws SQLException;

    @Override
    abstract public int getPrecision(int column) throws SQLException;

    @Override
    abstract public int getScale(int column) throws SQLException;

    @Override
    abstract public boolean isReadOnly(int column) throws SQLException;

    @Override
    abstract public boolean isWritable(int column) throws SQLException;

    /**
     * Not Implemented.
     */
    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public boolean isSearchable(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public boolean isCurrency(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public int isNullable(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public boolean isSigned(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public String getSchemaName(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public String getCatalogName(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public String getColumnClassName(int column) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not Implemented.
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
