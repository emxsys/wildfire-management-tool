/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
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
package com.emxsys.wmt.util;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * This utility class provides methods for working with filenames.
 *
 * @author Bruce Schubert
 * @version $Id: FileNameUtil.java 361 2012-11-30 19:21:21Z bdschubert $
 */
public class FileNameUtil
{

    /**
     * Gets a unique filename (without extension) within the specified folder. The filename is
     * encoded with URL-8 encoding.
     *
     * @param folder where the filename is unique.
     * @param basename name that will be appended with a numerical suffix.
     * @param extension filename extension for uniqueness test
     * @return a unique filename with URL-8 encoding (without an extension)
     */
    public static String getUniqueEncodedFilename(FileObject folder, String basename,
        String extension)
    {
        String filename = encodeFilename(basename);
        return FileUtil.findFreeFileName(folder, filename, extension);
    }


    /**
     * Gets the base name from an encoded filename.
     *
     * @param encodedFilename a filename encoded with encodeFilename()
     * @return decoded name without the extension.
     */
    public static String getDecodedBasename(String encodedFilename)
    {
        String name = decodeFilename(encodedFilename);
        return getFilenameWithoutExtension(name);
    }


    /**
     * Encodes a string as legal filename. Uses URL encoding.
     *
     * @param s string to encode
     * @return the string encoded as a URL.
     */
    public static String encodeFilename(String s)
    {
        try
        {
            //String filename = FILENAME_PREFIX + java.net.URLEncoder.encode(s, "UTF-8");
            String filename = java.net.URLEncoder.encode(s, "UTF-8");
            filename = filename.replace("*", "%2A");
            filename = filename.replace("+", "%20");
            return filename;
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 is an unknown encoding!?");
        }
    }


    /**
     * Decodes an encoded filename to readable string. Uses URL encoding.
     *
     * @param filename string to decode
     * @return a readable string suitable for a display name.
     */
    public static String decodeFilename(String filename)
    {
        try
        {
            String s = filename.replace("%2A", "*");
            s = s.replace("%20", "+");
            return java.net.URLDecoder.decode(s, "UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 is an unknown encoding!?");
        }
    }


    public static String getFilenameWithoutExtension(String filename)
    {
        int index = filename.lastIndexOf('.');
        if (index > 0 && index <= filename.length() - 2)
        {
            return filename.substring(0, index);
        }
        return filename;
    }
}
