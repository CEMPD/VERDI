#!/bin/csh -f
  
#script for testing command line options

echo 'running verdi_script_tile.csh'

cd $cwd/../.. 
foreach filename ( `ls $cwd/data/model/CCTM*.nc | xargs -n 1 basename` )
  foreach species ( O3 )
  ./verdi.sh \
         -f $cwd/data/model/$filename \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/${filename}_$species.png \
         -quit
   echo 'check outputfile' `ls -lrt $cwd/data/plots/${filename}_$species.png`
   end
 end
