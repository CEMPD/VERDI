<!-- BEGIN COMMENT -->
  
[<< Previous Chapter](VERDI_ch19.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch21.md)

<!-- END COMMENT -->

 Visualize Fine Scale Model Outputs
==========================
  Data from fine scale (neighborhood, or street level) model can be visualized on the street-level maps in VERDI.  Given user can provide fine scale map GIS layers (shapefiles) to be loaded into VERDI.  Here is a step by step example:
  
Step 1: Open VERDI GUI and load your fine scale model gridded datasets. you can get access to some sample model files at the following link. /work/MOD3DEV/jherwehe/wrf/urban/UACM-WRF-4.5/test/run.LISTOS_444m.nondguv28/wrfout_d01_2018-05-20_00:00:00

Step 2: Select variable "T2" and create a "tile plot".  To turn on USA States lines and turn off the USA County lines,  click on GIS Layers → Add Map Layers, then check the box in front of "USA States" and go back in again to uncheck the box in front of "USA Counties".	
  
  ![VERDI GIS Layers UI](./media/image106.png){#fig:Figure106} <br>
     
Step 3: To add your own fine scale GIS layers, click on GIS Layers → Add Map Layers → Other…
A pop-up file browser window will show up for you to navigate to the street-level shapefiles, click on "open".    	
  
  ![The pop-up window to load other GIS Layers UI](./media/image107.png){fig:Figure107} <br> 

Step 4: Wait for a few seconds until the GIS layers are added to the tile plot, then zoom into your area of interest and explore.
The sample shapefiles are used to create the following map can be found at VERDI/verdi_dist/distfiles/data/release/DCMStreetCenterLine.zip    
	
  ![The tile plot with NYC street centerlines](./media/image108.png){fig:Figure108} <br>





















<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch19.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch21.md)<br>
VERDI User Manual (c) 2023<br>

<!-- END COMMENT -->
