#!/bin/csh -f

#script for plotting percent difference of SO2 emissions

echo 'plot_SO2.emis.csh'
echo $argv


setenv VERSION VERDI_1.6.0_shapefile_mpas
setenv INDIR /home/MODEL/CMAQ-SMOKE_inputfiles/cmaq_ready/KNU_27_01
echo $INDIR
setenv VERDIR /home/bbaek/$VERSION/
echo $VERDIR
setenv CONFDIR /home/bbaek/utils/plots/configs
setenv OUTDIR /home/bbaek/utils/plots/output
setenv AFILE cmaq_ready.egts_l.2010.01.SUN.KNU_27_01.ncf
setenv BFILE cmaq_ready.egts_l.2010.01.THU.KNU_27_01.tshift.ncf
echo $INDIR/$AFILE
echo $INDIR/$BFILE


../verdi.sh \
"  -f $INDIR/$AFILE \
   -f $INDIR/$BFILE \
   -s CO[2]-CO[1] \
   -configFile $CONFDIR/verdi.CO.diff.config \
   -g tile \
   -saveImage "GIF" $OUTDIR/CO.diff.gif \
   -exit  \
   "
echo "check output file"
echo "checking to see that each VERDI version successfully creates an output gif file"
ls -lrt $OUTDIR/CO.diff.gif
