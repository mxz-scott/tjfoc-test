#! /bin/bash

#rm -f main.ini
#rm -f mem.ini
#rm -f sub.ini
#rm -f cli.ini


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


function addCliConf(){
	echo -e "KeyPath: \"./cert\/key.pem" > $tempdir/tempcliconf.yaml
	echo -e "PukPath: \"./cert\"" >> $tempdir/tempcliconf.yaml
	echo -e "Address: \"$1\"" >> $tempdir/tempcliconf.yaml
}

function genSelfConf(){
	echo "Self:" > $tempdir/tempself.yaml
    echo -e "     Id: \"$1\"" >> $tempdir/tempself.yaml
    echo -e "     Addr: \"$2:$3\"" >> $tempdir/tempself.yaml
    echo -e "     RpcPort: $4" >> $tempdir/tempself.yaml
    echo -e "     KeyPath: \"./cert\"" >> $tempdir/tempself.yaml
    echo -e "     Group: \"$5\"" >> $tempdir/tempself.yaml
}

function creatDir(){
	touch $tempdir/cluster.ini
	while read -r line
	do

	if [ -z "$line" ]
		then continue
	fi
 
	if [[ $line = *[* ]]
		then 
			continue
	fi

	if [ `grep -c "$line" $tempdir/cluster.ini` -eq '0' ];then
		echo $line >> $tempdir/cluster.ini
	fi
  	done < setting.ini

	while read -r line
	do
		scp -r $shdir/deleteDir.sh $line:/root 
	
	done < $tempdir/cluster.ini
	
	#先删除部署目录，再重新创建文件重新部署
	while read -r line
	do
		ssh root@$line "./deleteDir.sh $1" &
	
	done < $tempdir/cluster.ini
	
	while read -r line
	do
		scp -r $shdir/mkdir.sh $line:/root 
	
	done < $tempdir/cluster.ini
	
	
	while read -r line
	do
		ssh root@$line "./mkdir.sh $1" &
	
	done < $tempdir/cluster.ini
}

#######################################################################
starttime=$(date +%Y-%m-%d\ %H:%M:%S)
echo $starttime

#所有程序文件所在目录
dir=pt
cp pt/server/peer pt/server/peerMB

#需要拷贝至部署节点的文件，包括kill.sh mkdir.sh start.sh
shdir=sh

#临时文件解析目录
tempdir=temp

#节点部署目录
deploydir=/root/mbfttest

#定义各个不同节点类型的起始tcp port
mainTcpPort=41000
memTcpPort=42000
subTcpPort=43000

#定义各个不同节点类型的起始rpc port
mainRpcPort=51000
memRpcPort=52000
subRpcPort=53000

#收集所有机器部署目录以便copy文件
scpPathNo=0;
declare -a scpServerPathArr
declare -a serverIPs
declare -a serverPaths
declare -a memIPRpcPort

declare -a scpCliPathArr
declare -a cliIPs
declare -a cliPaths

#######################################################################

creatDir $deploydir


rm -rf temp
if [ ! -d "temp" ];then
    mkdir temp
fi

#将目录下所有的节点及客户端执行文件赋可执行权限
find . -name peerMB |xargs chmod +x
find . -name peer |xargs chmod +x
find . -name cli |xargs chmod +x


#在各个节点机器上创建部署目录


#分别获取各个section的位置
no1=`cat -n setting.ini |grep main|awk '{print $1}'`
no2=`cat -n setting.ini |grep mem|awk '{print $1}'`
no3=`cat -n setting.ini |grep sub|awk '{print $1}'`
no4=`cat -n setting.ini |grep cli|awk '{print $1}'`
no5=`cat setting.ini | wc -l`

sed -n ''$(($no1+1))','$(($no2-1))'p' setting.ini > $tempdir/main.ini
sed -n ''$(($no2+1))','$(($no3-1))'p' setting.ini > $tempdir/mem.ini
sed -n ''$(($no3+1))','$(($no4-1))'p' setting.ini > $tempdir/sub.ini
sed -n ''$(($no4+1))','$(($no5-1))'p' setting.ini > $tempdir/cli.ini


#初始化tempconf.yaml
initConf

#为检查进程启动将所有的ip+tcp ip+rpc保存
if [ -f $tempdir/iptcpports.ini ]; then
  rm -f $tempdir/iptcpports.ini
fi

if [ -f $tempdir/iprpcports.ini ]; then
  rm -f $tempdir/iprpcports.ini
fi

touch $tempdir/iptcpports.ini #为后面检查进程是否开启
touch $tempdir/iprpcports.ini #为后面检查进程是否开启

#添加MainGroup信息，拷贝sub文件以及self.yaml至配置的sub主机目录
offset=1
echo "MainGroup:" >> $tempdir/tempconf.yaml
while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi

  scp -r $dir/server $line:$deploydir/main$offset  
  addConf main$offset $line $(($mainTcpPort+$offset)) 
  genSelfConf main$offset $line $(($mainTcpPort+$offset)) $(($mainRpcPort+$offset)) main

  echo $line $(($mainTcpPort+$offset)) >> $tempdir/iptcpports.ini
  echo $line $(($mainRpcPort+$offset)) >> $tempdir/iprpcports.ini

  scp -r $tempdir/tempself.yaml $line:$deploydir/main$offset/self.yaml
  
  scpServerPathArr[scpPathNo]=$line:$deploydir/main$offset/
  serverIPs[scpPathNo]=$line
  serverPaths[scpPathNo]=$deploydir/main$offset/
  
  ((scpPathNo++))
  ((offset++))
done < $tempdir/main.ini


#添加Members信息，拷贝sub文件以及self.yaml至配置的sub主机目录
offset=1
index=0
echo "Members:" >> $tempdir/tempconf.yaml
while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi
 
 
  scp -r $dir/server $line:$deploydir/mem$offset
  addConf mem$offset $line $(($memTcpPort+$offset)) 
  genSelfConf mem$offset $line $(($memTcpPort+$offset)) $(($memRpcPort+$offset)) mem

  echo $line $(($memTcpPort+$offset)) >> $tempdir/iptcpports.ini
  echo $line $(($memRpcPort+$offset)) >> $tempdir/iprpcports.ini
  
  scp -r $tempdir/tempself.yaml $line:$deploydir/mem$offset/self.yaml
  
  scpServerPathArr[scpPathNo]=$line:$deploydir/mem$offset/
  serverIPs[scpPathNo]=$line
  serverPaths[scpPathNo]=$deploydir/mem$offset/  
  memIPRpcPort[index]=$line:$(($memRpcPort+$offset))
  #echo ${memIPRpcPort[index]}
  ((scpPathNo++))
  ((offset++))
  ((index++))
  
done < $tempdir/mem.ini



#编辑cliconf.yaml信息，拷贝cli文件以及cliconf.yaml至配置的cli主机目录
offset=1
index=0

while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi

  echo $dir/client
  echo $line:$deploydir/cli$offset
  scp -r $dir/client $line:$deploydir/cli$offset
  #echo "fffffff"
  addCliConf ${memIPRpcPort[index]}  
  scp -r $tempdir/tempcliconf.yaml $line:$deploydir/cli$offset/cliconf.yaml
  echo  ${memIPRpcPort[index]}

  scpCliPathArr[index]=$line:$deploydir/cli$offset/
  cliIPs[index]=$line
  cliPaths[index]=$deploydir/cli$offset/  
  
  ((offset++))
  ((index++))
done < $tempdir/cli.ini


#echo "33333333333"

#添加SubGroup信息，拷贝sub文件以及self.yaml至配置的sub主机目录
offset=1
mNo=0
sNo=1
echo "SubGroup:" >> $tempdir/tempconf.yaml
while read -r line
do
 #echo $line
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
  
  scp -r $dir/server $line:$deploydir/sub${mNo}_${sNo}
  addConf sub${mNo}_${sNo} $line $(($subTcpPort+$offset)) 
  genSelfConf sub${mNo}_${sNo} $line $(($subTcpPort+$offset)) $(($subRpcPort+$offset)) sub
 
  echo $line $(($subTcpPort+$offset)) >> $tempdir/iptcpports.ini
  echo $line $(($subRpcPort+$offset)) >> $tempdir/iprpcports.ini

  scp -r $tempdir/tempself.yaml $line:$deploydir/sub${mNo}_${sNo}/self.yaml
  
  scpServerPathArr[scpPathNo]=$line:$deploydir/sub${mNo}_${sNo}/
  serverIPs[scpPathNo]=$line
  serverPaths[scpPathNo]=$deploydir/sub${mNo}_${sNo}/
  
  ((scpPathNo++))
  ((sNo++))
  ((offset++))
done < $tempdir/sub.ini


#替换部署节点中的所有的conf.yaml文件
for path in ${scpServerPathArr[@]}
  do
     scp -r $tempdir/tempconf.yaml ${path}conf.yaml
     scp $shdir/kill.sh ${path}
	 scp $shdir/start.sh ${path}
  done
  
for path in ${scpCliPathArr[@]}
  do
     scp $shdir/kill.sh ${path}
	 scp $shdir/start.sh ${path}
  done


#杀掉已有的peerMB进程，之后启动peerMB进程
for i in ${!serverIPs[*]};
  do
    #echo ${serverIPs[$i]} ${serverPaths[$i]}
	ssh root@${serverIPs[$i]} "cd ${serverPaths[$i]};./kill.sh peerMB"	
	echo "complete kill @${serverIPs[$i]} peerMB process"
  done
  
sleep 1

for i in ${!serverIPs[*]};
  do
	ssh root@${serverIPs[$i]} "cd ${serverPaths[$i]};./start.sh ${serverPaths[$i]} peerMB >console.log 2>&1 &"	
	echo "complete start @${serverIPs[$i]}:${serverPaths[$i]}peerMB"
	sleep 1
  done

  
ttime=`date +"%Y-%m-%d %H:%M:%S"`
echo start time :$starttime
echo end time   :$ttime
