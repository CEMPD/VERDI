<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch01.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch03.md)

<!-- END COMMENT -->

Requirements for Using VERDI
============================

Java
------------------------

VERDI uses OpenJDK version "21.0.1". The OpenJDK<sup>TM</sup> is provided as part of the VERDI release for 32- and 64-bit Linux and Windows. If your computer requires a different version, see Section 3.3. <span id="_Toc197166113" class="anchor"><span id="_Toc292294996" class="anchor"></span></span>

Memory and CPU Requirements
---------------------------

VERDI’s memory and CPU requirements largely depend on the size of the datasets to be visualized. Small datasets can be visualized and manipulated using less than 1024 megabytes of RAM, while larger datasets may need considerably more. If you are using datasets that require either more or less than 1024 MB of memory, you can change the default maximum memory setting used by VERDI:

-   On Windows, edit the run.bat file that you use to launch VERDI. Look for the line that starts “set JAVACMD=” and change the value for the “Xmx” argument from the default heap size of 6144M.

-   On Linux or another Unix platform, you can edit verdi.sh and replace the 6144 in –Xmx6144M with a different value; for example, -Xmx6144M will allow VERDI to access up to 6144MB (or 3GB) of RAM.

Note that slower CPUs can quickly view and animate smaller datasets, whereas larger datasets require more time. As a user opens new Tile plots or other plot types, the memory requirements increase. As the user then closes the plots, the memory is released by VERDI.

Requirements to Run VERDI Remotely
----------------------------------

VERDI may be used to run on a remote compute server and have the graphics display locally on your desktop machine (Unix workstation, Mac, or PC) using the Tile Plot. Your computer needs to be configured to run X-Windows. Typically, you will connect to the remote compute server using secure shell (SSH). If you are using an X-Server and wish to generate 3-D plots using Open GL, you need to turn on Open GL support within the X-Server.

Graphics Requirements
---------------------

Three-dimensional contour plots require a graphics card with OpenGL or DirectX capability. By default VERDI uses OpenGL for 3D rendering. If you would like to use DirectX instead, add the line: j3d=-Dj3d.rend=d3d to the verdi.ini file.

Display Properties
------------------

VERDI works best on screen displays that have been set to a high or perhaps the highest screen resolution (1440 × 900 for Mac, or 1920 x 1080 for Windows 11/Linux). Follow these general instructions to adjust your screen resolution on the following types of computers.

-   Windows 11: Click on the Start button and select Control Panel. Select Display, then Adjust resolution. Use the drop-down boxes to select your type of display, screen resolution, etc. (it may already be set at the higest resolution by default). Select Scale and layout. Change the size of text, apps, and other items - select 100% rather than the default 150%.  (the 150% setting makes the VERDI app take up the entire screen, and this results in the footer information of the Tile Plot getting cut off.) Select Display Resolution: 1920x1080.

-   Mac: Go to Applications and double-click on System Preferences. Under Hardware select Displays and then Select 1440 × 900.

-   Linux: Right-click on desktop and select Display Settings. Under Resolution select option greater than (or equal to) 1920 x 1080 (16:9).

<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch01.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch03.md)<br>
VERDI User Manual (c) 2025<br>

<!-- END COMMENT -->
