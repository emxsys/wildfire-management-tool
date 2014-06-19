
/*
 * Copyright (C) 2011 Bruce Schubert <bruce@emxsys.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.emxsys.gis.shapefile;

import java.net.URL;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class TestData {

    public static final URL SHP_URL = ClassLoader.getSystemResource("tl_2012_us_state.shp");
    public static final URL DBF_URL = ClassLoader.getSystemResource("tl_2012_us_state.dbf");
    public static final String SHAPEFILE = SHP_URL.getFile();
    public static final String DBASEFILE = DBF_URL.getFile();
    public static final String SHAPEFILE_NAME = "tl_2012_us_state";
    public static final int SHAPEFILE_RECS = 56; // 50 States plus Wash DC. and properties
    public static final int FIRST_REC_NO = 1;
    public static final int LAST_REC_NO = SHAPEFILE_RECS;
}
