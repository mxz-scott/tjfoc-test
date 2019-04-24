package com.tjfintech.common.functionTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class TestMgTool {
    public static final int STARTSLEEPTIME=40000;
    TestBuilder testBuilder=TestBuilder.getInstance();
    Store store =testBuilder.getStore();

    String rpcPort="9300";
    String tcpPort="60030";
    String consType="L";
    String dataType="D";
    int basePeerNo = 3;
    int DynamicPeerNo = 4;


    String toolPath="cd "+PTPATH+"toolkit;";
    String peer1IPPort=PEER1IP+":"+PEER1RPCPort;
    String peer2IPPort=PEER2IP+":"+PEER2RPCPort;
    String peer3IPPort=PEER3IP+":"+PEER3RPCPort;

    long timeStamp=0;

    //@Before
//    public void beforeTest() throws Exception {
//
//    }
    @Before
    public void resetPeerEnv()throws Exception{
        BeforeCondition bf =new BeforeCondition();
        bf.initTest();

        setAndRestartPeerList("cp "+PTPATH+"peer/conf/baseOK.toml "+PTPATH+"peer/conf/base.toml");

        quitPeer(peer1IPPort,PEER3IP,tcpPort);

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellPeer3.execute("cp "+PTPATH+"peer/conf/baseOK.toml "+PTPATH+"peer/conf/base.toml");
        //startPeer(PEER3IP);
        queryPeerList(peer1IPPort,basePeerNo);

    }

    @Test
    public void chkFuncInterface() throws Exception{
        testFunc();
        testGetID();
    }

    //@Test
    public void testFunc() throws Exception{

        checkParam(PEER1IP,"./toolkit peer -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit peer","management");
        checkParam(PEER1IP,"./toolkit peer -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit peer -p "+ peer1IPPort,"too many colons in address");

        checkParam(PEER1IP,"./toolkit mem -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit mem","management");
        checkParam(PEER1IP,"./toolkit mem -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit mem -p "+ peer1IPPort,"too many colons in address");

        checkParam(PEER1IP,"./toolkit health -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit health","management");
        checkParam(PEER1IP,"./toolkit health -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit health -p "+ peer1IPPort,"too many colons in address");

        checkParam(PEER1IP,"./toolkit newtx -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit newtx","management");
        checkParam(PEER1IP,"./toolkit newtx -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit newtx -p "+ peer1IPPort,"too many colons in address");

        checkParam(PEER1IP,"./toolkit height -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit height","management");
        checkParam(PEER1IP,"./toolkit height -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit height -p "+ peer1IPPort,"too many colons in address");

        checkParam(PEER1IP,"./toolkit query -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit query -v","flag needs an argument: 'v' in -v");
        checkParam(PEER1IP,"./toolkit query","management");
        checkParam(PEER1IP,"./toolkit query -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit query -p "+ peer1IPPort,"too many colons in address");

        checkParam(PEER1IP,"./toolkit ntx -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit ntx","management");
        checkParam(PEER1IP,"./toolkit ntx -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit ntx -p "+ peer1IPPort,"too many colons in address");

        quitPeer(peer1IPPort,PEER3IP,tcpPort);
        queryPeerList(peer1IPPort,basePeerNo);
        checkParam(PEER1IP,"./toolkit join -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit join -n","flag needs an argument: 'n' in -n");
        checkParam(PEER1IP,"./toolkit join -l","flag needs an argument: 'l' in -l");
        checkParam(PEER1IP,"./toolkit join -w","flag needs an argument: 'w' in -w");
        checkParam(PEER1IP,"./toolkit join -s","flag needs an argument: 's' in -s");
        checkParam(PEER1IP,"./toolkit join","management");
        checkParam(PEER1IP,"./toolkit join -p "+ PEER1IP+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030 -s peer168","unknown port");
        checkParam(PEER1IP,"./toolkit join -p "+ peer1IPPort+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030 -s peer168","too many colons in address");

        queryPeerList(peer1IPPort,basePeerNo);

        checkParam(PEER1IP,"./toolkit observer -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit observer -n","flag needs an argument: 'n' in -n");
        checkParam(PEER1IP,"./toolkit observer -l","flag needs an argument: 'l' in -l");
        checkParam(PEER1IP,"./toolkit observer -w","flag needs an argument: 'w' in -w");
        checkParam(PEER1IP,"./toolkit observer -s","flag needs an argument: 's' in -s");
        checkParam(PEER1IP,"./toolkit observer","management");
        checkParam(PEER1IP,"./toolkit observer -p "+ PEER1IP+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030 -s peer168","unknown port");
        checkParam(PEER1IP,"./toolkit observer -p "+ peer1IPPort+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030 -s peer168","too many colons in address");

        queryPeerList(peer1IPPort,basePeerNo);

        checkParam(PEER1IP,"./toolkit quit -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit quit -n","flag needs an argument: 'n' in -n");
        checkParam(PEER1IP,"./toolkit quit -l","flag needs an argument: 'l' in -l");
        checkParam(PEER1IP,"./toolkit quit -w","flag needs an argument: 'w' in -w");
        checkParam(PEER1IP,"./toolkit quit","management");
        checkParam(PEER1IP,"./toolkit quit -p "+ PEER1IP+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030","unknown port");
        checkParam(PEER1IP,"./toolkit quit -p "+ peer1IPPort+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030","too many colons in address");
        queryPeerList(peer1IPPort,basePeerNo);


        checkParam(PEER1IP,"./toolkit permission -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit permission -d","flag needs an argument: 'd' in -d");
        checkParam(PEER1IP,"./toolkit permission -m","flag needs an argument: 'm' in -m");
        checkParam(PEER1IP,"./toolkit permission","management");
        checkParam(PEER1IP,"./toolkit permission -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit permission -p "+ peer1IPPort,"too many colons in address");

        checkParam(PEER1IP,"./toolkit getpermission -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit getpermission -d","flag needs an argument: 'd' in -d");
        checkParam(PEER1IP,"./toolkit getpermission","management");
        checkParam(PEER1IP,"./toolkit getpermission -p "+ PEER1IP,"unknown port");
        checkParam(PEER1IP,"./toolkit getpermission -p "+ peer1IPPort,"too many colons in address");

        checkParam(PEER1IP,"./toolkit getid -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./toolkit getid","management");


        checkParam(PEER1IP,"./license create -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./license create -m","flag needs an argument: 'm' in -m");
        checkParam(PEER1IP,"./license create","management");

        checkParam(PEER1IP,"./license decode -p","flag needs an argument: 'p' in -p");
        checkParam(PEER1IP,"./license decode","management");
    }

    public void checkParam(String shellIP,String cmd,String chkStr)throws Exception{
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String tempCmd=toolPath+cmd;
//        log.info(tempCmd);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        assertEquals(response.contains(chkStr),true);
    }

    @Test
    public void testPeerInfo()throws Exception{
        //resetPeerEnv();
        //先将待加入节点进程停止
        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellPeer3.execute("cp "+PTPATH+"peer/configjoin.toml "+PTPATH+"peer/config.toml");
        quitPeer(peer1IPPort,PEER3IP,tcpPort);

        Thread.sleep(2000);
        queryPeerList(peer1IPPort,basePeerNo);

        //检查配置文件中预设的共识节点，即搭建环境时配置的共识节点
        chkPeerSimInfoOK(peer1IPPort,tcpPort,version,consType);

        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        addConsensusPeer(peer1IPPort,PEER3IP,tcpPort,"update success");
        queryPeerList(peer1IPPort,DynamicPeerNo);

        Thread.sleep(3000);

        startPeer(PEER3IP);
        Thread.sleep(STARTSLEEPTIME);
        chkPeerSimInfoOK(peer3IPPort,tcpPort,version,consType);
        queryPeerList(peer1IPPort,DynamicPeerNo);
        queryPeerList(PEER3IP+":"+rpcPort,DynamicPeerNo);
        String height=queryBlockHeight(peer1IPPort);
        assertEquals(queryBlockHeight(peer2IPPort),height);
        //assertEquals(queryBlockHeight(peer3IPPort),height);//因数据较多时同步数据需要时间，此部分查询检查移除

        shellPeer3.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        Thread.sleep(3000);


        sendNewTx(peer1IPPort,"1","1");

        //检查动态加入的数据节点，即使用管理工具加入的数据节点信息
        quitPeer(peer1IPPort,PEER3IP,tcpPort);
        queryPeerList(peer1IPPort,basePeerNo);
        Thread.sleep(4000);

        addDataPeer(peer1IPPort,PEER3IP,tcpPort,"update success");
        queryPeerList(peer1IPPort,DynamicPeerNo);//通过共识节点查询集群列表
        shellPeer3.execute("cp "+PTPATH+"peer/configobs.toml "+PTPATH+"peer/config.toml");
        startPeer(PEER3IP);
        Thread.sleep(STARTSLEEPTIME);
        chkPeerSimInfoOK(peer3IPPort,tcpPort,version,dataType);
        queryPeerList(PEER3IP+":"+rpcPort,DynamicPeerNo);//通过非共识节点查询集群列表

        height=queryBlockHeight(peer1IPPort);
        assertEquals(queryBlockHeight(peer2IPPort),height);
        //assertEquals(queryBlockHeight(peer3IPPort),height);

        quitPeer(peer1IPPort,PEER3IP,tcpPort);
        queryPeerList(peer1IPPort,basePeerNo);
        shellPeer3.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        addConsensusPeer(peer1IPPort,PEER3IP,tcpPort,"update success");
        startPeer(PEER3IP);//此步骤应该启动不成功，因节点当前配置文件中Type=1，但是使用addConsensusPeer 即join加入，两者不一致时无法启动成功
        Thread.sleep(STARTSLEEPTIME);
//        queryPeerList(peer1IPPort,2);
        //shellPeer3.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");


        //检查配置文件中预设的数据节点，即搭建环境时配置的数据节点 此节点为另外一个系统的数据节点
        //chkPeerSimInfoOK(PEER3IP+":9400","60012","1904",dataType);


        //检查未启动或者不存在的节点
        chkPeerSimInfoErr(PEER1IP+":9800","connection refused");

        /** @param tcpPort 节点tcpport  //checkinfo[0]
        * @param version 节点版本 //checkinfo[1]
        * @param Type 节点类型共识or数据节点 //checkinfo[2]
        * @param launchTime 启动时间 //checkinfo[3]
        * @param log 日志级别 //checkinfo[4]
        * @param dbPath db数据库目录 //checkinfo[5]
        * @param Crypt 加密算法 //checkinfo[6]
        * @param Hash hash算法 //checkinfo[7]
        * @param Consensus 共识算法 //checkinfo[8]
        * @param dockerImage 合约版本 //checkinfo[9]
        * @param blockPackTime 打包时间 //checkinfo[10]
        * @param blockSize 区块大小 //checkinfo[11]* */
        chkPeerDetailsOK(peer1IPPort,"60030",version,consType,"2019-",
                        "Info","peer.db","sm2","sm3","Raft","tjfoc/tjfoc-ccenv 2.0","1000","1");
    }

    public void chkPeerSimInfoOK(String queryIPPort,String tcpPort,String version,String Type)throws Exception{
        String tempCmd="";
        //queryIPPort : peer1IPPort
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        tempCmd=toolPath+"./toolkit peer -p "+rpcPort;

        String[] temp = queryIP.split("\\.");

        //输出信息，检查主节点
        //String version="dev190301.2";
        String tcpIP=queryIP+":"+tcpPort; //10.1.3.240:60030
        String peerID=temp[3];  //取IP的最后一位点分十进制作为节点ID,ex. 240
        String peerName="peer"+temp[3];//peer240

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        //assertEquals(response.contains("失败"), false);
        assertEquals(response.contains(version), true);
        assertEquals(response.contains(tcpIP), true);
        assertEquals(response.contains(peerID), true);
        assertEquals(response.contains(peerName), true);
        assertEquals(response.contains(Type), true);
        assertEquals(response.contains(rpcPort), true);
    }

    public void chkPeerSimInfoErr(String queryIPPort,String ErrMsg)throws Exception{
        String tempCmd="";
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240

        tempCmd=toolPath+"./toolkit peer -p "+ rpcPort;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        assertEquals(response.contains(ErrMsg), true);
    }


    public void startPeer(String peerIP)throws Exception{
    //public void startPeer()throws Exception{
        //Shell shell1=new Shell(PEER3IP,USERNAME,PASSWD);
        Shell shell1=new Shell(peerIP,USERNAME,PASSWD);
        //shell1.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        Thread.sleep(2000);
        shell1.execute("sh "+PTPATH+"peer/start.sh");
        //Thread.sleep(20000);
    }

    /**
     *
     * @param queryIPPort  查看节点地址
     * @param tcpPort 节点tcpport  //checkinfo[0]
     * @param version 节点版本 //checkinfo[1]
     * @param Type 节点类型共识or数据节点 //checkinfo[2]
     * @param launchTime 启动时间 //checkinfo[3]
     * @param log 日志级别 //checkinfo[4]
     * @param dbPath db数据库目录 //checkinfo[5]
     * @param Crypt 加密算法 //checkinfo[6]
     * @param Hash hash算法 //checkinfo[7]
     * @param Consensus 共识算法 //checkinfo[8]
     * @param dockerImage 合约版本 //checkinfo[9]
     * @param blockPackTime 打包时间 //checkinfo[10]
     * @param blockSize 区块大小 //checkinfo[11]
     * @throws Exception
     */
    public void chkPeerDetailsOK(String queryIPPort,String...checkinfo)throws Exception{
        String tempCmd="";
        //queryIPPort : peer1IPPort
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        tempCmd=toolPath+"./toolkit peer -p "+rpcPort+" -i";

        String[] temp = queryIP.split("\\.");

        //输出信息，检查主节点
        String tcpIP=queryIP+":"+checkinfo[0]; //10.1.3.240:60030
        String peerID=temp[3];  //取IP的最后一位点分十进制作为节点ID,ex. 240
        String peerName="peer"+temp[3];//peer240

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        assertEquals(response.contains(checkinfo[1]), true);//检查版本信息
        assertEquals(response.contains(tcpIP), true);//检查tcpip信息
        assertEquals(response.contains(peerID), true);//检查peerID信息
        assertEquals(response.contains(peerName), true);//检查peerName信息
        assertEquals(response.contains(checkinfo[2]), true);//检查节点类型
        assertEquals(response.contains(rpcPort), true);//检查rpc端口
        assertEquals(response.contains(checkinfo[3]), true);//检查最近的启动时间
        assertEquals(response.contains(checkinfo[4]), true);//检查日志级别
        assertEquals(response.contains(checkinfo[5]), true);//检查链数据存储路径
        assertEquals(response.contains(checkinfo[6]), true);//检查非对称加密算法
        assertEquals(response.contains(checkinfo[7]), true);//检查hash算法
        assertEquals(response.contains(checkinfo[8]), true);//检查共识算法
        assertEquals(response.contains(checkinfo[9]), true);//检查运行合约的docker镜像以及docker版本号
        assertEquals(response.contains(checkinfo[10]), true);//检查区块打包时间
        assertEquals(response.contains(checkinfo[11]), true);//检查区块大小限制
    }



    public void queryPeerList(String queryIPPort,int peerNo) throws Exception{
        Thread.sleep(1500);
        String tempCmd="";

        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240

        tempCmd=toolPath+"./toolkit mem -p "+rpcPort;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        int No=0;
        ArrayList<String> stdout = shell1.getStandardOutput();
        for(String str :stdout)
        {
            if(str.contains("MemberList"))
                No++;
        }
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        assertEquals(peerNo,No);
        assertEquals(response.contains("isLeader"), true);

    }

   @Test
   public void chkHealth()throws Exception{
       checkPeerHealth(PEER1IP+":"+rpcPort);
   }

    //@Test
    public void checkPeerHealth(String queryIPPort)throws Exception{
        String tempCmd="";
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240

        tempCmd=toolPath+"./toolkit health -p "+rpcPort;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        assertEquals(response.contains("DiskUsedPercent"), true);
        assertEquals(response.contains("DiskTotal"), true);
        assertEquals(response.contains("MemUsed"), true);
        assertEquals(response.contains("MemAvailable"), true);
        assertEquals(response.contains("MemTotal"), true);
        assertEquals(response.contains("SingleCPUUsedPercent"), true);
        assertEquals(response.contains("TotalCPUUsedPercent"), true);
        assertEquals(response.contains("WsRateTotal"), true);
        assertEquals(response.contains("WsRateFromLast"), true);
        assertEquals(response.contains("WsRateEverySec10"), true);
        assertEquals(response.contains("Uploadspeed"), true);
        assertEquals(response.contains("Downloadspeed"), true);


        //-p参数中带IP
        tempCmd=toolPath+"./toolkit health -p "+queryIPPort;
        shell1.execute(tempCmd);
        ArrayList<String> stdout1 = shell1.getStandardOutput();
        String response1 = StringUtils.join(stdout1,"\n");
        log.info("\n"+response1);
        assertEquals(response1.contains("too many colons in address"), true);


        //无-p参数
        tempCmd=toolPath+"./toolkit health ";
        shell1.execute(tempCmd);
        ArrayList<String> stdout2 = shell1.getStandardOutput();
        String response2 = StringUtils.join(stdout2,"\n");
        log.info("\n"+response2);
        assertEquals(response2.contains("management"), true);


        //查询不是一个区块链系统中的节点
        tempCmd=toolPath+"./toolkit health -p 9800";
        shell1.execute(tempCmd);
        ArrayList<String> stdout3 = shell1.getStandardOutput();
        String response3 = StringUtils.join(stdout3,"\n");
        log.info("\n"+response3);
        assertEquals(response3.contains("connection refused"), true);
    }
    @Test
    public void testTXComplex() throws Exception{
        testTX();
        TxCheck();
    }

    //@Test
    public  void testTX()throws Exception{
        int blockHeight=0;
        String rsp="";
        blockHeight=Integer.parseInt(queryBlockHeight((peer1IPPort)));

        //确认管理工具获取的高度与sdk获取高度一致，需要注意sdk是否有权限执行获取高度
        rsp = store.GetHeight();
        assertEquals(JSONObject.fromObject(rsp).getString("Data"), queryBlockHeight((peer1IPPort)));

        //发送单笔交易
        rsp=sendNewTx(peer1IPPort,"","");
        assertEquals(rsp.contains("tjfoc"), true);
        Thread.sleep(6000);
        assertEquals(blockHeight+1, Integer.parseInt(queryBlockHeight((peer1IPPort))));

        rsp = queryBlockByHeight(peer1IPPort,String.valueOf(blockHeight));
        assertEquals(rsp.contains(String.valueOf(blockHeight)), true);

    }
    public String sendNewTx(String queryIPPort,String txNo,String txType)throws Exception{

        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240

        String tempTxNo= txNo.isEmpty()?"":" -n "+txNo;
        String tempTxType=txType.isEmpty()?"":" -t "+txType;
        //String tempCycleSend=cycleSend.isEmpty()?"":" -f "+cycleSend;
        String temp=tempTxNo+tempTxType;

        String response="";

        String tempCmd="";
        tempCmd=toolPath+"./toolkit newtx -p "+ rpcPort + temp;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        if(txNo !="-1"){
            ArrayList<String> stdout = shell1.getStandardOutput();
            response = StringUtils.join(stdout,"\n");
            log.info("\n"+response);
        }
        else
            response="send tx in cycle module";

        return response;
    }

    public String queryBlockHeight(String queryIPPort)throws Exception{
        String tempCmd="";
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        tempCmd=toolPath+"./toolkit height -p "+rpcPort;
        String blockHeight="";

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info("\n"+StringUtils.join(stdout,"\n"));

        for (String str : stdout) {
            if(str.contains("BlockHeight"))
            {
                blockHeight=str.substring(str.indexOf(":")+1).trim();
            }
            //blockHeight=str.trim();
        }
       return blockHeight;
    }


    public String queryBlockByHeight(String queryIPPort,String height)throws Exception{
        String tempCmd="";
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        tempCmd=toolPath+"./toolkit query -p "+rpcPort+" -v "+height;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        return response;
    }

    //@Test
    public void TxCheck()throws Exception{
        String rsp=queryPeerUnconfirmedTx(peer1IPPort);
        assertEquals(rsp.contains("Count:\t0"),true);
        //log.info("Current Unconfirmed Tx Count:"+rsp.substring(rsp.lastIndexOf("Count:")+1).trim());

        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/basePkTm20s.toml "+ PTPATH +"peer/conf/base.toml");

//        Shell shellPeer1=new Shell(PEER1IP,USERNAME,PASSWD);
//        shellPeer1.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
//        shellPeer1.execute("sed -i \"s/PackTime = 1000/PackTime = 10000/g\" "+PTPATH+"peer/conf/base.toml");
//
//        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
//        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
//        shellPeer2.execute("sed -i \"s/PackTime = 1000/PackTime = 10000/g\" "+PTPATH+"peer/conf/base.toml");
//
//        startPeer(PEER1IP);
//        startPeer(PEER2IP);
//
//        Thread.sleep(STARTSLEEPTIME);

        rsp = sendNewTx(peer1IPPort,"3","1");
        assertEquals(rsp.contains("HashData"),true);

        rsp=queryPeerUnconfirmedTx(peer1IPPort);

        assertEquals(rsp.contains("Count:\t3"),true);

        //恢复原始配置
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/base.toml");

    }

    public String queryPeerUnconfirmedTx(String queryIPPort)throws Exception{
        String tempCmd="";
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        tempCmd=toolPath+"./toolkit ntx -p "+rpcPort+" -i";

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        return response;
    }


    public void addConsensusPeer(String netPeerIP,String peerIP,String tcpPort,String chkMsg)throws Exception{
        String tempCmd="";
        String rpcPort=netPeerIP.split(":")[1];//9300
        String queryIP=netPeerIP.split(":")[0];//10.1.3.240

        String[] temp = peerIP.split("\\.");
        String peerID=temp[3];
        String peerName="peer"+temp[3];
        String peerIPlan=peerIP+":"+tcpPort;
        String peerIPwan=peerIP+":"+tcpPort;
        tempCmd=toolPath+"./toolkit join -p "+rpcPort+" -n "+peerID+" -s "+peerName
                +" -l "+peerIPlan+" -w "+peerIPwan;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        //assertEquals(response.contains("失败"), false);
        assertEquals(response.contains(chkMsg), true);
    }

    public void addDataPeer(String netPeerIP,String peerIP,String tcpPort,String chkMsg)throws Exception{
        String tempCmd="";
        String rpcPort=netPeerIP.split(":")[1];//9300
        String queryIP=netPeerIP.split(":")[0];//10.1.3.240

        String[] temp = peerIP.split("\\.");
        String peerID=temp[3];
        String peerName="peer"+temp[3];
        String peerIPlan=peerIP+":"+tcpPort;
        String peerIPwan=peerIP+":"+tcpPort;
        tempCmd=toolPath+"./toolkit observer -p "+rpcPort+" -n "+peerID+" -s "+peerName
                +" -l "+peerIPlan+" -w "+peerIPwan;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        //assertEquals(response.contains("失败"), false);
        assertEquals(response.contains(chkMsg), true);

    }

    public void quitPeer(String netPeerIP,String peerIP,String tcpPort)throws Exception{
        String tempCmd="";
        String rpcPort=netPeerIP.split(":")[1];//9300
        String queryIP=netPeerIP.split(":")[0];//10.1.3.240

        String[] temp = peerIP.split("\\.");
        String peerID=temp[3];
        String peerIPlan=peerIP+":"+tcpPort;
        String peerIPwan=peerIP+":"+tcpPort;
        tempCmd=toolPath+"./toolkit quit -p "+rpcPort+" -n "+peerID+" -l "+peerIPlan+ " -w "+peerIPwan;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response1 = StringUtils.join(stdout,",");
        log.info(response1);
        assertEquals(response1.contains("失败"), false);
        //assertEquals(response1.contains("update success"), true);
        if(response1.contains("update success"))
            log.info("quit success");
        else if(response1.contains("memberlist does not have peer"))
            log.info("memberlist does not have peer");

    }

    public String setPeerPerm(String netPeerIP,String sdkID,String permStr)throws Exception{
        String rpcPort=netPeerIP.split(":")[1];//9300
        String queryIP=netPeerIP.split(":")[0];//10.1.3.240

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);

        String cmd1=toolPath+"./toolkit permission -p "+rpcPort+" -d "+ sdkID+" -m "+permStr;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    public String getPeerPerm(String netPeerIP,String sdkID)throws Exception{
        String cmd1="";
        String rpcPort=netPeerIP.split(":")[1];//9300
        String queryIP=netPeerIP.split(":")[0];//10.1.3.240

        String response="wrong";
        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        if( sdkID != "") {
            cmd1 = toolPath + "./toolkit getpermission -p " + rpcPort + " -d " + sdkID;
            shell1.execute(cmd1);
            ArrayList<String> stdout = shell1.getStandardOutput();
            response = StringUtils.join(stdout, "\n");
        }else{
                cmd1=toolPath+"./toolkit getpermission -p "+rpcPort;
                shell1.execute(cmd1);
                ArrayList<String> stdout = shell1.getStandardOutput();
                response = StringUtils.join(stdout,"\n");
            }
        log.info(response);
        return response;
    }
    @Test
    public void testPeerPerm()throws Exception{
        String rsp="";
        String sdkID1="01de8e335282e28a6bae4081cf8b7ef61ee45d19b0503f3180a343355226b305a21bd6e714c5455becf28f83463a67ee771209b93c75d76c586b168a96666d26";
        String sdkID2="23de9d435282e28a6bae4081cf8b7ef61ee45d19b0503f3180a343355226b305a21bd6e714c5455becf28f83463a67ee771209b93c75d76c586b168a96666d26";
        String sdkID3="45ff9d435282e28a6bae4081cf8b7ef61ee45d19b0503f3180a343355226b305a21bd6e714c5455becf28f83463a67ee771209b93c75d76c586b168a96666d26";
        String sdkID4="sdk56";

        String succRsp="FuncUpdatePeerPermission success:  true";
        String errRsp="FuncUpdatePeerPermission err";
        //分别给sdkID1~3 赋值权限，之后再做查询
        rsp = setPeerPerm(peer1IPPort,sdkID1,"1,2,3");
        assertEquals(rsp.contains(succRsp), true);

        rsp = setPeerPerm(peer1IPPort,sdkID2,"0");
        assertEquals(rsp.contains(succRsp), true);

        rsp = setPeerPerm(peer1IPPort,sdkID3,"999");
        assertEquals(rsp.contains(succRsp), true);

        rsp = setPeerPerm(peer1IPPort,sdkID4,"1,2,3");
        assertEquals(rsp.contains(errRsp), true);

        Thread.sleep(3000);
        rsp=getPeerPerm(peer1IPPort,"");
        assertEquals(rsp.contains(sdkID1), true);
        assertEquals(rsp.contains("peermission:[1 2 3]"), true);
        assertEquals(rsp.contains(sdkID2), true);
        assertEquals(rsp.contains("peermission:[0]"), true);
        assertEquals(rsp.contains(sdkID3), true);
        assertEquals(rsp.contains("peermission:[1 2 3 4 5 6 7 8 9 10 21 22 23 24 25 211 212 221 222 223 224 231 232 233 234 235 236 251 252 253 254]"), true);
        assertEquals(rsp.contains(sdkID4), false);

        rsp=getPeerPerm(peer1IPPort,sdkID1);
        assertEquals(rsp.contains("peermission:[1 2 3]"), true);

    }

    //@Test
    public void testGetID()throws Exception{

        assertEquals(getID(PEER1IP,"tls/key.pem","sm2").contains("id"),true);
        assertEquals(getID(PEER1IP,"tls/key.pem","").contains("id"),true);
        assertEquals(getID(PEER1IP,"tls/key.pem","ecc").contains("failed to parse EC private key embedded in PKCS#8: x509"),true);
        assertEquals(getID(PEER1IP,"tls/key.pem","eee").contains("unsupport sign algorithm"),true);


        assertEquals(getID(PEER1IP,"ecdsa/key.pem","ecc").contains("id"),true);
        assertEquals(getID(PEER1IP,"ecdsa/key.pem","").contains("id"),true);
        assertEquals(getID(PEER1IP,"ecdsa/key.pem","sm2").contains("id"),true);
        assertEquals(getID(PEER1IP,"ecdsa/key.pem","eee").contains("unsupport sign algorithm"),true);

        assertEquals(getID(PEER1IP,"","sm2").contains("management"),true);
        assertEquals(getID(PEER1IP,"key.pem","ecc").contains(" no such file or directory"),true);
    }

    public String getID(String shellIP,String keyPath,String cryptType)throws Exception{
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
//        String keyPath="key.pem";
//        String cryptType="sm2";
//        String hashType="sm3";
        String keySetting=keyPath.isEmpty()?"":" -p "+keyPath;
        String crySetting=cryptType.isEmpty()?"":" -k "+cryptType;

        String cmd1=toolPath+"./toolkit getid"+keySetting + crySetting ;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    //@Test
    public void testLicGenAndDec() throws Exception{
        String rsp="";
        String dayTime="36500";
        String PeerNo="6";
        //生成证书
        rsp = genLicence(PEER1IP,PEER1MAC,PEER1IP,dayTime,PeerNo,version.substring(0,3));
        log.info(PEER1MAC);
        log.info(rsp);
        assertEquals(rsp.contains(PEER1MAC),true);
        assertEquals(rsp.contains(PEER1IP),true);
        assertEquals(rsp.contains("DayTime:"+dayTime),true);
        assertEquals(rsp.contains("PeerNum:"+PeerNo),true);
        assertEquals(rsp.contains("version:"+version.substring(0,3)),true);

        //解析证书 确认参数一致
        rsp = deLicence(PEER1IP,"peer.lic");
        assertEquals(rsp.contains("PeerNum:"+PeerNo),true);
        assertEquals(rsp.contains("PeerVersion:"+version.substring(0,3)),true);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String outTime =df.format(new Date(timeStamp+Long.parseLong(dayTime)*24*60*60*1000));
        log.info("OutTime:"+outTime);
        assertEquals(rsp.contains(outTime.split(" ")[0]),true); //确认有效期时间正确，因虚拟机与测试机可能不是同一台可能存在时间差，因此目前仅校验年月日是否正确

        Shell shellPeer1=new Shell(PEER1IP,USERNAME,PASSWD);
        shellPeer1.execute("rm -f "+PTPATH+"toolkit/peer.lic");
        assertEquals(deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);


        //生成使用无效的参数验证:无效的mac地址、无效IP地址、无效时间、无效节点数
        checkParam(PEER1IP,"./license create -m 12:11 -p 10.1.3.240 -d 100 -n 6 -v 2.0","invalid MAC address");
        assertEquals(deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);

        checkParam(PEER1IP,"./license create -m 02:42:fc:a2:5b:1b -p 10.1 -d 100 -n 6 -v 2.0","invalid IP address");
        assertEquals(deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);

        checkParam(PEER1IP,"./license create -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 0.5 -n 6 -v 2.0","invalid argument");
        assertEquals(deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);

        checkParam(PEER1IP,"./license create -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 5 -n 0.5 -v 2.0","invalid argument");
        assertEquals(deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);

        checkParam(PEER1IP,"./license create -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 5 -n 0 -v 2.0","success");

        rsp = deLicence(PEER1IP,"peer.lic");
        assertEquals(rsp.contains("PeerNum:0"),true);

        //解析证书使用无效参数
        checkParam(PEER1IP,"./license decode -p ./crypt/key.pem","data Illegal");

    }

    //@Test
    public void testLicValidForPeer()throws Exception{

        //验证已过期证书，此证书需要提前准备 246已有过期证书peer246d1n2.lic
        log.info("********************Test for licence timeout********************");
        //ToolIP= PEER2IP;
        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        //替换配置licence文件为过期文件
        shellPeer2.execute("cp "+PTPATH+"peer/conf/based1.toml "+PTPATH+"peer/conf/base.toml");
        startPeer(PEER2IP);
        Thread.sleep(STARTSLEEPTIME);
        checkParam(PEER2IP,"./toolkit health -p 9300","connection error");

        log.info("********************Test for dismatch license********************");
        //ToolIP= PEER2IP;
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(PEER2IP,PEER2MAC,PEER2IP,"200","3","test");
        shellPeer2.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerDisMatch.lic");
        //替换配置licence文件为过期文件
        shellPeer2.execute("cp "+PTPATH+"peer/conf/baseDisMatch.toml "+PTPATH+"peer/conf/base.toml");
        startPeer(PEER2IP);
        Thread.sleep(STARTSLEEPTIME);
        checkParam(PEER2IP,"./toolkit health -p 9300","connection error");

        log.info("********************Test for invalid peer count********************");
        //验证节点数据小于配置文件中节点数场景
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(PEER2IP,PEER2MAC,PEER2IP,"200","1",version.substring(0,3));
        shellPeer2.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peer246n1.lic");
        //替换配置licence文件为n=1文件
        shellPeer2.execute("cp "+PTPATH+"peer/conf/basen1.toml "+PTPATH+"peer/conf/base.toml");
        startPeer(PEER2IP);
        Thread.sleep(STARTSLEEPTIME);
        checkParam(PEER2IP,"./toolkit health -p 9300","connection error");

        log.info("********************Test for invalid MAC addr ********************");
        //证书IP正确，MAC不正确 替换配置licence文件为Mac1文件
        shellPeer2.execute("cp "+PTPATH+"peer/conf/baseMac1.toml "+PTPATH+"peer/conf/base.toml");
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(PEER2IP,PEER1MAC,PEER2IP,"200","3",version.substring(0,3));
        shellPeer2.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerMac1.lic");
        startPeer(PEER2IP);
        Thread.sleep(STARTSLEEPTIME);
        checkParam(PEER2IP,"./toolkit health -p 9300","connection error");

        log.info("********************Test for invalid IP addr ********************");
        //证书MAC正确，IP不正确 替换配置licence文件为IP1文件
        shellPeer2.execute("cp "+PTPATH+"peer/conf/baseIP1.toml "+PTPATH+"peer/conf/base.toml");
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(PEER2IP,PEER2MAC,PEER1IP,"200","3",version.substring(0,3));
        shellPeer2.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerIP1.lic");
        startPeer(PEER2IP);
        Thread.sleep(STARTSLEEPTIME);
        checkParam(PEER2IP,"./toolkit health -p 9300","connection error");

        log.info("********************Test for invalid IP&MAC addr ********************");
        //使用其他节点licence
        shellPeer2.execute("cp "+PTPATH+"peer/conf/baseIPMac1.toml "+PTPATH+"peer/conf/base.toml");
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(PEER1IP,PEER1MAC,PEER1IP,"200","3",version.substring(0,3));
        shellPeer2.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerIPMac1.lic");
        startPeer(PEER2IP);
         Thread.sleep(STARTSLEEPTIME);
        checkParam(PEER2IP,"./toolkit health -p 9300","connection error");

        log.info("********************Test for old license(no version check) ********************");
        //使用旧版本工具生成的不带version检查的licence 需要提前准备好旧版本的license
        shellPeer2.execute("cp "+PTPATH+"peer/conf/baseNoVer.toml "+PTPATH+"peer/conf/base.toml");
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        startPeer(PEER2IP);
        Thread.sleep(STARTSLEEPTIME);
        checkParam(PEER2IP,"./toolkit health -p 9300","connection error");

        log.info("********************Test for valid licence********************");
        //恢复配置并重启，使用有效证书验证
        shellPeer2.execute("cp "+PTPATH+"peer/conf/baseOK.toml "+PTPATH+"peer/conf/base.toml");
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        startPeer(PEER2IP);
        Thread.sleep(STARTSLEEPTIME);
        //ToolIP= PEER1IP;
        queryPeerList(peer1IPPort,basePeerNo);
    }

    @Test
    public void testLicence()throws Exception{
        testLicGenAndDec();
        testLicValidForPeer();
        testLicForAddPeer();
    }
    //此用例需要保证各个节点上都存在管理工具
    //目前规划目录：10.1.3.240/246/247  "+PTPATH+"toolkit

    public void testLicForAddPeer()throws Exception{

        //重新生成节点个数为3的240证书并拷贝至节点目录
        genLicence(PEER1IP,PEER1MAC,PEER1IP,"20",String.valueOf(basePeerNo),version.substring(0,3));

        //重新生成节点个数为3的246证书并拷贝至节点目录
        genLicence(PEER2IP,PEER2MAC,PEER2IP,"20",String.valueOf(basePeerNo),version.substring(0,3));

        //重新生成节点个数为3的168证书并拷贝至节点目录
        genLicence(PEER4IP,PEER4MAC,PEER4IP,"20",String.valueOf(basePeerNo),version.substring(0,3));

        //确认系统中无247节点
        quitPeer(peer1IPPort,PEER3IP,tcpPort);
        //停止节点，修改配置文件使用节点licence名称为peerTest.lic
        String okConfig="cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/base.toml";
        String peerTestlicConf="sed -i \"s/peer.lic/peerTest.lic/g\" "+PTPATH+"peer/conf/base.toml";
        String cpLic="cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerTest.lic";

        setAndRestartPeerList(okConfig,peerTestlicConf,cpLic);

//        Shell shellPeer1=new Shell(PEER1IP,USERNAME,PASSWD);
//        shellPeer1.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
//        shellPeer1.execute("cp "+PTPATH+"peer/conf/baseOK.toml "+PTPATH+"peer/conf/base.toml");
//        shellPeer1.execute("sed -i \"s/peer.lic/peerTest.lic/g\" "+PTPATH+"peer/conf/base.toml");
//
//        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
//        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
//        shellPeer2.execute("cp "+PTPATH+"peer/conf/baseOK.toml "+PTPATH+"peer/conf/base.toml");
//        shellPeer2.execute("sed -i \"s/peer.lic/peerTest.lic/g\" "+PTPATH+"peer/conf/base.toml");

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellPeer3.execute("cp "+PTPATH+"peer/conf/baseOK.toml "+PTPATH+"peer/conf/base.toml");
        shellPeer3.execute("sed -i \"s/peer.lic/peerTest.lic/g\" "+PTPATH+"peer/conf/base.toml");

//        //重新生成节点个数为2的240证书并拷贝至节点目录
//        genLicence(PEER1IP,PEER1MAC,PEER1IP,"20",String.valueOf(basePeerNo),version.substring(0,3));
//        shellPeer1.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerTest.lic");
//
//        //重新生成节点个数为2的246证书并拷贝至节点目录
//        genLicence(PEER2IP,PEER2MAC,PEER2IP,"20",String.valueOf(basePeerNo),version.substring(0,3));
//        shellPeer2.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerTest.lic");

        //重新生成节点个数为4的247证书并拷贝至节点目录
        genLicence(PEER3IP,PEER3MAC,PEER3IP,"20","4",version.substring(0,3));
        shellPeer3.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerTest.lic");

        //启动节点240/246
//        startPeer(PEER1IP);
//        startPeer(PEER2IP);

//        Thread.sleep(STARTSLEEPTIME);
        //检查当前节点列表个数basePeerNo,成功则证明节点启动无异常
        queryPeerList(peer1IPPort,basePeerNo);

        //动态加入节点247
        addConsensusPeer(peer1IPPort,PEER3IP,tcpPort,"peers exceed the limit(3)");
        startPeer(PEER3IP);
        Thread.sleep(STARTSLEEPTIME);
        queryPeerList(peer1IPPort,basePeerNo); //检查节点247已经启动成功

        quitPeer(peer1IPPort,PEER3IP,tcpPort);
        Thread.sleep(2000);
        shellPeer3.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        //重新生成节点个数为3的247证书并拷贝至节点目录
        //ToolIP=PEER3IP;
        genLicence(PEER3IP,PEER3MAC,PEER3IP,"20","3",version.substring(0,3));
        shellPeer3.execute("cp "+PTPATH+"toolkit/peer.lic "+PTPATH+"peer/peerTest.lic");

        //动态加入节点247
        addConsensusPeer(peer1IPPort,PEER3IP,tcpPort,"peers exceed the limit(3)");
        startPeer(PEER3IP);
        Thread.sleep(STARTSLEEPTIME);
        checkParam(PEER1IP,"./toolkit health -p "+PEER3IP+":"+rpcPort,"connection error");

        quitPeer(peer1IPPort,PEER3IP,tcpPort);
        Thread.sleep(2000);

        //恢复原始配置重新启动节点
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/base.toml");
        queryPeerList(peer1IPPort,basePeerNo);
    }

    public String genLicence(String shellIP,String macAddr,String ipAddr,String validPeriod,String maxPeerNo,String version)throws Exception{
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
//        String macAddr="02:42:70:46:5f:71";
//        String ipAddr="10.1.3.165";
//        String validPeriod="365";
//        String maxPeerNo = "6";
        String macSetting=macAddr.isEmpty()?"":" -m "+macAddr;
        String ipSetting=ipAddr.isEmpty()?"":" -p "+ipAddr;
        String validSetting=validPeriod.isEmpty()?"":" -d "+validPeriod;
        String NoSetting=maxPeerNo.isEmpty()?"":" -n "+maxPeerNo;
        String Version=version.isEmpty()?"":" -v "+version;
        //String cmd1=toolPath+"./license create"+macSetting + ipSetting + validSetting + NoSetting;
        String cmd1=toolPath+"./license create"+macSetting + ipSetting + validSetting + NoSetting + Version;
        shell1.execute(cmd1);
        Date date = new Date();
        timeStamp = date.getTime();
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    public String deLicence(String shellIP,String licPath)throws Exception{
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
//        String licPath="peer.lic";
        String licPayjSetting=licPath.isEmpty()?"":" -p "+licPath;
        String cmd1=toolPath+"./license decode" + licPayjSetting;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

}
