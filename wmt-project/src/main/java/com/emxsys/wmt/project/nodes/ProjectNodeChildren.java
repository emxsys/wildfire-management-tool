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
package com.emxsys.wmt.project.nodes;

import com.emxsys.wmt.project.WmtProject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * A factory creating the {@link ProjectNode}'s children. Not used, if ProjectNode is using
 * NodeFactorySupport.createCompositeChildren(...)
 *
 * @author Bruce Schubert
 */
public class ProjectNodeChildren extends FilterNode.Children {

    private final WmtProject project;

    public ProjectNodeChildren(Node original, WmtProject project) {
        super(original);
        this.project = project;
    }

    /**
     * Override creation of node representatives for nodes in the mirrored children list.
     *
     * @param node
     * @return a FilterNode representative
     */
    @Override
    protected Node copyNode(Node node) {
        final DataObject dob = node.getLookup().lookup(DataObject.class);
        final FileObject file = dob.getPrimaryFile();
//        if (file.equals(project.getBehaveFolder(true))) {
//            return new FiregroundNode(project.getLookup().lookup(Fireground.class));
//        } else if (file.equals(project.getWeatherFolder(true))) {
//            return new CpsWeatherNode(node);
//        } else
        {
            return new FilterNode(node);
        }
    }

    /**
     * Create nodes representing copies of the original node's children.
     *
     * @param node
     * @return an empty Node array for folders that should be ignored.
     */
    @Override
    protected Node[] createNodes(Node node) {
        final String name = node.getName();
        if (name.equals(WmtProject.CONFIG_FOLDER_NAME)
                || name.equals(WmtProject.DATA_FOLDER_NAME)) //                || name.equals(WmtProject.BEHAVE_DIR))
        {   // returning an empty Node array suppresses these folders.
            return new Node[0];
        } else {
            return super.createNodes(node);
        }
    }
}
