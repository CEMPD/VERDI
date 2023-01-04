#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_areal.csh'

foreach species ( O3 )
cd $cwd/../..
./verdi.sh \
         -f $cwd/data/model/CCTM_N1a_drydep_O3.20060701 \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/CCTM_N1a_drydep_O3.20060701_$species.png \
         -quit
 echo 'check outputfile' `ls -lrt $cwd/data/plots/CCTM_N1a_drydep_O3.20060701_$species.png`
 end
