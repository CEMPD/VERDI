#!/bin/csh -f
echo 'running verdi_script_wind_vector.csh'

cd $cwd/../..
./verdi.sh \
   -f "$cwd/data/model/CCTM47_aac_spr06.12k.CONC.2006113.O3.ncf" \
   -s "O3[1]" \
   -g tile \
   -f "$cwd/data/model/metdot3d_12k.uwind.vwind" \
    -s "UWIND[2]" \
    -s "VWIND[2]" \
    -vector "UWIND[2]" "VWIND[2]" "5" \
    -saveImage "GIF" $cwd/data/plots/CCTM47.O3.wind_vector.gif
 #-quit   # remove the comment in front of -quit once VERDI is able to write out animated gif prior to quitting.
 echo 'check output file: ' `ls -lrt $cwd/data/plots/CCTM47.O3.wind_vector.gif`
