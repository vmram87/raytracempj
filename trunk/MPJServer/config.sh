#!/bin/sh
service iptables stop
service nfs start

address=`ifconfig eth0 | grep '192.168.*.*' | sed 's/^.*addr://g' | sed 's/\sBcast.*$//g' | awk 'BEGIN {FS="."} {print $3}'`

sed -i "s/192\.168\.[0-9]\{1,3\}\./192.168.$address./g" /etc/hosts
