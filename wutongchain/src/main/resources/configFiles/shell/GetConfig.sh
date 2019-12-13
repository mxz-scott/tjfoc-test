#!/bin/sh
    
   iniFile=$1;
   section=$2;
   option=$3;
   value=`awk -F '=' "/\[${section}\]/{a=1}a==1" ${iniFile}|sed -e '1d' -e '/^$/d' -e 's/^[ \t]*//g' -e '/^\[.*\]/,$d' -e "/^${option}.*=.*/!d" -e "s/^${option}[ ]*= *//"`
   echo $value


