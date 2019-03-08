package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
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
    //public static final String ToolIP="10.1.3.240";
    String ToolIP="10.1.3.240";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    TestBuilder testBuilder=TestBuilder.getInstance();
    Store store =testBuilder.getStore();

    String version="dev190307.1";
    String rpcPort="9300";
    String tcpPort="60030";
    String consType="L";
    String dataType="D";

    String queryPeerIP="10.1.3.240:9300";

    String toolPath="cd /root/zll/permission/toolkit;";

    String MAC1_240="02:42:fc:a2:5b:1b";
    //String MAC2_240="00:0c:29:99:e6:75 ";
    String MAC1_246="02:42:c0:31:6b:5c";
    //String MAC2_246="00:0c:29:99:e6:75 ";
    String MAC1_247="02:42:dd:6c:4a:92";
    //String MAC2_247="00:0c:29:99:e6:75 ";
    String IP_240="10.1.3.240";
    String IP_246="10.1.3.246";
    String IP_247="10.1.3.247";

    long timeStamp=0;

    //@Before
//    public void beforeTest() throws Exception {
//
//    }


    /*
    * 此部分内容对节点加入（共识节点、数据节点）、退出功能的综合检查
    * */

    @Test
    public void testFunc() throws Exception{

        String tempCmd="";
        checkParam("./main peer -p","flag needs an argument: 'p' in -p");
        checkParam("./main peer","management");
        checkParam("./main peer -p "+ queryPeerIP.split(":")[0],"missing port");

        checkParam("./main mem -p","flag needs an argument: 'p' in -p");
        checkParam("./main mem","management");
        checkParam("./main mem -p "+ queryPeerIP.split(":")[0],"missing port");

        checkParam("./main health -p","flag needs an argument: 'p' in -p");
        checkParam("./main health","management");
        checkParam("./main health -p "+ queryPeerIP.split(":")[0],"missing port");

        checkParam("./main newtx -p","flag needs an argument: 'p' in -p");
        checkParam("./main newtx","management");
        checkParam("./main newtx -p "+ queryPeerIP.split(":")[0],"missing port");

        checkParam("./main height -p","flag needs an argument: 'p' in -p");
        checkParam("./main height","management");
        checkParam("./main height -p "+ queryPeerIP.split(":")[0],"missing port");

        checkParam("./main query -p","flag needs an argument: 'p' in -p");
        checkParam("./main query -v","flag needs an argument: 'v' in -v");
        checkParam("./main query","management");
        checkParam("./main query -p "+ queryPeerIP.split(":")[0],"missing port");

        checkParam("./main ntx -p","flag needs an argument: 'p' in -p");
        checkParam("./main ntx","management");
        checkParam("./main ntx -p "+ queryPeerIP.split(":")[0],"missing port");

        queryPeerList("10.1.3.240:9300",2);
        checkParam("./main join -p","flag needs an argument: 'p' in -p");
        checkParam("./main join -n","flag needs an argument: 'n' in -n");
        checkParam("./main join -l","flag needs an argument: 'l' in -l");
        checkParam("./main join -w","flag needs an argument: 'w' in -w");
        checkParam("./main join -s","flag needs an argument: 's' in -s");
        checkParam("./main join","management");
        checkParam("./main join -p "+ queryPeerIP.split(":")[0],"management");

        queryPeerList("10.1.3.240:9300",2);

        checkParam("./main observer -p","flag needs an argument: 'p' in -p");
        checkParam("./main observer -n","flag needs an argument: 'n' in -n");
        checkParam("./main observer -l","flag needs an argument: 'l' in -l");
        checkParam("./main observer -w","flag needs an argument: 'w' in -w");
        checkParam("./main observer -s","flag needs an argument: 's' in -s");
        checkParam("./main observer","management");
        checkParam("./main observer -p "+ queryPeerIP.split(":")[0],"management");

        queryPeerList("10.1.3.240:9300",2);


        checkParam("./main quit -p","flag needs an argument: 'p' in -p");
        checkParam("./main quit -n","flag needs an argument: 'n' in -n");
        checkParam("./main quit -l","flag needs an argument: 'l' in -l");
        checkParam("./main quit -w","flag needs an argument: 'w' in -w");
        checkParam("./main quit","management");
        checkParam("./main quit -p "+ queryPeerIP.split(":")[0],"management");
        queryPeerList("10.1.3.240:9300",2);

        checkParam("./main permission -p","flag needs an argument: 'p' in -p");
        checkParam("./main permission -d","flag needs an argument: 'd' in -d");
        checkParam("./main permission -m","flag needs an argument: 'm' in -m");
        checkParam("./main permission","management");
        checkParam("./main peer -p "+ queryPeerIP.split(":")[0],"missing port");

        checkParam("./main getpermission -p","flag needs an argument: 'p' in -p");
        checkParam("./main getpermission -d","flag needs an argument: 'd' in -d");
        checkParam("./main getpermission","management");
        checkParam("./main permission -p "+ queryPeerIP.split(":")[0],"missing port");

        checkParam("./main getid -p","flag needs an argument: 'p' in -p");
        checkParam("./main getid","management");


        checkParam("./main licence -p","flag needs an argument: 'p' in -p");
        checkParam("./main licence -m","flag needs an argument: 'm' in -m");
        checkParam("./main licence","management");

        checkParam("./main delicence -p","flag needs an argument: 'p' in -p");
        checkParam("./main delicence","management");
    }

    public void checkParam(String cmd,String chkStr)throws Exception{
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
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
        //先将待加入节点进程停止
        Shell shell1=new Shell("10.1.3.247",USERNAME,PASSWORD);
        shell1.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");

        quitPeer(queryPeerIP,"10.1.3.247",tcpPort);

        Thread.sleep(2000);
        queryPeerList(queryPeerIP,2);

        //检查配置文件中预设的共识节点，即搭建环境时配置的共识节点
        chkPeerSimInfoOK("10.1.3.240:9300",tcpPort,version,consType);

        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        addConsensusPeer(queryPeerIP,"10.1.3.247",tcpPort,"update success");
        queryPeerList(queryPeerIP,3);

        Thread.sleep(3000);

        startPeer("10.1.3.247");
        Thread.sleep(12000);
        chkPeerSimInfoOK("10.1.3.247:9300",tcpPort,version,consType);
        queryPeerList(queryPeerIP,3);
        queryPeerList("10.1.3.247"+":"+rpcPort,3);
        String height=queryBlockHeight("10.1.3.240:9300");
        assertEquals(queryBlockHeight("10.1.3.246:9300"),height);
        assertEquals(queryBlockHeight("10.1.3.247:9300"),height);

        shell1.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        Thread.sleep(3000);


        sendNewTx("10.1.3.240:9300","1","1");

        //检查动态加入的数据节点，即使用管理工具加入的数据节点信息
        quitPeer(queryPeerIP,"10.1.3.247",tcpPort);
        queryPeerList(queryPeerIP,2);
        Thread.sleep(4000);

        addDataPeer(queryPeerIP,"10.1.3.247",tcpPort,"update success");
        queryPeerList(queryPeerIP,3);//通过共识节点查询集群列表

        startPeer("10.1.3.247");
        Thread.sleep(12000);
        chkPeerSimInfoOK("10.1.3.247:9300",tcpPort,version,dataType);
        queryPeerList("10.1.3.247"+":"+rpcPort,3);//通过非共识节点查询集群列表

        height=queryBlockHeight("10.1.3.240:9300");
        assertEquals(queryBlockHeight("10.1.3.246:9300"),height);
        assertEquals(queryBlockHeight("10.1.3.247:9300"),height);

        quitPeer(queryPeerIP,"10.1.3.247",tcpPort);
        queryPeerList(queryPeerIP,2);
        shell1.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");

        //检查配置文件中预设的数据节点，即搭建环境时配置的数据节点
        chkPeerSimInfoOK("10.1.3.164:9100","60002",version,dataType);

        //检查未启动或者不存在的节点
        chkPeerSimInfoErr("10.1.3.164:9300","connection refused");
        chkPeerSimInfoErr("","management");

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
        chkPeerDetailsOK("10.1.3.240:9300","60030",version,consType,"LaunchTime:",
                        "Info","DbPath:","sm2","sm3","Consensus:","2.0","1000","102400");
    }

    public void chkPeerSimInfoOK(String queryIPPort,String tcpPort,String version,String Type)throws Exception{
        String tempCmd="";
        //queryIPPort : 10.1.3.240:9300
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        tempCmd=toolPath+"./main peer -p "+queryIPPort;

        String[] temp = queryIP.split("\\.");

        //输出信息，检查主节点
        //String version="dev190301.2";
        String tcpIP=queryIP+":"+tcpPort; //10.1.3.240:60030
        String peerID=temp[3];  //取IP的最后一位点分十进制作为节点ID,ex. 240
        String peerName="peer"+temp[3];//peer240

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        //assertEquals(response.contains("失败"), false);
        assertEquals(response.contains(version), true);
        assertEquals(response.contains(tcpIP), true);
        assertEquals(response.contains(peerID), true);
        assertEquals(response.contains(peerName), true);
        //assertEquals(response.contains(Type), true);
        assertEquals(response.contains(rpcPort), true);
    }

    public void chkPeerSimInfoErr(String peerIPPort,String ErrMsg)throws Exception{
        String tempCmd="";
        String IPSetting = peerIPPort.isEmpty()?"":" -p "+peerIPPort;
        tempCmd=toolPath+"./main peer "+IPSetting;

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        assertEquals(response.contains(ErrMsg), true);
    }


    public void startPeer(String peerIP)throws Exception{
    //public void startPeer()throws Exception{
        //Shell shell1=new Shell("10.1.3.247",USERNAME,PASSWORD);
        Shell shell1=new Shell(peerIP,USERNAME,PASSWORD);
        //shell1.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        Thread.sleep(2000);
        shell1.execute("sh /root/zll/permission/peer/start.sh");
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
        //queryIPPort : 10.1.3.240:9300
        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        tempCmd=toolPath+"./main peer -p "+queryIPPort+" -i";

        String[] temp = queryIP.split("\\.");

        //输出信息，检查主节点
        String tcpIP=queryIP+":"+checkinfo[0]; //10.1.3.240:60030
        String peerID=temp[3];  //取IP的最后一位点分十进制作为节点ID,ex. 240
        String peerName="peer"+temp[3];//peer240

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
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



    public void queryPeerList(String queryPeerIP,int peerNo) throws Exception{
        Thread.sleep(1500);
        String tempCmd="";

        tempCmd=toolPath+"./main mem -p "+queryPeerIP;

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        log.info("\nthe actual lines of stdout"+stdout.size());
        assertEquals(peerNo,stdout.size()-2);
        assertEquals(response.contains("isLeader"), true);


    }


    @Test
    public void checkPeerHealth()throws Exception{
        String tempCmd="";
        String targetPeerIp=queryPeerIP;
        tempCmd=toolPath+"./main health -p "+targetPeerIp;

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
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

        //-p参数中不带port
        tempCmd=toolPath+"./main health -p "+targetPeerIp.split(":")[0];
        shell1.execute(tempCmd);
        ArrayList<String> stdout1 = shell1.getStandardOutput();
        String response1 = StringUtils.join(stdout1,"\n");
        log.info("\n"+response1);
        assertEquals(response1.contains("missing port in address"), true);

        //无-p参数
        tempCmd=toolPath+"./main health ";
        shell1.execute(tempCmd);
        ArrayList<String> stdout2 = shell1.getStandardOutput();
        String response2 = StringUtils.join(stdout2,"\n");
        log.info("\n"+response2);
        assertEquals(response2.contains("missing port in address"), true);

        //查询不是一个区块链系统中的节点
        tempCmd=toolPath+"./main health -p 10.1.3.240:9500";
        shell1.execute(tempCmd);
        ArrayList<String> stdout3 = shell1.getStandardOutput();
        String response3 = StringUtils.join(stdout3,"\n");
        log.info("\n"+response3);
        assertEquals(response3.contains("connection refused"), true);
    }

    @Test
    public  void testTX()throws Exception{
        int blockHeight=0;
        String rsp="";
        blockHeight=Integer.parseInt(queryBlockHeight((queryPeerIP)));

        //确认管理工具获取的高度与sdk获取高度一致，需要注意sdk是否有权限执行获取高度
        rsp = store.GetHeight();
        assertEquals(JSONObject.fromObject(rsp).getString("Data"), queryBlockHeight((queryPeerIP)));

        //发送单笔交易
        rsp=sendNewTx(queryPeerIP,"","");
        assertEquals(rsp.contains(queryPeerIP), true);
        Thread.sleep(6000);
        assertEquals(blockHeight+1, Integer.parseInt(queryBlockHeight((queryPeerIP))));

        rsp = queryBlockByHeight(queryPeerIP,String.valueOf(blockHeight));
        assertEquals(rsp.contains(String.valueOf(blockHeight)), true);

    }
    public String sendNewTx(String queryPeerIP,String txNo,String txType)throws Exception{

        String tempTxNo= txNo.isEmpty()?"":" -n "+txNo;
        String tempTxType=txType.isEmpty()?"":" -t "+txType;
        //String tempCycleSend=cycleSend.isEmpty()?"":" -f "+cycleSend;
        String temp=tempTxNo+tempTxType;

        String response="";

        String tempCmd="";
        String targetPeerIp=queryPeerIP;
        tempCmd=toolPath+"./main newtx -p "+targetPeerIp+temp;

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
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

    public String queryBlockHeight(String queryPeerIP)throws Exception{
        String tempCmd="";
        tempCmd=toolPath+"./main height -p "+queryPeerIP;
        String blockHeight="";

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
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


    public String queryBlockByHeight(String queryPeerIP,String height)throws Exception{
        String tempCmd="";
        tempCmd=toolPath+"./main query -p "+queryPeerIP+" -v "+height;

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        return response;
    }

    @Test
    public void TxCheck()throws Exception{
        String rsp=queryPeerUnconfirmedTx("10.1.3.240:9300");
        assertEquals(rsp.contains("Count:\t0"),true);
        //log.info("Current Unconfirmed Tx Count:"+rsp.substring(rsp.lastIndexOf("Count:")+1).trim());

        Shell shell240=new Shell(IP_240,USERNAME,PASSWORD);
        shell240.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell240.execute("sed -i \"s/PackTime = 1000/PackTime = 10000/g\" /root/zll/permission/peer/conf/base.toml");

        Shell shell246=new Shell(IP_246,USERNAME,PASSWORD);
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell246.execute("sed -i \"s/PackTime = 1000/PackTime = 10000/g\" /root/zll/permission/peer/conf/base.toml");

        startPeer(IP_240);
        startPeer(IP_246);

        Thread.sleep(12000);

        rsp = sendNewTx("10.1.3.240:9300","3","1");
        assertEquals(rsp.contains("HashData"),true);

        rsp=queryPeerUnconfirmedTx("10.1.3.240:9300");

        assertEquals(rsp.contains("Count:\t3"),true);

        shell240.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell240.execute("sed -i \"s/PackTime = 10000/PackTime = 1000/g\" /root/zll/permission/peer/conf/base.toml");
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell246.execute("sed -i \"s/PackTime = 10000/PackTime = 1000/g\" /root/zll/permission/peer/conf/base.toml");

        startPeer(IP_240);
        startPeer(IP_246);

        Thread.sleep(12000);

    }

    public String queryPeerUnconfirmedTx(String queryPeerIP)throws Exception{
        String tempCmd="";
        String txID="";
        tempCmd=toolPath+"./main ntx -p "+queryPeerIP+" -i";

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        return response;
    }


    public void addConsensusPeer(String netPeerIP,String peerIP,String tcpPort,String chkMsg)throws Exception{
        String tempCmd="";
        String[] temp = peerIP.split("\\.");
        String peerID=temp[3];
        String peerName="peer"+temp[3];
        String peerIPlan=peerIP+":"+tcpPort;
        String peerIPwan=peerIP+":"+tcpPort;
        tempCmd=toolPath+"./main join -p "+netPeerIP+" -n "+peerID+" -s "+peerName
                +" -l "+peerIPlan+" -w "+peerIPwan;

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        //assertEquals(response.contains("失败"), false);
        assertEquals(response.contains(chkMsg), true);
    }

    public void addDataPeer(String netPeerIP,String peerIP,String tcpPort,String chkMsg)throws Exception{
        String tempCmd="";
        String[] temp = peerIP.split("\\.");
        String peerID=temp[3];
        String peerName="peer"+temp[3];
        String peerIPlan=peerIP+":"+tcpPort;
        String peerIPwan=peerIP+":"+tcpPort;
        tempCmd=toolPath+"./main observer -p "+netPeerIP+" -n "+peerID+" -s "+peerName
                +" -l "+peerIPlan+" -w "+peerIPwan;

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        //assertEquals(response.contains("失败"), false);
        assertEquals(response.contains(chkMsg), true);

    }

    public void quitPeer(String netPeer,String peerIP,String tcpPort)throws Exception{
        String tempCmd="";
        String[] temp = peerIP.split("\\.");

        String peerID=temp[3];
        String peerIPlan=peerIP+":"+tcpPort;
        String peerIPwan=peerIP+":"+tcpPort;
        tempCmd=toolPath+"./main quit -p "+netPeer+" -n "+peerID+" -l "+peerIPlan+ " -w "+peerIPwan;

        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
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
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        String cmd1=toolPath+"./main permission -p "+netPeerIP+" -d "+ sdkID+" -m "+permStr;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    public String getPeerPerm(String netPeerIP,String sdkID)throws Exception{
        String cmd1="";
        String response="wrong";
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        if( sdkID != "") {
            cmd1 = toolPath + "./main getpermission -p " + netPeerIP + " -d " + sdkID;
            shell1.execute(cmd1);
            ArrayList<String> stdout = shell1.getStandardOutput();
            response = StringUtils.join(stdout, "\n");
        }else{
                cmd1=toolPath+"./main getpermission -p "+netPeerIP;
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
        rsp = setPeerPerm(queryPeerIP,sdkID1,"1,2,3");
        assertEquals(rsp.contains(succRsp), true);

        rsp = setPeerPerm(queryPeerIP,sdkID2,"0");
        assertEquals(rsp.contains(succRsp), true);

        rsp = setPeerPerm(queryPeerIP,sdkID3,"999");
        assertEquals(rsp.contains(succRsp), true);

        rsp = setPeerPerm(queryPeerIP,sdkID4,"1,2,3");
        assertEquals(rsp.contains(errRsp), true);


        rsp=getPeerPerm(queryPeerIP,"");
        assertEquals(rsp.contains(sdkID1), true);
        assertEquals(rsp.contains("peermission:[1 2 3]"), true);
        assertEquals(rsp.contains(sdkID2), true);
        assertEquals(rsp.contains("peermission:[0]"), true);
        assertEquals(rsp.contains(sdkID3), true);
        assertEquals(rsp.contains("peermission:[1 2 3 4 5 6 7 8 9 10 21 22 23 24 25 211 212 221 222 223 231 232 233 234 235 251 252]"), true);
        assertEquals(rsp.contains(sdkID4), false);

        rsp=getPeerPerm(queryPeerIP,sdkID1);
        assertEquals(rsp.contains("peermission:[1 2 3]"), true);

    }

    @Test
    public void testGetID()throws Exception{
        ///root/zll/permission/toolkit/
        assertEquals(getID("tls/key.pem","sm2").contains("id"),true);
        assertEquals(getID("tls/key.pem","").contains("id"),true);
        assertEquals(getID("tls/key.pem","ecc").contains("failed to parse EC private key embedded in PKCS#8: x509"),true);
        assertEquals(getID("tls/key.pem","eee").contains("unsupport sign algorithm"),true);


        assertEquals(getID("ecdsa/key.pem","ecc").contains("id"),true);
        assertEquals(getID("ecdsa/key.pem","").contains("id"),true);
        assertEquals(getID("ecdsa/key.pem","sm2").contains("id"),true);
        assertEquals(getID("ecdsa/key.pem","eee").contains("unsupport sign algorithm"),true);

        assertEquals(getID("","sm2").contains("management"),true);
        assertEquals(getID("key.pem","ecc").contains(" no such file or directory"),true);
    }

    public String getID(String keyPath,String cryptType)throws Exception{
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
//        String keyPath="key.pem";
//        String cryptType="sm2";
//        String hashType="sm3";
        String keySetting=keyPath.isEmpty()?"":" -p "+keyPath;
        String crySetting=cryptType.isEmpty()?"":" -k "+cryptType;

        String cmd1=toolPath+"./main getid"+keySetting + crySetting ;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    @Test
    public void testLicGenAndDec() throws Exception{
        String rsp="";
        String dayTime="365";
        String PeerNo="6";
        //生成证书
        rsp = genLicence(MAC1_240,IP_240,dayTime,PeerNo);
        assertEquals(rsp.contains(MAC1_240),true);
        assertEquals(rsp.contains(IP_240),true);
        assertEquals(rsp.contains("dayTime:"+dayTime),true);
        assertEquals(rsp.contains("PeerNum:"+PeerNo),true);

        //解析证书 确认参数一致
        rsp = deLicence("peer.lic");
        assertEquals(rsp.contains("PeerNum:"+PeerNo),true);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String outTime =df.format(new Date(timeStamp+Long.parseLong(dayTime)*24*60*60*1000));
        log.info("OutTime:"+outTime);
        assertEquals(rsp.contains(outTime.split(" ")[0]),true); //确认有效期时间正确，因虚拟机与测试机可能不是同一台可能存在时间差，因此目前仅校验年月日是否正确

        Shell shell240=new Shell(IP_240,USERNAME,PASSWORD);
        shell240.execute("rm -f /root/zll/permission/toolkit/peer.lic");
        assertEquals(deLicence("peer.lic").contains("open peer.lic: no such file or directory"),true);


        //生成使用无效的参数验证:无效的mac地址、无效IP地址、无效时间、无效节点数
        checkParam("./main licence -m 12:11 -p 10.1.3.240 -d 100 -n 6","invalid MAC address");
        assertEquals(deLicence("peer.lic").contains("open peer.lic: no such file or directory"),true);

        checkParam("./main licence -m 02:42:fc:a2:5b:1b -p 10.1 -d 100 -n 6","invalid IP address");
        assertEquals(deLicence("peer.lic").contains("open peer.lic: no such file or directory"),true);

        checkParam("./main licence -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 0.5 -n 6","invalid argument");
        assertEquals(deLicence("peer.lic").contains("open peer.lic: no such file or directory"),true);

        checkParam("./main licence -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 5 -n 0.5","invalid argument");
        assertEquals(deLicence("peer.lic").contains("open peer.lic: no such file or directory"),true);

        checkParam("./main licence -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 5 -n 0","success");

        rsp = deLicence("peer.lic");
        assertEquals(rsp.contains("PeerNum:0"),true);

        //解析证书使用无效参数
        checkParam("./main delicence -p ./crypt/key.pem","data Illegal");
        checkParam("./main delicence -p ./crypt/key.pem","data Illegal");

    }

    @Test
    public void testLicValidForPeer()throws Exception{

        //验证已过期证书，此证书需要提前准备 246已有过期证书peer246d1n2.lic
        log.info("********************Test for licence timeout********************");
        ToolIP= IP_246;
        Shell shell246=new Shell(IP_246,USERNAME,PASSWORD);
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell246.execute("sed -i \"s/peer.lic/peer246d1n2.lic/g\" /root/zll/permission/peer/conf/base.toml");
        startPeer(IP_246);
        Thread.sleep(10000);
        checkParam("./main health -p 10.1.3.246:9300","connection error");
        shell246.execute("sed -i \"s/peer246d1n2.lic/peer.lic/g\" /root/zll/permission/peer/conf/base.toml");

        log.info("********************Test for invalid peer count********************");
        //验证节点数据小于配置文件中节点数场景
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(MAC1_246,IP_246,"200","1");
        shell246.execute("cp /root/zll/permission/toolkit/peer.lic /root/zll/permission/peer/peerTest.lic");
        shell246.execute("sed -i \"s/peer.lic/peerTest.lic/g\" /root/zll/permission/peer/conf/base.toml");
        startPeer(IP_246);
        Thread.sleep(10000);
        checkParam("./main health -p 10.1.3.246:9300","connection error");

        log.info("********************Test for invalid MAC addr ********************");
        //证书IP正确，MAC不正确
        shell246.execute("sed -i \"s/peerTest.lic/peer.lic/g\" /root/zll/permission/peer/conf/base.toml");
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(MAC1_240,IP_246,"200","3");
        shell246.execute("cp /root/zll/permission/toolkit/peer.lic /root/zll/permission/peer/peerTest.lic");
        shell246.execute("sed -i \"s/peer.lic/peerTest.lic/g\" /root/zll/permission/peer/conf/base.toml");
        startPeer(IP_246);
        Thread.sleep(10000);
        checkParam("./main health -p 10.1.3.246:9300","connection error");

        log.info("********************Test for invalid IP addr ********************");
        //证书MAC正确，IP不正确
        shell246.execute("sed -i \"s/peerTest.lic/peer.lic/g\" /root/zll/permission/peer/conf/base.toml");
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(MAC1_246,IP_240,"200","3");
        shell246.execute("cp /root/zll/permission/toolkit/peer.lic /root/zll/permission/peer/peerTest.lic");
        shell246.execute("sed -i \"s/peer.lic/peerTest.lic/g\" /root/zll/permission/peer/conf/base.toml");
        startPeer(IP_246);
        Thread.sleep(10000);
        checkParam("./main health -p 10.1.3.246:9300","connection error");

        log.info("********************Test for invalid IP&MAC addr ********************");
        //使用其他节点licence
        shell246.execute("sed -i \"s/peerTest.lic/peer.lic/g\" /root/zll/permission/peer/conf/base.toml");
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        genLicence(MAC1_240,IP_240,"200","3");
        shell246.execute("cp /root/zll/permission/toolkit/peer.lic /root/zll/permission/peer/peerTest.lic");
        shell246.execute("sed -i \"s/peer.lic/peerTest.lic/g\" /root/zll/permission/peer/conf/base.toml");
        startPeer(IP_246);
        Thread.sleep(10000);
        checkParam("./main health -p 10.1.3.246:9300","connection error");

        log.info("********************Test for valid licence********************");
        //恢复配置并重启，使用有效证书验证
        shell246.execute("sed -i \"s/peerTest.lic/peer.lic/g\" /root/zll/permission/peer/conf/base.toml");
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        startPeer(IP_246);
        Thread.sleep(10000);
        ToolIP= IP_240;
        queryPeerList(queryPeerIP,2);
    }

    //此用例需要保证各个节点上都存在管理工具
    //目前规划目录：10.1.3.240/246/247  /root/zll/permission/toolkit
    @Test
    public void testLicForAddPeer()throws Exception{
        //确认系统中无247节点
        quitPeer(queryPeerIP,IP_247,tcpPort);
        //停止节点，修改配置文件使用节点licence名称为peerTest.lic
        Shell shell240=new Shell(IP_240,USERNAME,PASSWORD);
        shell240.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell240.execute("sed -i \"s/peer.lic/peerTest.lic/g\" /root/zll/permission/peer/conf/base.toml");

        Shell shell246=new Shell(IP_246,USERNAME,PASSWORD);
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell246.execute("sed -i \"s/peer.lic/peerTest.lic/g\" /root/zll/permission/peer/conf/base.toml");

        Shell shell247=new Shell(IP_247,USERNAME,PASSWORD);
        shell247.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell247.execute("sed -i \"s/peer.lic/peerTest.lic/g\" /root/zll/permission/peer/conf/base.toml");

        //重新生成节点个数为2的240证书并拷贝至节点目录
        genLicence(MAC1_240,IP_240,"20","2");
        shell240.execute("cp /root/zll/permission/toolkit/peer.lic /root/zll/permission/peer/peerTest.lic");

        //重新生成节点个数为2的246证书并拷贝至节点目录
        ToolIP="10.1.3.246";
        genLicence(MAC1_246,IP_246,"20","2");
        shell246.execute("cp /root/zll/permission/toolkit/peer.lic /root/zll/permission/peer/peerTest.lic");

        //重新生成节点个数为2的247证书并拷贝至节点目录
        ToolIP="10.1.3.247";
        genLicence(MAC1_247,IP_247,"20","3");
        shell247.execute("cp /root/zll/permission/toolkit/peer.lic /root/zll/permission/peer/peerTest.lic");

        ToolIP="10.1.3.240";
        //启动节点240/246
        startPeer(IP_240);
        startPeer(IP_246);

        Thread.sleep(12000);
        //检查当前节点列表个数为2,成功则证明节点启动无异常
        queryPeerList(queryPeerIP,2);

        //动态加入节点247
        addConsensusPeer(queryPeerIP,IP_247,tcpPort,"update success");
        startPeer(IP_247);
        Thread.sleep(12000);
        queryPeerList("10.1.3.247:9300",3); //检查节点247已经启动成功

        quitPeer(queryPeerIP,IP_247,tcpPort);
        Thread.sleep(2000);
        shell247.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");

        //重新生成节点个数为2的247证书并拷贝至节点目录
        ToolIP="10.1.3.247";
        genLicence(MAC1_247,IP_247,"20","2");
        shell247.execute("cp /root/zll/permission/toolkit/peer.lic /root/zll/permission/peer/peerTest.lic");

        //动态加入节点247
        addConsensusPeer(queryPeerIP,IP_247,tcpPort,"update success");
        startPeer(IP_247);
        Thread.sleep(12000);
        checkParam("./main health -p "+IP_247+":"+rpcPort,"connection error");

        quitPeer(queryPeerIP,IP_247,tcpPort);
        Thread.sleep(2000);

        //恢复原始配置重新启动节点
        shell240.execute("sed -i \"s/peerTest.lic/peer.lic/g\" /root/zll/permission/peer/conf/base.toml");
        shell246.execute("sed -i \"s/peerTest.lic/peer.lic/g\" /root/zll/permission/peer/conf/base.toml");
        shell240.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        shell246.execute("ps -ef |grep peer |grep -v grep |awk '{print $2}'|xargs kill -9");
        startPeer(IP_240);
        startPeer(IP_246);
        Thread.sleep(12000);
        queryPeerList(queryPeerIP,2);
        ToolIP="10.1.3.240";
    }

    public String genLicence(String macAddr,String ipAddr,String validPeriod,String maxPeerNo)throws Exception{
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
//        String macAddr="02:42:70:46:5f:71";
//        String ipAddr="10.1.3.165";
//        String validPeriod="365";
//        String maxPeerNo = "6";
        String macSetting=macAddr.isEmpty()?"":" -m "+macAddr;
        String ipSetting=ipAddr.isEmpty()?"":" -p "+ipAddr;
        String validSetting=validPeriod.isEmpty()?"":" -d "+validPeriod;
        String NoSetting=maxPeerNo.isEmpty()?"":" -n "+maxPeerNo;
        String cmd1=toolPath+"./main licence"+macSetting + ipSetting + validSetting + NoSetting;
        shell1.execute(cmd1);
        Date date = new Date();
        timeStamp = date.getTime();
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    public String deLicence(String licPath)throws Exception{
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
//        String licPath="peer.lic";
        String licPayjSetting=licPath.isEmpty()?"":" -p "+licPath;
        String cmd1=toolPath+"./main delicence" + licPayjSetting;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

}
