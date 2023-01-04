#!/bin/csh -f
  
#script for testing command line options

echo 'running verdi_script_layer.csh'

cd $cwd/../..
foreach species ( O3 )
foreach layer ( 1 2 3 4 5 )
./verdi.sh \
         -f $cwd/data/model/CCTM_CONC_v53_gcc_SE53BENCH_20160701.nc \
         -configFile $cwd/data/configs/cmaq_config_O3.txt \
         -titleString "CMAQ v53 Species: ${species} Layer: $layer" \
         -s "${species}[1]" \
         -layer $layer \
         -g tile \
         -saveImage "png" $cwd/data/plots/CONC_v53_${species}_$layer.png \
          -quit
 echo 'check outputfile' `ls -lrt $cwd/data/plots/CONC_v53_${species}_$layer.png`
 end
