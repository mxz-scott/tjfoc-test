#! /bin/bash

#rm -f main.ini
#rm -f mem.ini
#rm -f sub.ini
#rm -f cli.ini

if [ ! -d "temp" ];then
    mkdir temp
fi

dir=pt
tempdir=temp


declare -a mainIP
declare -a memIP
declare -a subIP
declare -a cliIP

no1=`cat -n setting.ini |grep main|awk '{print $1}'`
no2=`cat -n setting.ini |grep mem|awk '{print $1}'`
no3=`cat -n setting.ini |grep sub|awk '{print $1}'`
no4=`cat -n setting.ini |grep cli|awk '{print $1}'`

no5=`cat setting.ini | wc -l`

sed -n ''$(($no1+1))','$(($no2-1))'p' setting.ini > $tempdir/main.ini
sed -n ''$(($no2+1))','$(($no3-1))'p' setting.ini > $tempdir/mem.ini
sed -n ''$(($no3+1))','$(($no4-1))'p' setting.ini > $tempdir/sub.ini
sed -n ''$(($no4+1))','$(($no5-1))'p' setting.ini > $tempdir/cli.ini




while read -r line
do
 echo $line
 if [ -z "$line"]
   then continue
 fi 
  scp -r $dir/main $line:/root/zll/
done < $tempdir/main.ini


#echo ${#array[@]}
#
#for var in ${array[*]}
#do 
# echo $var
#done  

