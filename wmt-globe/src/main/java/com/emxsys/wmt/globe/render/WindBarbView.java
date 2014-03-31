/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package com.emxsys.wmt.globe.render;

import java.util.Map;
import java.util.HashMap;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

/**
 * This class is responsible for understanding the representation of the wind barb contained in the
 * WindBarbModel and translating it to a java.awt.Shape. Shapes produced by this class may
 * optionally be rotated by a specific angle, or scaled to a certain number of display units.
 * @author Bryce Nordgren / USDA Forest Service
 * @version $Id$
 * @since 2.2
 */
public class WindBarbView {

    final private static Map shapeCache = new HashMap();
    final private static float STAFF_LEN = 0.85f;
    final private static float FLAG_ANGLE = 60.f;
    final private static float FLAG_LEN = 0.3f;
    final private static float FLAG_DEL_Y;
    final private static float FLAG_DEL_X;
    final private static float HFLAG_DEL_Y;
    final private static float HFLAG_DEL_X;
    final private static float STEP;
    private double size = Double.NaN;
    private WindBarbModel model = null;

    static {
        double flagAngle = Math.toRadians(FLAG_ANGLE);
        FLAG_DEL_X = (float) (FLAG_LEN * Math.sin(flagAngle));
        FLAG_DEL_Y = (float) (FLAG_LEN * Math.cos(flagAngle));
        HFLAG_DEL_X = FLAG_DEL_X / 2;
        HFLAG_DEL_Y = FLAG_DEL_Y / 2;
        STEP = FLAG_DEL_Y;
    }

    /**
     * Creates a WindBarbView to represent the given model. This model will be used to calculate the
     * appearance of the wind barb.
     */
    public WindBarbView(WindBarbModel model) {
        this.model = model;
    }

    /**
     * Calculates the length of a wind barb from the head to the tail. All wind barbs will be this
     * same length.
     * @return length of a wind barb.
     */
    public static float getSize() {
        return STAFF_LEN + FLAG_DEL_Y;
    }

    /**
     * This is the workhorse which generates the Shapes of the wind barbs.
     */
    private Shape makeBarb() {
        if (model == null) {
            return null;
        }

        Shape barb = null;

        if (!model.isCalm()) {
            // initialize the Geometry.
            GeneralPath geom = new GeneralPath(GeneralPath.WIND_NON_ZERO);

            // draw the "staff"
            geom.moveTo(0, 0);
            geom.lineTo(0, STAFF_LEN);

            // start drawing at the end of the staff.
            float y = STAFF_LEN;

            // draw the pennants
            for (int i = model.getPennants(); i > 0; i--) {
                makePennant(geom, y);
                y -= STEP;
            }

            // draw the full flags
            for (int i = model.getFullFlags(); i > 0; i--) {
                makeFullFlag(geom, y);
                y -= STEP;
            }

            // draw the half flags...
            if (model.isLoneHalfFlag()) {
                y -= STEP;
            }
            if (model.isHalfFlag()) {
                makeHalfFlag(geom, y);
            }

            // set the return value
            barb = geom;
        } else {
            barb = makeCalm();
        }

        return barb;
    }

    private static void makePennant(GeneralPath barb, float y) {
        barb.moveTo(0, y);
        barb.lineTo(FLAG_DEL_X, FLAG_DEL_Y + y);
        barb.lineTo(0, FLAG_DEL_Y + y);
        barb.closePath();
    }

    private static void makeFullFlag(GeneralPath barb, float y) {
        barb.moveTo(0, y);
        barb.lineTo(FLAG_DEL_X, y + FLAG_DEL_Y);
    }

    private static void makeHalfFlag(GeneralPath barb, float y) {
        barb.moveTo(0, y);
        barb.lineTo(HFLAG_DEL_X, y + HFLAG_DEL_Y);
    }

    private Shape makeCalm() {
        Area outer = new Area(new Ellipse2D.Double(-0.3, -0.3, 0.6, 0.6));
        Area inner = new Area(new Ellipse2D.Double(-0.25, -0.25, 0.5, 0.5));

        // hollow out the interior
        outer.subtract(inner);

        return outer;
    }

    /**
     * This method returns the wind barb as a Shape. It is ready to be scaled, translated, or
     * rotated. It manages a cache of wind barb shapes so that each one only has to be made once.
     * @return a shape representing the wind barb.
     */
    public Shape getShape() {
        if (model == null) {
            return null;
        }

        // check to see if we already made this one.
        Object retval = shapeCache.get(model);

        // if not, make it and store a copy in the cache..
        if (retval == null) {
            retval = makeBarb();
            if (retval instanceof GeneralPath) {
                shapeCache.put(model.clone(), ((GeneralPath) retval).clone());
            } else {
                shapeCache.put(model.clone(), ((Area) retval).clone());
            }
        }

        return (Shape) retval;
    }

}
