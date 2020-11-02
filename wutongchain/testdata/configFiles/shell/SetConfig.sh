#!/bin/sh
    
   iniFile=$1
   section=$2
   option=$3
   value=$4

   sed -i -e '/^[ ]*#/d' ${iniFile}
   exist=$(grep -n ${section} ${iniFile})
   if [ -z "$exist" ]; then
       sed -i '$a \['$section'\]' ${iniFile}
       echo "add section " ${section}
   fi

   awk "/\[${section}\]/{a=1}a==1" ${iniFile}|sed -e '1d' -e '/^$/d'  -e 's/[ \t]*$//g' -e 's/^[ \t]*//g' -e '/\[/,$d'|grep "${option}.\?=">/dev/null

  if [ "$?" = "0" ];then
       sectionNum=$(sed -n -e "/\[${section}\]/=" ${iniFile})
       sed -i "${sectionNum},/^\[.*\]/s/\(${option}.\?=\).*/\1 ${value}/g" ${iniFile}
       echo "[success] update [$iniFile][$section][$option][$value]"
   else
       sed -i "/^\[${section}\]/a\\${option} = ${value}" ${iniFile}
       echo "[success] add [$iniFile][$section][$option][$value]"
   fi




