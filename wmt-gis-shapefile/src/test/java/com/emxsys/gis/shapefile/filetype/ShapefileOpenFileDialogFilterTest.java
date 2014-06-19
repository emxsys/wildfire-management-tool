/*
 * Copyright (C) 2011 Bruce Schubert <bruce@emxsys.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.emxsys.gis.shapefile.filetype;

import org.openide.util.Lookup;
import org.netbeans.modules.openfile.OpenFileDialogFilter;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapefileOpenFileDialogFilterTest {

    public ShapefileOpenFileDialogFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getFilter method, of class ShapefileOpenFileDialogFilter.
     */
    @Test
    public void testGetFilter() {
        System.out.println("getFilter - OpenFileDialog filters:");
        for (OpenFileDialogFilter f : Lookup.getDefault().lookupAll(OpenFileDialogFilter.class)) {
            // Dump the list to output window
            System.out.println("  " + f.getDescription());
        }
        for (OpenFileDialogFilter f : Lookup.getDefault().lookupAll(OpenFileDialogFilter.class)) {
            if (f.getDescription() != null) {
                Assert.assertFalse("Java filter present.", f.getDescriptionString().contains("Java"));
                Assert.assertFalse("Text filter present.", f.getDescriptionString().contains("Text"));
            }
        }
        for (OpenFileDialogFilter f : Lookup.getDefault().lookupAll(OpenFileDialogFilter.class)) {
            if (f.getDescription().contains("Shapefile")) {
                return;
            }
        }
        fail("Shapefile filter not present!");
    }
}
