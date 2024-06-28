#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_drydep_CONUS.csh'

cd $cwd/../..
foreach species ( O3 )
./verdi.sh \
         -f $cwd/data/model/CCTM_DRYDEP_v53_intel18.0_2016_CONUS_test_20151222.nc \
         -titleString "CMAQ ${species} Layer 1" \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/CCTM_DRYDEP_states_areal_interpolation_$species.png \
 # using the GUI - load the US Map as an shapefile and test the areal interpolation plot using the US States
 #         -quit
  echo 'check output file: ' `ls -lrt $cwd/data/plots/CCTM_DRYDEP_states_areal_interpolation_$species.png`
# the areal interpoloation plot isn't saved, only the tile plot, need to capture a screenshot or save to PNG in the GUI after loading the 
# VERDI_2.1.5_20240627/plugins/bootstrap/data/cb_2018_us_state_500k.shp
 end
