-----------------------------
RELEASE NOTES FOR VERDI 2.1.6
-----------------------------
**REPLACE ALL PRIOR** versions of VERDI

* Updated netCDF-Java to version 5.5.3 (https://github.com/Unidata/netcdf-java/releases/tag/v5.3.3)

The following new features were added:

* Visualize differences between time-independent model files (#341)

* Reading netCDF files with unconventional dimension names (#344)

* Calculate layer (sum and mean) statistics for all layers or a subset of layers chosen in the dataset panel followed by creation of a new tile plot. (#345)


The following issues were fixed:

* Incorrect values for probed grid cells for statistics "maximum 8hr mean" in tile plot (#325)
* Batch script does not turn off column/row labels and legends as specified in the configuration file (#347)

RELEASE NOTES FOR VERDI 2.1.5
-----------------------------

**REPLACE ALL PRIOR** versions of VERDI with this patched release version.

* Updated OpenJDK to version java 21.0.1 2023-10-17 LTS to fix security vulnerability.

The following new features were added:

* Open and display a MPAS Mesh File (#324)
* Documentation on how to visualize fine scale model output datafiles with GIS layers
* Support sub-domain on a Vertical Cross Section Plot from the command line (#326)

The following issues were fixed.

* Save tile plot as shapefile from GUI (#322)
* MPAS scatterplot tool from GUI (#255)
* Saving and Opening a Project (#318)
* Read zgrid variable from MPAS using VERDI CLI (#257)

The following enhancement was made (#200).

* VERDI saves color values using six digit hex color codes to a saved configuration file and reads configure files using either 32 bit integer codes or the hex color codes.

The following features were depreciated

* Disabled Domain Window including "Metadata Me" (#320)


-----------------------------
RELEASE NOTES FOR VERDI 2.1.4
-----------------------------

**REPLACE ALL PRIOR** versions of VERDI with this patched release version.

* Updated OpenJDK to version 17.0.2 to fix security vulnerability: https://openjdk.java.net/groups/vulnerability/advisories/2021-10-19

The following issues were fixed.

* Ability to display 3-D contour plot (#184, #304)

* Ability to display “zgrid” tile plot for MPAS dataset (#257)
    
* Allow user to modify scatter plot dot size (#260)

* Allow animate plot to be exported as .mp4 movie format (#270)

*  Fix Tile Plot Legend Placement (#307)

-----------------------------
RELEASE NOTES FOR VERDI 2.1.3
-----------------------------

* Updated VERDI to log4j 2.17.1 to fix security vulnerability: https://logging.apache.org/log4j/2.x/index.html


-----------------------------
RELEASE NOTES FOR VERDI 2.1.2
-----------------------------

* Updated VERDI to log4j 2.17 to fix security vulnerability: https://logging.apache.org/log4j/2.x/index.html

* Fixed linux build to use openjdk 16.0.2 2021-07-20.

* Re-enabled the splash screen with automatic version numbering.

* The User Manual for VERDI has not been updated. It will be updated for the next Major Release.

-----------------------------
RELEASE NOTES FOR VERDI 2.1.1
-----------------------------

Hot Patch for VERDI with log4j 2.0.16 to remove message lookup capability to eliminate the log4shell security vulnerability: https://logging.apache.org/log4j/2.x/security.html

Additional bug fixes with this patched version.

* Fixed Polar Stereographic Projection Issue for Tile Plot Display of WRF Files.

-----------------------------
RELEASE NOTES FOR VERDI 2.1
-----------------------------

These notes describe the differences between the VERDI 2.1 release and the previous public release VERDI 2.0 beta

NEW FEATURES IN V2.1
* Enhanced user interface and interactive features related to observation overlays (e.g., preview feature, custom symbols)
* Added support for setting legend levels from a batch script.
* Improved error messaging (when an observation data set does not contain data for the map being shown)
* Updated Java Runtime Environment to OpenJDK 15

BUG FIXES IN 2.1
* Improved geolocation of observation overlay sites.
* Fixed bug where VERDI read map files as observations.
* Fixed bug where map information is missing for certain aspect ratios (plot taller than wide).
* Fixed problem in loading GRIDCRO2D files.
* Fixed bug when VERDI exported WRF and CMAQ gridded data as shapefile
* Added high resolution Map of United States

KNOWN ISSUES IN 2.1
* Display of elevation for vertical cross sections only works for MPAS files.
* Location and thickness of MPAS vertical cross sections are currently in whole degrees of longitude or latitude.
* Contour plot data interpolation and configuration need improvement to reduce spiky appearance.
* Tile plot of WRF data created using the polar stereographic projection (MAP_PROJ=2) is incorrectly plotted - is shifted geographically, LU_INDEX does not match the coastline 

-----------------------------
RELEASE NOTES FOR VERDI V2.0 beta 
------------------------------
These notes describe the differences between the VERDI 2.0 beta release and the previous public release v1.6 alpha

NEW FEATURES IN V2.0 beta:
* Support for MPAS netCDF files.
* Updated world map.
* Option to display elevation for vertical cross-section plots.
* Can specify pixel size of exported raster (e.g., PNG) images.
* New statistics: count, fourth maximum, and custom percentile.
* Preliminary support for MCIP v5.0+ netCDF files.
* Configure plot and set row/column/lat/lon ranges dialogue boxes open on left to avoid blocking view of plot window.

BUG FIXES IN 2.0 beta:
* Fixed geolocation problem.
* Fixed dynamic release of computer memory.
* Fixed interactive plot resizing on Linux when plot is undocked.
* Fixed GIS layer wrap around for global data.
* Fixed several problems when using command line scripts (e.g., wildcards for filenames).

KNOWN ISSUES IN 2.0 beta:
* Display of elevation for vertical cross sections only works for MPAS files.
* Location and thickness of MPAS vertical cross sections are currently in whole degrees of longitude or latitude.
* Contour plot data interpolation and configuration need improvement to reduce spiky appearance.


-----------------------------
RELEASE NOTES FOR VERDI V1.6 alpha
------------------------------

These notes describe the differences between the VERDI 1.6 alpha release and the previous public release v1.5.

NEW FEATURES IN V1.6 alpha:
* GIS Layers in the Tile Plot now use shapefiles directly. 
* The shape2bin conversion program is no longer needed.
* Additional GIS layers can be added via user-provided shapefiles.
* The default GIS layers (i.e. World, North America, USA States, etc.) have been updated with new shapefiles.
* Tile Plot data can be exported as a shapefile.
* GIS layers are now drawn using the GeoTools library, which has improved coordinate reference system handling.
* Many plots now support themes, with new font and color options.
* New plot configuration options have been added, including disabling the legend.
* The Tile Plot has a new default color palette.

BUG FIXES IN V1.6 alpha:
* Fixed handling of blank plot titles.
* Fixed a problem removing files from the Area Files panel.
* Fixed a problem reading saved project files.
* Changed the assumed Earth radius to 6,370 km for data files in Lambert, Mercator, or UTM projections.

KNOWN ISSUES IN V1.6 alpha:
* GIS layer customization is not supported.
* GIS layers don't display properly for polar stereographic projections.
* Shapefiles for areal interpolation must use units of degrees (not meters) and should use the following datum:
* DATUM["unknown", SPHEROID["SPHERE", 6370000.0, 0.0], TOWGS84[0, 0, 0]]


------------------------------
RELEASE NOTES FOR VERDI V1.5
------------------------------

These notes describe the differences between current version 1.5 and the previous public releases of VERDI (version 1.4.1).  The notes describe:

        New Features
        Bug Fixes
        Known Issues
        Build Environment

NEW FEATURES IN V1.5:
* Java3D binaries are provided with the installation.
* All messages are now passed through a single logger.
* Updated underlying libraries to Java 7-compatibility.
* Streamlined installation. Administrator privileges are no longer required to install VERDI.
* Now Released on 64-bit Windows 7 in addition to 32-bit and 64-bit Linux, 32-bit Windows 7 and Mac

BUG FIXES IN V1.5:
* Replaced Vector Plot with Vector Overlay of the Fast Tile Plot.
* Fixed the calculation for areal interpolation for variables that did not have units of deposition (g/ha).
* Fixed the -subdomain option for the batch scripting method.
* Fixed the print output format from the Fast Tile Plot.
* Fixed printing from the contour plot.

KNOWN ISSUES IN V1.5:
* Export to shapefile has been disabled pending a geolocation bug fix.
* Export shapefile from the Areal Interpolation plot provides a shapefile of the gridded data instead of the geographic polygons.
* Using an 8-color bin default, the two middle colors are very similar.
* Periods in the variable name are not accepted by VERDI, need to use _ or other value. (PM2.5 - use PM2_5 as an alternative).
* Formula calculations for mint, maxt, minx, maxx, miny, maxy, minz, maxz do not work.
* Need additional support for user configuration of plots including the Vertical Cross Section, Time Series, Time Series Bar, and Scatter plot.

BUILD ENVIRONMENT FOR V1.5:
* Java 7 - java version "1.7.0_71" Java(TM) SE Runtime Environment (build 1.7.0_71)

------------------------------
RELEASE NOTES FOR VERDI V1.4.1
------------------------------

These notes describe the differences between the previous public releases of VERDI (version 1.4) and the current version 1.4.1. The notes describe:
        New Features
        Bug Fixes
        Known Issues
        Build Environment

NEW FEATURES IN V1.4.1:
* Added configuration capability to the Vertical Cross Section Plot to allow user to set the color palette and minimum and maximum values for the legend.
* Added ASCII observational data supported time zones: EST, EDT, PST, PDT, MST, MDT, CSD, CDT, HAST, HADT, AKST, AKDT, LST, LDT, and GMT/UTC. Previously VERDI had supported only UTC.
* Added option to allow user to specify the base map within the command line script option –mapName.Added ability to use an alphanumeric string value for the Site ID in an ASCII observational dataset.
* Modified the netCDF Java library to include the capability to read additional variables from WRF files (e.g., land use fraction).
* Modified the help documentation within the VERDI GUI, linking to the PDF and html versions of the documentation, rather than an ASCII file, to facilitate searches.

BUG FIXES IN V1.4.1:
* Configuration File: Fixed issue with obtaining correct legend, color scale, and title when using -configFile option in command line script method, and when saving and loading i
nteractively.
* CAMx: Fixed issue with loading CAMx files.
* Fast tile plot: When a configuration file set the lowest value to zero, when switching to a log scale, VERDI incorrectly set all values to zero. To avoid taking the log of zero
, VERDI calculates the log of the full range.
* Fast tile plot, -subDomain xmin ymin xmax ymax: VERDI was relabeling the grid on the starting at (1,1) rather than at (xmin, ymin), which resulted in losing subdomain plot loca
tion within the full domain.
* Fast tile plot, -levelRange indexing issue, -levelRange 1 2: VERDI subsetted the data and provided levels 2 and 3, rather than levels 1 and 2.
* Fixed polar stereographic mapping issue within VERDI by modifying the netCDF Java to remove the M3IOVGGridConvention.java so that these files would be read using the M3IOConven
tion.java convention.
* Areal interpolation plot: Fixed bug with how NaNs are handled.
* Areal interpolation plot: Changed the item "show grid" to "show gridded data".
* Batch Scripting: Hide the splash screen, to increase speed in creating plots.
* Tiff export issue was fixed. (Note: On Windows 32 machines, you may need to install an application that can display tiff images to correctly view the image that VERDI creates.)
* Shapefile export: Fixed a bug in writing shapefiles (bug was identified using Excel and UDig).
* Loading data: Fixed an issue with loading data that had units that contained multiple slashes (for example: kg/ha/m2).
* Loading data: Fixed an issue with loading data that had units strings that were empty.

KNOWN ISSUES IN V1.4.1
* Contour Plot: If the contour plot doesn’t work on Windows 32, install DirectX SDK from http://www.microsoft.com/en-us/download/details.aspx?id=6812
* Script options to specify time within a formula using the following notation are not supported: s=VARNAME[1]:time. For example: for s=PM25_TOT[1]:5-PM25_TOT[2]:5 -g tile, VERDI plots the first hour in the dataset [1] not the 5th hour. The notation -ts=5 does work, but this option does not allow the user to create formulas using multiple time steps such as calculating 8-hour average ozone: s=((O3[1]:1+O3[1]:2+O3[1]:3+O3[1]:4+O3[1]:5+O3[1]:6+O3[1]:7+O3[1]:8)/8).
* Areal interpolation plot: When changing between different views, using Options> Show Area Averages, Options> Show Area Totals, and Options> Show Gridded Data, the min/max values are not updated correctly (always shows the min/max for the gridded data, rather than changing to the option selected by the user).

BUILD ENVIRONMENT FOR V1.4.1:
Java 6 - java version "1.6.0_35" Java(TM) SE Runtime Environment (build 1.6.0_35)
Java HotSpot(TM) Client VM (build 11.3-b02, mixed mode, sharing)


----------------------------
RELEASE NOTES FOR VERDI V1.4
----------------------------

These notes describe the difference between the previous public release of VERDI (version 1.3) and the current version 1.4. The notes describe:
      New Features
      Bug Fixes
      Known Issues
      Build Environment

NEW FEATURES IN V1.4:
New projection types are supported for the fast tile plot (Mercator, UTM, etc.).
Grid cell time-aggregate statistics (e.g., per-cell maximum 8-hour average, hours of noncompliance, etc.) feature is available in fast tile plot.
Batch scripting capability has been added into both the GUI window and the command line.
Adding multiple observational overlay data sets is now supported.
Adding vector overlay data is now supported by the fast tile plot.
Adding and configuring multiple GIS layers are now supported by the fast tile plot.
New remote file access capability has been added to allow users to run VERDI locally and to access data from remote server.
Ability to export CMAQ data and the results of VERDI formulas as ESRI Shapefiles (.shp) and ASCII Grid files (.asc) has been added.
Allowing alternative numeric scale--logarithmic to the fast tile plot is now supported.
All text on fast tile plots is configurable so that the font, size, color, etc. can be changed. Users have the option to turn off the display of a particular text item.
Allowing user to specify a time step with a variable in the formula's panel is now supported.
CSV type and tab-delimited format for observational data are now supported.
Remote hosts list is configurable through config.properties file.
Remote file reading utility program and ssh program path are now configurable through config.properties file.
Temporary folder for storing subsetted remote data files is now configurable through config.properties file.
A splash screen has been added at the start of the program.
A minor change to the GUI's look and feel happened due to the change of docking frame libraries.

BUG FIXES IN V1.4:
(The format of the information below is “Bugzilla ID: Description”)
2574: Problem with Map Projections other than Lambert (other map projections are now recognized; this may have been fixed in version 1.2).
3166: Not able to specify a limited layer range within a script; this results in a layer mismatch between two files.
KNOWN ISSUES IN V1.4:
2503: BCON file support is not available.
2640: Bilinear interpolation (smoothing) of data is not available.
3014: VERDI does not support some WRF file formats.
Tile plots are not displayed completely (sometimes the legend or domain label is not visible).
Improve documentation on a blue-red diverging palette that is appropriate for some types of plots.
Move the metadata button to a better place (perhaps to the popup menu for the dataset).
Pan function should be added to fast tile plot.
MPEG movie export is not available.
Slow down factor: Would be nice to have it register the value without having to hit enter.
Add kmz export for gridded data.
Vector plots give a sense of direction but not magnitude.
Make formulas independent of the input files timestamp; example> allowing monthly average PM2.5 to be added and averaged over 3 consecutive months (3 different files) with different I/O API date stamps.
Need capability to reverse the color scheme in the tile plot configuration menu.
Support vector overlay with magnitude for fast tile plot
Subdomain selected from within the domain editor starts with values of 1 for x and y domain axis on fast tile plot, rather than keeping the range value of the larger domain.
Batch script does not support multiple time steps, layers, and subset domains.
Areal interpolation calculation assumes units of concentration per meter squared for the gridded data. Need to provide unit checking, and conversion (i.e., concentration interpolated using an area file to concentration per area to support areal interpolation of air quality data in addition to water deposition data.)

BUILD ENVIRONMENT FOR V1.4:
Java 6 - java version "1.6.0_13" Java(TM) SE Runtime Environment (build 1.6.0_13-b03)
Java HotSpot(TM) Client VM (build 11.3-b02, mixed mode, sharing)


----------------------------
RELEASE NOTES FOR VERDI V1.3
----------------------------

These notes describe the difference between the previous public releases of VERDI (version 1.2) and version 1.3. The notes describe:
      New Features
      Bug Fixes
      Known Issues
      Build Environment

NEW FEATURES IN V1.3:
Fast tile plot supports adding multiple observational overlay data with 6 symbols and dataset names listed in subtitle 1.
Edit, save, and run batch text format scripts using the script editor within VERDI or at the command line.
Fast tile plot maps are configurable (color, transparency, line thickness).
The edit domain dialog window now contains a base map to facilitate selection of the subdomain range of interest.
Supports remote file access.Supports additional projections (Mercator, UTM, polar stereographic).
Fixed the number format for the fast tile plot legend.

BUG FIXES IN V1.3:(The format of the information below is “Bugzilla ID: Description”)
2554: Configure size and color and shape of observational overlay symbols.3401: Missing values incorrectly represented on fast tile plot legend display of minimum, maximum range (fixed for fast tile plot, not for regular tile plot).
2574: Problem with map projections other than Lambert (other map projections are now recognized; this may have been fixed in version 1.2).
2968: State and county maps do not match the lines of the North America map.Probe returns incorrect X,Y coordinate. A dataset was created to help resolve this issue.
3166: Not able to specify a limited layer range within a script; this results in a layer mismatch between two files.
Need to use the specified legend number format for probing and running values on fast tile plot.
KNOWN ISSUES IN V1.3:
2503: BCON file support is not available.2937 + 2907: Does not support plot average (daily totals) or whole simulation period averaging.
2640: Bilinear interpolation (smoothing) of data is not available.
3014: VERDI does not support some WRF file formats.
Sometimes the fast tile plot picture does not redraw and you need to click something to refresh it.
Tile plots not displayed completely (sometimes the legend or domain label is not visible).
Adding overlay of vectors does not work for fast tile plot.
Formatting of legend numbers is not accessible from scripting language.
Labels for domain and range axis cannot be entered on the fast tile plot.
Improve documentation on a blue-red diverging palette that is appropriate for some types of plots.
Move the metadata button to a better place (perhaps to the popup menu for the dataset).
Pan function should be added to fast tile plot.
MPEG movie export is not available.
Slow down factor: Would be nice to have it register the value without having to hit enter.
Remote file browser: Would be nice to be able to type in a directory for faster access (rather than navigating manually all the way from the home directory to work and then dow
n the chain).
Add capability for CSV-formatted obs for overlays.
Add shapefile and kmz export for gridded data.
Certain mathematical functions (such as max) do not work as described in the manual. The max function finds the max over all times steps and grid cells rather than finding the 
max over all time steps at each grid cell.
Do not want a numerical label shown for every change in color in the legend; when discreet changes in color are numerous (like >16 bins of color), the plot legend is hard to read with so many numbers on it.
Vector plots give a sense of direction but not magnitude.
Make formulas independent of the input files timestamp; example> allowing monthly average PM2.5 to be added and averaged over 3 consecutive months (3 different files) with different I/O API date stamps.
Need capability to reverse the color scheme in the tile plot configuration menu.
Support vector overlay with magnitude for fast tile plot.
Subdomain selected from within the domain editor starts with values of 1 for x and y domain axis on fast tile plot, rather than keeping the range value of the larger domain.
Batch script does not support multiple time steps, layers, and subset domains.
Areal interpolation calculation assumes units of concentration per meter squared for the gridded data. Need to provide unit checking, and conversion (i.e., concentration interpolated using an area file to concentration per area to support areal interpolation of air quality data in addition to water deposition data.)

BUILD ENVIRONMENT FOR V1.3:
Java 6 - java version "1.6.0_13" Java(TM) SE Runtime Environment (build 1.6.0_13-b03)
Java HotSpot(TM) Client VM (build 11.3-b02, mixed mode, sharing)

----------------------------
RELEASE NOTES FOR VERDI V1.2
----------------------------

These notes describe the difference between the previous public release of VERDI (version 1.1) and version 1.2. The notes describe:
      New Features
      Bug Fixes
      Known Issues

NEW FEATURES IN V1.2:
Made fast tile plot menus consistent with the other tile plot (unavailable options are grayed out); these include enabling 'Show Lat/Lon'.
Fast tile plot has spinners instead sliders for time steps and layers.
Grid lines can be shown/hidden by checking the 'Show Grid Lines' check box menu for fast tile plot; this feature can also be configured through Configure Plot dialog.
EPS format is available for exporting fast tile plot image.
Fast tile plot is capable of probing single grid cell and showing row/column values as cursor moves around.
Fast tile plot keeps zoom-in while probing.
Fast tile plot zooms in to the right area.
A message box will pop up if there is an OutOfMemory error.
On formulas tab, right-clicking variables can add values into the editor for editing.
Configurations through Configure Plot dialog can be applied immediately through the 'Apply' button.
All plots have a subtitle showing dataset sources by default.
Areal interpolation plot type is available to visualize and analyze the watershed deposition datasets.
CAMx data files are readable.
Java heap size increased to 1024 MB.
Mac distribution file (.dmg) is available.
More sample data files are included in the release.

BUG FIXES IN V1.2:
(The format of the information below is “Bugzilla ID: Description”)
2900: World map oddities: The world map still has an oddity, but VERDI has been updated to allow the user to turn off the world map and use a regional map.
2525: Format data on legend.
Layer number in the fast tile plot title is consistent with the current layer.
Layer number and time step are 1-based now instead of 0-based.
Double imaging issue is removed when fast tile plot is exported.
Probing fast tile plot grid cells does not have focus issues on the probed data table.


KNOWN ISSUES IN V1.2:
Specify domain range does not work for fast tile plot (works only for regular plot).
Sometimes the fast tile plot picture does not redraw and you need to click something to refresh it.Add overlay of observations does not work for fast tile plot.
Add overlay of vectors does not work for fast tile plot.
BCON file support is not available.
Formatting of legend numbers is not accessible from scripting language.
Labels for domain and range axis cannot be entered on the fat tile plot.
Streaming of script commands to a live session is not yet available.
Bilinear interpolation of data is not available.
MPEG movie export is not available.
Pan function should be added to fast tile plot.
Need to use the specified legend number format for probing and running values on fast tile plot.
Support adjusting the min and max for the legend on the fast tile plot (fixed in v1.3).
Add a blue-red diverging palette that is appropriate for some types of plots.
Make the maps configurable on fast tile plot (color, transparency, etc.), and add a note regarding transparency (fixed in v1.3).
State and county maps do not match the lines of the North America map.
Move the metadata button to a better place (perhaps to the popup menu for the dataset).
Show the map in the edit domain dialog (fixed in v1.3).

----------------------------
RELEASE NOTES FOR VERDI V1.1
----------------------------

These notes describe the difference between the previous public release of VERDI (version 1.05) and version 1.1. The notes describe:
      New Features
      Bug Fixes
      Known Issues

NEW FEATURES IN V1.1:
Made fast tile plot menus consistent with the other tile plot (unavailable options are grayed out); these include probing, saving animations, specifying subtitles, added a popup menu.
Added a conversion program (shape2bin) to convert shape files to .bin format files for the fast tile plot.
Allow users to add one or more custom maps for the fast tile plot.
Allow users to select or deselect maps displayed on fast tile plot.
The format of the values on the legend can be specified.
Lat-lon data are now supported for tile plot.

BUG FIXES IN V1.1:
(The format of the information below is “Bugzilla ID: Description”)
2900: World map oddities: The world map still has an oddity, but VERDI has been updated to allow the user to turn off the world map and use a regional map.
Bug 2525: Format data on legend.
The edit domain window is no longer too big for the screen.

KNOWN ISSUES IN V1.1:
Specify domain range does not work for fast tile plot (works only for regular plot).
Sometimes the fast tile plot picture does not redraw and you need to click something to refresh it.
Add overlay of observations does not work for fast tile plot.
Add overlay of vectors does not work for fast tile plot.
BCON file support is not available.
Labeling issue on the time series plot.
Formatting of legend numbers is not accessible from scripting language.
Labels for domain and range axis cannot be entered on the fast tile plot.
Show lat-lon does not work for fast tile plot.
Draw grid lines does not work for fast tile plot.
EPS output does not work for fast tile plot.
Streaming of script commands to a live session is not yet available.
Bilinear interpolation of data is not available.
MPEG movie export is not available.
Title is cut off of exported quicktime movies.
Time steps and layers on Formulas/datasets tabs should be 1-based instead of 0-based.
Sliders should be changed to spinners on fast tile plot.
Pan function should be added to fast tile plot.
Probing single grid cell on the fast tile plot should show up as running value, with the lat-lon and/or grid cell, like the old tile plot.
When you probe on the fast tile plot, it redraws the tile plot over your table temporarily.
An Apply button would be nice to add to the configure window.
Need to use the specified legend number format for probing and running values on fast tile plot.
Support adjusting the min and max for the legend on the fast tile plot.
Add a blue-red diverging palette that is appropriate for some types of plots.
On the formulas tab, support using right-click to add values into the editor for editing.
Zoom out to the extent of picture (from the popup menu on the fast tile plot).
Make the maps configurable on fast tile plot (color, transparency, etc.), and add a note regarding transparency.
State and county maps do not match the lines of the North America map.
Move the metadata button to a better place (perhaps to the popup menu for the dataset).
Show the map in the edit domain dialog.
