#!/bin/sh
source ~/.bashrc
source /etc/profile

interval='1'
if [ ! -z $1 ];then
 interval=$1
fi
echo "interval=$interval"
DT=`date --date   "$interval days ago" '+%Y%m%d'`

echo "dt=$DT"

echo "start job ....."

EXPORTPATH="."

hive -e"
SET hive.exec.dynamic.partition.mode=nonstrict;
SET hive.exec.max.dynamic.partitions.pernode = 1000;
SET hive.exec.max.dynamic.partitions=1000;
set mapred.job.name='user_overview_report_daily task';
select a1.new_user_cnt,
a2.active_user_cnt,
a3.total_user_cnt,
${DT} date_time 
from 
(select count(t1.user_id) new_user_cnt 
from pdw.new_user_detail_pt t1 
where t1.dt='${DT}') a1,
(select count(distinct t2.user_id) active_user_cnt
from pdw.userlog_detail_pt t2
where t2.dt='${DT}') a2,
(select count(t3.user_id) total_user_cnt 
from pdw.new_user_detail_pt t3) a3
;" > "${EXPORTPATH}/user_overview_report_daily_${DT}.csv"

if [ $? -ne 0 ]; then
   echo "user_overview_report_daily task failed!"
   exit 1
fi

echo "end job !"
