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
package com.emxsys.gis.shapefile;

import com.emxsys.gis.api.AbstractFeature;
import com.emxsys.gis.api.Box;
import com.emxsys.gis.api.GeoSector;
import com.emxsys.gis.api.data.GisResultSetMetaData;
import static com.emxsys.gis.shapefile.TestData.FIRST_REC_NO;
import com.emxsys.gis.shapefile.format.RandomAccessShapefile;
import java.io.File;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapefileResultSetTest {

    private ShapefileResultSet instance;

    public ShapefileResultSetTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        FileObject testData = FileUtil.toFileObject(new File(TestData.SHAPEFILE));
        assertNotNull(testData);

//        RandomAccessShapefile shapefile = new RandomAccessShapefile(testData);
//        instance = new ShapefileResultSet(shapefile);
//        assertNotNull(instance);
        RandomAccessShapefile shapefile = new RandomAccessShapefile(testData);
        Box queryBounds = new GeoSector(-119.0, 45, -119.0, 45);
        instance = new ShapefileResultSet(shapefile, queryBounds);
        assertNotNull(instance);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of next method, of class ShapefileResultSet.
     */
    @Test
    public void testNext() throws Exception {
        System.out.println("next");
        int lastRow = 0;
        assertTrue(instance.isBeforeFirst());
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            assertFalse(instance.isBeforeFirst());
            assertFalse(instance.isAfterLast());
            int thisRow = instance.getRow();
            assertEquals(lastRow + 1, thisRow);

            // Read an attribute to force the record buffer to be updated/moved.
            assertNotNull(instance.getString(1));
            assertEquals(lastRow + 1, instance.getRow());

            System.out.println("   Row: " + thisRow + " : " + instance.getString("STATE_NAME"));

            lastRow = thisRow;
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
        assumeTrue(instance.getRowCount() > 0);
        assertEquals(instance.getRowCount(), lastRow);
        assertEquals(0, instance.getRow());
        assertFalse(instance.isLast());
        assertTrue(instance.isAfterLast());
    }

    /**
     * Test of isFirst method, of class ShapefileResultSet.
     */
    @Test
    public void testIsFirst() throws Exception {
        System.out.println("isFirst");
        assertTrue(instance.isBeforeFirst());
        assertFalse(instance.isFirst());
        assumeTrue(instance.getRowCount() > 0);
        assertTrue(instance.next());
        assertTrue(instance.isFirst());
        assertEquals(TestData.FIRST_REC_NO, instance.getRow());
        assertTrue(instance.next());
        assertFalse(instance.isFirst());
    }

    /**
     * Test of isLast method, of class ShapefileResultSet.
     */
    @Test
    public void testIsLast() throws Exception {
        assertFalse(instance.isLast());
        assumeTrue(instance.getRowCount() > 0);
        assertTrue(instance.last());
        assertTrue(instance.isLast());
        assertFalse(instance.isAfterLast());
        assertEquals(instance.getRowCount(), instance.getRow());
        assertFalse(instance.next());
        assertFalse(instance.isLast());
        assertTrue(instance.isAfterLast());
        assertEquals(0, instance.getRow());
    }

    /**
     * Test of previous method, of class ShapefileResultSet.
     */
    @Test
    public void testPrevious() throws Exception {
        System.out.println("previous");

        int lastRow = 0;
        while (instance.next()) {
            lastRow = instance.getRow();
        }
        assertFalse(instance.isLast());
        assumeTrue(instance.getRowCount() > 0);
        assertTrue(instance.isAfterLast());
        lastRow = instance.getRowCount() + 1;
        long startTimeMillis = System.currentTimeMillis();
        while (instance.previous()) {
            assertFalse(instance.isBeforeFirst());
            assertFalse(instance.isAfterLast());

            int thisRow = instance.getRow();
            assertEquals(lastRow - 1, thisRow);

            // Read an attribute to force the record buffer to be updated/moved.
            assertNotNull(instance.getString(1));
            assertEquals(lastRow - 1, instance.getRow());

            System.out.println("   Row: " + thisRow);
            lastRow = thisRow;
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
        assertFalse(instance.isFirst());
        assertTrue(instance.isBeforeFirst());
        assertFalse(instance.isAfterLast());
    }

    /**
     * Test of first method, of class ShapefileResultSet.
     */
    @Test
    public void testFirst() throws Exception {
        System.out.println("first");
        assertTrue(instance.isBeforeFirst());
        assertFalse(instance.isFirst());
        assumeTrue(instance.getRowCount() > 0);
        assertTrue(instance.first());
        assertTrue(instance.isFirst());
        assertFalse(instance.isBeforeFirst());
        assertFalse(instance.isAfterLast());

        assertTrue(instance.next());
        assertFalse(instance.isFirst());

        assertTrue(instance.first());
        assertTrue(instance.isFirst());
    }

    /**
     * Test of last method, of class ShapefileResultSet.
     */
    @Test
    public void testLast() throws Exception {
        System.out.println("last");
        assertFalse(instance.isFirst());
        assertFalse(instance.isLast());

        assumeTrue(instance.getRowCount() > 0);
        assertTrue(instance.last());
        assertFalse(instance.isFirst());
        assertTrue(instance.isLast());

        assertTrue(instance.previous());
        assertFalse(instance.isLast());

        assertTrue(instance.last());
        assertTrue(instance.isLast());

        assertTrue(instance.last());
        assertTrue(instance.isLast());
    }

    /**
     * Test of getRow method, of class ShapefileResultSet.
     */
    @Test
    public void testGetRow() throws Exception {
        System.out.println("getRow");
        while (instance.next()) {
            assertTrue(instance.getRow() >= TestData.FIRST_REC_NO);
            assertTrue(instance.getRow() <= TestData.LAST_REC_NO);
        }
    }

    /**
     * Test of absolute method, of class ShapefileResultSet.
     */
    @Test
    public void testAbsolute() throws Exception {
        System.out.println("absolute");
        Random rand = new Random();
        long startTimeMillis = System.currentTimeMillis();
        int lastRecNo = instance.getRowCount();
        assumeTrue(instance.getRowCount() > 0);
        for (int i = 0; i <= 1000; i++) {
            int recNo = rand.nextInt(lastRecNo) + FIRST_REC_NO;
            assertTrue(instance.absolute(recNo));
            assertEquals(recNo, instance.getRow());

            // Read an attribute to force the record buffer to be updated/moved.
            assertNotNull(instance.getString(1));
            assertEquals(recNo, instance.getRow());

        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    /**
     * Test of relative method, of class ShapefileResultSet.
     */
    @Test
    public void testRelative() throws Exception {
        System.out.println("relative");
        assumeTrue(instance.getRowCount() > 0);
        int rowCount = instance.getRowCount();
        int ROWS_TO_SKIP = 7;
        assertTrue(instance.last());
        assertTrue(instance.relative(-ROWS_TO_SKIP));
        assertTrue(instance.getRow() == rowCount - ROWS_TO_SKIP);
        assertTrue(instance.relative(0));
        assertTrue(instance.getRow() == rowCount - ROWS_TO_SKIP);
        assertTrue(instance.relative(ROWS_TO_SKIP));
        assertTrue(instance.isLast());
        assertTrue(instance.relative(-(rowCount - 1)));
        assertTrue(instance.isFirst());
        assertTrue(instance.relative(rowCount - 1));
        assertTrue(instance.isLast());

        assertFalse(instance.relative(1));
        assertTrue(instance.isAfterLast());
        assertFalse(instance.isLast());
        assertFalse(instance.isBeforeFirst());

        assertTrue(instance.first());
        assertTrue(instance.isFirst());
        assertFalse(instance.relative(-1));
        assertTrue(instance.isBeforeFirst());
        assertFalse(instance.isFirst());

    }

    /**
     * Test of getMetaData method, of class ShapefileResultSet.
     */
    @Test
    public void testGetMetaData() throws Exception {
        System.out.println("getMetaData");
        GisResultSetMetaData metaData = (GisResultSetMetaData) instance.getMetaData();
        assertNotNull(metaData);
        System.out.println("   Shape Type: " + metaData.getShapeTypeName());
        System.out.println("   Column Count: " + metaData.getColumnCount());
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            System.out.println("   Column " + i + ": " + columnName + "[" + metaData.getColumnTypeName(i) + " Len: " + metaData.getPrecision(i) + "." + metaData.getScale(i) + "]");
        }

    }

    /**
     * Test of findColumn method, of class ShapefileResultSet.
     */
    @Test
    public void testFindColumn_String() throws Exception {
        System.out.println("findColumn");

        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            int columnIndex = instance.findColumn(columnName);
            assertEquals(i, columnIndex);
        }
    }

    /**
     * Test of findColumn method, of class ShapefileResultSet.
     */
    @Test
    public void testFindColumn_int() throws Exception {
        System.out.println("findColumn");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = instance.findColumn(i);
            assertEquals(metaData.getColumnName(i), columnName);
        }
    }

    /**
     * Test of getString method, of class ShapefileResultSet.
     */
    @Test
    public void testGetString_int() throws Exception {
        System.out.println("getString");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();

        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String value = instance.getString(i);
                if (instance.isFirst() || instance.isLast()) {
                    print(metaData, i, value);
                }
            }
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
        try {
            instance.getString(0);
            fail("SQLException not thrown.");
            instance.getString(columnCount + 1);
            fail("SQLException not thrown.");
        }
        catch (SQLException e) {
        }
    }

    /**
     * Test of getString method, of class ShapefileResultSet.
     */
    @Test
    public void testGetString_String() throws Exception {
        System.out.println("getString");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();

        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String value = instance.getString(metaData.getColumnName(i));
                if (instance.isFirst() || instance.isLast()) {
                    print(metaData, i, value);
                }
            }
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
        try {
            instance.getString("invalid column name");
            fail("SQLException not thrown.");
        }
        catch (SQLException e) {
        }
    }

    /**
     * Test of getBoolean method, of class ShapefileResultSet.
     */
    @Test
    public void testGetBoolean_int() throws Exception {
        System.out.println("getBoolean");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();
        boolean hasBooleanColumn = false;
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                int columnType = metaData.getColumnType(i);
                if (columnType == Types.BOOLEAN) {
                    boolean value = instance.getBoolean(i);
                    if (instance.isFirst() || instance.isLast()) {
                        print(metaData, i, value);
                    }
                    hasBooleanColumn = true;
                }
                else {
                    try {
                        boolean value = instance.getBoolean(i);
                        fail("SQLException not thrown for column " + i + " with value " + value);
                    }
                    catch (SQLException e) {
                    }
                }
            }
            assumeTrue(hasBooleanColumn);
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));

    }

    /**
     * Test of getBoolean method, of class ShapefileResultSet.
     */
    @Test
    public void testGetBoolean_String() throws Exception {
        System.out.println("getBoolean");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();
        boolean hasBooleanColumn = false;
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                int columnType = metaData.getColumnType(i);
                if (columnType == Types.BOOLEAN) {
                    boolean value = instance.getBoolean(columnName);
                    if (instance.isFirst() || instance.isLast()) {
                        print(metaData, i, value);
                    }
                    hasBooleanColumn = true;
                }
                else {
                    try {
                        boolean value = instance.getBoolean(columnName);
                        fail("SQLException not thrown for column " + columnName + " with value " + value);
                    }
                    catch (SQLException e) {
                    }
                }
            }
            assumeTrue(hasBooleanColumn);
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    /**
     * Test of getLong method, of class ShapefileResultSet.
     */
    @Test
    public void testGetLong_int() throws Exception {
        System.out.println("getLong");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();
        boolean foundColumn = false;
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                int columnType = metaData.getColumnType(i);
                if (columnType == Types.INTEGER || columnType == Types.DOUBLE) {
                    long value = instance.getLong(i);
                    if (instance.isFirst() || instance.isLast()) {
                        //print(metaData, i, value);
                    }
                    foundColumn = true;
                }
                else {
                    try {
                        long value = instance.getLong(i);
                        fail("SQLException not thrown for column " + i + " with value " + value);
                    }
                    catch (SQLException e) {
                    }
                }
            }
            if (!foundColumn) {
                fail("Test inconclusive: did not find a Long column.");
            }
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    /**
     * Test of getLong method, of class ShapefileResultSet.
     */
    @Test
    public void testGetLong_String() throws Exception {
        System.out.println("getLong");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();
        boolean foundColumn = false;
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                int columnType = metaData.getColumnType(i);
                if (columnType == Types.INTEGER || columnType == Types.DOUBLE) {
                    long value = instance.getLong(columnName);
                    if (instance.isFirst() || instance.isLast()) {
                        //print(metaData, i, value);
                    }
                    foundColumn = true;
                }
                else {
                    try {
                        long value = instance.getLong(columnName);
                        fail("SQLException not thrown for column " + columnName + " with value " + value);
                    }
                    catch (SQLException e) {
                    }
                }
            }
            if (!foundColumn) {
                fail("Test inconclusive: did not find a Long column.");
            }
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    /**
     * Test of getDouble method, of class ShapefileResultSet.
     */
    @Test
    public void testGetDouble_int() throws Exception {
        System.out.println("getDouble");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();
        boolean foundColumn = false;
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                int columnType = metaData.getColumnType(i);
                if (columnType == Types.DOUBLE || columnType == Types.INTEGER) {
                    double value = instance.getDouble(i);
                    if (instance.isFirst() || instance.isLast()) {
                        //print(metaData, i, value);
                    }
                    foundColumn = true;
                }
                else {
                    try {
                        double value = instance.getDouble(i);
                        fail("SQLException not thrown for column " + i + " with value " + value);
                    }
                    catch (SQLException e) {
                    }
                }
            }
            if (!foundColumn) {
                fail("Test inconclusive: did not find a Double column.");
            }
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    /**
     * Test of getDouble method, of class ShapefileResultSet.
     */
    @Test
    public void testGetDouble_String() throws Exception {
        System.out.println("getDouble");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();
        boolean foundColumn = false;
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                int columnType = metaData.getColumnType(i);
                if (columnType == Types.DOUBLE || columnType == Types.INTEGER) {
                    double value = instance.getDouble(i);
                    if (instance.isFirst() || instance.isLast()) {
                        //print(metaData, i, value);
                    }
                    foundColumn = true;
                }
                else {
                    try {
                        double value = instance.getDouble(columnName);
                        fail("SQLException not thrown for column " + columnName + " with value " + value);
                    }
                    catch (SQLException e) {
                    }
                }
            }
            if (!foundColumn) {
                fail("Test inconclusive: did not find a Double column.");
            }
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    /**
     * Test of getObject method, of class ShapefileResultSet.
     */
    @Test
    public void testGetObject_int() throws Exception {
        System.out.println("getObject");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                Object object = instance.getObject(i);
                assertNotNull(object);
                assertEquals(metaData.getColumnClassName(i), object.getClass().getName());
            }
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    /**
     * Test of getObject method, of class ShapefileResultSet.
     */
    @Test
    public void testGetObject_String() throws Exception {
        System.out.println("getObject");
        ResultSetMetaData metaData = instance.getMetaData();
        int columnCount = metaData.getColumnCount();
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object object = instance.getObject(columnName);
                assertNotNull(object);
                assertEquals(metaData.getColumnClassName(i), object.getClass().getName());
            }
        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    /**
     * Test of getFeature method, of class ShapefileResultSet.
     */
    @Test
    public void testGetFeature() throws Exception {
        System.out.println("getFeature");
        long startTimeMillis = System.currentTimeMillis();
        while (instance.next()) {
            AbstractFeature feature = (AbstractFeature) instance.getFeature();
            assertNotNull(feature);
            assertNotNull(feature.getGeometry());
            assertNotNull(feature.getGeometry().getExtents());

        }
        System.out.println("Elapsed time: " + msToSecs(System.currentTimeMillis() - startTimeMillis));
    }

    private void print(ResultSetMetaData meta, int columnIndex, Object value) throws SQLException {
        System.out.println("   Column " + columnIndex + ": " + meta.getColumnName(columnIndex)
                + " [" + value.toString() + "]");
    }

    /** Convert a long ("time_t") to seconds and thousandths. */
    private static String msToSecs(long t) {
        return Double.toString(t / 1000D) + " s";
    }
}
