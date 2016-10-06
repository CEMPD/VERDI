#!/bin/csh -f

#script for plotting percent difference of CO emissions
#script should be identical to the verdi_script_tileplot_pdiff.launch that is run within eclipse

echo 'running verdi_script_tileplot_pdiff.csh'
setenv dir $cwd
echo 'print dir:'$dir

/Users/lizadams/VERDI_1.6.0_shapefile_mpas/verdi.command \
"   -f $dir/../model/cmaq_ready.egts_l.2010.01.SUN.KNU_27_01.CO.ncf \
   -f  $dir/../model/cmaq_ready.egts_l.2010.01.THU.KNU_27_01.tshift.CO.ncf \
   -s (CO[2]-CO[1])/CO[1]*100 \
   -configFile $dir/../configs/pdiff.diverge.config.-100to100 \
   -g tile \
   -saveImage "GIF" $dir/../plots/CO.pdiff.tileplot.gif \
 -quit"
echo 'check output file: '$dir/../plots/CO.pdiff.tileplot.gif
