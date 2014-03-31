///*
// *    Geotools2 - OpenSource mapping toolkit
// *    http://geotools.org
// *    (C) 2002, Geotools Project Managment Committee (PMC)
// *
// *    This library is free software; you can redistribute it and/or
// *    modify it under the terms of the GNU Lesser General Public
// *    License as published by the Free Software Foundation;
// *    version 2.1 of the License.
// *
// *    This library is distributed in the hope that it will be useful,
// *    but WITHOUT ANY WARRANTY; without even the implied warranty of
// *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// *    Lesser General Public License for more details.
// *
// */
//
//package org.geotools.renderer.style;
//
//// J2SE dependencies
//import java.awt.Shape;
//import java.awt.geom.Rectangle2D;
//import java.awt.geom.Point2D;
//import java.util.logging.Logger ; 
//
//import org.opengis.referencing.crs.CoordinateReferenceSystem ; 
//import org.opengis.referencing.operation.TransformException ; 
//
//
//
//import org.geotools.renderer.HeadingScreenTransformer ; 
//import org.geotools.renderer.lite.TransformedShape;
//import org.geotools.resources.Utilities;
//
//
///**
// * Style to dynamically compute the Shape of the Wind Barb Mark, based
// * on the wind speed and rotation.  This class inherits most of it's 
// * functionality from the MarkStyle2D class.  The primary difference
// * is that this class intercepts the Rotation and Size setter methods,
// * redirecting the inputs to the "bearing" and "windSpeed" parameters,
// * respectively.  The getter methods, however, are unaffected.  This 
// * has the effect that bearing and windSpeed can be set by either 
// * the bearing and windSpeed setter methods <i>or</i> by the 
// * rotation and size setters.  The getter methods all behave as expected.
// * This strategy permits the user to query the "direction" of the mark
// * in the CRS applicable to the Feature, OR by the rotation on the screen.
// * 
// * <p>
// * There are three critical attributes of any renderer, and this styler
// * must be aware of all three in order to produce the correct rotation
// * on the screen: 
// * <ol>
// *  <li> The CRS of the feature.
// *  <li> The CRS of the &quot;world&quot;: this is the CRS of the Map, 
// *      sometimes referred to as the &quot;destination&quot; CRS.</li>
// *  <li> The transform from the projected map to the display.</li>
// * </ol>
// *
// * @author Bryce Nordgren / USDA Forest Service
// * @since 2.2
// * @version $Id$
// */
//public class WindBarbStyle2D extends MarkStyle2D {
//
//    // container for all the default shapes.
//    static private Shape[]            shapeCache    = null ; 
//    final static private Logger       LOGGER = 
//        Logger.getLogger("org.geotools.rendering") ; 
//
//    private WindBarbModel             barbModel     = null ; 
//    private WindBarbView              barbView      = null ;
//    private int                       windSpeed     = -1 ; 
//    private double                    bearing       = Double.NaN ; 
//    private HeadingScreenTransformer  headingXform  = null ; 
//    private boolean                   upWind        = false ;
//
//    /**
//     * Creates a wind barb object.
//     */
//    public WindBarbStyle2D(HeadingScreenTransformer xform) { 
//        // configure default mark size, in pixels.
//        super.setSize(20) ; 
//
//        // secure a local reference to the heading transformer.
//        headingXform = xform ; 
//
//        // initialize the wind barb model/view.
//        barbModel = new WindBarbModel() ; 
//        barbView  = new WindBarbView(barbModel) ; 
//    }
//      
//
//    /**
//     * Returns the shape to be used to render the mark
//     *
//     * @return
//     */
//    public Shape getShape() {
//        return barbView.getShape() ; 
//    }
//
//    /**
//     * Returns a shape that can be used to draw the mark at the x, y coordinates
//     * with appropriate rotation and size (according to the current style)
//     *
//     * @param x the x coordinate where the mark will be drawn
//     * @param y the y coordinate where the mark will be drawn
//     *
//     * @return a shape that can be used to draw the mark 
//     */
//    public Shape getTransformedShape(float x, float y) {
//        // retrieve the wind barb shape & store locally
//        Shape barb = barbView.getShape() ; 
//        shape = barb ; 
//
//        Shape retval = null ;
//        if (barb != null) {
//            float barbLen = WindBarbView.getSize() ; 
//            double scale = size / barbLen ;
//
//            // calculate the rotation.
//            calcRotation(x,y) ; 
//
//            // transform the shape via the user's request.
//            TransformedShape ts = new TransformedShape();
//            ts.shape = barb;
//            ts.translate(x, y);
//            ts.rotate(rotation);
//            ts.scale(scale, -scale);
//
//            retval = ts ; 
//        } 
//
//        return retval ;
//    }
//
//    /**
//     * This method calculates the coordinates of x,y in 
//     * feature space, constructs a unit-length direction arrow
//     * at that point, then translates the direction arrow to 
//     * screen space.  The rotation is then calculated from the 
//     * components of the head and tail.
//     * @param x x coordinate of the wind barb
//     * @param y y coordinate of the wind barb.
//     */
//    private void calcRotation(float x, float y) { 
//        // transform location to feature CRS
//        headingXform.setTailScreen(x,y) ; 
//        headingXform.setHeadScreen(x,y-1) ; // ignored.
//        try { 
//            headingXform.transformReverse() ; 
//        } catch (TransformException te) { 
//            LOGGER.warning(
//                "Cannot translate wind barb location back to feature's CRS.");
//            rotation = 0 ; 
//            return ; 
//        }
//
//        // now calculate the wind direction (deg) in feature space.
//        double dir = Math.toDegrees(bearing) ; 
//        if (!upWind) { 
//            dir += 180. ; 
//        }
//
//        // calculate the head of the arrow in feature space.
//        headingXform.setHeadInCrs1(1., dir) ; 
//
//        // transform the direction arrow to screen space.
//        try { 
//            headingXform.transformForward() ; 
//        } catch (TransformException te) { 
//            LOGGER.warning(
//             "Cannot translate wind barb heading from Feature CRS to screen.");
//            rotation = 0 ; 
//            return ; 
//        }
//
//        // and calculate the rotation from the components.
//        Point2D head = headingXform.getHeadScreen() ; 
//        Point2D tail = headingXform.getTailScreen() ; 
//        double dx = head.getX() - tail.getX() ; 
//        double dy = head.getY() - tail.getY() ; 
//        rotation = (float)Math.atan2( dx, dy) ; // backwards because ref is +y
//    }
//
//    /**
//     * Sets the wind direction.  This rotation is an azimuth clockwise from
//     * the Y axis in the feature's CRS.  Units are radians.  The interpretation
//     * of this parameter is dependent on the setting of the upWind attribute.
//     * If upWind is true, the direction indicates the azimuth from which
//     * the wind is blowing.  If false, it indicates the azimuth into which 
//     * the wind is blowing.
//     * <p>
//     * This method calculates the actual rotation of the shape based on 
//     * the current value of the upWind attribute, so make sure that this is 
//     * correct.
//     *
//     * @param windDirection direction of the wind.
//     */
//    public void setRotation(float windDirection) {
//        // save local copy of bearing in feature CRS
//        bearing = windDirection ; 
//    }
//
//    /**
//     * returns the bearing in the feature CRS.  This is in radians clockwise
//     * from the y axis.
//     */
//    public float getBearing() { 
//        return (float)bearing ; 
//    }
//
//    /**
//     * Sets the shape to be used to render the mark
//     * This method is provided for compatibility, but is ignored.
//     * The shape is computed on demand given the value of the 
//     * "size" parameter.
//     *
//     * @param shape
//     */
//    public void setShape(Shape shape) {
//      ; // does nothing
//    }
//
//    /**
//     * This accessor method has been hijacked by the wind speed. 
//     * The "Size" of the wind barb on the display is not currently 
//     * configurable.  
//     *
//     * @param i wind speed in knots.
//     */
//    public void setSize(int i) {
//        barbModel.setSpeed(i) ; 
//    }
//
//    /**
//     * This accessor method has been hijacked by the wind speed. 
//     * The "Size" of the wind barb on the display is not currently 
//     * configurable.  
//     *
//     * @param speed wind speed in knots.
//     */
//    public void setSize(float speed) {
//        barbModel.setSpeed(speed) ; 
//    }
//
//    /**
//     * Returns a string representation of this style.
//     */
//    public String toString() {
//        return Utilities.getShortClassName(this) + '[' + shape + ']';
//    }
//
//    /**
//     * Allows the user to set whether the direction specified is 
//     * &quot;into the wind&quot; or &quot;in the direction of the wind&quot;.
//     * @param upWind true if rotation specifies upwind direction.
//     */
//    public void setUpWind(boolean intoWind) { 
//        this.upWind = upWind ; 
//    }
//
//    /**
//     * Informs user whether directions are upwind or downwind directions.
//     * @return true if rotation specifies upwind direction.
//     */
//    public boolean isUpWind() { 
//        return upWind ;
//    }
//}
