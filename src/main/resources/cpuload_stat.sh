#!/bin/sh

HOST=`hostname`
TSTAMP=`date +%s`
VALUE=`uptime | cut -d " " -f 11`

echo cpuload.$HOST $TSTAMP $VALUE | nc -u4 -w1 $HOST 4005
