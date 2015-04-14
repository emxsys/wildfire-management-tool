<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="java.time.ZonedDateTime"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>WMT RESTful API</title>
    </head>
    <body>
        <%@page import="java.text.DecimalFormat" %>

        <h1>Wildfire Management Tool RESTful API</h1>
        <h2><a href="doc/index.html">API Documentation</a></h3>
        <h3>Example: <a href="rs/fuelmodels">All Fuel Models</a></h3>
        <h3>Example: <a href="rs/fuelmodels/6">Single Fuel Model (6)</a></h3>
        <h3>Example: <a href="rs/sunlight?time=<%= ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) %>&latitude=34.2&longitude=-119.2">Sunlight at KOXR</a></h2>
    </body>
</html>
