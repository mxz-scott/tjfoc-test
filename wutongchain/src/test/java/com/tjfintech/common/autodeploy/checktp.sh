#! /bin/bash
if [ -f temp/chkrslt.ini ]; then
  rm -f temp/chkrslt.ini
fi
touch temp/chkrslt.ini

declare -a arr
index=0

while read -r line
 do  
  arr[index]=$line
  ((index++))
done < temp/iprpcports.ini

for i in ${!arr[*]};
  do
     nc -v ${arr[$i]} &
	 #echo ${arr[$i]}
  done

index=0
while read -r line
 do  
  arr[index]=$line
  ((index++))
done < temp/iptcpports.ini

for i in ${!arr[*]};
  do
     nc -v ${arr[$i]} &
	 #echo ${arr[$i]}
  done
