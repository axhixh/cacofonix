#!/bin/sh
# script to feed system load on unix
# assume cacofonix runs on localhost

HOST=`hostname`
TSTAMP=`date +%s`
MILLIS="$TSTAMP"000
VALUE=`uptime | cut -d " " -f 15`

echo -n "cpuload.$HOST $MILLIS $VALUE"
echo -n "cpuload.$HOST $MILLIS $VALUE" | nc -u -w1 localhost 4005
