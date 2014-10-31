package com.emxsys.time.api;

import java.time.ZonedDateTime;

public interface TimeControl {

    ZonedDateTime getCurrentTime();

    TimeFrame getTimeFrame();

}