package com.emxsys.time.api;

import java.time.Duration;
import java.time.ZonedDateTime;

public class DynamicTimeControl implements TimeControl {

    private ZonedDateTime currentTime;
    private FloatingTimeFrame timeFrame;

    public DynamicTimeControl(ZonedDateTime begin, Duration duration, ZonedDateTime current) {
        timeFrame = new FloatingTimeFrame(begin, duration);
        if (!timeFrame.contains(current)) {
            throw new IllegalArgumentException("Constructor(...) : current time (" + current + ") is outside of time frame.");
        }
    }

    @Override
    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    @Override
    public ZonedDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(ZonedDateTime time) {
        if (timeFrame.contains(time)) {
            currentTime = time;
        } else {
            throw new IllegalArgumentException("setCurrentTime() : current time (" + time + ") is outside of time frame.");
        }
    }
}
