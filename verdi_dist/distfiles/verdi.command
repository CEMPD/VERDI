#!/bin/sh

export VERDI_HOME=$(cd "$(dirname "$0")"; pwd)

DIR=$VERDI_HOME
cd $VERDI_HOME/plugins/bootstrap


JAVA=java
JAVAMAXMEM="-Xmx6144M"


# Limit the number of default spawned threads (eca):
JAVAOPTS="-XX:+UseParallelGC -XX:ParallelGCThreads=1 -Dlog4j.debug=false --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/com.sun.imageio.spi=ALL-UNNAMED"

JAVACMD="$JAVA $JAVAOPTS $JAVAMAXMEM $DISPOPTS -classpath ./bootstrap.jar:./lib/saf.core.runtime.jar:./lib/jpf.jar:./lib/jpf-boot.jar:../core/lib/*:../core/lib/geo-19/* saf.core.runtime.Boot"

export PATH=$VERDI_HOME/jre/Contents/Home/bin:$PATH

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

