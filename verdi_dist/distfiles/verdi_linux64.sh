#! /bin/sh
export VERDI_HOME=${PWD}
export VERDI_USER=$HOME
DIR=`pwd`
cd $VERDI_HOME/plugins/bootstrap

JAVA=../../jre/bin/java
JAVAMAXMEM="-Xmx6144M"

# Limit the number of default spawned threads (eca):
JAVAOPTS="-XX:+UseParallelGC -XX:ParallelGCThreads=1 -Duser.home=$HOME -Djava.library.path=../core/lib"

JAVACMD="$JAVA $JAVAOPTS $JAVAMAXMEM -classpath ./bootstrap.jar:./lib/saf.core.runtime.jar:./lib/jpf.jar:./lib/jpf-boot.jar:./lib/:../core/lib/* saf.core.runtime.Boot"

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
