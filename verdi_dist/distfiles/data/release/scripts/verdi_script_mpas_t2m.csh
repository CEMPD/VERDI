#!/bin/csh -f
echo 'running verdi_script_mpas.csh'

cd $cwd/../..
foreach species ( t2m )
./verdi.sh \
         -f $cwd/data/model/t2m_only.nc \
         -s "${species}[1]" \
         -g tile \
         -saveImage "png" $cwd/data/plots/t2m_only_$species.png  \
        -quit
        echo 'check output file: ' `ls -lrt $cwd/data/plots/t2m_only_$species.png`
 end
