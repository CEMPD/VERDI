#! /bin/sh
export VERDI_HOME=/Users/lizadams/VERDI_1.5.0/VERDI_1.5.0
DIR=`pwd`
cd $VERDI_HOME/plugins/bootstrap


JAVA=../../jre/Commands/java
JAVAMAXMEM="-Xmx1024M"


# Limit the number of default spawned threads (eca):
JAVAOPTS="-XX:+UseParallelGC -XX:ParallelGCThreads=1 -Djava.ext.dirs="

JAVACMD="$JAVA $JAVAOPTS $JAVAMAXMEM -classpath ./bootstrap.jar:./lib/saf.core.runtime.jar:./lib/jpf.jar:./lib/jpf-boot.jar:../core/lib/MacOSX/*:../core/lib/* saf.core.runtime.Boot"

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
