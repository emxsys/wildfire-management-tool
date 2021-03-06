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
package com.emxsys.wmt.globe.ribbons;

import com.terramenta.ribbon.RibbonManager;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;

/**
 * Utility class for working with the Ribbon UI.
 * @author Bruce Schubert
 */
public class RibbonUtil {
    private static final Logger logger = Logger.getLogger(RibbonUtil.class.getName());
    /**
     * Gets the contextual task group with the given title.
     * @param groupTitle The title of the group to locate (case insensitive).
     * @return The task group, or null if not found.
     */
    public static RibbonContextualTaskGroup getContextualTaskGroup(String groupTitle) {
        JRibbon ribbon = Lookup.getDefault().lookup(RibbonManager.class).getRibbon();
        if (ribbon == null) {
            logger.log(WARNING,"getContextualTaskGroup({0}) failed. The Ribbon is null.", groupTitle);
            return null;
        }
        // Find the named task group
        for (int i = 0; i < ribbon.getContextualTaskGroupCount(); i++) {
            RibbonContextualTaskGroup group = ribbon.getContextualTaskGroup(i);
            if (group.getTitle().equalsIgnoreCase(groupTitle)) {
                return group;
            }
        }
        logger.log(SEVERE,"getContextualTaskGroup({0}) failed. Cannot find task pane titled {0}.", groupTitle);
        return null;
    }

    private RibbonUtil() {
    }

}
