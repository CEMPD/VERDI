#!/bin/csh -f
  
#script for testing command line options
echo 'running verdi_script_drydep.csh'

foreach species ( O3 )
cd $cwd/../..
./verdi.sh \
         -f $cwd/data/model/CCTM_DRYDEP_v53_intel_SE53BENCH_20160701.nc \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/CCTM_DRYDEP_$species.png \
         -quit
          echo 'check outputfile' `ls -lrt $cwd/data/plots/CCTM_DRYDEP_$species.png`
 end
