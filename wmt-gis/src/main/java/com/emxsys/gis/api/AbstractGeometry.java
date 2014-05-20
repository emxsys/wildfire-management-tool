/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.gis.api;

import com.emxsys.gis.api.Geometry;
import com.emxsys.gis.api.Part;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import visad.CoordinateSystem;
import visad.ErrorEstimate;
import visad.Gridded2DSet;
import visad.RealTupleType;
import visad.SampledSet;
import visad.UnionSet;
import visad.Unit;

//import ucar.unidata.geoloc.*;
//import ucar.unidata.geoloc.projection.*;
//import ucar.visad.data.MapSet;
/**
 * This abstract class implements the Geometry getLookup() method with a lazy initialization of an
 * AbstractLookup. Derived classes can add and remove content via the {@code content} member.
 *
 * @author Bruce Schubert
 * @version $Id$
 * @see AbstractLookup
 * @see InstanceContent
 */
public abstract class AbstractGeometry implements Geometry {

    private Lookup lookup;
    /**
     * The lookup contents for capabilities and rendering attributes.
     */
    private InstanceContent content = new InstanceContent();

    /**
     * Get the (lazy) lookup for this geometry object. The lookup may contain capabilities and
     * attributes for rendering the Geometry in a viewer. The lookup is initialized on the first use
     * of this method.
     *
     * @return an AbstractLookup containing the capabilities for this object.
     */
    @Override
    public Lookup getLookup() {
        if (this.lookup == null) {
            this.lookup = new AbstractLookup(this.content);
        }
        return this.lookup;
    }

    /**
     * Provides access to the lookup content so derived classes can add/remove items.
     *
     * @return the content used in an AbstractLookup.
     */
    protected InstanceContent getInstanceContent() {
        return this.content;
    }

//    /**
//     * Convert this Geometry to a java.awt.Shape. The data coordinate system
//     * is assumed to be (lat, lon), use the projection to transform points, so
//     * project.latLonToProj(gisFeature(x,y)) -> screen(x,y).
//     *
//     * @param displayProject Projection to use.
//     *
//     * @return shape corresponding to this feature
//     */
//    public Shape getProjectedShape(ProjectionImpl displayProject) {
//        LatLonPointImpl     workL = new LatLonPointImpl();
//        ProjectionPointImpl lastW = new ProjectionPointImpl();
//        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
//                                           getNumPoints());
//
//        boolean showPts =
//            ucar.unidata.util.Debug.isSet("projection.showPoints");
//
//        java.util.Iterator pi = getParts();
//        while (pi.hasNext()) {
//            Part  gp = (Part) pi.next();
//            double[] xx = gp.getX();
//            double[] yy = gp.getY();
//            for (int i = 0; i < gp.getNumPoints(); i++) {
//                workL.set(yy[i], xx[i]);
//                ProjectionPoint pt = displayProject.latLonToProj(workL);
//
//                if (showPts) {
//                    System.out.println("getProjectedShape 1 " + xx[i] + " "
//                                       + yy[i] + " === " + pt.getX() + " "
//                                       + pt.getY());
//                    if (displayProject.crossSeam(pt, lastW)) {
//                        System.out.println("***cross seam");
//                    }
//                }
//
//                if ((i == 0) || displayProject.crossSeam(pt, lastW)) {
//                    path.moveTo((float) pt.getX(), (float) pt.getY());
//                } else {
//                    path.lineTo((float) pt.getX(), (float) pt.getY());
//                }
//
//                lastW.setLocation(pt);
//            }
//        }
//        return path;
//    }
//
//    /**
//     * Convert this Geometry to a java.awt.Shape. The data coordinate system
//     * is in the coordinates of dataProject, and the screen is in the coordinates of
//     * displayProject. So:
//     * displayProject.latLonToProj( dataProject.projToLatLon(gisFeature(x,y))) -> screen(x,y).
//     *
//     * @param dataProject     data Projection to use.
//     * @param displayProject  display Projection to use.
//     * @return shape corresponding to this feature
//     */
//    public Shape getProjectedShape(ProjectionImpl dataProject,
//                                   ProjectionImpl displayProject) {
//        ProjectionPointImpl pt1   = new ProjectionPointImpl();
//        ProjectionPointImpl lastW = new ProjectionPointImpl();
//        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
//                                           getNumPoints());
//
//        boolean showPts =
//            ucar.unidata.util.Debug.isSet("projection.showPoints");
//
//        java.util.Iterator pi = getParts();
//        while (pi.hasNext()) {
//            Part  gp = (Part) pi.next();
//            double[] xx = gp.getX();
//            double[] yy = gp.getY();
//            for (int i = 0; i < gp.getNumPoints(); i++) {
//                pt1.setLocation(xx[i], yy[i]);
//                LatLonPoint     llpt = dataProject.projToLatLon(pt1);
//                ProjectionPoint pt2  = displayProject.latLonToProj(llpt);
//
//                if (showPts) {
//                    System.out.println("getProjectedShape 2 " + xx[i] + " "
//                                       + yy[i] + " === " + pt2.getX() + " "
//                                       + pt2.getY());
//                    if (displayProject.crossSeam(pt2, lastW)) {
//                        System.out.println("***cross seam");
//                    }
//                }
//
//                if ((i == 0) || displayProject.crossSeam(pt2, lastW)) {
//                    path.moveTo((float) pt2.getX(), (float) pt2.getY());
//                } else {
//                    path.lineTo((float) pt2.getX(), (float) pt2.getY());
//                }
//
//                lastW.setLocation(pt2);
//            }
//        }
//        return path;
//    }
    /**
     * Number of points in MapLines
     */
    private int pointCnt = 0;

