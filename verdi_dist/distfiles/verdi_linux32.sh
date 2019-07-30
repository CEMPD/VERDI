#! /bin/sh
export VERDI_HOME=`dirname "$0"`
export VERDI_USER=$HOME
DIR=`pwd`
cd "$VERDI_HOME/plugins/bootstrap"

if [ "`uname -m`" = "ia64" ]; then
  JAVA=/usr/java/jdk/bin/java
  JAVAMAXMEM="-Xmx6144M"
else
  JAVA=../../jre/bin/java
  JAVAMAXMEM="-Xmx1024M"
fi

# Limit the number of default spawned threads (eca):
JAVAOPTS="-XX:+UseParallelGC -XX:ParallelGCThreads=1 -XX:+HeapDumpOnOutOfMemoryError"

if [ "$DISPLAY" = "" ]; then
  DISPOPTS="-Djava.awt.headless=true"
fi

JAVACMD="$JAVA $JAVAOPTS $JAVAMAXMEM $DISPOPTS -classpath ./bootstrap.jar:./lib/saf.core.runtime.jar:./lib/commons-logging.jar:./lib/jpf-boot.jar:./lib/jpf.jar:./lib/log4j-1.2.13.jar:../../jre/lib/jai_codec.jar:../../jre/lib/jai_core.jar:../../jre/lib/jai_imageio.jar:../../jre/lib/mlibwrapper_jai.jar saf.core.runtime.Boot"

BATCHCMD=$1

if [ ! -e "$2" ]; then
   BATCHFILE="$DIR""/""$2"
else
   BATCHFILE="$2"
fi

if [ "$BATCHCMD" = "-b" -o "$BATCHCMD" = "-batch" ]; then
   if [ "$#" -eq 1 ]; then
        $JAVACMD $1
   else
        $JAVACMD $1 $BATCHFILE
   fi
else
  $JAVACMD ${1+"$@"}
fi
