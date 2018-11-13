#!/bin/bash
echo "stop zkserver..."
for i in 1 2 3
do
ssh node0$i "source /etc/profile;/usr/local/zookeeper/bin/zkServer.sh stop"
done
echo "zkServer stoped!"
