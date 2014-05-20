/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.util;

import java.lang.reflect.ParameterizedType;
import java.net.URL;
import org.openide.util.Lookup;

/**
 * A utility class for working with Java classes.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: ClassUtil.java 524 2013-04-10 00:11:48Z bdschubert $
 */
public class ClassUtil {

    private ClassUtil() {
    }

    /**
     * Gets the parameterized type from a parameterized class.
     *
     * @param clazz
     * @return the type T from the param Class<T>
     */
    public static Class<?> getParameterizedType(Class<?> clazz) {
        Class<?> type = ((Class) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0]);
        return type;
    }

    /**
     * Gets the local resource path from the URL if its found on the local machine's classpath.
     * I.e., it strips the "jar:file:...!/" from the URL and then searches the classpath.
     *
     * @param resourceUrl image address url, possibly referring to different PC or Mac, (e.g.,
     * "jar:file:/Applications/cps.app/Contents/.../com-emxsys-markers-ics.jar!/com/emxsys/markers/ics/resources/Fire_Location24.png"
     * @return a URL string referring to the local resource if found on the local classpath;
     * otherwise, the original address is returned.
     */
    public static URL findLocalResource(String resourceUrl) {
        if (resourceUrl == null) {
            return null;
        }
        int indexOfSep = resourceUrl.indexOf("!/");
        ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
        return classLoader.getResource(indexOfSep == -1 ? resourceUrl : resourceUrl.substring(indexOfSep + 1));
    }
}
