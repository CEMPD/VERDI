#!/bin/csh -f

#script for plotting timeseries plot of Temperature 

echo 'running verdi_script_timeseries_layer.csh'

cd $cwd/../..
echo directory `pwd`
 ./verdi.sh \
   -f $cwd/data/model/METCRO3D_CMAS-Training.nc \
   -s "TA[1]" \
   -layer 15 \
   -g line \
   -saveImage "GIF" $cwd/data/plots/TA.timeseries.layer.15.gif \
   -quit
 echo 'check output file: ' `ls -lrt $cwd/data/plots/TA.timeseries.layer.15.gif`
