#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_wrf_subset.csh'

cd $cwd/../..
foreach species ( T2 )
./verdi.sh \
         -f $cwd/data/model/wrfv3.8_for_mcipv4.5/subset_wrfout_d01_2011-06-30_00:00:00 \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/subset_wrfout_d01_2011-06-30_00:00:00.$species.png \
          -quit
 echo 'check output file: ' `ls -lrt $cwd/data/plots/subset_wrfout_d01_2011-06-30_00:00:00.$species.png`
 end
