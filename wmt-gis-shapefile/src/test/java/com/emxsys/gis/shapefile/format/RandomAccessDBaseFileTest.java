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
import gov.nasa.worldwind.formats.shapefile.DBaseRecord;

import java.util.Random;
import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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
public class RandomAccessDBaseFileTest {

    private static final String DBASEFILE = TestData.DBASEFILE;
    private static final int DBASEFILE_RECS = TestData.SHAPEFILE_RECS;
    private static final int FIRST_REC_NO = 1;
    private static final int LAST_REC_NO = DBASEFILE_RECS;
    private RandomAccessDBaseFile instance;
    private FileObject testData;

    public RandomAccessDBaseFileTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        testData = FileUtil.toFileObject(new File(DBASEFILE));
        assertNotNull(testData);

        instance = new RandomAccessDBaseFile(testData);
        assertNotNull(instance);

        int numberOfRecords = instance.getNumberOfRecords();
        assertEquals(DBASEFILE_RECS, numberOfRecords);

    }

    @After
    public void tearDown() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    /**
     * Test of setNextRecordToRead method, of class RandomAccessDBaseFile.
     */
    @Test
    public void testSetNextRecordToRead() {
        System.out.println("setNextRecordToRead");
        for (int recNo = FIRST_REC_NO; recNo <= LAST_REC_NO; recNo++) {
            instance.setNextRecordToRead(recNo);
            DBaseRecord record = instance.nextRecord();
            assertEquals(recNo, record.getRecordNumber());
            Object value = record.getValue("NAME");
            assertNotNull(value);
            System.out.println("   Record " + recNo + ": " + value);
        }

        Random rand = new Random();
        for (int i = 0; i <= 1000; i++) {
            // nextInt is normally exclusive of the top value,
            // so add 1 to make it inclusive
            int recNo = rand.nextInt(LAST_REC_NO - FIRST_REC_NO + 1) + FIRST_REC_NO;
            instance.setNextRecordToRead(recNo);
            DBaseRecord record = instance.nextRecord();
            assertEquals(recNo, record.getRecordNumber());
            Object value = record.getValue("NAME");
            assertNotNull(value);
            System.out.println("   Record " + recNo + ": " + value);
        }
    }
}
