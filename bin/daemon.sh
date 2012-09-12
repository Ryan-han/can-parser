#!/usr/bin/env /bin/bash

usage="Usage: $0.sh (start|stop) <args...>"

# if no args specified, show usage
if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

# get arguments
startStop=$1

shift
command=$1

shift

flume_rotate_log ()
{
    log=$1;
    num=5;
    if [ -n "$2" ]; then
	num=$2
    fi
    if [ -f "$log" ]; then # rotate logs
	while [ $num -gt 1 ]; do
	    prev=`expr $num - 1`
	    [ -f "$log.$prev" ] && mv "$log.$prev" "$log.$num"
	    num=$prev
	done
	mv "$log" "$log.$num";
    fi
}

if [ -f "${bin}/env" ]; then
    source "${bin}/env"
fi

if [ "$DAEMON_HOME" = "" ]; then
  export DAEMON_HOME=/usr/lib/flume
fi

if [ "$LOG_DIR" = "" ]; then
  export LOG_DIR="/var/log/flume"
elif [ ! -d "$LOG_DIR" ]; then
  mkdir -p $LOG_DIR
fi

if [ "$NICENESS" = "" ]; then
	export NICENESS=0
fi 

if [ "$PID_DIR" = "" ]; then
  PID_DIR=/var/run/flume
elif [ ! -d "$PID_DIR" ]; then
  mkdir -p $PID_DIR
fi

if [ "$IDENT_STRING" = "" ]; then
  export IDENT_STRING="$USER"
fi

# some variables
export FLUME_LOGFILE=flume-$IDENT_STRING-$command-$HOSTNAME.log
export FLUME_ROOT_LOGGER="INFO,DRFA"
export ZOOKEEPER_ROOT_LOGGER="INFO,zookeeper"
export WATCHDOG_ROOT_LOGGER="INFO,watchdog"
log=$LOG_DIR/flume-$IDENT_STRING-$command-$HOSTNAME.out
pid=$PID_DIR/flume-$IDENT_STRING-$command.pid

case $startStop in

  (start)
    mkdir -p "$PID_DIR"
    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi

    flume_rotate_log $log
    echo starting $command, logging to $log
    cd "$DAEMON_HOME"
    echo "==================="
    echo "${DAEMON_HOME}"/bin/run.sh start $command "$@"
    echo "==================="
    nohup  nice -n ${NICENESS} "${DAEMON_HOME}"/bin/run.sh start $command "$@" > "$log" 2>&1 < /dev/null &
    echo $! > $pid
    sleep 1; head "$log"
    ;;
          
  (stop)

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo stopping $command
        kill `cat $pid`
      else
        echo no $command to stop
      fi
    else
      echo no $command to stop
    fi
    ;;

  (*)
    echo $usage
    exit 1
    ;;

esac
