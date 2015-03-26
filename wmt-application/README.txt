Release Notes
Wildfire Management Tool (WMT)
==============================

Installers:
- WMT_windows-x64_4_0_BETA2A.exe: 	Windows 64bit installer.
- WMT_windows-x64_4_0_BETA2A_JRE.exe: 	Windows 64bit installer with Java 8.
- WMT_windows_4_0_BETA2A.exe: 		Windows 32bit installer.
- WMT_windows_4_0_BETA2A_JRE.exe: 	Windows 32bit installer with Java 8.
- wmt-application-4.0-BETA2A.zip:	No installer -- see bin folder for launchers

Known Issues:
 - Offline mode does not work.  You must have an internet connection.
 - US National Weather Service is the only working weather provider.
 - You must move the globe and the time to activate the weather after it is downloaded.
 - Project fireground analysis do not work. Only point based fire-behavior is enabled.

26-MAR-2015 v4.0 BETA2A
-----------------------
Fixed Bugs:
 - [134] Tactical Symbols do not work when offline (https://bitbucket.org/emxsys/wildfire-management-tool/issue/134/) 
 - [133] Enable Manual Override checkboxes in CPS Forces (https://bitbucket.org/emxsys/wildfire-management-tool/issue/133/) 
 - [128] WMT 4.0 will not launch on Windows 7. (https://bitbucket.org/emxsys/wildfire-management-tool/issue/128/) 
 - [126] Fireground sectors are not displayed on Globe (https://bitbucket.org/emxsys/wildfire-management-tool/issue/126/) 
 - [124] GeoMAC Current Fire Perimeters do not display (https://bitbucket.org/emxsys/wildfire-management-tool/issue/124/) 

Enhancements:
 - [132] Add Tooltip to Fire Shape Ellipse (https://bitbucket.org/emxsys/wildfire-management-tool/issue/132/) 

Completed Tasks:
 - [15] Add Articles to CPS module documentation (https://bitbucket.org/emxsys/wildfire-management-tool/issue/15/) 

07-OCT-2014 v4.0 BETA1
----------------------

Fixed Bugs:
 - [112] Pushpin Markers are INOP (https://bitbucket.org/emxsys/wildfire-management-tool/issue/112/) 
 - [108] Marker icons not being displayed on Globe (https://bitbucket.org/emxsys/wildfire-management-tool/issue/108/) 
 - [106] Enable Fuel Moisure/Fuel Temperature (https://bitbucket.org/emxsys/wildfire-management-tool/issue/106/) 
 - [98] LANDFIRE GetCapabilities fails: Bad Request (https://bitbucket.org/emxsys/wildfire-management-tool/issue/98/) 
 - [92] Establish default time zone options (https://bitbucket.org/emxsys/wildfire-management-tool/issue/92/) 
 - [96] Diurnal weather in early afternoon is wrong. (https://bitbucket.org/emxsys/wildfire-management-tool/issue/96/) 
 - [87] Analyze Fireground fails (https://bitbucket.org/emxsys/wildfire-management-tool/issue/87/) 
 - [86] Inconsistancies in WeatherModel dates (https://bitbucket.org/emxsys/wildfire-management-tool/issue/86/) 
 - [85] NWS Weather Marker throw Unsatisfied Link Error execption (WebView64) not on classpath (https://bitbucket.org/emxsys/wildfire-management-tool/issue/85/) 
 - [84] JavaFX based JFreeCharts have memory leak (https://bitbucket.org/emxsys/wildfire-management-tool/issue/84/) 
 - [82] Spread Direction is sometimes off by 180 degrees (https://bitbucket.org/emxsys/wildfire-management-tool/issue/82/) 
 - [76] FuelBed Reaction Intensity failed unit test for SH9 (https://bitbucket.org/emxsys/wildfire-management-tool/issue/76/) 
 - [74] Behave fails Unit Tests (https://bitbucket.org/emxsys/wildfire-management-tool/issue/74/) 
 - [73] Show Projects and Show Files actions are not prominent (https://bitbucket.org/emxsys/wildfire-management-tool/issue/73/) 
 - [66] Native WorldWind config (layerlist) is not being overridden by custom worldwind.xml (https://bitbucket.org/emxsys/wildfire-management-tool/issue/66/) 
 - [65] WMT Application stalls after converting to Terramenta 2.0 (https://bitbucket.org/emxsys/wildfire-management-tool/issue/65/) 
 - [45] Activated map layers are not displayed on the globe (https://bitbucket.org/emxsys/wildfire-management-tool/issue/45/) 
 - [49] Show > Log and Show > Properties display small icons (https://bitbucket.org/emxsys/wildfire-management-tool/issue/49/) 
 - [47] Fix compound Screenshot buttons (https://bitbucket.org/emxsys/wildfire-management-tool/issue/47/) 
 - [35] New Project does not work (https://bitbucket.org/emxsys/wildfire-management-tool/issue/35/) 
Enhancements:
 - [115] Haul Chart should respect Wildfire UOM preferences (https://bitbucket.org/emxsys/wildfire-management-tool/issue/115/) 
 - [104] Move Terramenta Animate buttons to WMT Home taskbar (https://bitbucket.org/emxsys/wildfire-management-tool/issue/104/) 
 - [91] Use lines instead of splines in weather charts (https://bitbucket.org/emxsys/wildfire-management-tool/issue/91/) 
 - [57] Upgrade WMT project to Terramenta 2.0 (WorldWind 2.0.0) (https://bitbucket.org/emxsys/wildfire-management-tool/issue/57/) 
Completed Tasks:
 - [109] Add Progress Bar to weather forecast/observation downloads (https://bitbucket.org/emxsys/wildfire-management-tool/issue/109/) 
 - [24] Add Maps : GeoMAC (https://bitbucket.org/emxsys/wildfire-management-tool/issue/24/) 
 - [105] Create Installer (https://bitbucket.org/emxsys/wildfire-management-tool/issue/105/) 
 - [18] Add CPS Module (https://bitbucket.org/emxsys/wildfire-management-tool/issue/18/) 
 - [17] Add Wildfire Module (https://bitbucket.org/emxsys/wildfire-management-tool/issue/17/) 
 - [64] Add Weather Top Component (https://bitbucket.org/emxsys/wildfire-management-tool/issue/64/) 
 - [102] Establish Weather units of measure and diurnal options (https://bitbucket.org/emxsys/wildfire-management-tool/issue/102/) 
 - [95] Establish GIS units of measure options (https://bitbucket.org/emxsys/wildfire-management-tool/issue/95/) 
 - [94] Establish wildfire unit of measure options (https://bitbucket.org/emxsys/wildfire-management-tool/issue/94/) 
 - [90] Establish default screen layout (https://bitbucket.org/emxsys/wildfire-management-tool/issue/90/) 
 - [81] Tasks 2014-Jun-30 (https://bitbucket.org/emxsys/wildfire-management-tool/issue/81/) 
 - [80] Add ESRI Shapefile support (https://bitbucket.org/emxsys/wildfire-management-tool/issue/80/) 
 - [67] Customize NetBeans logging (https://bitbucket.org/emxsys/wildfire-management-tool/issue/67/) 
 - [61] Add Fireground Sector Support (https://bitbucket.org/emxsys/wildfire-management-tool/issue/61/) 
 - [60] Add Sector Selection support (https://bitbucket.org/emxsys/wildfire-management-tool/issue/60/) 
 - [54] Add Network Online/Offline toggle button (https://bitbucket.org/emxsys/wildfire-management-tool/issue/54/) 
 - [56] Add Options to CPS module (https://bitbucket.org/emxsys/wildfire-management-tool/issue/56/) 
 - [55] Add Map Cache support (https://bitbucket.org/emxsys/wildfire-management-tool/issue/55/) 
 - [53] Create new SunPosition calculator class (https://bitbucket.org/emxsys/wildfire-management-tool/issue/53/) 
 - [32] Add Weather Marker (https://bitbucket.org/emxsys/wildfire-management-tool/issue/32/) 
 - [34] Add Scenes (https://bitbucket.org/emxsys/wildfire-management-tool/issue/34/) 
Accepted Proposals:
 - [97] BETA1 Release 2014.09.26 (https://bitbucket.org/emxsys/wildfire-management-tool/issue/97/) 
 