#!/bin/bash

apt install ntp
apt install ntpdate

str=`grep "ntp  123/tcp" /etc/services`
if [ -z "$str" ];then
	echo "ntp  123/tcp" >>/etc/services
fi

str=`grep "ntp  123/udp" /etc/services`
if [ -z "$str" ];then
	echo "ntp  123/udp" >>/etc/services
fi

cp /etc/localtime /etc/localtime1
rm -rf /etc/localtime
ln -s /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

#服务器A为时间服务器
service ntp start


#加入以下配置
str=`grep "server 127.127.1.0" /etc/ntp.conf`
if [ -z "$str" ];then
   echo "server 127.127.1.0" >>/etc/ntp.conf
   echo "fudge 127.127.1.0 stratum 10" >>/etc/ntp.conf
fi

service ntp restart
