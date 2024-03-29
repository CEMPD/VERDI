#!/bin/csh -f
  
#script for testing command line options

echo 'running verdi_script_metcro.csh'

foreach species ( DENS )
cd $cwd/../..
./verdi.sh \
         -f $cwd/data/model/METCRO3D_CMAS-Training \
         -s "${species}[1]" \
         -g tile \
         -saveImage "jpg" $cwd/data/plots/METCRO3D_$species.png \
         -quit
         echo 'check outputfile' `ls -lrt $cwd/data/plots/METCRO3D_$species.png`
end
