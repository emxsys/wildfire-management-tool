package com.emxsys.time.api;

import java.time.Duration;
import java.time.ZonedDateTime;

public class FloatingTimeFrame extends BasicTimeFrame {

    public FloatingTimeFrame(ZonedDateTime begin, Duration duration) {
        super(begin, begin.plus(duration));
    }

    /**
     * Sets the begin (and end) of the timeframe. The end is based on the beginning and duration.
     * @param newBegin Beginning of timeframe.
     */
    @Override
    public void setBegin(ZonedDateTime newBegin) {
        super.setBegin(newBegin);
        super.setEnd(newBegin.plus(getDuration()));
    }

    /**
     * Sets the end of the timeframe based on the beginning and duration.
     * @param newDuration Duration of timeframe
     */
    public void setDuration(Duration newDuration) {
        super.setEnd(getBegin().plus(getDuration()));
    }

}
