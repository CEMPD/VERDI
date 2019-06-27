#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_camx.csh'
foreach species ( O3 )

cd $cwd/../..
./verdi.sh \
         -f $cwd/data/CAMx/outputs/camx.v6.10.CB6r2.3SAQS_Base11a_sensWinterO3.25L.2011033.avrg.grd02 \
         -titleString "CAMx GRD02 ${species} Layer 1" \
         -configFile  ~/verdi_config_camx_gr02 \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/VERDI.camx.grd02.$species.png \
         -quit
         echo 'check output file: ' `ls -lrt $cwd/data/plots/VERDI.camx.grd02.$species.png`
 end
