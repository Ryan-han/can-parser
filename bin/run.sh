#!/usr/bin/env /bin/bash


# name of script
BINPATH=`dirname $0`


if [ -f "${BINPATH}/env" ]; then
    source "$BINPATH/env"
fi


CMDPATH=`pwd`

# some Java parameters
if [ "$JAVA_HOME" != "" ]; then
  #echo "run java in $JAVA_HOME"
  JAVA_HOME=$JAVA_HOME
fi

if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

JAVA=$JAVA_HOME/bin/java

# name of path
CMDPATH=`dirname $CMDPATH`

SHELL=$CMDPATH/bin/run.sh

CMD="$1"


THREAD_NUM="$2"

pathsep=":"
function append_jars_onto_classpath() {
    local JARS
    JARS=`find $1/*.jar 2> /dev/null || true`
    for i in $JARS; do
        if [ -n "$CLASSPATH" ]; then
            CLASSPATH=${CLASSPATH}${pathsep}${i}
        else
            CLASSPATH=${i}
        fi
    done
}

JOPTS="-Dlog.dir=${DAEMON_HOME}/logs"
JOPTS="$JOPTS -Dlog4j.configuration=log4j.properties "

append_jars_onto_classpath "$DAEMON_HOME/lib"
export CLASSPATH
echo "+++++++++++++++++++++++++++"
echo $DAEMON_HOME/lib

if [ "$CMD" = "-h" ] ; then
  usage
elif [ "$CMD" = "start" ] ; then
#	if [ "$OPTION" = "-a" ] ; then
		# there can be multiple nodes.
  		echo $JAVA $JOPTS $UOPTS -cp $CLASSPATH com.nexr.hmc.HMCThreadParser "${DBC_DIR}" "${INPUT_DIR}" "${OUTPUT_DIR}" "${THREAD_NUM}"
  		exec $JAVA $JOPTS $UOPTS -cp $CLASSPATH com.nexr.hmc.HMCThreadParser "${DBC_DIR}" "${INPUT_DIR}" "${OUTPUT_DIR}" "${THREAD_NUM}"
#	else
#		# there can be multiple nodes.
#  		echo $JAVA $JOPTS $UOPTS -cp $CLASSPATH com.nexr.hmc.HMCParser "${DBC_DIR}" "${INPUT_DIR}" "${OUTPUT_DIR}"
#  		exec $JAVA $JOPTS $UOPTS -cp $CLASSPATH com.nexr.hmc.HMCParser "${DBC_DIR}" "${INPUT_DIR}" "${OUTPUT_DIR}"
#	fi
fi
