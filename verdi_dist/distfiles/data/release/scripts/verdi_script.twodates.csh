#!/bin/csh -f
  
#script for testing command line options

echo 'running verdi_script_two_dates.csh'

cd $cwd/../..
foreach species ( O3 )
./verdi.sh \
         -f $cwd/data/model/CCTM_ACONC_v53_intel_SE53BENCH_20160701.nc \
         -f $cwd/data/model/CCTM_ACONC_v53_intel_SE53BENCH_20160702.nc \
         -s "${species}[1]" \
         -g tile  \
         -saveImage "jpg" $cwd/data/plots/CCTM_ACONC_v53_intel_SE53BENCH_20160701.$species.png \
         -s "${species}[2]" \
         -g tile \
         -saveImage "jpg" $cwd/data/plots/CCTM_ACONC_v53_intel_SE53BENCH_20160702.$species.png \
          -quit
 echo 'check output file: ' `ls -rlt $cwd/data/plots/CCTM_ACONC_v53_intel_SE53BENCH_20160701.$species.png`
 echo 'check output file: ' `ls -lrt $cwd/data/plots/CCTM_ACONC_v53_intel_SE53BENCH_20160702.$species.png`
 end
