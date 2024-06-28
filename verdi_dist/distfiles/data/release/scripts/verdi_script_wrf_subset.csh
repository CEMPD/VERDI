#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_wrf_subset.csh'

cd $cwd/../..
foreach species ( LU_INDEX )
./verdi.sh \
         -f $cwd/data/model/wrfout_d01_2011-01-01_00_LU_INDEX \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/wrfout_d01_2011-01-01_00_LU_INDEX.$species.png \
          -quit
 echo 'check output file: ' `ls -lrt $cwd/data/plots/wrfout_d01_2011-01-01_00_LU_INDEX.$species.png`
 end
