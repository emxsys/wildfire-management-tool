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
package com.emxsys.gis.shapefile.filetype;

import com.emxsys.gis.api.data.GisResultSet;
import com.emxsys.gis.api.data.GisResultSetMetaData;
import com.emxsys.gis.shapefile.ShapefileDataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import javax.swing.Action;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class ShapefileDataNode extends DataNode {

    //private final InstanceContent content;
    /**
     * The default DataObject's getLookup() method calls the node's getLookup() -- this constructor
     * will merge the DataObject's lookup with the Node's.
     * @param dataObject the object represented by this node
     * @param dataObjLookup the DataObject's lookup.
     */
    public ShapefileDataNode(ShapefileDataObject dataObject, Lookup dataObjLookup) {
        //this(dataObject, Children.LEAF, dataObjLookup, new InstanceContent());
        // TODO: Suspect problems with proxy lookup... 
        super(dataObject, Children.LEAF, dataObjLookup);
    }

    /**
     * Private constructor to merge the lookups
     * @param dataObject
     * @param kids
     * @param dataObjLookup
     * @param content
     */
//    private ShapefileDataNode(ShapefileDataObject dataObject, Children kids, Lookup dataObjLookup, InstanceContent content)
//    {
//        super(dataObject, kids, new ProxyLookup(dataObjLookup, new AbstractLookup(content)));
//        this.content = content;
//
//        // TODO: Add the default capabilities (which can be removed by derived classes/nodes)
//    }
    /**
     *
     * @param popup - whether to find actions for context meaning or for the node itself (ignored)
     * @return the currently supported actions
     */
    @Override
    public Action[] getActions(boolean popup) {
        //        ArrayList<Action> actions = new ArrayList<Action>();
        //
        //        Viewable viewable = getLookup().lookup(Viewable.class);
        //        if (viewable != null)
        //        {
        //            actions.add(new ViewShapefile(viewable));
        //        }
        //        // Note: can return null Actions for separators
        //return actions.toArray(new Action[actions.size()]);
        SystemAction[] loaderActions = getDataObject().getLoader().getActions();
        Action[] superActions = super.getActions(popup);
        return superActions;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setName("shapefile"); // use a unique name to prevent overwriting existing file-based property set
        set.setDisplayName("Shapefile Properties");

        Sheet.Set set2 = Sheet.createPropertiesSet();
        set2.setName("attributes"); // use a unique name to prevent overwriting existing file-based property set
        set2.setDisplayName("Data Attributes");

        ShapefileDataSource dataSource = getDataObject().getLookup().lookup(ShapefileDataSource.class);
        GisResultSet resultSet = dataSource.getResultSet();
        try {
            GisResultSetMetaData metaData = (GisResultSetMetaData) resultSet.getMetaData();
            set.put(new RecordCountProperty(resultSet));
            set.put(new ShapeTypeProperty(metaData));
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                set2.put(new ColumnTypeProperty(metaData, i));
            }
        }
        catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }

        sheet.put(set);
        sheet.put(set2);

        return sheet;
    }

    private class RecordCountProperty extends ReadOnly<Integer> {

        private int recordCount;

        public RecordCountProperty(GisResultSet resultSet) {
            super("recordCount", Integer.class, "Record Count", "Number of Records");
            try {
                if (resultSet.last()) {
                    this.recordCount = resultSet.getRow();
                    resultSet.first();
                }
            }
            catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public Integer getValue() throws IllegalAccessException, InvocationTargetException {
            return this.recordCount;
        }
    }

    private static class ShapeTypeProperty extends ReadOnly<String> {

        private final String shapeType;

        public ShapeTypeProperty(GisResultSetMetaData metaData) throws SQLException {
            super("shapetype", String.class, "Shape Type", "Feature types contained in this file");
            this.shapeType = metaData.getShapeTypeName();
        }

        @Override
        public String getValue() throws IllegalAccessException,
                InvocationTargetException {
            return this.shapeType;
        }
    }

    private static class ColumnTypeProperty extends ReadOnly<String> {

        private final String columnType;

        public ColumnTypeProperty(GisResultSetMetaData metaData, int column) throws SQLException {
            super(metaData.getColumnName(column), String.class,
                    "Column " + column + ": " + metaData.getColumnName(column), "Column Type");
            this.columnType = metaData.getColumnTypeName(column)
                    + " [" + metaData.getPrecision(column) + "." + metaData.getScale(column) + "]";
        }

        @Override
        public String getValue() throws IllegalAccessException,
                InvocationTargetException {
            return this.columnType;
        }
    }
}
