#!/bin/sh

export VERDI_HOME=$(cd "$(dirname "$0")"; pwd)

DIR=$VERDI_HOME
cd $VERDI_HOME/plugins/bootstrap


JAVA=java
JAVAMAXMEM="-Xmx4096M"


# Limit the number of default spawned threads (eca):
JAVAOPTS="--illegal-access=permit -XX:+UseParallelGC -XX:ParallelGCThreads=1 -Djava.ext.dirs="

JAVACMD="$JAVA $JAVAOPTS $JAVAMAXMEM -classpath ./bootstrap.jar:./lib/saf.core.runtime.jar:./lib/jpf.jar:./lib/jpf-boot.jar:./lib/:../core/lib/MacOSX/*:../core/lib/org.apache.xalan_2.7.1.v201005080400.jar:../core/lib/org.apache.xml.serializer_2.7.1.v201005080400.jar:../core/lib/* saf.core.runtime.Boot"

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
