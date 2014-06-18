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
import java.time.ZonedDateTime;

/**
 *
 * @author Bruce Schubert
 */
public abstract class GoalForage extends Goal {

    private ZonedDateTime lastTime;

    public enum Validation {

        Invalid, Valid, Success
    };

    public GoalForage() {
    }

    @Override
    public boolean execute(Agent agent) {
        if (isFinished()) {
            return true;
        }
        // Get the time slice (duration) for this iteration
        ZonedDateTime time = agent.getEnvironment().getTime();
        if (lastTime == null) {
            lastTime = time;
        }
        Duration duration = Duration.between(lastTime, time);
        lastTime = time;

        int MAX_TRIES = 20;
        for (int i = 0; i < MAX_TRIES; i++) {

            Coord2D destination = selectDestination(agent, duration);
            Validation result = validateDestination(agent, destination);
            if (result == Validation.Invalid) {
                // Try again!
                continue;
            }
            // Move the agent (ant) - extending the path
            agent.moveTo(destination, duration);

            if (result == Validation.Success) {
                // The search is complete!
                setFinished(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public Type getType() {
        return Type.Forage;
    }

    public abstract Coord2D selectDestination(Agent agent, Duration duration);

    public abstract Validation validateDestination(Agent agent, Coord2D destination);

}
