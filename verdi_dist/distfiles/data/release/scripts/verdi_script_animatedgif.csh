#!/bin/csh -f
echo 'running verdi_script_animatedgif.csh'

cd $cwd/../..
./verdi.sh \
   -f "$cwd/data/model/CCTM46_P16.baseO2a.36k.O3MAX" \
   -s "O3[1]" \
   -g tile 
   -mapName "$cwd/data/hucRegion/huc03.shp" \
   -saveImage "GIF" $cwd/data/plots/CCTM47.O3.gif \
 #  -animatedGIF $cwd/data/plots/CCTM47.O3.animated.gif \
 #-quit   # remove the comment in front of -quit once VERDI is able to write out animated gif prior to quitting.
 echo 'check output file: ' `ls -lrt $cwd/data/plots/CCTM47.O3.gif`
 echo 'check output file: ' `ls -lrt $cwd/data/plots/CCTM47.O3.animated.gif`
