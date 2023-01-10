#!/bin/csh -f
  
#script for testing command line options
echo 'running verdi_script_wrf.csh'

foreach species ( LANDMASK )

#change directory to where VERDI is installed
cd $cwd/../..
./verdi.sh \
         -f $cwd/data/model/wrf_usgs_tx1km_04112012.nc \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/wrf_usgs_tx1km_04112012_$species.png \
         -quit
         echo 'check outputfile' `ls -lrt $cwd/data/plots/wrf_usgs_tx1km_04112012_$species.png`
 end
