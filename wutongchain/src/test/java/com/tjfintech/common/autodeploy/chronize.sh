#! /bin/bash

#rm -f main.ini
#rm -f mem.ini
#rm -f sub.ini
#rm -f cli.ini


function getclusterips(){

touch temp/tempcluster.ini


#从setting获取所有的集群IP列表（去重复IP,去掉本机IP）
while read -r line
do

 if [ -z "$line" ]
   then continue
 fi
 
 if [[ $line = *[* ]]
   then 
      #echo $line
      continue
 fi
 
  if [[ $line = 172.16.7.3 ]]
   then 
      #echo $line
      continue
 fi

  if [ `grep -c "$line" temp/tempcluster.ini` -eq '0' ];then
   	   echo $line >> temp/tempcluster.ini
  fi
  
done < setting.ini
}

#将一些辅助的sh文件拷贝至集群列表IP的根目录
function copyfile(){
while read -r line
do
  #scp -r sh/deleteDir.sh $line:/root 
  #scp -r ./mkdir.sh $line:/root 
  #scp -r sh/kill.sh $line:/root 
  scp -r $1 $line:/root 
  echo $line
done < temp/tempcluster.ini
}


getclusterips

copyfile ntp/ntpclient.sh

while read -r line
do
  ssh root@$line "./ntpclient.sh" &
  echo $line
done < temp/tempcluster.ini