<!-- BEGIN COMMENT -->
  
[<< Previous Chapter](VERDI_ch11.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch13.md)

<!-- END COMMENT -->

Supported Grid and Coordinate Systems (Map Projections)
======================================

VERDI makes calls to the netCDF Java library to obtain the grid and coordinate system information about the data directly from the model data input files when the input data files are self-describing (CMAQ, SMOKE, WRF netCDF format files).

Models-3 I/O API Data Convention
----------------------

For the Models-3 I/O API, support for Lambert conformal conic (LCC) map projection, Universal Transverse Mercator (UTM) map projection, and polar stereographic map projection was added in VERDI 1.1., and Mercator projection in VERDI 1.2. The grid projections listed on the following website are supported, although not all have been tested: 
https://www.cmascenter.org/ioapi/documentation/all_versions/html/.

Users that need VERDI to support other projections are encouraged to provide small input datasets as attachments to emails to the m3user listserv, or to github.com/CEMPD/VERDI/issues, for testing and to facilitate future development efforts. PDF: ([Fig-@fig:Figure73])) or GitHub:[Figure 73](#Figure73) through ([Fig-@fig:Figure75])) or GitHub:[Figure 75](#Figure75) illustrate sample plots generated for datasets with LCC, polar stereographic, Mercator, and UTM map projections, respectively.

Figure 73. Lambert Conformal Conic Map Projection Example Plot<br>

![Lambert Conformal Conic Map Projection Example Plot](./media/image073.png){#fig:Figure73}


Figure 74. Polar Stereographic Map Projection Example Plot<br>

![Polar Stereographic Map Projection Example Plot](./media/image074.png){#fig:Figure74}


Figure 75. Mercator Map Projection Example Plot<br>

![Mercator Map Projection Example Plot](./media/image075.png){#fig:Figure75}


Figure 76. UTM Map Projection Example Plot<br>

![UTM Map Projection Example Plot](./media/image076.png){#fig:Figure76}


CAMx Gridded Data Convention
--------------------------

The netCDF-java library used in VERDI includes support for CAMx UAM-IV binary files using a preset default projection. CAMx or UAM binary files contain information about the x and y offsets from the center of the projection in meters, but do not contain information about the projection. The projection information is available in separate diagnostic files, which are part of the CAMx output along with the UAM binaries PDF: ([Fig-@fig:Figure76])) or GitHub:[Figure 76](#Figure76).



Figure 77. Example CAMx diagnostic text file<br>

![Example CAMx diagnostic text file](./media/image077.png){#fig:Figure77}



The netCDF-java library writes the default projection information to a text file in the directory where the CAMx binary (UAM-IV) file is located. You can then review and edit the projection information to make it consistent with the projection specified in the CAMx diagnostic text files. The definitions of the projection parameters used in the camxproj.txt file are defined using Models-3 I/O API format https://www.cmascenter.org/ioapi/documentation/all_versions/html/. You must edit the camxproj.txt file to match the grid description information provided in the corresponding camx.diag file.  PDF: ([Fig-@fig:Figure77])) or GitHub:[Figure 77](#Figure77) shows the definition for the grid projection parameters for a Lambert conformal conic projection.


Figure 78. Models-3 I/O API Map Projection Parameters for Lambert Conformal Conic Projection<br>

![Models-3 I/O API Map Projection Parameters for Lambert Conformal Conic Projection](./media/image078.png){#fig:Figure78}

 PDF: ([Fig-@fig:Figure78])) or GitHub:[Figure 78](#Figure78) shows the values of the camxproj.txt after editing it to match the values of the camx.diag file and using the definitions of the Models-3 grid parameters.  PDF: ([Fig-@fig:Figure79])) or GitHub:[Figure 79](#Figure79)  shows the resulting Tile Plot of the CAMx sample dataset.

Figure 79. Edited Example Projection File: camxproj.txt<br>

![Edited Example Projection File: camxproj.txt](./media/image079.png){#fig:Figure79}


Figure 80. CAMx Example Plot<br>

![CAMx Example Plot](./media/image080.png){#fig:Figure80}

WRF netCDF Data Convention
-------------------------
The WRF netCDF data convention is supported in VERDI. https://www.mmm.ucar.edu/weather-research-and-forecasting-model

Figure 100. WRF Example Plot of Height in Meters on a 1km Texas Domain<br>

![WRF Example Plot of Height in Meters on a 1km Texas Domain](./media/image100.png){#fig:Figure100}

MPAS netCDF Data Convention
--------------------------
The MPAS netCDF data convention is supported in VERDI https://mpas-dev.github.io/.
Figure 101. MPAS Example Plot of 2 meter Temperature on World Map

![MPAS Example Plot of 2 meter Temperature on World Map](./media/image101.png){#fig:Figure101}

Figure 12-11. MPAS Example Plot of 2 meter Temperature Zoomed in to California

![MPAS Example Plot of 2 meter Temperature Zoomed in to California](./media/image102.png){#fig:Figure102}


<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch11.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch13.md)<br>
VERDI User Manual (c) 2021<br>

<!-- END COMMENT -->

