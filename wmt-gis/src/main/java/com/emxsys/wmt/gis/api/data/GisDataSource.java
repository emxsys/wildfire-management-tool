
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
package com.emxsys.wmt.gis.api.data;

import com.emxsys.wmt.gis.api.Box;
import com.emxsys.wmt.gis.api.Named;
import org.openide.util.Lookup;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public interface GisDataSource extends Named, Lookup.Provider {

    /**
     * Get a {@link GisResultSet} provided by this data source.
     *
     * @return a {@link ResultSet} based result set containing all the rows in the data source.
     */
    GisResultSet getResultSet();

    /**
     * Get a {@link GisResultSet} provided by this data source who's features
     * are contained in or intersecting the bounding box.
     *
     * @param boundingBox the rectangle defined the features to be returned
     * @return a {@link ResultSet} containing the features within or intersecting the bounding box.
     */
    GisResultSet getResultSet(Box boundingBox);

    /**
     * Get a {@link GisResultSet} provided by this data source where a column's values match
     * a the query value argument.
     *
     * @param columnLabel the name of the column to query
     * @param queryValue the query value/criteria
     * @param ignoreCase instructs the query processor to perform a case insensitive equality test
     * @return a {@link ResultSet} containing the rows that match the column's query value
     */
    GisResultSet getResultSet(String columnLabel, String queryValue, boolean ignoreCase);
}
