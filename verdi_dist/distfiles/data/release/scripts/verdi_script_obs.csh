#!/bin/csh -f
#script for testing command line options
echo 'running verdi_script_obs.csh'

foreach species ( O3 )
cd $cwd/../..
./verdi.sh \
         -f $cwd/data/model/CCTM_ACONC_v5.3.b1_intel17.0_2016_CONUS_M3Dry_Bidi_OCoating_20160320.nc \
         -f $cwd/data/obs/AQS_Hourly_PAVE_VERDI_overlay_2016.ncf \
        # -configFile $cwd/data/configs/10_rbg_config \
         -s "${species}[1]" \
         -g tile \
         -saveImage "jpg" $cwd/data/plots/ACONC_${species}_obs.png \
# need to add the obs layer by hand as there isn't a command line option to do this
         #-quit
         echo 'check outputfile' `ls -lrt $cwd/data/plots/ACONC_${species}_obs.png` 
 end
