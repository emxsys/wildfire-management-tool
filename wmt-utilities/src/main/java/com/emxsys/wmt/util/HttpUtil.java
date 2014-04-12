/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * HTTP utility class.
 *
 * @author Bruce Schubert
 */
public class HttpUtil {

    protected static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private static final Logger logger = Logger.getLogger(HttpUtil.class.getName());

    /**
     * Requests a Web service with an HTTP GET.
     *
     * @param urlString the HTTP GET request.
     * @return the web service results.
     * @throws MalformedURLException
     * @throws IOException
     * @throws RuntimeException
     */
    public static String callWebService(String urlString) throws MalformedURLException, IOException {
            if (urlString == null || urlString.isEmpty()) {
                throw new IllegalArgumentException("URL cannot be null or empty.");
            }
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Connection Failed: " + responseMessage + " [" + urlString + "]");
            }
            String contentType = connection.getContentType();
            String charsetName = getCharsetName(contentType);
            return readStreamToString(connection.getInputStream(), charsetName);
    }

    /**
     * Reads an input stream to a String.
     * @param stream the input stream to read
     * @param charsetName if null will default to UTF-8
     * @return The input as a string; closes the InputStream.
     */
    public static String readStreamToString(InputStream stream, String charsetName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, charsetName))) {
            String line;
            while (null != (line = reader.readLine())) {
                sb.append(line).append("\r\n");
            }
        }
        catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sb.toString();
    }

    public static String getCharsetName(String contentType) {

        if (contentType == null || contentType.toLowerCase().indexOf("charset") == -1) {
            return DEFAULT_CHARSET_NAME;
        }

        String[] pairs = contentType.split(";");
        for (String pair : pairs) {
            if (pair.toLowerCase().trim().startsWith("charset")) {
                String[] av = pair.split("=");
                if (av.length > 1 && av[1].trim().length() > 0) {
                    return av[1].trim();
                }
            }
        }
        return DEFAULT_CHARSET_NAME;
    }

    /**
     * Private constructor for utility class.
     */
    private HttpUtil() {

    }
}
