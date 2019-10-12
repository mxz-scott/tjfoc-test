# /bin/bash

rm -f peer.lic

sys=`hostnamectl`
#echo $sys
if [[ $sys == *"Ubuntu"* ]];then
   echo "\033[34m Ubuntu System \033[0m"
   IP=`ifconfig|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:"|sed -n '1p'`
   MAC=`ifconfig | grep HWaddr|awk '{print $5}'|sed -n '1p'`
elif [[ $sys == *"CentOS"* ]];then
   echo "\033[34m CentOS System \033[0m"
   IP=`ifconfig|grep inet|grep -v 127.0.0.1|grep -v inet6|awk '{print $2}'|tr -d "addr:"|sed -n '1p'`
   MAC=`ifconfig | grep ether|awk '{print $2}'|sed -n '1p'`
else
   echo "\033[31m unsupport system \033[0m"
   exit	   
fi

Ver=2.1

./license create -m $MAC -d 36500
#cp peer.lic ../peer/
#cp peer.lic $1
