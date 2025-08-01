#! /bin/sh
export VERDI_HOME=`dirname "$0"`
export VERDI_USER=$HOME
cd "$VERDI_HOME"
DIR=`pwd`
VERDI_HOME="$DIR"
cd "$VERDI_HOME/plugins/bootstrap"

JAVA=../../jre/bin/java
JAVAMAXMEM="-Xmx6144M"

# Limit the number of default spawned threads (eca):
JAVAOPTS="-XX:+UseParallelGC -XX:ParallelGCThreads=1 -Duser.home=$HOME -Dlog4j.debug=false -Djava.library.path=../core/lib --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/com.sun.imageio.spi=ALL-UNNAMED"

if [ "$DISPLAY" = "" ]; then
  DISPOPTS="-Djava.awt.headless=true"
fi

JAVACMD="$JAVA $JAVAOPTS $JAVAMAXMEM $DISPOPTS -classpath ./bootstrap.jar:./lib/saf.core.runtime.jar:./lib/jpf.jar:./lib/jpf-boot.jar:./lib/:../core/lib/*:../core/lib/geo-19/* saf.core.runtime.Boot"

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
