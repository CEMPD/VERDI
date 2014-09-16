#! /bin/sh
VERDI_HOME=/Applications/verdi_1.4.1
DIR=`pwd`
cd $VERDI_HOME/plugins/bootstrap


JAVA=../../jre/Home/bin/java
JAVAMAXMEM="-Xmx1024M"


# Limit the number of default spawned threads (eca):
JAVAOPTS="-XX:+UseParallelGC -XX:ParallelGCThreads=1"

JAVACMD="$JAVA $JAVAOPTS $JAVAMAXMEM -classpath ./bootstrap.jar:./lib/saf.core.runtime.jar:./lib/commons-logging.jar:./lib/jpf-boot.jar:./lib/jpf.jar:./lib/log4j-1.2.13.jar saf.core.runtime.Boot"

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
  $JAVACMD $*
fi
