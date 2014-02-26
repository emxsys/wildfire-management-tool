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
package com.emxsys.wmt.gis.api;


/**
 * Spatial representations per ESRI ArcGIS 9.2 Help Text.
 * http://webhelp.esri.com/arcgisdesktop/9.2/index.cfm?TopicName=Feature_class_basics
 * <pre>
 * <ul>
 * <li>Points— Features that are too small to represent as lines or polygons as well as point
 * locations (such as a GPS observations).
 * <li>Lines— Represent the shape and location of geographic objects, such as street centerlines and
 * streams, too narrow to depict as areas. Lines are also used to represent features that have
 * length but no area such as contour lines and boundaries.
 * <li>Polygons— A set of many-sided area features that represent the shape and location of
 * homogeneous feature types such as states, counties, parcels, soil types, and land-use zones.
 * <li>Annotation— Map text including properties for how the text is rendered. For example, in
 * addition to the text string of each annotation, other properties are included such as the shape
 * points for placing the text, its font and point size, and other display properties. Annotation
 * can also be feature-linked and can contain subclasses.
 * <li>Dimensions— A special kind of annotation that shows specific lengths or distances, for
 * example, to indicate the length of a side of a building, a land parcel, or the distance between
 * two features. Dimensions are heavily used in design, engineering, and facilities applications for
 * GIS.
 * <li>Multipoints— Features that are composed of more than one point. Multipoints are often used to
 * manage arrays of very large point collections such as LIDAR point clusters which can contain
 * literally billions of points. Using a single row for such point geometry is not feasible.
 * Clustering these into multipoint rows enables the geodatabase to handle massive point sets.
 * <li>Multipatches— A 3D geometry used to represent the outer surface, or shell, of features that
 * occupy a discrete area or volume in three-dimensional space. Multipatches comprise planar 3D
 * rings and triangles that are used in combination to model a three-dimensional shell. Multipatches
 * can be used to represent anything from simple objects, such as spheres and cubes, or complex
 * objects, such as iso-surfaces and buildings.
 * <li>Null— A null shape, with no geometric data for the shape. Often null shapes are place
 * holders; they are used during shapefile creation and are populated with geometric data soon after
 * they are created.
 * </ul>
 * </pre>
 *
 * @author Bruce Schubert
 * @version $Id: FeatureClass.java 527 2013-04-18 15:01:15Z bdschubert $
 */
public enum FeatureClass
{
    /**
     * Features that are too small to represent as lines or polygons as well as point locations
     * (such as a GPS observations)
     */
    POINT,
    /**
     * Represents the shape and location of geographic objects, such as street centerlines and
     * streams, too narrow to depict as areas. Lines are also used to represent features that have
     * length but no area such as contour lines and boundaries.
     */
    LINE,
    /**
     * A set of many-sided area features that represent the shape and location of homogeneous
     * feature types such as states, counties, parcels, soil types, and land-use zones.
     */
    POLYGON,
    /**
     * Map text including properties for how the text is rendered. For example, in addition to the
     * text string of each annotation, other properties are included such as the shape points for
     * placing the text, its font and point size, and other display properties. Annotation can also
     * be feature-linked and can contain subclasses.
     */
    ANNOTATION,
    /**
     * A special kind of annotation that shows specific lengths or distances, for example, to
     * indicate the length of a side of a building, a land parcel, or the distance between two
     * features. Dimensions are heavily used in design, engineering, and facilities applications for
     * GIS.
     */
    DIMENSION,
    /**
     * Features that are composed of more than one point. Multipoints are often used to manage
     * arrays of very large point collections such as LIDAR point clusters which can contain
     * literally billions of points. Using a single row for such point geometry is not feasible.
     * Clustering these into multipoint rows enables the geodatabase to handle massive point sets.
     */
    MULTIPOINT,
    /**
     * A 3D geometry used to represent the outer surface, or shell, of features that occupy a
     * discrete area or volume in three-dimensional space. Multipatches comprise planar 3D rings and
     * triangles that are used in combination to model a three-dimensional shell. Multipatches can
     * be used to represent anything from simple objects, such as spheres and cubes, or complex
     * objects, such as iso-surfaces and buildings.
     */
    MULTIPATCH,
    /**
     * A null shape, with no geometric data for the shape. Often null shapes are place holders; they
     * are used during shapefile creation and are populated with geometric data soon after they are
     * created.
     */
    UNDEFINED
}
