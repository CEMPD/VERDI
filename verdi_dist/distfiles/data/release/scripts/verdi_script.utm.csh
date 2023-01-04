#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_utm.csh'
foreach species ( HOUSEHOLDS )

cd $cwd/../..
./verdi.sh \
         -f $cwd/data/model/grid_pophous_utm.ncf \
         -s "${species}[1]" \
         -g tile \
         -saveImage "jpg" $cwd/data/plots/grid_pophous_utm.ncf.$species.png \
         -quit
 echo 'check output file: ' `ls -lrt $cwd/data/plots/grid_pophous_utm.ncf.$species.png`
 end
