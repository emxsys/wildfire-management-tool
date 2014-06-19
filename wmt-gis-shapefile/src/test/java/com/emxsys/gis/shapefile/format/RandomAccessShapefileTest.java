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
package com.emxsys.gis.shapefile.format;

import com.emxsys.gis.shapefile.TestData;
import gov.nasa.worldwind.formats.shapefile.DBaseField;
import java.io.File;
import org.openide.filesystems.FileObject;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class RandomAccessShapefileTest {

    private static final String SHAPEFILE = TestData.SHAPEFILE;
    private static final String SHAPEFILE_NAME = TestData.SHAPEFILE_NAME;
    private static final int SHAPEFILE_RECS = TestData.SHAPEFILE_RECS;
    private static final int FIRST_REC_NO = 1;
    private static final int LAST_REC_NO = SHAPEFILE_RECS;
    private RandomAccessShapefile instance;
    private FileObject testData;

    public RandomAccessShapefileTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        testData = FileUtil.toFileObject(new File(SHAPEFILE));
        assertNotNull(testData);

        instance = new RandomAccessShapefile(testData);
        assertNotNull(instance);
        int numberOfRecords = instance.getNumberOfRecords();
        assertEquals(SHAPEFILE_RECS, numberOfRecords);
    }

    @After
    public void tearDown() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
        testData = null;
    }

    /**
     * Test of getName method, of class RandomAccessShapefile.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        String expResult = SHAPEFILE_NAME;
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of nextRecord method, of class RandomAccessShapefile.
     */
    @Test
    public void testNextRecord() {
        System.out.println("nextRecord");

        for (int i = 0; i < instance.getNumberOfRecords(); i++) {
            ShapefileRecord result = instance.nextRecord();
            assertNotNull(result);

            int curRecNo = result.getRecordNumber();
            assertEquals(FIRST_REC_NO + i, curRecNo);
        }
        assertFalse(instance.hasNext());
    }

    /**
     * Test of getRecordLength method, of class RandomAccessShapefile.
     */
    @Test
    public void testGetRecordLength() {
        System.out.println("getRecordLength");
        for (int recNo = FIRST_REC_NO; recNo <= LAST_REC_NO; recNo++) {
            int result = instance.getRecordLength(recNo);
            System.out.println("   Record " + recNo + " length: " + result);
            assertTrue(result > 0);
        }
    }

    /**
     * Test of setNextRecordToRead method, of class RandomAccessShapefile.
     */
    @Test
    public void testSetNextRecordToRead() {
        System.out.println("setNextRecordToRead");
        int recNo = 0;
        try {
            for (recNo = FIRST_REC_NO; recNo <= LAST_REC_NO; recNo++) {
                System.out.println("   Setting to record " + recNo);
                instance.setNextRecordToRead(recNo);
            }
        }
        catch (Exception e) {
            fail(e.toString() + " Failure at record number " + recNo);
        }
    }

    /**
     * Test of getCurrentRecordNumber method, of class RandomAccessShapefile.
     */
    @Test
    public void testGetCurrentRecordNumber() {
        System.out.println("getCurrentRecordNumber");
        assertEquals(FIRST_REC_NO, instance.getCurrentRecordNumber());

        while (instance.hasNext()) {
            int currentRecordNumber = instance.getCurrentRecordNumber();
            int recordNumber = instance.nextRecord().getRecordNumber();
            assertEquals(recordNumber, currentRecordNumber);
        }
        assertEquals(RandomAccessShapefile.SHAPEFILE_EOF, instance.getCurrentRecordNumber());
    }

    /**
     * Test of first method, of class RandomAccessShapefile.
     */
    @Test
    public void testFirst() {
        System.out.println("first");

        int currentRecordNumber = instance.getCurrentRecordNumber();
        assertEquals(FIRST_REC_NO, currentRecordNumber);

        boolean result = instance.first();
        currentRecordNumber = instance.getCurrentRecordNumber();
        assertEquals(true, result);
        assertEquals(FIRST_REC_NO, currentRecordNumber);

        // Read a rec
        ShapefileRecord rec = instance.nextRecord();
        int curRecNo = rec.getRecordNumber();
        assertEquals(FIRST_REC_NO, curRecNo);

        // Roll back 
        result = instance.first();
        currentRecordNumber = instance.getCurrentRecordNumber();
        assertEquals(true, result);
        assertEquals(FIRST_REC_NO, currentRecordNumber);

        // Roll forward a few times
        rec = instance.nextRecord();
        curRecNo = rec.getRecordNumber();
        assertEquals(FIRST_REC_NO, curRecNo);

        rec = instance.nextRecord();
        curRecNo = rec.getRecordNumber();
        assertEquals(FIRST_REC_NO + 1, curRecNo);

        result = instance.first();
        currentRecordNumber = instance.getCurrentRecordNumber();
        assertEquals(true, result);
        assertEquals(FIRST_REC_NO, currentRecordNumber);
    }

    /**
     * Test of last method, of class RandomAccessShapefile.
     */
    @Test
    public void testLast() {
        System.out.println("last");

        boolean result = instance.last();
        assertTrue(result);

        int currentRecordNumber = instance.getCurrentRecordNumber();
        assertEquals(LAST_REC_NO, currentRecordNumber);

        ShapefileRecord rec = instance.nextRecord();
        int recordNumber = rec.getRecordNumber();
        assertEquals(LAST_REC_NO, recordNumber);

        currentRecordNumber = instance.getCurrentRecordNumber();
        assertEquals(RandomAccessShapefile.SHAPEFILE_EOF, currentRecordNumber);
    }

    /**
     * Test of next method, of class RandomAccessShapefile.
     */
    @Test
    public void testNext() {
        System.out.println("next");
        boolean result = instance.next();
        assertEquals(true, result);

        result = instance.last();
        assertEquals(true, result);

        result = instance.next();
        assertEquals(false, result);

    }

    /**
     * Test of previous method, of class RandomAccessShapefile.
     */
    @Test
    public void testPrevious() {
        System.out.println("previous");

        ShapefileRecord rec = instance.nextRecord();
        int curRecNo = rec.getRecordNumber();
        assertEquals(FIRST_REC_NO, curRecNo);

        boolean result = instance.previous();
        assertEquals(true, result);

        rec = instance.nextRecord();
        curRecNo = rec.getRecordNumber();
        assertEquals(FIRST_REC_NO, curRecNo);

        result = instance.previous();
        assertEquals(true, result);

        result = instance.previous();
        assertEquals(false, result);

    }

    /**
     * Test of relative method, of class RandomAccessShapefile.
     */
    @Test
    public void testRelative() {
        System.out.println("relative");

        int currentRecordNumber = instance.getCurrentRecordNumber();
        while (currentRecordNumber < LAST_REC_NO) {
            System.out.println("   Current record " + currentRecordNumber);
            assertTrue(instance.relative(1));
            assertEquals(currentRecordNumber + 1, instance.getCurrentRecordNumber());
            currentRecordNumber = instance.getCurrentRecordNumber();
        }
        assertEquals(LAST_REC_NO, instance.getCurrentRecordNumber());
        assertFalse(instance.relative(1));
        assertTrue(instance.hasNext());

        assertTrue(instance.first());
        currentRecordNumber = instance.getCurrentRecordNumber();
        while (currentRecordNumber < LAST_REC_NO - 1) {
            System.out.println("   Current record " + currentRecordNumber);
            assertTrue(instance.relative(2));
            assertEquals(currentRecordNumber + 2, instance.getCurrentRecordNumber());
            currentRecordNumber = instance.getCurrentRecordNumber();
        }

        assertTrue(instance.first());
        assertTrue(instance.relative(10));
        assertEquals(11, instance.getCurrentRecordNumber());
        assertTrue(instance.relative(-2));
        assertEquals(9, instance.getCurrentRecordNumber());
        assertTrue(instance.relative(-8));
        assertEquals(1, instance.getCurrentRecordNumber());

        assertTrue(instance.relative(0));
        assertEquals(1, instance.getCurrentRecordNumber());
    }

    /**
     * Test of absolute method, of class RandomAccessShapefile.
     */
    @Test
    public void testAbsolute() {
        System.out.println("absolute");
        Random rand = new Random();
        for (int i = 0; i <= 1000; i++) {
            // nextInt is normally exclusive of the top value,
            // so add 1 to make it inclusive
            int recNo = rand.nextInt(LAST_REC_NO - FIRST_REC_NO + 1) + FIRST_REC_NO;
            assertTrue(instance.absolute(recNo));
            ShapefileRecord record = instance.nextRecord();
            assertEquals(recNo, record.getRecordNumber());
            Object value = record.getAttributes().getValue("NAME");
            assertNotNull(value);            
            System.out.println("   Record " + recNo + ": " + record.getNumberOfPoints() + " : " + value);
        }

    }

    /**
     * Test of getFields method, of class RandomAccessShapefile.
     */
    @Test
    public void testGetFields() {
        System.out.println("getFields");
        List<DBaseField> result = instance.getFields();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        result.stream().forEach((field) -> {
            System.out.println(field.getName() + ", " + field.getType() + " [" + field.getLength() + "." + field.getDecimals() + "]");
        });
    }

    /**
     * Test of getFieldNames method, of class RandomAccessShapefile.
     */
    @Test
    public void testGetFieldNames() {
        System.out.println("getFieldNames");
        List<String> result = instance.getFieldNames();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        result.stream().forEach((field) -> {
            System.out.println(field);
        });
    }
}
