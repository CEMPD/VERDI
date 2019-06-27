#!/bin/csh -f
  
#script for testing command line options
echo 'running verdi_script_tile_with_config.csh'

cd $cwd/../..
foreach species ( O3 )
./verdi.sh \
         -f $cwd/data/model/CCTM46_P16.baseO2a.36k.O3MAX \
          -configFile $cwd/data/configs/10bin_new_config \
         -titleString "CMAQv46 ${species} Layer 1" \
         -s "${species}[1]" \
         -g tile \
         -saveImage "jpg" $cwd/data/plots/CCTM.10bin.$species.png \
         -quit
 echo 'check output file: ' `ls -lrt $cwd/data/plots/CCTM.10bin.$species.png`
 end
