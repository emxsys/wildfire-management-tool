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
package com.emxsys.wmt.globe.analytics;

import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.gis.api.layer.BasicLayerCategory;
import com.emxsys.wmt.gis.api.layer.BasicLayerGroup;
import com.emxsys.wmt.gis.api.layer.BasicLayerType;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.wmt.globe.layers.GisLayerProxy;
import com.emxsys.wmt.time.api.TimeEvent;
import com.emxsys.wmt.time.api.TimeListener;
import com.emxsys.wmt.time.api.TimeProvider;
import com.emxsys.wmt.time.spi.DefaultTimeProvider;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.rmi.RemoteException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import visad.Data;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.LinearLatLonSet;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.VisADException;

/**
 * Particle Analytics draws moving particles on the surface of the globe with comet-like trail. This
 * class was inspired the Wind Map by Fernanda Viégas and Martin Wattenberg at
 * http://hint.fm/projects/wind/
 *
 * @author Bruce Schubert
 */
public class ParticleAnalytics {

    public static final String PREF_PARTICLE_ANALYTICS_NUM_PARTICLES = "particle_analytics_num_particles";
    public static final int DEFAULT_NUM_PARTICLES = 2000;
    private final String layerName;
    private final TimeProvider controller = DefaultTimeProvider.getInstance();
    private SurfaceImage surface;
    private BufferedImage image;
    private Sector sector;
    private ArrayList<Particle> particles;
    private FunctionType temporalFunction;
    private FunctionType spatialFunction;
    private FieldImpl field;
    private RenderableLayer analyticSurfaceLayer;
    private int directionIndex;
    private int velocityIndex;
    private int magnitudeIndex;
    private int numTimeValues;
    // Annimation controllers
    private ZonedDateTime time;
    private ZonedDateTime oldTime;
    private Timer timer;
    private int timeIndex = -1;
    private int oldTimeIndex = 0;
    private int startTimeIndex;
    private int endTimeIndex;
    // Origin and bounds
    double upperLat;
    double leftLon;
    double deltaLatDegrees;
    double deltaLonDegrees;
    int height;
    int width;
    private final TimeListener timerListener = new TimeListener() {

        @Override
        public void updateTime(TimeEvent evt) {
            oldTime = time;
            time = evt.getNewTime();
        }

    };
    // TODO: BDS - refactor to use TimeEvent instead of old TimeController
//    {
//        @Override
//        public void propertyChange(PropertyChangeEvent evt)
//        {
//            if (evt.getPropertyName().equals(TimeControl.PROP_TIMER_INDEX))
//            {
//                oldTimeIndex = timeIndex;
//                timeIndex = (Integer) evt.getNewValue();
//            }
//        }
//    };
    private static final RequestProcessor THREAD_POOL = new RequestProcessor(ParticleAnalytics.class.getName(), 1);
    private static final Logger logger = Logger.getLogger(ParticleAnalytics.class.getName());
    private GisLayerProxy gisLayerAdaptor;

    /**
     * Constructs particle engine analytics object that animates particles based on their direction,
     * speed and magnitude (size).
     *
     * @param data
     * @param directionType
     * @param velocityType
     * @param magnitudeType
     */
    public ParticleAnalytics(String uniqueLayerName, DataImpl data,
                             MathType directionType, MathType velocityType, MathType magnitudeType) {
        if (controller == null) {
            throw new IllegalArgumentException("controller cannot be null!");
        }
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null!");
        }

        logger.log(Level.INFO, "Intializing TemporalSpatialAnalytics for : {0}", data.getType().prettyString());

