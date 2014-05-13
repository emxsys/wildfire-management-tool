/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.project;

import com.emxsys.wmt.project.nodes.ProjectNode;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 * This class provides the logical view, i.e., Node hierarchy, for the NetBeans Project Manager. An
 * instance of this class should be placed in the project's lookup.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id $
 */
public class WmtProjectLogicalView implements LogicalViewProvider {

    private final WmtProject project;
    private static final Logger logger = Logger.getLogger(WmtProjectLogicalView.class.getName());

    /**
     * The sole constructor.
     */
    public WmtProjectLogicalView(WmtProject project) {
        this.project = project;
    }

    /**
     * This method creates the tree.
     *
     * @return a Node representing the root of the project.
     */
    @Override
    public Node createLogicalView() {
        try {
            // Get the root folder
            DataObject obj = DataObject.find(this.project.getProjectDirectory());
            // Create the root node
            if (obj != null) {
                // Get the native node for the folder ...
                Node root = obj.getNodeDelegate();
                // ... and now create a FilterNode clone that supports Filter.Children
                return new ProjectNode(root, project);
            }
        } catch (DataObjectNotFoundException exception) {
            logger.log(Level.SEVERE, "Unable to initialize the logical view.", exception); //NOI18N
        }
        return Node.EMPTY;
    }

    /**
     * Try to find a given node in the logical view. If some node within the logical view tree has
     * the supplied object in its lookup, it ought to be returned if that is practical. If there are
     * multiple such nodes, the one most suitable for display to the user should be returned. This
     * may be used to select nodes corresponding to files, etc.
     *
     * @param root a root node
     * @param target a target cookie
     * @return a subnode with the cookie, or null if not found.
     */
    @Override
    public Node findPath(Node root, Object target) {
        System.out.println(getClass().getName() + " findPath() stub invoked: returning null"); //NOI18N
        System.out.println(" root arg: " + root.toString()); //NOI18N
        System.out.println(" target arg: " + target.toString()); //NOI18N
        return null;
    }
}
