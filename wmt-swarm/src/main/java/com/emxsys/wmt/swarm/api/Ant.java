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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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
package com.emxsys.wmt.swarm.api;

import com.emxsys.gis.api.Coord2D;
import java.time.Duration;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bruce Schubert
 */
public abstract class Ant implements Agent {

    private static final Logger logger = Logger.getLogger(Ant.class.getName());
    private boolean alive;
    private boolean working;
    private Goal goal;
    private Coord2D location;
    private Duration elapsedTime = Duration.ZERO;
    private final Environment environment;
    private final ArrayList<Coord2D> coords = new ArrayList<>();
    private static int counter = 0;
    private int id = ++counter;

    public Ant(Goal goal, Coord2D location, Environment environment) {
        this.goal = goal;
        this.location = location;
        this.environment = environment;
        this.coords.add(location);
        this.alive = true;
        this.working = true;
    }

    @Override
    public Goal getGoal() {
        return this.goal;
    }

    public void reset(Goal goal) {
        reset(goal, this.location);
    }

    public void reset(Goal goal, Coord2D location) {
        this.alive = true;
        this.working = true;
        this.goal = goal;
        this.goal.setFinished(false);
        this.elapsedTime = Duration.ZERO;
        switch (goal.getType()) {
            case Forage:
                // Start a new path
                coords.clear();
                coords.add(location);
                break;
        }
    }

    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    @Override
    public Coord2D getLocation() {
        return location;
    }

    @Override
    public void setLocation(Coord2D coord) {
        location = coord;
    }

    @Override
    public void moveTo(Coord2D coord, Duration enrouteTime) {
        location = coord;
        switch (goal.getType()) {
            case Forage:
                // Build a path if foraging
                coords.add(coord);
                elapsedTime.plus(enrouteTime);
                break;
        }
    }

    @Override
    public boolean update() {
        if (goal.isFinished()) {
            return true;
        }
        boolean success = goal.execute(this);
        switch (goal.getType()) {
            case Forage:
                if (success) {
                    if (goal.isFinished()) {
                        // Add the completed path to the environment
                        environment.addPath(new Path(coords, elapsedTime));
                        // Goal completed
                        working = false;
                        logger.log(Level.INFO, "Finished: {0}", this);
                    }
                    return true;
                } else {
                    killMe();
                    return false;
                }
            default:
        }

        return false;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isWorking() {
        return working;
    }

    public void killMe() {
        alive = false;
        logger.log(Level.INFO, "Killed {0}", this);
    }

    @Override
    public String toString() {
        return "Ant{" + "id=" + id + ", alive=" + alive + ", working=" + working + ", coords=" + coords.size() + '}';
    }

    
}
