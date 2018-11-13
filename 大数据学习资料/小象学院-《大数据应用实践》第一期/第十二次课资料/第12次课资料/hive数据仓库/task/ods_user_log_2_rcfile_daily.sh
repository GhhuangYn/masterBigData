#!/bin/sh
source ~/.bashrc
source /etc/profile
#####################################
#ods库中的用户行为日志转RCFILE
#####################################
interval='1'
if [ ! -z $1 ];then
 interval=$1
fi
echo "interval=$interval"
DT=`date --date   "$interval days ago" '+%Y%m%d'`
echo "dt=$DT"

echo "start job ....."

hive -e "msck repair table ods.userlog_external_pt;"
hive -e"
SET hive.exec.dynamic.partition.mode=nonstrict;
SET hive.exec.max.dynamic.partitions.pernode = 1000;
SET hive.exec.max.dynamic.partitions=1000;
set mapred.job.name='ods_user_log_2_rcfile';
insert overwrite table pdw.userlog_detail_pt partition(dt)
select t1.user_id,
t1.client_version,
rel1.type client_type,
t1.area_id,
rel2.name area_name,
t1.time,
case when t1.user_behavior==1 then 1 else 0 end  pv_cnt,
case when t1.user_behavior==2 then 1 else 0 end  click_cnt,
t1.dt
from ods.userlog_external_pt t1 
left join rel.client_version_type_info rel1 on t1.client_version = rel1.version_id 
left join rel.area_info rel2 on t1.area_id = rel2.id
where t1.dt='${DT}' 
;"

if [ $? -ne 0 ]; then
  echo " ods.userlog_external_pt to userlog_detail_pt failed !"
  exit 1
fi

hive -e "msck repair table pdw.userlog_detail_pt;"

echo "end job !"
