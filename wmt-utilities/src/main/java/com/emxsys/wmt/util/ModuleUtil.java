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
package com.emxsys.wmt.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.Lookups;

/**
 * Utilities for interacting with a NetBeans layer file. Layer files are small XML files provided by
 * modules, which define a virtual filesystem. The layer file defines folders and files that will be
 * merged into the system filesystem that makes up the runtime configuration information NetBeans
 * and its modules use.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ModuleUtil {

    private static final Logger logger = Logger.getLogger(ModuleUtil.class.getName());

    /**
     * Retrieves an action instance.
     *
     * @param category e.g., "Maps"
     * @param id e.g., "com-emxsys-worldwind-ribbon-actions-ToggleLayerAction"
     * @return the Action instance or null
     */
    public static Action getAction(String category, String id) {
        final String FOLDER = "Actions/" + category + "/";
        Lookup pathLookup = Lookups.forPath(FOLDER);
        String actionId = id.replace(".", "-");
        Template<Action> actionTemplate = new Template<Action>(Action.class, FOLDER + actionId, null);
        Item<Action> item = pathLookup.lookupItem(actionTemplate);
//        Result<Action> lookupResult = pathLookup.lookup(actionTemplate);
//        Collection<? extends Action> foundActions = lookupResult.allInstances();
        if (item != null) {
            return item.getInstance();
        }
        return null;
    }

    /**
     * Getting a Java object (Action) from an instance file in the XML layer.
     *
     * @param actionConfigFile e.g.,
     * "Actions/Maps/com-emxsys-worldwind-ribbon-actions-ToggleLayerAction.instance"
     * @return the Action instance or null
     */
    public static Action getAction(String actionConfigFile) {
        try {
            FileObject configFile = FileUtil.getConfigFile(actionConfigFile);
            if (configFile != null) {
                DataObject ob = DataObject.find(configFile);
                InstanceCookie ck = ob.getLookup().lookup(InstanceCookie.class);
                if (ck != null) {
                    Action action = (Action) ck.instanceCreate();
                    return action;
                }
            }
        }
        catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static Object getInstance(String configFile) {
        return getInstance(FileUtil.getConfigFile(configFile));
    }

    /**
     * Gets an instance of a Java object from an instance file in the XML layer.
     *
     * @param fo fileObject containing an instance cookie.
     * @return the instance or null
     */
    public static Object getInstance(FileObject fo) {
        try {
            DataObject ob = DataObject.find(fo);
            InstanceCookie ck = ob.getLookup().lookup(InstanceCookie.class);
            if (ck != null) {
                return ck.instanceCreate();
            }
        }
        catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Method that understands the NetBeans nbinst:// URL protocol.
     *
     * @param url a URL that can specify the nbinst:// protocol.
     * @return the file referenced in the URL or null if cannot be created.
     */
    public static File createFileFromUrl(URL url) {
        // Use URLMapper to decode nbinst protocal into an absolute path
        FileObject attribFileObject = URLMapper.findFileObject(url);
        if (attribFileObject != null) {
            return FileUtil.toFile(attribFileObject);
        }
        else {
            logger.log(Level.WARNING, "No FileObject for {0}. ", url);
        }
        return null;
    }

    public static List<FileObject> getSortedChildren(String folderPath) {
        FileObject folder = FileUtil.getConfigFile(folderPath);
        if (folder == null) {
            logger.log(Level.WARNING, "No FileObject for {0}. ", folderPath);
            return new ArrayList<FileObject>();
        }
        FileObject[] unsortedChildren = folder.getChildren();
        List<FileObject> sortedChildren = FileUtil.getOrder(Arrays.asList(unsortedChildren), true);
        return sortedChildren;
    }
}
