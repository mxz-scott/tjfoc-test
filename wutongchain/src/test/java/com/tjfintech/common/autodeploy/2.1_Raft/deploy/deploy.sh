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
deploydir=$deployRootdir/ake
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

walletEnabled=true
remoteSDKDBProvider=mysql
remoteSDKDBPath="root:root@tcp(10.1.3.162:3306)/wallet210?charset=utf8"

peerCAEnabled=false
peerCAAddr=10.1.3.224:9001

contractEnabled=true
consenType=0

#是否自动生成证书peer.lic
licAuto=true

#定义是否所有节点使用同一个port端口
#same=1
#定义节点tcp port
peerTcpPort=56000

#定义节点rpc port
peerRpcPort=9600

#定义sdk使用的port接口
sdkPort=9999


#定义数组存放指定数据
declare -a peerIPs
declare -a peerPaths

declare -a sdkIPs
declare -a sdkPaths

declare -a types

declare -a users
declare -a passwds

declare -A usersMap
declare -A pwdsMap



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
 #添加节点集群信息，即节点config.toml	
 function addConf(){
	echo -e "[[Members.Peers]]" >> $remotepeerconfig
	echo -e "Id = \"empty$2\"" >> $remotepeerconfig
	echo -e "ShownName = \"$1\"" >> $remotepeerconfig
	echo -e "Type = $4" >> $remotepeerconfig
	echo -e "Addr = \"/ip4/$2/tcp/$3\"" >> $remotepeerconfig
	echo -e >> $remotepeerconfig
 }

 ##//-----------------------------------------------------------------------------//
 #根据节点信息添加节点集群信息
 function addSDKconfCluster(){
	echo -e "[[Peer]]" >> $tempdir/tempsdkcluster.toml
	echo -e "Address = \"$1\"" >> $tempdir/tempsdkcluster.toml
	echo -e "TLSEnabled = \"true\"" >> $tempdir/tempsdkcluster.toml
	echo -e >> $tempdir/tempsdkcluster.toml
 }

 ##//-----------------------------------------------------------------------------//
 #创建部署目录
 function creatDir(){
	touch $tempdir/temp.ini
	while read -r line
	do

	if [ -z "$line" ]
		then continue
	fi
 
	if [[ $line = *[* ]]
		then 
			continue
	fi
	
	if [[ $line = *[type]* ]]
		then 
			break
	fi

	if [ `grep -c "$line" $tempdir/temp.ini` -eq '0' ];then
		echo $line >> $tempdir/temp.ini
	fi
  
	done < setting.ini

	while read -r line
	do
		#./transfer.sh ./mkdir.sh $line /root 
		./ssh.sh $line "rm -rf $1" ${usersMap[$line]} ${pwdsMap[$line]}
		./ssh.sh $line "rm -rf $1" ${usersMap[$line]} ${pwdsMap[$line]}
		./ssh.sh $line "mkdir $1" ${usersMap[$line]} ${pwdsMap[$line]}
	
	done < $tempdir/temp.ini
 }

 ##//-----------------------------------------------------------------------------//
 #替换指定文件中的tcp/rpc port CA info
 function matchBaseInfo(){

	replaceAndInsert "TcpPort" $1 $remoteconfbase
	replaceAndInsert "RpcPort" $2 $remoteconfbase
	
	contractlineno=`grep -n Contract ${remoteconfbase} | cut -d ":" -f 1`
	sed -i -e ''$((contractlineno+1))'d' $remoteconfbase
	sed -i ''$((contractlineno+1))'i Enabled = '$3'' $remoteconfbase
	
 }
 
 #此func仅适用于被赋值的值为整数
 function replaceAndInsert(){

    lineno=`grep -n $1 $3 | cut -d ":" -f 1`
	sed -i -e ''$lineno'd' $3
	sed -i ''$lineno'i '$1' = '$2'' $3
 }

 ##//-----------------------------------------------------------------------------//
 function updateSDKPeerClusterInfo(){
	sed -i -e '/[[Peer]]/d' $remotesdkconf
	sed -i -e '/Address/d' $remotesdkconf
	sed -i -e '/TLSEnabled/d' $remotesdkconf
	echo "***********************************************"
	echo $tempdir/tempsdkcluster.toml
	echo $remotesdkconf
	sed -i '2 r temp/tempsdkcluster.toml' $remotesdkconf	
 }

 ##//-----------------------------------------------------------------------------//
 function replaceCert(){
 #参数1为部署目录中指定证书目录名称 参数2为远程主机IP 参数3位传送至远程主机目录
 #参数4和5分别是远程主机账户密码
	./transfer.sh "$certdir/$1/ca.pem" $2 $3 $4 $5
	./transfer.sh "$certdir/$1/cert.pem" $2 $3 $4 $5
	./transfer.sh "$certdir/$1/key.pem" $2 $3 $4 $5
	./transfer.sh "$certdir/$1/pubkey.pem" $2 $3 $4 $5
 }
 
 ##//-----------------------------------------------------------------------------// 
 function replacePeerId(){
	
	#replace peer id information
	#参数1是远程主机IP；参数2是匹配关键字 ID；参数3是进入节点目录的指令:cd /root/zll/ate/wtchain1，参数4是节点init命令：./peer init
	#参数5 和参数6分别是主机的账户密码
	./getRemoteInfo.sh "$1" "$2" "$3" "$4" "$5" "$6"
	sed -i 's/[\r]//g' $tempdir/${2}_$1
	echo **********************replacePeerId**************************
	peerId=`grep $1 $tempdir/${2}_$1 | cut -d ":" -f 2`
	echo $peerId

	file=$tempdir/peerconfigtemp.toml
	
	#配置文件中初始默认设置节点id为temp10.1.3.240的形式
	sed -i 's/'empty$1'/'${peerId}'/g' $file
	#替换后可能存在dos下的换行，将其删除处理
	sed -i 's/[\r]//g' $file	
 }
 
 ##//-----------------------------------------------------------------------------// 
 #需要在tranfer $remoteconfbase至各个节点之前操作（拷贝管理工具之后操作）
 function replaceAdminId(){
  #获取管理工具ID并替换节点base文件中的Admin
  echo **********************replaceAdminId**************************
   #参数1是远程主机IP；参数2是匹配关键字 id；参数3是进入节点目录的指令:cd /root/zll/ate/wttool1，参数4是节点getid命令：./wtkit getid -p crypt/key.pem
   #参数5 和参数6分别是主机的账户密码
  ./getRemoteInfo.sh "$1" "$2" "$3" "$4" "$5" "$6"
    #替换文件中可能存在的行尾换行符
    sed -i 's/[\r]//g' $tempdir/${2}_$1
    toolId=`grep $1 $tempdir/${2}_$1 | cut -d ":" -f 2`
	echo $toolId
    idlineno=`grep -n Admin $remoteconfbase | cut -d ":" -f 1`
	sed -i -e ''$idlineno'd' $remoteconfbase
	sed -i ''$idlineno'i Admin = \"'$toolId'\"' $remoteconfbase
	
	#替换后可能存在dos下的换行，将其删除处理
	sed -i 's/[\r]//g' $remoteconfbase
 }
 
 ##//-----------------------------------------------------------------------------//
 #替换sdk配置文件中的http端口号(参数$1)
 function replaceSDKHttpPort(){
    echo **********************replaceSDKHttpPort**************************
    lineno=`grep -n Port $remotesdkconf | cut -d ":" -f 1`
	sed -i -e ''$lineno'd' $remotesdkconf
	#sed -i '1i Port = '$1'' $remotesdkconf
	sed -i ''$lineno'i Port = '$1'' $remotesdkconf
	#替换后可能存在dos下的换行，将其删除处理
	sed -i 's/[\r]//g' $remoteconfbase
 }
 
 ##//-----------------------------------------------------------------------------//
 #替换sdk配置文件中的数据库配置信息
 function replaceDBInfo(){
    echo **********************replaceDBInfo**************************
    lineno=`grep -n Provider $remotesdkconf | cut -d ":" -f 1`
	sed -i -e ''$((lineno-1))'d' $remotesdkconf
	sed -i ''$((lineno-1))'i Enabled = '$3'' $remotesdkconf
	
	sed -i -e ''$lineno'd' $remotesdkconf	
	sed -i ''$lineno'i Provider = \"'$1'\"' $remotesdkconf
	
	lineno=`grep -n DBPath $remotesdkconf | cut -d ":" -f 1`
	sed -i -e ''$lineno'd' $remotesdkconf
	sed -i ''$lineno'i DBPath = \"'$2'\"' $remotesdkconf
	
	
	#替换后可能存在dos下的换行，将其删除处理
	sed -i 's/[\r]//g' $remoteconfbase
 }

 ##//-----------------------------------------------------------------------------//
 #创建peer.lic并替换peer节点目录下的peerlic
 #如果使用该方法创建并替换peerlic 必须tool和peer配置的IP地址顺序及内容完全一致
 function createpeerlic(){
    index=0
	while read -r line
    do
	 	#空行不处理
        if [ -z "$line" ]
           then continue
        fi
	    echo ++++++++++++++++++++++++++++createpeerlic exe++++++++++++++++++++++++++++++++++++++
		./transfer.sh createLic.sh $line ${toolPaths[$index]} ${usersMap[$line]} ${pwdsMap[$line]}
		./transfer.sh license $line ${toolPaths[$index]} ${usersMap[$line]} ${pwdsMap[$line]}
		./ssh.sh $line "rm -f ${peerPaths[$index]}peer.lic" ${usersMap[$line]} ${pwdsMap[$line]}
		./ssh.sh $line "cd ${toolPaths[$index]};./createLic.sh" ${usersMap[$line]} ${pwdsMap[$line]}
		./ssh.sh $line "cp ${toolPaths[$index]}peer.lic ${peerPaths[$index]}" ${usersMap[$line]} ${pwdsMap[$line]}
		
		((index++))		
	done < $tempdir/peer.ini
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
	 ./ssh.sh $line "$checkpces" ${usersMap[$line]} ${pwdsMap[$line]}
	 ./ssh.sh $line "$killprocess" ${usersMap[$line]} ${pwdsMap[$line]}
  
	done < $2
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
	no5=`cat -n setting.ini |grep IPUserPwd|awk '{print $1}'`
	no6=`cat setting.ini | wc -l`


	sed -n ''$(($no1+1))','$(($no2-1))'p' setting.ini > $tempdir/peer.ini
	sed -n ''$(($no2+1))','$(($no3-1))'p' setting.ini > $tempdir/sdk.ini
	sed -n ''$(($no3+1))','$(($no4-1))'p' setting.ini > $tempdir/tool.ini
	sed -n ''$(($no4+1))','$(($no5))'p' setting.ini > $tempdir/type.ini
	sed -n ''$(($no5+1))','$(($no6))'p' setting.ini > $tempdir/IPUserPwd.ini
 }
 ##//-----------------------------------------------------------------------------//

 function getArrFromFile(){
   declare -a retArr
   #参数$1为待读取的文件 参数2为要存的数组名
   arrNo=0
	if [ -f $1 ];then
		while read -r line
		  do
			if [ -z "$line" ]
				then continue
			fi
		
			retArr[arrNo]=$line
			((arrNo++))
	  
		  done < $1
		echo ${retArr[*]} 
	fi
 
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
#sed -i 's/[\r]//g' $tempdir/IPUserPwd.ini
#将IP user 及IP passwd保存到map中
while read -r line
do 
	array=(`echo $line`)
	
	usersMap[${array[0]}]=${array[1]}
	pwdsMap[${array[0]}]=${array[2]}
	
done < $tempdir/IPUserPwd.ini

#获取setting各个配置项数组
types=($(getArrFromFile $tempdir/type.ini $arg1))
typeNo=${#types[*]}
echo "consensus type array no: $typeNo"



#for i in ${!peerIPs[@]}
#do
#	echored ${peerIPs[$i]}
#done


#先将节点及sdk进程关闭
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~pre kill~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
preKillProcessIPFromFile $remotepeerName $tempdir/peer.ini
preKillProcessIPFromFile $remotesdkName $tempdir/sdk.ini

#在各个节点机器上创建部署目录
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~creat deploy dir~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
creatDir $deploydir


#########################################################################
#拷贝wttool文件夹至每个节点主机的对应目录
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
  

  ./transfer.sh $dir/$localtoolDir $line $deploydir/$remotetoolDir ${usersMap[$line]} ${pwdsMap[$line]}
  ./ssh.sh $line "cd $deploydir/$remotetoolDir;mv $localtoolName $remotetoolName" ${usersMap[$line]} ${pwdsMap[$line]}

  
  replaceCert $toolCert$toolNo $line $deploydir/$remotetoolDir/tls ${usersMap[$line]} ${pwdsMap[$line]}
  #管理工具auth建议使用同一个key.pem 因此暂时不做auth目录下文件替换
  #replaceCert $toolCert$toolNo $line $deploydir/$remotetoolDir/auth ${usersMap[$line]} ${pwdsMap[$line]}
 
  toolIPs[scpPathNo]=$line
  toolPaths[scpPathNo]=$deploydir/$remotetoolDir/
  
  ((toolNo++))
  ((scpPathNo++))
  
done < $tempdir/tool.ini


#########################################################################
#替换中的$tempdir/basetemp.toml 仅取其中一个wttool的id 默认第一个
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~replaceAdminId~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
replaceAdminId ${toolIPs[0]} "id:" "cd ${toolPaths[0]}" "./$remotetoolName getid -p crypt/key.pem" ${usersMap[${toolIPs[0]}]} ${pwdsMap[${toolIPs[0]}]}


#########################################################################
#拷贝wtchain文件夹至配置的peer主机目录
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~copy wtchain files~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
#offset=0
peerNo=1
scpPathNo=0;
echo "[Members]" > $remotepeerconfig
while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi
  
 #传送节点文件
 ./transfer.sh $dir/$localpeerDir $line $deploydir/$remotepeerDir ${usersMap[$line]} ${pwdsMap[$line]}
 ./ssh.sh $line "cd $deploydir/$remotepeerDir;mv $localpeerName $remotepeerName" ${usersMap[$line]} ${pwdsMap[$line]}

 
 #替换peer.lic  licAuto=false时执行 判断文件夹中的lic个数是否满足主机个数，不满足则退出
 if [[ $licAuto == false ]];then
    licno=$(ls -l |grep "^-"|wc -l)
	if [ $licno -le $peerNo ];then
	    echored "peerlic is not enough,please check \"$licdir\""
		exit
	fi
    ./transfer.sh $licdir/peer$peerNo.lic $line $deploydir/$remotepeerDir/peer.lic ${usersMap[$line]} ${pwdsMap[$line]}
 fi
 
 #替换tls cert 目录下证书
 replaceCert $peerCert$peerNo $line $deploydir/$remotepeerDir/tls ${usersMap[$line]} ${pwdsMap[$line]}
 replaceCert $peerCert$peerNo $line $deploydir/$remotepeerDir/cert ${usersMap[$line]} ${pwdsMap[$line]}
 #替换/ca/crypt目录下证书 与管理系统通讯证书
 replaceCert $peerCACert $line $deploydir/$remotepeerDir/ca/crypt ${usersMap[$line]} ${pwdsMap[$line]}
 
 
 echo "********************replace peer conf/base.toml********************"
 matchBaseInfo $(($peerTcpPort)) $(($peerRpcPort)) $contractEnabled
 ./transfer.sh $tempdir/basetemp.toml $line $deploydir/$remotepeerDir/conf/base.toml ${usersMap[$line]} ${pwdsMap[$line]}
 #rm -f $tempdir/basetemp.toml

#配置peerconfigtemp.toml中的节点信息 
#判断是否有type的配置信息 有则替换 无则默认值
if [[ $typeNo -gt 0 ]];then
	consenType=${types[$scpPathNo]}
fi

addConf peer$peerNo $line $(($peerTcpPort)) $consenType


#$(($peerRpcPort))
#替换peerconfigtemp.toml中的节点Id信息
replacePeerId $line "ID:" "cd $deploydir/$remotepeerDir/" "./$remotepeerName id" ${usersMap[$line]} ${pwdsMap[$line]}


addSDKconfCluster $line:$(($peerRpcPort))
  
peerIPs[scpPathNo]=$line
peerPaths[scpPathNo]=$deploydir/$remotepeerDir/
((peerNo++))
((scpPathNo++))
  #if [ $same -eq 0 ];then     
  #   ((offset++))
  #fi
done < $tempdir/peer.ini


#替换部署节点中的所有的config.toml文件
for i in ${!peerPaths[@]}
  do
	 echo "********************replace peer config.toml********************"
     ./transfer.sh $remotepeerconfig ${peerIPs[$i]} ${peerPaths[$i]}config.toml ${usersMap[${peerIPs[$i]}]} ${pwdsMap[${peerIPs[$i]}]}
  done
  

#########################################################################
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~replace peerlic~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
#创建peer.lic并替换peer节点目录下的peerlic
#如果使用该方法创建并替换peerlic 必须tool和peer配置的IP地址顺序及内容完全一致
if [[ $licAuto == true ]];then
	createpeerlic
fi


#########################################################################
#拷贝sdk文件以及sdkconf.yaml至配置的sdk主机目录
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~copy sdk files~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
#offset=0
index=0
sdkNo=1

while read -r line
do
 #echo $line
 if [ -z "$line" ]
   then continue
 fi

  ./transfer.sh $dir/$localsdkDir $line $deploydir/$remotesdkDir ${usersMap[$line]} ${pwdsMap[$line]}
  ./ssh.sh $line "cd $deploydir/$remotesdkDir;mv $localsdkName $remotesdkName" ${usersMap[$line]} ${pwdsMap[$line]}

  
  #更新证书
  replaceCert $sdkCert$sdkNo $line $deploydir/$remotesdkDir/tls ${usersMap[$line]} ${pwdsMap[$line]}
  replaceCert $sdkCert$sdkNo $line $deploydir/$remotesdkDir/auth ${usersMap[$line]} ${pwdsMap[$line]}

  #将节点集群信息更新到sdk/conf/config.toml文件中
  updateSDKPeerClusterInfo
  echo ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  #替换sdk配置文件中的端口号
  replaceSDKHttpPort $(($sdkPort))
  replaceDBInfo $remoteSDKDBProvider $remoteSDKDBPath $walletEnabled
  #传送sdk配置文件至远程sdk主机
  ./transfer.sh $remotesdkconf $line $deploydir/$remotesdkDir/conf/config.toml ${usersMap[$line]} ${pwdsMap[$line]}

  sdkIPs[index]=$line
  sdkPaths[index]=$deploydir/$remotesdkDir 
  
  ((sdkNo++))
  ((index++))
  #if [ $same -eq 0 ];then
  #   ((offset++))
  #fi
done < $tempdir/sdk.ini

  
#########################################################################
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~start peer process~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
#启动所有节点和SDK
for i in ${!peerPaths[@]}
  do
     ./ssh.sh ${peerIPs[$i]} "cd ${peerPaths[$i]};./$remotepeerName start -d" ${usersMap[${peerIPs[$i]}]} ${pwdsMap[${peerIPs[$i]}]}
  done

sleep 10

#########################################################################
#通过wtpeer test指令检查节点情况,需要查看脚本执行信息  红底白字
echowhitered "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~wtpeer test status~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
./ssh.sh ${peerIPs[0]} "cd ${peerPaths[0]};./$remotepeerName test" ${usersMap[${peerIPs[0]}]} ${pwdsMap[${peerIPs[0]}]}
./ssh.sh ${peerIPs[1]} "cd ${peerPaths[1]};./$remotepeerName test" ${usersMap[${peerIPs[1]}]} ${pwdsMap[${peerIPs[1]}]}

#########################################################################
#给所有sdk赋权限
echowhitered "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~set SDK permission~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
for i in ${!sdkIPs[@]}
  do
     echo "********************start $remotesdkName process********************"
	 key="SDK_ID:"
     ./getRemoteInfo.sh "${sdkIPs[$i]}" "$key" "cd ${sdkPaths[$i]}" "./$remotesdkName getid" ${usersMap[${sdkIPs[$i]}]} ${pwdsMap[${sdkIPs[$i]}]}
	 #去掉文件中可能存在的行尾换行符
	 sed -i 's/[\r]//g' $tempdir/${key}_${sdkIPs[$i]}
	 cat $tempdir/${key}_${sdkIPs[$i]}
	 
     sdkId=`grep ${sdkIPs[$i]} $tempdir/${key}_${sdkIPs[$i]} | cut -d ":" -f 2`
	 echo "********************set permission 999********************"
	 rpcport=$(($peerRpcPort))
	 setperm="./$remotetoolName permission -p $rpcport -d $sdkId -m 999"
	./getRemoteInfo.sh ${toolIPs[0]} "success:" "cd ${toolPaths[0]}" "$setperm" ${usersMap[${toolIPs[0]}]} ${pwdsMap[${toolIPs[0]}]}
     
  done
  
#########################################################################
#可访问SDK地址
echo -e "\033[36;37m ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~SDK http info~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \033[0m"
for i in ${!sdkIPs[@]}
  do	 
	 echo "http://${sdkIPs[$i]}:$sdkPort"     
  done

endtime=$(date '+%Y-%m-%d %H:%M:%S')  
echo =====================execute period:$starttime ~~~~~ $endtime=====================   
echo  ====================================== complete ======================================

