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
set mapred.job.name='device_type_pv_click_report_daily task';
select client_type,
sum(pv_cnt) pv_cnt,
sum(click_cnt) click_cnt,
${DT} date_time 
from pdw.userlog_detail_pt
where dt='${DT}' 
group by client_type,${DT}
;" > "${EXPORTPATH}/device_type_pv_click_report_daily_${DT}.csv"

if [ $? -ne 0 ]; then
   echo "device_type_pv_click_report_daily task failed!"
   exit 1
fi

echo "end job !"
