#!/bin/csh -f
echo 'running verdi_script_mpas.csh'

cd $cwd/../..
foreach species ( t2m )
./verdi.sh \
         -f $cwd/data/model/t2m_only.nc \
         -s "${species}[1]" \
# following is commented out due to bug ERROR anl.verdi.plot.gui.MeshPlot - Error rendering MeshPlot
#        -g tile \
#        -saveImage "png" $cwd/data/plots/history.2013-07-10.nc_$species.png  \
# once verdi loads with the precipw species, click on tile plot in the gui, then save it as an image
# if the bug is fixed, then uncomment the verdi command line options
#        -quit
        echo 'check output file: ' `ls -lrt $cwd/data/plots/history.2013-07-10.nc_$species.png`
 end
