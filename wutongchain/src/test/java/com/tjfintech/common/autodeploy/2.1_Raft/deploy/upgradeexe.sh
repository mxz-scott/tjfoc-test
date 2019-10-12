#! /bin/bash

###########################变量定义###########################
#所有程序文件所在目录
workdir=$(cd $(dirname $0); pwd)
dir=$workdir/bin
localpeerDir=wtchain
localpeerName=wtchain
localsdkDir=wtsdk
localsdkName=wtsdk
localtoolDir=wttool
localtoolName=wttool
certdir=$workdir/cert

licdir=$workdir/lic
peerCert=peer
sdkCert=sdk
toolCert=tool
peerCACert=cacert

#节点部署目录 仅支持创建最后一级目录，即主机当前必须存在deployRootdir目录
deployRootdir=/root
deploydir=$deployRootdir/ate
if [[ ${deployRootdir//\//}  == ${deploydir//\//} ]];then
   echo "\"deploydir\" should not be equal to \"deployRootdir\" for rm file risk"
   exit
fi


remotepeerDir=wtchain
remotepeerName=wtpeer
remotesdkDir=wtsdk
remotesdkName=wtcli
remotetoolDir=wttool
remotetoolName=wtkit

walletEnabled=false
remoteSDKDBProvider=mysql
remoteSDKDBPath="root:root@tcp(10.1.3.246:3306)/wallet210?charset=utf8"

peerCAEnabled=false
peerCAAddr=10.1.3.224:9001

contractEnabled=true

#是否自动生成证书peer.lic
licAuto=true

#定义是否所有节点使用同一个port端口
#same=1
#定义节点tcp port
peerTcpPort=50030

#定义节点rpc port
peerRpcPort=9800

#定义sdk使用的port接口
sdkPort=9999


#定义数组存放指定数据
declare -a peerIPs
declare -a peerPaths

declare -a sdkIPs
declare -a sdkPaths


#临时文件解析目录
tempdir=temp


#将需要处理的配置文件备份一份至temp目录下
remoteconfbase=$tempdir/basetemp.toml
remotesdkconf=$tempdir/sdkconfigtemp.toml
remotepeerconfig=$tempdir/peerconfigtemp.toml


###########################函数定义###########################
 ##//-----------------------------------------------------------------------------//
 function chkExpect(){
  chk=`which expect`

  if [ -z "$chk" ];then
	sys=`hostnamectl`
	echo $sys
	if [[ $sys == *"Ubuntu"* ]];then
	   sudo apt-get install tcl tk expect
	elif [[ $sys == *"CentOS"* ]];then
	   yum install expect
	else
	   echo "unsupport system please install expect first"
	   exit	   
	fi
  fi
 }

 
 
 ##//-----------------------------------------------------------------------------//
 #部署之前先将目标主机节点及sdk进程关闭，否则可能会对数据产生影响
 #此操作需要在createDir之前
 function preKillProcessIPFromFile(){
	while read -r line
	do
	 #空行跳过不处理
	 if [ -z "$line" ]
		then continue
	 fi
 
	 echo "********************kill $1 process********************"
	 killprocess="ps -ef |grep $1 |grep -v grep |awk '{print \$2}'|xargs kill -9"
	 checkpces="ps -ef |grep $1 |grep -v grep "
	 ./ssh.sh $line "$checkpces"	
	 ./ssh.sh $line "$killprocess"	
  
	done < $2
 }
 
 ##//-----------------------------------------------------------------------------//
 #设置echo显示字体突出颜色 天蓝色字
 function echoblue(){
    echo -e "\033[36m $1 \033[0m"
 }
 
 #设置echo显示字体突出颜色 红字
 function echored(){
    echo -e "\033[31m $1 \033[0m"
 }
 
 #设置echo显示字体突出颜色 红底白字
 function echowhitered(){
    echo -e "\033[41;37m $1 \033[0m"
 }
 
 
 ##//-----------------------------------------------------------------------------// 
 function parseSettingIni(){
    #预先处理获取节点、sdk及管理工具部署主机IP配置信息，分别获取各个section的位置
	#删除setting.ini中的空行，之后再在末尾添加一行 
	grep -v "^$" setting.ini
	echo -e >> setting.ini

	no1=`cat -n setting.ini |grep peer|awk '{print $1}'`
	no2=`cat -n setting.ini |grep sdk|awk '{print $1}'`
	no3=`cat -n setting.ini |grep tool|awk '{print $1}'`
	no4=`cat -n setting.ini |grep type|awk '{print $1}'`

	if [[ -z $no4 ]];then
		no4=`cat setting.ini | wc -l`
	else
		no5=`cat setting.ini | wc -l`
	fi

	sed -n ''$(($no1+1))','$(($no2-1))'p' setting.ini > $tempdir/peer.ini
	sed -n ''$(($no2+1))','$(($no3-1))'p' setting.ini > $tempdir/sdk.ini

	if [[ -n $no5 ]];then
		sed -n ''$(($no3+1))','$(($no4-1))'p' setting.ini > $tempdir/tool.ini
		sed -n ''$(($no4+1))','$(($no5))'p' setting.ini > $tempdir/type.ini
	else
		sed -n ''$(($no3+1))','$(($no4))'p' setting.ini > $tempdir/tool.ini
	fi
  
 }
 
 ##//-----------------------------------------------------------------------------//
 
 
 
######======================================处理执行========================================######
echo  ====================================== start ======================================
#预先处理
#起始执行时间
starttime=$(date '+%Y-%m-%d %H:%M:%S')
echo =====================start time $starttime=====================

#检查是否安装expect工具，若未安装则安装
chkExpect

rm -rf $tempdir

mkdir $tempdir

#将需要处理的配置文件备份一份至temp目录下
cp $dir/$localpeerDir/conf/base.toml  $remoteconfbase
cp $dir/$localsdkDir/conf/config.toml  $remotesdkconf


#rm -f $tempdir/id_*

#将目录下所有的节点及客户端执行文件赋可执行权限
find . -name $localpeerName |xargs chmod +x
find . -name $localsdkName |xargs chmod +x
find . -name $localtoolName |xargs chmod +x
find . -name license |xargs chmod +x 
 
#将setting.ini文件中的各个项分别另存到另外一个对应的文件中
#setting.ini --> peer.ini sdk.ini tool.ini type.ini
parseSettingIni

#先将节点及sdk进程关闭
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~pre kill~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
preKillProcessIPFromFile $remotepeerName $tempdir/peer.ini
preKillProcessIPFromFile $remotesdkName $tempdir/sdk.ini


#########################################################################
#拷贝wttool文件夹中的可执行文件至每个节点主机的对应目录
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~scp wttool files~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
#offset=0
scpPathNo=0
toolNo=1
while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi
  
  ./transfer.sh $dir/$localtoolDir/$localtoolName $line $deploydir/$remotetoolDir/$remotetoolName
 
  toolIPs[scpPathNo]=$line
  toolPaths[scpPathNo]=$deploydir/$remotetoolDir/
  
  ((toolNo++))
  ((scpPathNo++))

done < $tempdir/tool.ini


#########################################################################
#拷贝wtchain执行文件至配置的peer主机目录并启动
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~copy wtchain files~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
#offset=0
#peerNo=1
scpPathNo=0;
echo "[Members]" > $remotepeerconfig
while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi
  
   #传送节点执行文件
  ./transfer.sh $dir/$localpeerDir/$localpeerName $line $deploydir/$remotepeerDir/$remotepeerName
  ./ssh.sh $line "cd $deploydir/$remotepeerDir;./$remotepeerName start -d"

  peerIPs[scpPathNo]=$line
  peerPaths[scpPathNo]=$deploydir/$remotepeerDir/
  #((peerNo++))
  ((scpPathNo++))
done < $tempdir/peer.ini

sleep 10
#########################################################################
#拷贝sdk执行文件并启动
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~copy sdk exe~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
#offset=0
index=0
#sdkNo=1

while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi
  ./transfer.sh $dir/$localsdkDir/$localsdkName $line $deploydir/$remotesdkDir/$remotesdkName
  ./ssh.sh $line "cd $deploydir/$remotesdkDir;./$remotesdkName start -d"

  sdkIPs[index]=$line
  sdkPaths[index]=$deploydir/$remotesdkDir 
  
  #((sdkNo++))
  ((index++))
done < $tempdir/sdk.ini

  
#########################################################################
#echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~start peer process~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
#启动所有节点
#for i in ${!peerPaths[@]}
#  do
#     ./ssh.sh ${peerIPs[$i]} "cd ${peerPaths[$i]};./$remotepeerName start -d"
#  done



#########################################################################
#通过wtpeer test指令检查节点情况,需要查看脚本执行信息  红底白字
echowhitered "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~wtpeer test status~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
./ssh.sh ${peerIPs[0]} "cd ${peerPaths[0]};./$remotepeerName test"
./ssh.sh ${peerIPs[1]} "cd ${peerPaths[1]};./$remotepeerName test"


endtime=$(date '+%Y-%m-%d %H:%M:%S')  
echo =====================execute period:$starttime ~~~~~ $endtime=====================   
echo  ====================================== complete ======================================

