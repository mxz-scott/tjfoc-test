#! /bin/bash

#rm -f main.ini
#rm -f mem.ini
#rm -f sub.ini
#rm -f cli.ini

##############################################################
function initConf(){
	echo "BlockChain:" > $tempdir/tempconf.yaml
	echo -e "     BlockPackTime: 10000" >> $tempdir/tempconf.yaml
	echo -e "     SubPackTime: 1000" >> $tempdir/tempconf.yaml
	echo -e "     SubBlockSizeKB: 100" >> $tempdir/tempconf.yaml
	echo -e "     P2PCycle: 3000" >> $tempdir/tempconf.yaml  
	echo -e "" >> $tempdir/tempconf.yaml

}

function addConf(){
	echo -e "     - Id: \"$1\"" >> $tempdir/tempconf.yaml
	echo -e "       Addr: \"$2:$3\"" >> $tempdir/tempconf.yaml
	echo -e "       KeyPath: \"./cert\"" >> $tempdir/tempconf.yaml
}
##############################################################


#所有程序文件所在目录
dir=update

#临时文件解析目录
tempdir=tempnew

#节点部署目录
deploydir=/root/mbfttest


#收集所有机器部署目录以便copy文件
scpPathNo=0;
declare -a scpServerPathArr
declare -a serverIPs
declare -a serverPaths
declare -a memIPTcpPort

declare -a scpCliPathArr
declare -a cliIPs
declare -a cliPaths


#定义各个不同节点类型的起始tcp port
mainTcpPort=41000
memTcpPort=42000
subTcpPort=43000

#定义各个不同节点类型的起始rpc port
mainRpcPort=51000
memRpcPort=52000
subRpcPort=53000


############################################################

if [ ! -d "$tempdir" ];then
    mkdir $tempdir
fi

#将目录下所有的节点及客户端执行文件赋可执行权限
find . -name peer |xargs chmod +x
find . -name cli |xargs chmod +x

#分别获取各个section的位置
no1=`cat -n setting.ini |grep main|awk '{print $1}'`
no2=`cat -n setting.ini |grep mem|awk '{print $1}'`
no3=`cat -n setting.ini |grep sub|awk '{print $1}'`
no4=`cat -n setting.ini |grep cli|awk '{print $1}'`
no5=`cat -n setting.ini |grep conf|awk '{print $1}'`
no6=`cat setting.ini | wc -l`

sed -n ''$(($no1+1))','$(($no2-1))'p' setting.ini > $tempdir/main.ini
sed -n ''$(($no2+1))','$(($no3-1))'p' setting.ini > $tempdir/mem.ini
sed -n ''$(($no3+1))','$(($no4-1))'p' setting.ini > $tempdir/sub.ini
sed -n ''$(($no4+1))','$(($no5-1))'p' setting.ini > $tempdir/cli.ini

#初始化tempconf.yaml
#initConf
sed -n ''$(($no5+1))','$(($no6))'p' setting.ini > $tempdir/tempconf.yaml
echo -e "" >> $tempdir/tempconf.yaml

#获取MainGroup节点目录相关信息
offset=1
echo "MainGroup:" >> $tempdir/tempconf.yaml
while read -r line
do

 if [ -z "$line" ]
   then continue
 fi

  addConf main$offset $line $(($mainTcpPort+$offset)) 
 
  scpServerPathArr[scpPathNo]=$line:$deploydir/main$offset/
  serverIPs[scpPathNo]=$line
  serverPaths[scpPathNo]=$deploydir/main$offset/
  
  ((scpPathNo++))
  ((offset++))
done < $tempdir/main.ini


#获取Members节点目录相关信息
offset=1
index=0
echo "Members:" >> $tempdir/tempconf.yaml
while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi
  
  addConf mem$offset $line $(($memTcpPort+$offset)) 
  
  scpServerPathArr[scpPathNo]=$line:$deploydir/mem$offset/
  serverIPs[scpPathNo]=$line
  serverPaths[scpPathNo]=$deploydir/mem$offset/  
  memIPTcpPort[index]=$line:$(($memRpcPort+$offset))

  ((scpPathNo++))
  ((offset++))
  ((index++))
  
done < $tempdir/mem.ini



#获取cli节点目录相关信息
offset=1
index=0

while read -r line
do

 if [ -z "$line" ]
   then continue
 fi

  scpCliPathArr[index]=$line:$deploydir/cli$offset/
  cliIPs[index]=$line
  cliPaths[index]=$deploydir/cli$offset/  
  
  ((offset++))
  ((index++))
done < $tempdir/cli.ini




#获取sub节点目录相关信息
offset=1
mNo=0
sNo=1
echo "SubGroup:" >> $tempdir/tempconf.yaml
while read -r line
do

 if [ -z "$line" ]
   then continue
 fi
 
 if [[ $line == *dsb* ]]
   then 
      sNo=1
	  ((mNo++))
      echo "    -" >> $tempdir/tempconf.yaml	  
	  continue
 fi
  
  addConf sub${mNo}_${sNo} $line $(($subTcpPort+$offset)) 
  
  scpServerPathArr[scpPathNo]=$line:$deploydir/sub${mNo}_${sNo}/
  serverIPs[scpPathNo]=$line
  serverPaths[scpPathNo]=$deploydir/sub${mNo}_${sNo}/
  
  ((scpPathNo++))
  ((sNo++))
  ((offset++))
done < $tempdir/sub.ini


#杀掉已有的peerMB进程，之后启动peerMB进程
for i in ${!serverIPs[*]};
  do
    #echo ${serverIPs[$i]} ${serverPaths[$i]}
	ssh root@${serverIPs[$i]} "cd ${serverPaths[$i]};./kill.sh peerMB"	
	echo "complete kill @${serverIPs[$i]} peerMB process"
  done

  sleep 30
  
#替换部署节点中的所有的conf.yaml文件
for path in ${scpServerPathArr[@]}
  do
     scp -r $tempdir/tempconf.yaml ${path}conf.yaml
  done

  

for i in ${!serverIPs[*]};
  do
	ssh root@${serverIPs[$i]} "cd ${serverPaths[$i]};./start.sh ${serverPaths[$i]} peerMB >console.log 2>&1 &"	
	echo "complete start @${serverIPs[$i]}:${serverPaths[$i]}peerMB"
	sleep 1
  done
  