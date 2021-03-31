#!/bin/csh -f
  
#script for testing command line options

echo 'running verdi_script_timeseries_bar.csh'

cd $cwd/../.. 
foreach filename ( `ls $cwd/data/model/CCTM* | xargs -n 1 basename` )
  foreach species ( O3 )
  ./verdi.sh \
         -f $cwd/data/model/$filename \
         -s "${species}[1]" \
         -g bar \
         -saveImage "png" $cwd/data/plots/${filename}_timeseries_line_$species.png \
         -quit
   echo 'check outputfile' `ls -lrt $cwd/data/plots/${filename}_timeseries_line_$species.png`
   end
 end
