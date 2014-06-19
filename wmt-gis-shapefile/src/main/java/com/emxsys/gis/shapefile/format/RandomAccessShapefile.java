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
package com.emxsys.gis.shapefile.format;

import gov.nasa.worldwind.formats.shapefile.DBaseField;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class provides random access to a shapefile's features and its attributes.
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class RandomAccessShapefile extends Shapefile {

    public static final int SHAPEFILE_EOF = -1;
    private static final int SHAPEFILE_RECORD_HEADER_LENGTH = 8;
    private FileObject primaryFile;
    private List<DBaseField> fields;
    private List<String> fieldNames;
    private ShapefileRecord record;

    public RandomAccessShapefile(File shpFile) {
        this(FileUtil.toFileObject(shpFile));
    }

    /**
     * This constructor replaces the base class attribute member with our RandomAccessDBaseFile
     * implementation.
     * @param primaryFile the .shp file
     */
    public RandomAccessShapefile(FileObject primaryFile) {
        super(FileUtil.toFile(primaryFile));
        this.primaryFile = primaryFile;

        // Release the Forward-Only DBaseFile implmentation and replace with our Random Access version
        if (super.attributeFile != null) {
            super.attributeFile.close();
        }
        FileObject secondaryFile = FileUtil.findBrother(primaryFile, "dbf");
        if (secondaryFile != null) {
            super.attributeFile = new RandomAccessDBaseFile(FileUtil.toFile(secondaryFile));
        }
    }

    @Override
    public ShapefileRecord nextRecord() {
        return super.nextRecord();
    }

    /**
     * Position the record pointer on the first record. A subsequent call to nextRecord will return
     * the first record's contents and position the record pointer record number 2, or EOF if it
     * doesn't exist.
     * @return true if successful, false otherwise.
     */
    public boolean first() {
        return absolute(1);
    }

    /**
     * Position the record pointer on the last record. A subsequent call to nextRecord will return
     * the last record's contents and position the record pointer at EOF.
     * @return true if successful, false otherwise.
     */
    public boolean last() {
        return absolute(getNumberOfRecords());
    }

    /**
     * Position the record pointer on the next record. A subsequent call to nextRecord will return
     * the record's contents and position the record pointer on the following record, or EOF if it
     * doesn't exist.
     * @return true if successful, false otherwise.
     */
    public boolean next() {
        if (getCurrentRecordNumber() == SHAPEFILE_EOF) {
            return false;
        }
        return absolute(getCurrentRecordNumber() + 1);
    }

    /**
     * Position the record pointer on the previous record. A subsequent call to nextRecord will
     * return the record's contents and position the record pointer on the original record from
     * which previous was called.
     * @return true if successful, false otherwise.
     */
    public boolean previous() {
        if (getCurrentRecordNumber() == SHAPEFILE_EOF) {
            return last();
        }
        return absolute(getCurrentRecordNumber() - 1);
    }

    /**
     * Position the record pointer n records away from the current record. Positive values move
     * forward; negative values move backwards; and zero has no effect.
     * @return true if successful, false otherwise.
     */
    public boolean relative(int relativeNo) {
        return absolute(getCurrentRecordNumber() + relativeNo);
    }

    /**
     * Position the record pointer at the specified one-based record number.
     * @param recordNo the record number to move to. If recordNo is negative, then treat the last
     * record as the base, i.e., 1 is first record and -1 is the last record.
     * @returns true if the record pointer was moved, false otherwise
     */
    public boolean absolute(int recordNo) {
        int targetRecNo = recordNo > 0 ? recordNo : getNumberOfRecords() + recordNo + 1;
        if (targetRecNo > 0 && targetRecNo <= getNumberOfRecords()) {
            setNextRecordToRead(targetRecNo);
            return true;
        }
        return false;
    }

    /**
     * Reposition the record buffer(s) at the specified record number for subsequent reading by the
     * base class.
     * @param recordNo the desired record number.
     */
    protected void setNextRecordToRead(int recordNo) {
        // Reset the base class members to the start of the given record.
        // Use the offset of the desired record as the number of bytes read: 
        //  the index array is ordered [offset 0][length 0][offset 1][length 1]...[offset n][length n]
        int offsetToNextRecBytes = super.index[(recordNo - 1) * 2];
        super.numBytesRead = offsetToNextRecBytes - HEADER_LENGTH;
        super.numRecordsRead = recordNo - 1;

        // Reposition the base class buffer(s) at the desired record
        if (super.mappedShpBuffer != null) {
            super.mappedShpBuffer.rewind();
            super.mappedShpBuffer.position(HEADER_LENGTH + super.numBytesRead);
        }
        else if (super.recordContentBuffer != null) {
            super.recordContentBuffer.rewind();
            super.recordContentBuffer.position(HEADER_LENGTH + super.numBytesRead);
        }

        // Sync the attribute file record pointer to the shapefile
        if (super.attributeFile != null && super.attributeFile instanceof RandomAccessDBaseFile) {
            RandomAccessDBaseFile dbfFile = (RandomAccessDBaseFile) super.attributeFile;
            dbfFile.setNextRecordToRead(recordNo);
        }
    }

    /**
     * Gets the record length for the specified record.
     * @param recordNo
     * @returns the variable length record length.
     */
    protected int getRecordLength(int recordNo) {
        if (super.index == null) {
            return 0;
        }
        // The index array is ordered like this: 
        // [offset rec 1][length rec 1][offset rec 2][length rec 2]...[offset rec n][length rec n] 
        int i = recordNo - 1;
        int contentLenIndex = (i * 2) + 1;
        int recordLength = super.index[contentLenIndex] + SHAPEFILE_RECORD_HEADER_LENGTH;

        return recordLength;
    }

    /**
     * Returns the one-based record number of the record that would be returned by nextRecord.
     *
     * @return the record number or {@link SHAPEFILE_EOF} on EOF or an empty shapefile.
     */
    public int getCurrentRecordNumber() {
        int recNo = super.numRecordsRead + 1;
        if (recNo <= super.getNumberOfRecords()) {
            return recNo;
        }
        else {
            return SHAPEFILE_EOF;
        }
    }

    public List<DBaseField> getFields() {
        if (this.fields == null) {
            this.fields = new ArrayList<>();
            if (this.attributeFile != null) {
                this.fields.addAll(Arrays.asList(this.attributeFile.getFields()));
            }
        }
        return fields;
    }

    public List<String> getFieldNames() {
        if (this.fieldNames == null) {
            this.fieldNames = new ArrayList<>();
            if (super.attributeFile != null) {
                for (DBaseField field : this.attributeFile.getFields()) {
                    this.fieldNames.add(field.getName());
                }
            }
        }
        return this.fieldNames;
    }

    public String getName() {
        return this.primaryFile.getName();
    }
}
