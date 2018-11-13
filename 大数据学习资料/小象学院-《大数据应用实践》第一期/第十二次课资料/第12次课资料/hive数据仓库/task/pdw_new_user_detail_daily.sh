#!/bin/sh
source ~/.bashrc
source /etc/profile
#####################################
#pdw层新增用户明细计算
#####################################
interval='1'
if [ ! -z $1 ];then
 interval=$1
fi
echo "interval=$interval"

DT=`date --date   "$interval days ago" '+%Y%m%d'`

echo "dt=$DT"
echo "start job ....."
hive -e"
SET hive.exec.dynamic.partition.mode=nonstrict;
SET hive.exec.max.dynamic.partitions.pernode = 1000;
SET hive.exec.max.dynamic.partitions=1000;
set mapred.job.name='pdw.new_user_detail_pt task';
insert into table pdw.new_user_detail_pt partition(dt)
select t1.user_id,
min(t1.time),
t1.dt 
from pdw.userlog_detail_pt t1 
where t1.dt='${DT}' 
and t1.user_id not in (select t2.user_id from pdw.new_user_detail_pt t2)
group by t1.user_id,t1.dt
;"
if [ $? -ne 0 ]; then
   echo "pdw.new_user_detail_pt task failed!"
   exit 1
fi

hive -e "msck repair table pdw.new_user_detail_pt;"

echo "end job !"
