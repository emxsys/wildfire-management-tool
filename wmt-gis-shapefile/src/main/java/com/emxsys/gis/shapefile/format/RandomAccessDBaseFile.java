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

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.shapefile.DBaseFile;
import gov.nasa.worldwind.util.Logging;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class RandomAccessDBaseFile extends DBaseFile {

    private static final Logger LOG = Logger.getLogger(RandomAccessDBaseFile.class.getName());

    public RandomAccessDBaseFile(FileObject fo) {
        super(FileUtil.toFile(fo));
    }

    public RandomAccessDBaseFile(File file) {
        super(file);
    }

    public void setNextRecordToRead(int recordNo) {
        if (super.channel instanceof FileChannel) {
            super.numRecordsRead = recordNo - 1;
            int newPosition = super.header.headerLength + super.numRecordsRead * this.getRecordLength();
            FileChannel fileChannel = (FileChannel) super.channel;
            try {
                fileChannel.position(newPosition);
            }
            catch (IOException ex) {
                String message = "setNextRecordToRead(" + recordNo + " failed.";
                Logging.logger().severe(message);
                throw new WWRuntimeException(message, ex);
            }
        }
        else {
            String message = "RandomAccessDBaseFile is not using a FileChannel. Random access is not supported.";
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);

        }
    }

    @Override
    protected void initializeFromFile(File file) throws IOException {
        if (!file.exists()) {
            String message = Logging.getMessage("generic.FileNotFound", file.getPath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }

        //this.channel = Channels.newChannel(WWIO.getBufferedInputStream(new FileInputStream(file)));
        super.channel = Channels.newChannel(new FileInputStream(file));
        super.initialize();
    }
}
