#!/bin/csh -f

#script for plotting difference of CO emissions on two different days
#script should be identical to the verdi_script_tileplot_diff.launch that is run within eclipse

echo 'running verdi_script_tileplot_diff.csh'
setenv dir $cwd
echo 'current directory:'$dir

/Users/lizadams/VERDI_1.6.0_shapefile_mpas/verdi.command \
"   -f $dir/../model/cmaq_ready.egts_l.2010.01.SUN.KNU_27_01.CO.ncf \
   -f  $dir/../model/cmaq_ready.egts_l.2010.01.THU.KNU_27_01.tshift.CO.ncf \
   -s (CO[2]-CO[1])/CO[1]*100 \
   -configFile $dir/../configs/verdi.CO.diff.config \
   -g tile \
   -saveImage "GIF" $dir/../plots/CO.diff.tileplot.gif \
 -quit"
echo 'check output file: '$dir/../plots/CO.diff.tileplot.gif
