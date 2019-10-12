#!/bin/bash
ntpserver=172.16.7.3

apt install ntp
apt install ntpdate

str=`grep "nameserver 8.8.8.8" /etc/resolv.conf`
if [ -z "$str" ];then
	echo "nameserver 8.8.8.8" >>/etc/resolv.conf
fi


str=`grep "ntp  123/tcp" /etc/services`
if [ -z "$str" ];then
	echo "ntp  123/tcp" >>/etc/services
fi

str=`grep "ntp  123/udp" /etc/services`
if [ -z "$str" ];then
	echo "ntp  123/udp" >>/etc/services
fi

rm -rf /etc/localtime
ln -s /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

#其他服务器设定
service ntp stop
ntpdate -u $ntpserver
#设定定时任务
str=`grep "$ntpserver" /var/spool/cron/crontabs/root`
if [ -z "$str" ];then
    echo "0 23 * * * /usr/sbin/ntpdate -u $ntpserver" >>/var/spool/cron/crontabs/root
fi
echo complete
