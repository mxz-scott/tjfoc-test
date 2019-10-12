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
 #添加节点集群信息，即节点config.toml	
 function addConf(){
	echo -e "[[Members.Peers]]" >> $remotepeerconfig
	echo -e "Id = \"empty$2\"" >> $remotepeerconfig
	echo -e "ShownName = \"$1\"" >> $remotepeerconfig
	echo -e "Type = 0" >> $remotepeerconfig
	echo -e "Addr = \"/ip4/$2/tcp/$3\"" >> $remotepeerconfig
	echo -e "OutAddr = \"/ip4/$2/tcp/$3\"" >> $remotepeerconfig
	echo -e "RpcPort = $4" >> $remotepeerconfig
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

	if [ `grep -c "$line" $tempdir/temp.ini` -eq '0' ];then
		echo $line >> $tempdir/temp.ini
	fi
  
	done < setting.ini

	while read -r line
	do
		#./transfer.sh ./mkdir.sh $line /root 
		./ssh.sh $line "rm -rf $1" 
		./ssh.sh $line "rm -rf $1" 
		./ssh.sh $line "mkdir $1"
	
	done < $tempdir/temp.ini
 }

 ##//-----------------------------------------------------------------------------//
 #替换指定文件中的tcp/rpc port CA info
 function matchBaseInfo(){

	replaceAndInsert "TcpPort" $1 $remoteconfbase
	replaceAndInsert "RpcPort" $2 $remoteconfbase
	replaceAndInsert "OnlineCAEnabled" $3 $remoteconfbase
	replaceAndInsert "CAAddress" \"$4\" $remoteconfbase
	
	contractlineno=`grep -n Contract ${remoteconfbase} | cut -d ":" -f 1`
	sed -i -e ''$((contractlineno+1))'d' $remoteconfbase
	sed -i ''$((contractlineno+1))'i Enabled = '$5'' $remoteconfbase
	
 }
 
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
	#sed -i '1 r "${tempdir}/tempsdkcluster.toml"' $remotesdkconf
	sed -i '1 r temp/tempsdkcluster.toml' $remotesdkconf	
 }

 ##//-----------------------------------------------------------------------------//
 function replaceCert(){
	./transfer.sh "$certdir/$1/ca.pem" $2 $3
	./transfer.sh "$certdir/$1/cert.pem" $2 $3
	./transfer.sh "$certdir/$1/key.pem" $2 $3
	./transfer.sh "$certdir/$1/pubkey.pem" $2 $3
 }
 
 ##//-----------------------------------------------------------------------------// 
 function replacePeerId(){
	
	#replace peer id information
	#参数1是远程主机IP；参数2是匹配关键字 ID；参数3是进入节点目录的指令:cd /root/zll/ate/wtchain1，参数4是节点init命令：./peer init
	./getRemoteInfo.sh "$1" "$2" "$3" "$4"
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
  ./getRemoteInfo.sh "$1" "$2" "$3" "$4"
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
	sed -i '1i Port = '$1'' $remotesdkconf
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
		./transfer.sh createLic.sh $line ${toolPaths[$index]}
		./transfer.sh license $line ${toolPaths[$index]}
		./ssh.sh $line "rm -f ${peerPaths[$index]}peer.lic"
		./ssh.sh $line "cd ${toolPaths[$index]};./createLic.sh"
		./ssh.sh $line "cp ${toolPaths[$index]}peer.lic ${peerPaths[$index]}"
		
		((index++))		
	done < $tempdir/peer.ini
 }
 
 ##//-----------------------------------------------------------------------------//
 #确认在节点启动后是否存在无法连通的节点 使用wtchain test命令
 function checkPeerstatus(){

    echo **********************checkPeerstatus**************************
    #参数1是远程主机IP；参数2是匹配关键字 id；参数3是进入节点目录的指令:cd /root/zll/ate/wtchain，参数4是节点test命令：./wtpeer test
   ./getRemoteInfo.sh "$1" "$2" "$3" "$4"
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
 
#预先处理获取节点、sdk及管理工具部署主机IP配置信息，分别获取各个section的位置
no1=`cat -n setting.ini |grep peer|awk '{print $1}'`
no2=`cat -n setting.ini |grep sdk|awk '{print $1}'`
no3=`cat -n setting.ini |grep tool|awk '{print $1}'`
no4=`cat setting.ini | wc -l`

sed -n ''$(($no1+1))','$(($no2-1))'p' setting.ini > $tempdir/peer.ini
sed -n ''$(($no2+1))','$(($no3-1))'p' setting.ini > $tempdir/sdk.ini
sed -n ''$(($no3+1))','$(($no4-1))'p' setting.ini > $tempdir/tool.ini

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
  

  ./transfer.sh $dir/$localtoolDir $line $deploydir/$remotetoolDir
  ./ssh.sh $line "cd $deploydir/$remotetoolDir;mv $localtoolName $remotetoolName"

  
  replaceCert $toolCert$toolNo $line $deploydir/$remotetoolDir/tls
  #管理工具auth建议使用同一个key.pem 因此暂时不做auth目录下文件替换
  #replaceCert $toolCert$toolNo $line $deploydir/$remotetoolDir/auth
 
  
  scpToolPathArr[scpPathNo]=$line:$deploydir/$remotetoolDir/
  toolIPs[scpPathNo]=$line
  toolPaths[scpPathNo]=$deploydir/$remotetoolDir/
  
  ((toolNo++))
  ((scpPathNo++))
  
  #if [ $same -eq 0 ];then
  #   ((offset++))
  #fi
done < $tempdir/tool.ini


#########################################################################
#替换中的$tempdir/basetemp.toml 仅取其中一个wttool的id 默认第一个
echoblue " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~replaceAdminId~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
replaceAdminId ${toolIPs[0]} "id:" "cd ${toolPaths[0]}" "./$remotetoolName getid -p crypt/key.pem"


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
 ./transfer.sh $dir/$localpeerDir $line $deploydir/$remotepeerDir
 ./ssh.sh $line "cd $deploydir/$remotepeerDir;mv $localpeerName $remotepeerName"

 
 #替换peer.lic  licAuto=false时执行 判断文件夹中的lic个数是否满足主机个数，不满足则退出
 if [[ $licAuto == false ]];then
    licno=$(ls -l |grep "^-"|wc -l)
	if [ $licno -le $peerNo ];then
	    echored "peerlic is not enough,please check \"$licdir\""
		exit
	fi
    ./transfer.sh $licdir/peer$peerNo.lic $line $deploydir/$remotepeerDir/peer.lic
 fi
 
 #替换tls cert 目录下证书
 replaceCert $peerCert$peerNo $line $deploydir/$remotepeerDir/tls
 replaceCert $peerCert$peerNo $line $deploydir/$remotepeerDir/cert
 #替换/ca/crypt目录下证书 与管理系统通讯证书
 replaceCert $peerCACert $line $deploydir/$remotepeerDir/ca/crypt
 
 
 echo "********************replace peer conf/base.toml********************"
 matchBaseInfo $(($peerTcpPort)) $(($peerRpcPort)) $peerCAEnabled $peerCAAddr $contractEnabled
 ./transfer.sh $tempdir/basetemp.toml $line $deploydir/$remotepeerDir/conf/base.toml
 #rm -f $tempdir/basetemp.toml

#配置peerconfigtemp.toml中的节点信息 
addConf peer$peerNo $line $(($peerTcpPort)) $(($peerRpcPort))
#替换peerconfigtemp.toml中的节点Id信息
replacePeerId $line "ID:" "cd $deploydir/$remotepeerDir/" "./$remotepeerName id"


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
     ./transfer.sh $remotepeerconfig ${peerIPs[$i]} ${peerPaths[$i]}config.toml
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

  ./transfer.sh $dir/$localsdkDir $line $deploydir/$remotesdkDir
  ./ssh.sh $line "cd $deploydir/$remotesdkDir;mv $localsdkName $remotesdkName"

  
  #更新证书
  replaceCert $sdkCert$sdkNo $line $deploydir/$remotesdkDir/tls
  replaceCert $sdkCert$sdkNo $line $deploydir/$remotesdkDir/auth

  #将节点集群信息更新到sdk/conf/config.toml文件中
  updateSDKPeerClusterInfo
  echo ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  #替换sdk配置文件中的端口号
  replaceSDKHttpPort $(($sdkPort))
  replaceDBInfo $remoteSDKDBProvider $remoteSDKDBPath $walletEnabled
  #传送sdk配置文件至远程sdk主机
  ./transfer.sh $remotesdkconf $line $deploydir/$remotesdkDir/conf/config.toml

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
     ./ssh.sh ${peerIPs[$i]} "cd ${peerPaths[$i]};./$remotepeerName start -d"
  done

sleep 10

#########################################################################
#通过wtpeer test指令检查节点情况,需要查看脚本执行信息  红底白字
echowhitered "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~wtpeer test status~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
./ssh.sh ${peerIPs[0]} "cd ${peerPaths[0]};./$remotepeerName test"
./ssh.sh ${peerIPs[1]} "cd ${peerPaths[1]};./$remotepeerName test"

#########################################################################
#给所有sdk赋权限
echowhitered "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~set SDK permission~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
for i in ${!sdkIPs[@]}
  do
     echo "********************start $remotesdkName process********************"
	 key="SDK_ID:"
     ./getRemoteInfo.sh "${sdkIPs[$i]}" "$key" "cd ${sdkPaths[$i]}" "./$remotesdkName start -d"
	 #去掉文件中可能存在的行尾换行符
	 sed -i 's/[\r]//g' $tempdir/${key}_${sdkIPs[$i]}
	 cat $tempdir/${key}_${sdkIPs[$i]}
	 
     sdkId=`grep ${sdkIPs[$i]} $tempdir/${key}_${sdkIPs[$i]} | cut -d ":" -f 2`
	 echo "********************set permission 999********************"
	 rpcport=$(($peerRpcPort))
	 setperm="./$remotetoolName permission -p $rpcport -d $sdkId -m 999"
	./getRemoteInfo.sh ${toolIPs[0]} "success:" "cd ${toolPaths[0]}" "$setperm"
     
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

