/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
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
 * 
 * 
 * Portions of this file were copied from:
 *  Project:      GASCOM - GML Application Schema Object Model
 *  Copyright:    Copyright (c) 2004. Galdos Systems Inc. All rights reserved.
 *  Company:      Galdos Systems Inc., http://www.galdosinc.com
 *  Licensed under the BSD license.
 */
package com.emxsys.wmt.gis.gml;


/**
 *
 * @author Bruce Schubert, Emxsys
 * @author mtrninic, Galdos Systems Inc.
 * @version $Id: GmlConstants.java 466 2012-12-31 14:23:47Z bdschubert $
 */
public class GmlConstants
{

    public static final String GML_NS_URI = "http://www.opengis.net/gml";
    public static final String GML_PREFIX = "gml";
    public static final String SRS_EPSG_4326 = "http://www.opengis.net/gml/srs/epsg.xml#4326";
    public static final String SRS_WGS84 = SRS_EPSG_4326;
	// base GML types
	public static final String ABS_GML_ELEMENT_NAME = "_GML";
	public static final String ABSTRACT_GML_TYPE_NAME = "AbstractGMLType";
    // GML-2 abstract geometry elements
    public static final String ABS_GEOMETRY_ELEMENT_NAME = "_Geometry";
    public static final String ABS_GEOMETRY_COLLECTION_ELEMENT_NAME = "_GeometryCollection";
    // new GML-3 abstract geometry element names
    public static final String ABS_SURFACE_ELEMENT_NAME = "_Surface";
    public static final String ABS_RING_ELEMENT_NAME = "_Ring";
    public static final String ABS_GEOMETRIC_PRIMITIVE_ELEMENT_NAME = "_GeometricPrimitive";
    public static final String ABS_CURVE_ELEMENT_NAME = "_Curve";
    public static final String ABS_CURVE_SEGMENT_ELEMENT_NAME = "_CurveSegment";
    public static final String ABS_GEOMETRIC_AGGREGATE_ELEMENT_NAME = "_GeometricAggregate";
    // GML-2 geometry element names
    public static final String POINT_ELEMENT_NAME = "Point";
    public static final String LINESTRING_ELEMENT_NAME = "LineString";
    public static final String POLYGON_ELEMENT_NAME = "Polygon";
    public static final String LINEARRING_ELEMENT_NAME = "LinearRing";
    public static final String MULTIPOINT_ELEMENT_NAME = "MultiPoint";
    public static final String MULTILINESTRING_ELEMENT_NAME = "MultiLineString";
    public static final String MULTIPOLYGON_ELEMENT_NAME = "MultiPolygon";
    public static final String MULTIGEOMETRY_ELEMENT_NAME = "MultiGeometry";
    // new GML-3 concrete geometry element names
    public static final String BOX_ELEMENT_NAME = "Box";
    public static final String ARC_BY_CENTER_POINT_ELEMENT_NAME = "ArcByCenterPoint";
    public static final String CIRCLE_ELEMENT_NAME = "Circle";
    public static final String MULTISURFACE_ELEMENT_NAME = "MultiSurface";
    public static final String COMPOSITE_SURFACE_ELEMENT_NAME = "CompositeSurface";
    public static final String CUBIC_SPLINE_ELEMENT_NAME = "CubicSpline";
    public static final String ARC_STRING_BY_BULGE_ELEMENT_NAME = "ArcStringByBulge";
    public static final String RING_ELEMENT_NAME = "Ring";
    public static final String COMPOSITE_CURVE_ELEMENT_NAME = "CompositeCurve";
    public static final String ORIENTABLE_SURFACE_ELEMENT_NAME = "OrientableSurface";
    public static final String BSPLINE_ELEMENT_NAME = "BSpline";
    public static final String GEOMETRIC_COMPLEX_ELEMENT_NAME = "GeometricComplex";
    public static final String ORIENTABLE_CURVE_ELEMENT_NAME = "OrientableCurve";
    public static final String ARC_ELEMENT_NAME = "Arc";
    public static final String CIRCLE_BY_CENTER_POINT_ELEMENT_NAME = "CircleByCenterPoint";
    public static final String BEZIER_ELEMENT_NAME = "Bezier";
    public static final String SOLID_ELEMENT_NAME = "Solid";
    public static final String ENVELOPE_ELEMENT_NAME = "Envelope";
    public static final String ARC_STRING_ELEMENT_NAME = "ArcString";
    public static final String COMPOSITE_SOLID_ELEMENT_NAME = "CompositeSolid";
    public static final String ARC_BY_BULGE_ELEMENT_NAME = "ArcByBulge";
    public static final String MULTISOLID_ELEMENT_NAME = "MultiSolid";
    public static final String CURVE_ELEMENT_NAME = "Curve";
    public static final String MULTICURVE_ELEMENT_NAME = "MultiCurve";
    public static final String LINESTRING_SEGMENT_ELEMENT_NAME = "LineStringSegment";
    public static final String SURFACE_ELEMENT_NAME = "Surface";
    // GML-2 abstract geometry type name
    public static final String ABSTRACT_GEOMETRY_TYPE_NAME = "AbstractGeometryType";
    public static final String ABSTRACT_GEOMETRY_COLLECTION_TYPE_NAME = "AbstractGeometryCollectionType";
    // GML-2 geometry type names
    public static final String POINT_TYPE_NAME = "PointType";
    public static final String LINESTRING_TYPE_NAME = "LineStringType";
    public static final String POLYGON_TYPE_NAME = "PolygonType";
    public static final String LINEARRING_TYPE_NAME = "LinearRingType";
    public static final String MULTIPOINT_TYPE_NAME = "MultiPointType";
    public static final String MULTILINESTRING_TYPE_NAME = "MultiLineStringType";
    public static final String MULTIPOLYGON_TYPE_NAME = "MultiPolygonType";
    public static final String MULTIGEOMETRY_TYPE_NAME = "MultiGeometryType";
    // new GML-3 abstract geometry type names
    public static final String ABSTRACT_SURFACE_TYPE_NAME = "AbstractSurfaceType";
    public static final String ABSTRACT_RING_TYPE_NAME = "AbstractRingType";
    public static final String ABSTRACT_GEOMETRIC_PRIMITIVE_TYPE_NAME = "AbstractGeometricPrimitiveType";
    public static final String ABSTRACT_CURVE_TYPE_NAME = "AbstractCurveType";
    public static final String ABSTRACT_CURVE_SEGMENT_TYPE_NAME = "AbstractCurveSegmentType";
    public static final String ABSTRACT_GEOMETRIC_AGGREGATE_TYPE_NAME = "AbstractGeometricAggregateType";
    // new GML-3 concrete geometry type names
    public static final String BOX_TYPE_NAME = "BoxType";
    public static final String ARC_BY_CENTER_POINT_TYPE_NAME = "ArcByCenterPointType";
    public static final String CIRCLE_TYPE_NAME = "CircleType";
    public static final String MULTISURFACE_TYPE_NAME = "MultiSurfaceType";
    public static final String COMPOSITE_SURFACE_TYPE_NAME = "CompositeSurfaceType";
    public static final String CUBIC_SPLINE_TYPE_NAME = "CubicSplineType";
    public static final String ARC_STRING_BY_BULGE_TYPE_NAME = "ArcStringByBulgeType";
    public static final String RING_TYPE_NAME = "RingType";
    public static final String COMPOSITE_CURVE_TYPE_NAME = "CompositeCurveType";
    public static final String ORIENTABLE_SURFACE_TYPE_NAME = "OrientableSurfaceType";
    public static final String BSPLINE_TYPE_NAME = "BSplineType";
    public static final String GEOMETRIC_COMPLEX_TYPE_NAME = "GeometricComplexType";
    public static final String ORIENTABLE_CURVE_TYPE_NAME = "OrientableCurveType";
    public static final String ARC_TYPE_NAME = "ArcType";
    public static final String CIRCLE_BY_CENTER_POINT_TYPE_NAME = "CircleByCenterPointType";
    public static final String BEZIER_TYPE_NAME = "BezierType";
    public static final String SOLID_TYPE_NAME = "SolidType";
    public static final String ENVELOPE_TYPE_NAME = "EnvelopeType";
    public static final String ARC_STRING_TYPE_NAME = "ArcStringType";
    public static final String COMPOSITE_SOLID_TYPE_NAME = "CompositeSolidType";
    public static final String ARC_BY_BULGE_TYPE_NAME = "ArcByBulgeType";
    public static final String MULTISOLID_TYPE_NAME = "MultiSolidType";
    public static final String MULTICURVE_TYPE_NAME = "MultiCurveType";
    public static final String LINESTRING_SEGMENT_TYPE_NAME = "LineStringSegmentType";
    public static final String SURFACE_TYPE_NAME = "SurfaceType";
    public static final String CURVE_TYPE_NAME = "CurveType";
    // GML-2 geometry property types
    public static final String POINT_PROPERTY_TYPE_NAME = "PointPropertyType";
    public static final String LINESTRING_PROPERTY_TYPE_NAME = "LineStringPropertyType";
    public static final String POLYGON_PROPERTY_TYPE_NAME = "PolygonPropertyType";
    public static final String MULTIPOINT_PROPERTY_TYPE_NAME = "MultiPointPropertyType";
    public static final String MULTILINESTRING_PROPERTY_TYPE_NAME = "MultiLineStringPropertyType";
    public static final String MULTIPOLYGON_PROPERTY_TYPE_NAME = "MultiPolygonPropertyType";
    // new GML-3 geometry property types
    public static final String SURFACE_PROPERTY_TYPE_NAME = "SurfacePropertyType";
    public static final String SURFACE_ARRAY_PROPERTY_TYPE_NAME = "SurfaceArrayPropertyType";
    public static final String ABSTRACT_RING_PROPERTY_TYPE_NAME = "AbstractRingPropertyType";
    public static final String LINEAR_RING_PROPERTY_TYPE_NAME = "LinearRingPropertyType";
    public static final String POINT_ARRAY_PROPERTY_TYPE_NAME = "PointArrayPropertyType";
    public static final String CURVE_PROPERTY_TYPE_NAME = "CurvePropertyType";
    public static final String CURVE_ARRAY_PROPERTY_TYPE_NAME = "CurveArrayPropertyType";
    public static final String GEOMETRIC_COMPLEX_PROPERTY_TYPE_NAME = "GeometricComplexPropertyType";
    public static final String CURVE_SEGMENT_ARRAY_PROPERTY_TYPE_NAME = "CurveSegmentArrayPropertyType";
    public static final String MULTIGEOMETRY_PROPERTY_TYPE_NAME = "MultiGeometryPropertyType";
    public static final String MULTICURVE_PROPERTY_TYPE_NAME = "MultiCurvePropertyType";
    public static final String MULTISURFACE_PROPERTY_TYPE_NAME = "MultiSurfacePropertyType";
    public static final String MULTISOLID_PROPERTY_TYPE_NAME = "MultiSolidPropertyType";
    // codes of GML geometries codes (the order must match the order 
    // of respective values in many arrays in this class)
    public static final int UNKNOWN_GEOMETRY_CODE = -1;
    public static final int POINT_CODE = 0;
    public static final int POLYGON_CODE = 1;
    public static final int LINESTRING_CODE = 2;
    public static final int LINEARRING_CODE = 3;
    public static final int MULTIPOINT_CODE = 4;
    public static final int MULTIPOLYGON_CODE = 5;
    public static final int MULTILINESTRING_CODE = 6;
    public static final int BOX_CODE = 7;
    public static final int ARC_BY_CENTER_POINT_CODE = 8;
    public static final int CIRCLE_CODE = 9;
    public static final int MULTISURFACE_CODE = 10;
    public static final int COMPOSITE_SURFACE_CODE = 11;
    public static final int CUBIC_SPLINE_CODE = 12;
    public static final int ARC_STRING_BY_BULGE_CODE = 13;
    public static final int RING_CODE = 14;
    public static final int COMPOSITE_CURVE_CODE = 15;
    public static final int ORIENTABLE_SURFACE_CODE = 16;
    public static final int BSPLINE_CODE = 17;
    public static final int GEOMETRIC_COMPLEX_CODE = 18;
    public static final int ORIENTABLE_CURVE_CODE = 19;
    public static final int ARC_CODE = 20;
    public static final int CIRCLE_BY_CENTER_POINT_CODE = 21;
    public static final int BEZIER_CODE = 22;
    public static final int SOLID_CODE = 23;
    public static final int ENVELOPE_CODE = 24;
    public static final int ARC_STRING_CODE = 25;
    public static final int COMPOSITE_SOLID_CODE = 26;
    public static final int ARC_BY_BULGE_CODE = 27;
    public static final int MULTISOLID_CODE = 28;
    public static final int MULTICURVE_CODE = 29;
    public static final int LINESTRING_SEGMENT_CODE = 30;
    public static final int SURFACE_CODE = 31;
    public static final int MULTIGEOMETRY_CODE = 32;
    // abstract elements	
    public static final int ABS_GEOMETRY_CODE = 33;
    public static final int ABS_GEOMETRY_COLLECTION_CODE = 34;
    public static final int ABS_CURVE_CODE = 35;
    public static final int ABS_SURFACE_CODE = 36;
    public static final int ABS_RING_CODE = 37;
    public static final int ABS_GEOMETRIC_PRIMITIVE_CODE = 38;
    public static final int ABS_CURVE_SEGMENT_CODE = 39;
    public static final int ABS_GEOMETRIC_AGGREGATE_CODE = 40;
    public static final int CURVE_CODE = 41;
    public static final String[] GEOMETRY_TYPE_NAMES =
    {
        POINT_TYPE_NAME,
        POLYGON_TYPE_NAME,
        LINESTRING_TYPE_NAME,
        LINEARRING_TYPE_NAME,
        MULTIPOINT_TYPE_NAME,
        MULTIPOLYGON_TYPE_NAME,
        MULTILINESTRING_TYPE_NAME,
        BOX_TYPE_NAME,
        ARC_BY_CENTER_POINT_TYPE_NAME,
        CIRCLE_TYPE_NAME,
        MULTISURFACE_TYPE_NAME,
        COMPOSITE_SURFACE_TYPE_NAME,
        CUBIC_SPLINE_TYPE_NAME,
        ARC_STRING_BY_BULGE_TYPE_NAME,
        RING_TYPE_NAME,
        COMPOSITE_CURVE_TYPE_NAME,
        ORIENTABLE_SURFACE_TYPE_NAME,
        BSPLINE_TYPE_NAME,
        GEOMETRIC_COMPLEX_TYPE_NAME,
        ORIENTABLE_CURVE_TYPE_NAME,
        ARC_TYPE_NAME,
        CIRCLE_BY_CENTER_POINT_TYPE_NAME,
        BEZIER_TYPE_NAME,
        SOLID_TYPE_NAME,
        ENVELOPE_TYPE_NAME,
        ARC_STRING_TYPE_NAME,
        COMPOSITE_SOLID_TYPE_NAME,
        ARC_BY_BULGE_TYPE_NAME,
        MULTISOLID_TYPE_NAME,
        MULTICURVE_TYPE_NAME,
        LINESTRING_SEGMENT_TYPE_NAME,
        SURFACE_TYPE_NAME,
        MULTIGEOMETRY_TYPE_NAME,
        ABSTRACT_GEOMETRY_TYPE_NAME,
        ABSTRACT_GEOMETRY_COLLECTION_TYPE_NAME,
        ABSTRACT_CURVE_TYPE_NAME,
        ABSTRACT_SURFACE_TYPE_NAME,
        ABSTRACT_RING_TYPE_NAME,
        ABSTRACT_GEOMETRIC_PRIMITIVE_TYPE_NAME,
        ABSTRACT_CURVE_SEGMENT_TYPE_NAME,
        ABSTRACT_GEOMETRIC_AGGREGATE_TYPE_NAME,
        CURVE_TYPE_NAME
    };
    public static final String[] GEOMETRY_ELEMENT_NAMES =
    {
        POINT_ELEMENT_NAME,
        POLYGON_ELEMENT_NAME,
        LINESTRING_ELEMENT_NAME,
        LINEARRING_ELEMENT_NAME,
        MULTIPOINT_ELEMENT_NAME,
        MULTIPOLYGON_ELEMENT_NAME,
        MULTILINESTRING_ELEMENT_NAME,
        BOX_ELEMENT_NAME,
        ARC_BY_CENTER_POINT_ELEMENT_NAME,
        CIRCLE_ELEMENT_NAME,
        MULTISURFACE_ELEMENT_NAME,
        COMPOSITE_SURFACE_ELEMENT_NAME,
        CUBIC_SPLINE_ELEMENT_NAME,
        ARC_STRING_BY_BULGE_ELEMENT_NAME,
        RING_ELEMENT_NAME,
        COMPOSITE_CURVE_ELEMENT_NAME,
        ORIENTABLE_SURFACE_ELEMENT_NAME,
        BSPLINE_ELEMENT_NAME,
        GEOMETRIC_COMPLEX_ELEMENT_NAME,
        ORIENTABLE_CURVE_ELEMENT_NAME,
        ARC_ELEMENT_NAME,
        CIRCLE_BY_CENTER_POINT_ELEMENT_NAME,
        BEZIER_ELEMENT_NAME,
        SOLID_ELEMENT_NAME,
        ENVELOPE_ELEMENT_NAME,
        ARC_STRING_ELEMENT_NAME,
        COMPOSITE_SOLID_ELEMENT_NAME,
        ARC_BY_BULGE_ELEMENT_NAME,
        MULTISOLID_ELEMENT_NAME,
        MULTICURVE_ELEMENT_NAME,
        LINESTRING_SEGMENT_ELEMENT_NAME,
        SURFACE_ELEMENT_NAME,
        MULTIGEOMETRY_ELEMENT_NAME,
        ABS_GEOMETRY_ELEMENT_NAME,
        ABS_GEOMETRY_COLLECTION_ELEMENT_NAME,
        ABS_CURVE_ELEMENT_NAME,
        ABS_SURFACE_ELEMENT_NAME,
        ABS_RING_ELEMENT_NAME,
        ABS_GEOMETRIC_PRIMITIVE_ELEMENT_NAME,
        ABS_CURVE_SEGMENT_ELEMENT_NAME,
        ABS_GEOMETRIC_AGGREGATE_ELEMENT_NAME,
        CURVE_ELEMENT_NAME
    };
    // GML-2 basic geometric properties
    public static final String BOUNDED_BY_PROPERTY_ELEMENT_NAME = "boundedBy";
    public static final String DESCRIPTION_PROPERTY_ELEMENT_NAME = "description";
    public static final String NAME_PROPERTY_ELEMENT_NAME = "name";
    public static final String POINT_PROPERTY_ELEMENT_NAME = "pointProperty";
    public static final String POLYGON_PROPERTY_ELEMENT_NAME = "polygonProperty";
    public static final String LINESTRING_PROPERTY_ELEMENT_NAME = "lineStringProperty";
    public static final String MULTI_POINT_PROPERTY_ELEMENT_NAME = "multiPointProperty";
    public static final String MULTI_LINESTRING_PROPERTY_ELEMENT_NAME = "multiLineStringProperty";
    public static final String MULTI_POYGON_PROPERTY_ELEMENT_NAME = "multiPoloygonProperty";
    // GML-2 basic aliases and roles
    public static final String LOCATION_ELEMENT_NAME = "location";
    public static final String CENTEROF_ELEMENT_NAME = "centerOf";
    public static final String POSITION_ELEMENT_NAME = "position";
    