    public int getMapLinesPointCount() {
        return pointCnt;
    }

    /**
     * Convert a Geometry to a visad.SampledSet, which is either a single Gridded2DSet (if there is
     * only one part to the feature) or a UnionSet of Gridded2DSet (if there are multiple parts).
     * Each Gridded2DSet is a sequence of line segments that is supposed to be drawn as a continuous
     * line.
     *
     * @return UnionSet of Gridded2DSet corresponding to this feature.
     */
    public SampledSet getMapLines() {
        return getMapLines(null);
    }

    /**
     * Convert a Geometry to a visad.SampledSet, which is either a single Gridded2DSet (if there is
     * only one part to the feature) or a UnionSet of Gridded2DSet (if there are multiple parts).
     * Each Gridded2DSet is a sequence of line segments that is supposed to be drawn as a continuous
     * line.
     *
     * @return UnionSet of Gridded2DSet corresponding to this feature.
     */
    public SampledSet getMapLines(Rectangle2D bbox) {
        this.pointCnt = 0;
        if ((getNumParts() == 0) || (getNumPoints() == 0)) {
            return null;
        }
        SampledSet maplines = null;
        List<Gridded2DSet> lines = new ArrayList<>();

        try {
            RealTupleType coordMathType = RealTupleType.SpatialEarth2DTuple;
            Iterator<Part> pi = getParts().iterator();
            while (pi.hasNext()) {
                Part gp = pi.next();
                Iterable<double[]> points = gp.getPoints();

                if (bbox != null) {
                    boolean inBox = false;
                    for (double[] point : points) {
                        if (bbox.contains(point[0], point[1])) {
                            inBox = true;
                            break;
                        }
                    }
                    if (!inBox) {
                        continue;
                    }
                }

                int np = gp.getNumPoints();
                double[] xx = gp.getX();
                double[] yy = gp.getY();

                pointCnt += np;
                float[][] part = new float[2][np];
                for (int i = 0; i < np; i++) {
                    part[0][i] = (float) xx[i];
                    part[1][i] = (float) yy[i];
                }
// BDS - Switched from MapSet to Gridded2DSet
// TODO: Switch back to MapSet when ucar.unidata libs are used.                
//                lines.add(new MapSet(coordMathType, part, np,
//                        (CoordinateSystem) null, (Unit[]) null,
//                        (ErrorEstimate[]) null, false));
                lines.add(new Gridded2DSet(coordMathType, part, np,
                        (CoordinateSystem) null, (Unit[]) null,
                        (ErrorEstimate[]) null, false));
            }

            if (lines.isEmpty()) {
                return null;
            }
            Gridded2DSet[] latLonLines = lines.toArray(new Gridded2DSet[lines.size()]);
            if (latLonLines.length > 1) {
                maplines = new UnionSet(coordMathType, latLonLines,
                        (CoordinateSystem) null,
                        (Unit[]) null,
                        (ErrorEstimate[]) null, false);  // no copy
            }
            else {
                maplines = latLonLines[0];
            }
        }
        catch (visad.VisADException e) {
            e.printStackTrace();
            System.out.println("numParts = " + getNumParts());
        }
        return maplines;
    }
}
