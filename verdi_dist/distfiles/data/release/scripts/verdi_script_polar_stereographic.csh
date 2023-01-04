#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_polar_stereographic.csh'

foreach species ( LWMASK )
cd $cwd/../..
./verdi.sh \
         -f $cwd/data/model/GRIDCRO2D_2006075_polar_stereographic.ncf \
         -s "${species}[1]" \
         -g tile \
         -saveImage "jpg" $cwd/data/plots/test.polar_stereographic.$species.png \
         -quit
          echo 'check outputfile' `ls -lrt $cwd/data/plots/test.polar_stereographic.$species.png`
 end