    // GML-3 coordinates
    public static final String DIRECT_POSITION_TYPE_NAME = "DirectPositionType";
    public static final String VECTOR_TYPE_NAME = "VectorType";
    public static final String COORDINATES_TYPE_NAME = "CoordinatesType";
    public static final String COORD_TYPE_NAME = "CoordType";
    public static final String COORDINATES_ELEMENT_NAME = "coordinates";
    public static final String COORD_ELEMENT_NAME = "coord";
    public static final String POS_ELEMENT_NAME = "pos";
    public static final String POINT_REP_ELEMENT_NAME = "pointRep";
    public static final String POS_LIST_ELEMENT_NAME = "posList";
    public static final String SEGMENTS_ELEMENT_NAME = "segments";
    public static final String FEATURE_MEMBER_PROPERTY_ELEMENT_NAME = "featureMember";
    public static final String GEOMETRY_MEMBER_PROPERTY_ELEMENT_NAME = "geometryMember";
    // coordinates specific
    public static final String COORDINATE_SEPARATOR_ATTR_NAME = "cs";
    public static final String TUPLE_SEPARATOR_ATTR_NAME = "ts";
    public static final String DECIMAL_POINT_ATTR_NAME = "decimal";
    public static final String DEFAULT_COORDINATE_SEPARATOR = ",";
    public static final String DEFAULT_TUPLE_SEPARATOR = " ";
    public static final String DEFAULT_DECIMAL_POINT = ".";
    // multiline specific
    public static final String LINESTRING_MEMBER_ELEMENT_NAME = "lineStringMember";
    // polygon specific
    public static final String EXTERIOR_ELEMENT_NAME = "exterior";
    public static final String INTERIOR_ELEMENT_NAME = "interior";    
    // posList specific
    public static final String DIMENSION_ATTR_NAME = "dimension";
    // orientable curve specific
    public static final String ORIENTATION_ATTR_NAME = "orientation";
    // point specific
    public static final String POINT_MEMBER = "pointMember";
    public static final String POINT_MEMBERS = "pointMembers";
    // surface specific
    public static final String SURFACE_MEMBER = "surfaceMember";
    public static final String SURFACE_MEMBERS = "surfaceMembers";
    // default size of position list (applicable to 'pos' element dimension size)
    public static final int DEFAULT_POSITION_LIST_DIMENSION = 1;
    
    public static final String DEFAULT_POSITION_LIST_SEPARATOR = " ";
    public static final String X_ELEMENT_NAME = "X";
    public static final String Y_ELEMENT_NAME = "Y";
    public static final String Z_ELEMENT_NAME = "Z";
    public static final String GML2_NULL_ELEMENT_NAME = "null";
    public static final String GML3_NULL_ELEMENT_NAME = "Null";
    public static final String NULL_ENUM_VALUE_INAPPLICABLE = "inapplicable";
    public static final String NULL_ENUM_VALUE_UNKNOWN = "unknown";
    public static final String NULL_ENUM_VALUE_WITHHELD = "withheld";
    public static final String NULL_ENUM_VALUE_MISSING = "missing";
    public static final String NULL_ENUM_VALUE_TEMPLATE = "template";
    public static final String SRS_NAME_ATTRIBUTE_NAME = "srsName";
    public static final String FID_ATTR_NAME = "fid";
    public static final String GID_ATTR_NAME = "gid";
    public static final String GML_ID_ATTR_NAME = "id";
}
