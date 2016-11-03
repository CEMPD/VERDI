#!/bin/csh -f

#script for plotting timeseries plot of Temperature 

echo 'running verdi_script_timeseries_layer.ccsh'
setenv dir $cwd
echo 'print dir:'$dir

/Users/lizadams/VERDI_1.6.0_shapefile_mpas/verdi.command \
"   -f $dir/../model/METCRO3D_CMAS-Training \
   -s TA[1] \
    -layer 15 \
   -g line \
   -saveImage "GIF" $dir/../plots/TA.timeseries.layer.gif \
 -quit"
echo 'check output file: '$dir/../plots/TA.timeseries.layer.gif
