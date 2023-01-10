#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_camx_combine.csh'
foreach species ( O3 )

cd $cwd/../..
./verdi.sh \
       #  -f $cwd/data/CAMx/avrg/2016202.12US2.35.2016ff_cb6camx_16j.epa.camx.avrg.grd01 \
         -f $cwd/data/model/combine.aconc.2016ff_cb6camx_16j.12US2.201607.p1.ncf \
         -titleString "CAMx.12US2 ${species} Layer 1" \
         -s "${species}[1]" \
         -g tile \
         -saveImage "jpg" $cwd/data/plots/combine.aconc.$species.png \
          -quit
 echo 'check outputfile' `ls -lrt $cwd/data/plots/combine.aconc.$species.png`
 end
