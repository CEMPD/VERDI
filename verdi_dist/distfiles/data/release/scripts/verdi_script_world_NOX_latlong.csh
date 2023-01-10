#!/bin/csh -f
  
#script for testing command line options

echo 'running verdi_script_world.csh'
cd $cwd/../..
foreach species ( NOX )
./verdi.sh \
         -f $cwd/data/model/world_NOX_latlon.ncf \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/world_NOX_latlon_$species.png \
         -quit
 echo 'check outputfile' `ls -lrt $cwd/data/plots/world_NOX_latlon_$species.png`
 end