        try {
            // Validate temporal requirements
            this.temporalFunction = (FunctionType) data.getType();
            if (this.temporalFunction == null
                    || !(this.temporalFunction.getDomain().getComponent(0).equals(RealType.Time))) {
                throw new IllegalArgumentException("data domain type must be RealType.Time : "
                        + data.getType().prettyString());
            }

            // Validate spatial requirements
            this.spatialFunction = (FunctionType) this.temporalFunction.getRange();
            if (this.spatialFunction == null
                    || !(this.spatialFunction.getDomain().equals(RealTupleType.LatitudeLongitudeTuple)
                    || this.spatialFunction.getDomain().equals(GeoCoord2D.DEFAULT_TUPLE_TYPE))) {
                throw new IllegalArgumentException("data range's domain type must be [RealType.Latitude, RealType.Longitude] : "
                        + data.getType().prettyString());
            }

            // Validate spatial range data contains given types
            this.directionIndex = this.spatialFunction.getFlatRange().getIndex(directionType);
            if (directionIndex < 0) {
                throw new IllegalArgumentException("data range does not have supplied type : "
                        + directionType.prettyString() + " : " + data.getType().prettyString());
            }
            this.velocityIndex = this.spatialFunction.getFlatRange().getIndex(velocityType);
            if (velocityIndex < 0) {
                throw new IllegalArgumentException("data range does not have supplied type : "
                        + velocityType.prettyString() + " : " + data.getType().prettyString());
            }
            this.magnitudeIndex = this.spatialFunction.getFlatRange().getIndex(magnitudeType);
            if (magnitudeIndex < 0) {
                throw new IllegalArgumentException("data range does not have supplied type : "
                        + velocityType.prettyString() + " : " + data.getType().prettyString());
            }

            this.layerName = uniqueLayerName;

            this.field = (FieldImpl) data;
            Set timeSet = this.field.getDomainSet();
            this.numTimeValues = timeSet.getLength();

            // Attach to animation controller
            this.controller.addTimeListener(WeakListeners.create(TimeListener.class, this.timerListener, this.controller));

            // Intialize the surface
            initialize();

        } catch (VisADException ex) {
            throw new IllegalStateException(ex.toString());
        }
    }

    /**
     * Initializes the RenderableLayer and SurfaceImage.
     */
    private void initialize() {
        // Create Layer
        this.analyticSurfaceLayer = new RenderableLayer();
        this.analyticSurfaceLayer.setPickEnabled(false);
        this.analyticSurfaceLayer.setName(this.layerName);
        this.analyticSurfaceLayer.setEnabled(true);

        this.gisLayerAdaptor = new GisLayerProxy(analyticSurfaceLayer,
                BasicLayerType.Other,
                BasicLayerGroup.Analytic,
                BasicLayerCategory.Other);

        try {
            // Create SurfaceImage based on the Field dimension

            FlatField spatialField = (FlatField) field.getSample(0);
            LinearLatLonSet spatialSet = (LinearLatLonSet) spatialField.getDomainSet();

            int rows = spatialSet.getLength(0); // latitudes
            int cols = spatialSet.getLength(1); // longitudes

            float[] low = spatialSet.getLow();
            float[] hi = spatialSet.getHi();

            float minLat = low[0];
            float minLon = low[1];
            float maxLat = hi[0];
            float maxLon = hi[1];

            // Initialize the a surface to the data dimensions
            // Note: TYPE_INT_ARGB has transparent background whereas TYPE_INT_RGB draws a black blackground
            // TODO: Make image size relative to eye altitude
            this.image = new BufferedImage(cols * 10, rows * 10, BufferedImage.TYPE_INT_ARGB);
            this.sector = Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
            this.surface = new SurfaceImage(this.image, this.sector);
            this.analyticSurfaceLayer.addRenderable(this.surface);

            // SurfaceImage origin and bounds
            this.upperLat = sector.getMaxLatitude().degrees;
            this.leftLon = sector.getMinLongitude().degrees;
            this.deltaLatDegrees = sector.getDeltaLatDegrees();
            this.deltaLonDegrees = sector.getDeltaLonDegrees();
            this.height = image.getHeight();
            this.width = image.getWidth();

            // TODO: Make number of particles relative to pixel dimensions of sector
            int numParticles = NbPreferences.forModule(this.getClass()).getInt(
                    PREF_PARTICLE_ANALYTICS_NUM_PARTICLES, DEFAULT_NUM_PARTICLES);
            makeParticles(numParticles);

        } catch (VisADException | RemoteException ex) {
            String msg = String.format("initialize() failed! %1s", ex);
            logger.severe(msg);
            throw new RuntimeException(msg, ex);
        }

    }

    public String getLayerName() {
        return layerName;
    }

    /**
     * Creates the particle collection
     *
     * @param count
     */
    private void makeParticles(int count) {
        this.particles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.particles.add(new Particle());
        }
    }

    private void resetParticles() {
        for (Particle p : particles) {
            p.firstTime = true;
            p.initialize();
            p.firstTime = false;
        }
    }

    private void clearImage() {
        Graphics2D g2 = this.image.createGraphics();
        g2.setBackground(new Color(0f, 0f, 0f, 0f));
        g2.clearRect(0, 0, width, height);
        g2.dispose();

    }

    /**
     * Initiates the particle animation.
     *
     * @param intervalMs
     */
    public final void animate(final int intervalMs) {
        // Start the time domain animation
        // FIXME
        //this.controller.run();

        // Start the animation for a given time slice
        if (this.timer != null) {
            this.timer.stop();
        }
        this.timer = new Timer(intervalMs, new ActionListener() {
            private int counter = 0;
            private int lastTimeIndex = 0;
            private FlatField spatialField;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (timeIndex != lastTimeIndex || spatialField == null) {
                        spatialField = (FlatField) field.getSample(timeIndex);
                        if (timeIndex < lastTimeIndex) {
                            clearImage();
                            resetParticles();
                        }
                        lastTimeIndex = timeIndex;
                    }

                    moveParticles(spatialField, directionIndex, velocityIndex, magnitudeIndex, particles);
                    image = drawImage(particles, sector, image);
                    surface.setImageSource(image, sector);

                    analyticSurfaceLayer.firePropertyChange(AVKey.LAYER, null, analyticSurfaceLayer);

                } catch (VisADException | RemoteException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        timer.start();

        // Add the renderable layer to the viewer's layers (and indirectly to the layer manager) 
        Globe.getInstance().addGisLayer(gisLayerAdaptor);

        // Add this instance to the viewer lookup so that actions can find it (and invoke cancel).\
        // FIXME
        //Globe.getInstance().addParticleAnalytics(this);
    }

    public static void cancelAll() {
        Collection<? extends ParticleAnalytics> instances = Globe.getInstance().getLookup().lookupAll(ParticleAnalytics.class);
        for (ParticleAnalytics instance : instances) {
            instance.cancel();
        }
    }

    /**
     *
     */
    public void cancel() {
        timer.stop();
        this.gisLayerAdaptor.setEnabled(false);
        // FIXME
        //ViewerUtil.getViewerFromLookup().removeParticleAnalytics(this);
        Globe.getInstance().removeGisLayer(gisLayerAdaptor);

    }

    /**
     * Move and age the particles based on their position within the spatial field.
     *
     * @param spatialField defines inputs for individual particle movement
     * @param directionIndex tuple index of direction component
     * @param velocityIndex tuple index of speed component
     * @param magnitudeIndex tuple index of size component
     * @param particles to be moved
     */
    static void moveParticles(FlatField spatialField, int directionIndex,
                              int velocityIndex, int magnitudeIndex, List<Particle> particles) {
        for (Particle p : particles) {
            try {
                // TODO: Fire Behavior -- extinquish this particle if the fuel is unburnable

                // Nearest Neighbor should be faster than weighted average.
                // Also, the weighted average method causes some strange reversals of particle streamms.
                RealTuple tuple = (RealTuple) spatialField.evaluate(p.getLatLonTuple(), Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
                Real dir = (Real) tuple.getComponent(directionIndex);
                Real vel = (Real) tuple.getComponent(velocityIndex);
                Real mag = (Real) tuple.getComponent(magnitudeIndex);
                p.move(dir, vel, mag);
            } catch (VisADException | RemoteException ex) {
                logger.log(Level.SEVERE, "moveParticles() failed: {0}", ex.toString());
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Draws the image in a two step process: first, the image is darkened which fades the trails of
     * the previously rendered particles; then the individual particles are rendered.
     *
     * @param particles
     * @param sector defines lat/lon
     * @param image
     */
    static BufferedImage drawImage(final List<Particle> particles, final Sector sector,
                                   final BufferedImage image) {
        // Fade the existing trails of the particles

        BufferedImage newImage = fadeImage(image);

        // Draw the particles
        Graphics2D g2 = newImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR); // or VALUE_INTERPOLATION_BILINEAR
        for (Particle p : particles) {
            p.draw(g2);
        }
        g2.dispose();

        return newImage;
    }

    static BufferedImage fadeImage(final BufferedImage image) {
        // TODO: make darken percentage relative to delay time
        // Create a new image that is darken by a %
        //  offsets adjust brightness
        //  scaleFactors adjust contrast
        float[] scales
                = {
                    0.98f, 0.98f, 0.98f, 0.98f
                };
        float[] offsets = new float[4];
        RescaleOp op = new RescaleOp(scales, offsets, null); // scale factor, offsets, hints
        BufferedImage newImage = op.filter(image, null);
        return newImage;
    }

    /**
     * Particle class maintains the position, age, and renderable properties of a particle
     *
     * @author Bruce Schubert
     */
    class Particle {

        public static final double SCALE = 0.0007;
        public static final int MAX_AGE = 35;
        private double lat;
        private double lon;
        private int x;
        private int y;
        private int age;
        private double dir;
        private double spd;
        private double mag;
        private boolean firstTime = true;

        Particle() {
            initialize();
            this.firstTime = false;
        }

        /**
         * Assigns a random position and age to the particle.
         */
        final void initialize() {

            // Randomly place this particle within the sector bounds
            double dx = Math.random();
            double dy = Math.random();

            this.lat = upperLat - (deltaLatDegrees * dy);
            this.lon = leftLon + (deltaLonDegrees * dx);

            this.x = (int) Math.floor(width * dx);
            this.y = (int) Math.floor(height * dy);

            // TODO: create configuration option for random age vs fixed age...which is better?
            // Random Age Theory: more frequent runs should create a higher density of fading trails
            this.age = (int) (1 + Math.random() * MAX_AGE);  // Random age between 1 and MAX_AGE 

            // TODO: Fix Age particles aren't working correctly: it pulses.  
            // Hunch: I suspect the first path through move() is reseting all the particles
            // Fixed Age Theory: longer runs should create a higher density of fading trails             
            //  Randomizing the age the first time to vary the renew interval
            //this.age = this.firstTime ? (int) (1 + Math.random() * MAX_AGE) : MAX_AGE;  
        }

        /**
         * @return the current lat/lon
         */
        RealTuple getLatLonTuple() {
            // return GeoCoord2D.fromDegrees(this.lat, this.lon);
            try {
                // Create a tuple compatible with the spatial field's domain coordinate system.
                return new RealTuple(RealTupleType.LatitudeLongitudeTuple,
                        new double[]{
                            this.lat, this.lon
                        });
            } catch (VisADException | RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Moves the particle to a new position based on the direction, speed and magnitude.
         *
         * @param direction to move
         * @param speed velocity
         * @param size magnitude
         */
        void move(Real direction, Real speed, Real size) {
            // Missing values can result from moving the particle outside of the sector
            if (direction.isMissing() || speed.isMissing() || size.isMissing()) {
                initialize();
            }
            if (this.age-- > 0) {
                // TODO: make scale relative to delay time

                // Compute the new lat lon coordinates of particle
                Angle distToMove = Angle.fromDegrees(speed.getValue() * SCALE);
                LatLon newPos = Position.linearEndPosition(LatLon.fromDegrees(lat, lon),
                        Angle.fromDegrees(direction.getValue()), distToMove);

                this.lat = newPos.latitude.degrees;
                this.lon = newPos.longitude.degrees;
                this.dir = direction.getValue();    // direction of travel
                this.spd = speed.getValue();     // meters per sec
                this.mag = size.getValue();    // meters

                // Extiquish this particle after drawing if it goes outside the sector
                if (!sector.contains(newPos)) {
                    // Set to zero to allow this cycle draw up to the sector border.
                    this.age = 0;
                }
            }
            // Allow the particle to remain idle (invisible) for awhile before renewing
            if (this.age < -2) {
                initialize();
            }
        }

        /**
         * Draws this particle via a line segment from previous position to new position with a
         * color based on the magnitude.
         *
         * @param g
         */
        void draw(Graphics2D g) {
            // Don't draw if expired
            if (this.age < 0) {
                return;
            }

            // Dtermine the new end point
            double dx = Math.abs(leftLon - this.lon) / deltaLonDegrees;
            double dy = (upperLat - this.lat) / deltaLatDegrees;
            int x2 = (int) Math.floor(width * dx);
            int y2 = (int) Math.floor(height * dy);

            // TODO: pass in a lookup table for colors
            // Compute the color
            Color c;
            if (this.mag > 4.5) // 15 ft
            {
                c = Color.red;// EXTREME
            } else if (this.mag > 2.1) // 7ft
            {
                c = Color.magenta;// VERY ACTIVE
            } else if (this.mag > 0.9) // 3ft
            {
                c = Color.orange;// ACTIVE
            } else if (this.mag > 0.3) // 1ft
            {
                c = Color.green;// MODERAGE
            } else {
                c = Color.blue;// LOW
            }

            // TODO: Vary the the color and/or the alpha component based on velocity
            //  Reducing the alpha component if speed less than 1 m/s (~2.25 mph)
            float[] rgba = c.getRGBComponents(null);
            rgba[3] = (float) Math.min(1.0, Math.max(0.6, this.spd));

            // Draw the particle trail
            g.setColor(new Color(rgba[0], rgba[1], rgba[2], rgba[3]));
            g.drawLine(this.x, this.y, x2, y2);

            // The end point becomes the start point for the next iteration
            this.x = x2;
            this.y = y2;
        }
    };
}
