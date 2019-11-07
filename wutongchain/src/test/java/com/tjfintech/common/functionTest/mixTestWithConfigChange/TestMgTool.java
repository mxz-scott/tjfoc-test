package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

//管理工具不再对外提供 故管理工具仅作为测试辅助工具 不作为版本测试内容 20191105
@Slf4j
public class TestMgTool {
    public static final int STARTSLEEPTIME=40000;
    TestBuilder testBuilder=TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();

    String rpcPort=PEER3RPCPort;
    String tcpPort=PEER3TCPPort;
    String consType="L";
    String dataType="D";
    int basePeerNo = 3;
    int DynamicPeerNo = 4;
    String ipType="/ip4/";
    String tcpType="/tcp/";
    int memInfoNo = 12;//memberlist中返回节点信息字段个数，目前返回id，state，version，port，shownName，inAddr，outAddr，typ，height


    String toolPath="cd " + ToolPATH + ";";
    String peer1IPPort=PEER1IP+":"+PEER1RPCPort;
    String peer2IPPort=PEER2IP+":"+PEER2RPCPort;
    String peer3IPPort=PEER3IP+":"+PEER3RPCPort;

    ArrayList<String > txHashList =new ArrayList<>();

    //@Before
    public void resetPeerEnv()throws Exception{
        BeforeCondition bf =new BeforeCondition();
        bf.setPermission999();
        PEER1MAC=getMACAddr(PEER1IP,USERNAME,PASSWD).trim();
        PEER2MAC=getMACAddr(PEER2IP,USERNAME,PASSWD).trim();
        PEER3MAC=getMACAddr(PEER3IP,USERNAME,PASSWD).trim();
        PEER4MAC=getMACAddr(PEER4IP,USERNAME,PASSWD).trim();

        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute(killPeerCmd);
        shellPeer3.execute(resetPeerBase);
        //startPeer(PEER3IP);
        queryPeerListNo(peer1IPPort,basePeerNo);

    }

    public String parseMemInfo(String sourceStr,String uniqueValue,String queryKey) throws Exception{
        JSONObject memObj = JSONObject.fromObject(sourceStr.substring(sourceStr.indexOf("{")));
        JSONArray jsonArrayMem = memObj.getJSONArray("memberList");
        log.info("Member No.: " + jsonArrayMem.size());
        String queryValue = "";
        log.info("unique string: " + uniqueValue);
        for(int i = 0 ;i < jsonArrayMem.size();i++){
            String temp = jsonArrayMem.get(i).toString();
            if(!(temp.contains(uniqueValue) && temp.contains(queryKey))) continue;

            JSONObject eachMem = JSONObject.fromObject(temp);
            log.info("each mem key no.: " + eachMem.size());
            log.info(eachMem.toString());
            assertEquals(memInfoNo,eachMem.size());
            queryValue = eachMem.getString(queryKey);
        }
        assertEquals(false,queryValue.isEmpty()); //确认查询关键字结果非空
        return queryValue.trim();
    }

    public void checkMemInfoExHeight(String chkResp,String peerIP,String...memInfoArr)throws Exception{
        assertEquals(memInfoArr[0],parseMemInfo(chkResp,peerIP,"id"));
        assertEquals(memInfoArr[1],parseMemInfo(chkResp,peerIP,"state"));
        assertEquals(memInfoArr[2],parseMemInfo(chkResp,peerIP,"version"));
        assertEquals(memInfoArr[3],parseMemInfo(chkResp,peerIP,"port"));
        assertEquals(memInfoArr[4],parseMemInfo(chkResp,peerIP,"shownName"));
        assertEquals(memInfoArr[5],parseMemInfo(chkResp,peerIP,"inAddr"));
        assertEquals(memInfoArr[6],parseMemInfo(chkResp,peerIP,"outAddr"));
        assertEquals(memInfoArr[7],parseMemInfo(chkResp,peerIP,"typ"));
//        assertEquals(memInfoArr[8],parseMemInfo(chkResp,peerIP,"height"));
        assertEquals(memInfoArr[8],parseMemInfo(chkResp,peerIP,"TLSEnabled"));
        assertEquals(memInfoArr[9],parseMemInfo(chkResp,peerIP,"hashType"));
        assertEquals(memInfoArr[10],parseMemInfo(chkResp,peerIP,"consensus"));

    }

   // @Test
    public void CheckMemRespInfo() throws Exception{
        String response = mgToolCmd.queryMemberList(PEER1IP + ":" +PEER1RPCPort);
        parseMemInfo(response,"10.1.3.240","state");
    }

    @Test
    public void chkMemberList() throws Exception{
        long killSleepTime = 5000;//节点状态是通过p2p实现状态信息传送，当前默认4s 因此需要差不多此时间的等待
        peerList.clear();
        peerList.add(PEER1IP);
        peerList.add(PEER2IP);
        peerList.add(PEER4IP);

        ArrayList<String> portList = new ArrayList<>();
        portList.add(PEER1RPCPort);
        portList.add(PEER2RPCPort);
        portList.add(PEER4RPCPort);


        //检查所有集群列表中的leader信息、节点信息及节点状态all connect is true信息 2.1.1不再显示isLeader信息
        for (int i=0;i<peerList.size();i++) {
            String response = mgToolCmd.queryMemberList(peerList.get(i) + ":" + portList.get(i));
            //assertEquals(response.contains("isLeader"), true); 2.1.1版本不再支持显示isLeader信息
            assertEquals(response.contains(PEER1IP), true);
            assertEquals(response.contains(PEER2IP), true);
            assertEquals(response.contains(PEER4IP), true);

        }

        //检查停止节点进行后的集群列表节点状态信息
        Shell shellPeer1=new Shell(peerList.get(0),USERNAME,PASSWD);
        Shell shellPeer2=new Shell(peerList.get(1),USERNAME,PASSWD);
        Shell shellPeer4=new Shell(peerList.get(2),USERNAME,PASSWD);

        shellPeer2.execute(killPeerCmd);
        sleepAndSaveInfo(killSleepTime,"kill peer exe waiting p2p sync...");
        String queryPeer10 = mgToolCmd.queryMemberList(peerList.get(0) + ":" + portList.get(0));
        assertEquals(parseMemInfo(queryPeer10,peerList.get(1),"state"),"1");

        shellPeer4.execute(killPeerCmd);
        sleepAndSaveInfo(killSleepTime,"kill peer exe waiting p2p sync...");

        String queryPeer11 = mgToolCmd.queryMemberList(peerList.get(0) + ":" + portList.get(0));
        assertEquals(parseMemInfo(queryPeer11,peerList.get(1),"state"),"1");
        assertEquals(parseMemInfo(queryPeer11,peerList.get(2),"state"),"1");

        shellPeer2.execute(startPeerCmd);
        shellPeer4.execute(startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);

        String queryPeer12 = mgToolCmd.queryMemberList(peerList.get(0) + ":" + portList.get(0));
        assertEquals(parseMemInfo(queryPeer12,peerList.get(1),"state"),"0");
        assertEquals(parseMemInfo(queryPeer12,peerList.get(2),"state"),"0");

    }
    
    @Test
    public void testFunc() throws Exception{

        ExeToolCmdAndChk(PEER1IP,"./toolkit peer -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit peer","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit peer -p "+ PEER1IP,"unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit peer -p "+ peer1IPPort,"too many colons in address");

        ExeToolCmdAndChk(PEER1IP,"./toolkit mem -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit mem","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit mem -p "+ PEER1IP,"unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit mem -p "+ peer1IPPort,"too many colons in address");

        ExeToolCmdAndChk(PEER1IP,"./toolkit health -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit health","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit health -p "+ PEER1IP,"unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit health -p "+ peer1IPPort,"too many colons in address");

        ExeToolCmdAndChk(PEER1IP,"./toolkit newtx -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit newtx","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit newtx -p "+ PEER1IP,"unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit newtx -p "+ peer1IPPort,"too many colons in address");

        ExeToolCmdAndChk(PEER1IP,"./toolkit height -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit height","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit height -p "+ PEER1IP,"unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit height -p "+ peer1IPPort,"too many colons in address");

        ExeToolCmdAndChk(PEER1IP,"./toolkit query -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit query -v","flag needs an argument: 'v' in -v");
        ExeToolCmdAndChk(PEER1IP,"./toolkit query","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit query -p "+ PEER1IP,"unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit query -p "+ peer1IPPort,"too many colons in address");

//        ExeToolCmdAndChk(PEER1IP,"./toolkit ntx -p","flag needs an argument: 'p' in -p");
//        ExeToolCmdAndChk(PEER1IP,"./toolkit ntx","management");
//        ExeToolCmdAndChk(PEER1IP,"./toolkit ntx -p "+ PEER1IP,"unknown port");
//        ExeToolCmdAndChk(PEER1IP,"./toolkit ntx -p "+ peer1IPPort,"too many colons in address");

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);
        queryPeerListNo(peer1IPPort,basePeerNo);
        ExeToolCmdAndChk(PEER1IP,"./toolkit join -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit join -n","flag needs an argument: 'n' in -n");
        ExeToolCmdAndChk(PEER1IP,"./toolkit join -l","flag needs an argument: 'l' in -l");
        ExeToolCmdAndChk(PEER1IP,"./toolkit join -w","flag needs an argument: 'w' in -w");
        ExeToolCmdAndChk(PEER1IP,"./toolkit join -s","flag needs an argument: 's' in -s");
        ExeToolCmdAndChk(PEER1IP,"./toolkit join","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit join -p "+ PEER1IP+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030 -s peer168","unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit join -p "+ peer1IPPort+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030 -s peer168","too many colons in address");

        queryPeerListNo(peer1IPPort,basePeerNo);

        ExeToolCmdAndChk(PEER1IP,"./toolkit observer -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit observer -n","flag needs an argument: 'n' in -n");
        ExeToolCmdAndChk(PEER1IP,"./toolkit observer -l","flag needs an argument: 'l' in -l");
        ExeToolCmdAndChk(PEER1IP,"./toolkit observer -w","flag needs an argument: 'w' in -w");
        ExeToolCmdAndChk(PEER1IP,"./toolkit observer -s","flag needs an argument: 's' in -s");
        ExeToolCmdAndChk(PEER1IP,"./toolkit observer","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit observer -p "+ PEER1IP+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030 -s peer168","unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit observer -p "+ peer1IPPort+" -n 111 -l 10.1.3.168:60030 -w 10.1.3.168:60030 -s peer168","too many colons in address");

        queryPeerListNo(peer1IPPort,basePeerNo);

        ExeToolCmdAndChk(PEER1IP,"./toolkit quit -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit quit -n","flag needs an argument: 'n' in -n");
        ExeToolCmdAndChk(PEER1IP,"./toolkit quit","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit quit -p "+ PEER1IP+" -n 111","unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit quit -p "+ peer1IPPort+" -n 111","too many colons in address");
        queryPeerListNo(peer1IPPort,basePeerNo);


        ExeToolCmdAndChk(PEER1IP,"./toolkit permission -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit permission -d","flag needs an argument: 'd' in -d");
        ExeToolCmdAndChk(PEER1IP,"./toolkit permission -m","flag needs an argument: 'm' in -m");
        ExeToolCmdAndChk(PEER1IP,"./toolkit permission","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit permission -p "+ PEER1IP,"management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit permission -p "+ peer1IPPort,"management");

        ExeToolCmdAndChk(PEER1IP,"./toolkit getpermission -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit getpermission -d","flag needs an argument: 'd' in -d");
        ExeToolCmdAndChk(PEER1IP,"./toolkit getpermission","management");
        ExeToolCmdAndChk(PEER1IP,"./toolkit getpermission -p "+ PEER1IP,"unknown port");
        ExeToolCmdAndChk(PEER1IP,"./toolkit getpermission -p "+ peer1IPPort,"too many colons in address");

        ExeToolCmdAndChk(PEER1IP,"./toolkit mgToolCmd.getID -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./toolkit mgToolCmd.getID","management");


        ExeToolCmdAndChk(PEER1IP,"./license create -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./license create -m","flag needs an argument: 'm' in -m");
        ExeToolCmdAndChk(PEER1IP,"./license create","management");

        ExeToolCmdAndChk(PEER1IP,"./license decode -p","flag needs an argument: 'p' in -p");
        ExeToolCmdAndChk(PEER1IP,"./license decode","management");
    }
    
    @Test
    public void testPeerInfo()throws Exception{
        //resetPeerEnv();
        //先将待加入节点进程停止
        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute(killPeerCmd);
        shellPeer3.execute("cp "+ PeerPATH + "configjoin.toml "+ PeerPATH + ""+PeerMemConfig+".toml");
        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);

        Thread.sleep(2000);
        queryPeerListNo(peer1IPPort,basePeerNo);

        //检查配置文件中预设的共识节点，即搭建环境时配置的共识节点
        chkPeerSimInfoOK(peer1IPPort,tcpPort,version,consType);

        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("join",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        queryPeerListNo(peer1IPPort,DynamicPeerNo);

        Thread.sleep(3000);

        shellExeCmd(PEER3IP,startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        chkPeerSimInfoOK(peer3IPPort,tcpPort,version,consType);
        queryPeerListNo(peer1IPPort,DynamicPeerNo);
        queryPeerListNo(PEER3IP+":"+rpcPort,DynamicPeerNo);
        String height=mgToolCmd.queryBlockHeight(peer1IPPort);
        assertEquals(mgToolCmd.queryBlockHeight(peer2IPPort),height);
        //assertEquals(mgToolCmd.queryBlockHeight(peer3IPPort),height);//因数据较多时同步数据需要时间，此部分查询检查移除

        shellPeer3.execute(killPeerCmd);
        Thread.sleep(3000);


        mgToolCmd.sendNewTx(peer1IPPort,"1","1");

        //检查动态加入的数据节点，即使用管理工具加入的数据节点信息
        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);
        queryPeerListNo(peer1IPPort,basePeerNo);
        Thread.sleep(4000);

        String resp2 = mgToolCmd.addPeer("observer",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp2.contains("success"));
        queryPeerListNo(peer1IPPort,DynamicPeerNo);//通过共识节点查询集群列表
        shellPeer3.execute("cp "+ PeerPATH + "configobs.toml "+ PeerPATH + ""+PeerMemConfig+".toml");
        shellExeCmd(PEER3IP,startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        chkPeerSimInfoOK(peer3IPPort,tcpPort,version,dataType);
        queryPeerListNo(PEER3IP+":"+rpcPort,DynamicPeerNo);//通过非共识节点查询集群列表

        height=mgToolCmd.queryBlockHeight(peer1IPPort);
        assertEquals(mgToolCmd.queryBlockHeight(peer2IPPort),height);
        //assertEquals(mgToolCmd.queryBlockHeight(peer3IPPort),height);

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);
        queryPeerListNo(peer1IPPort,basePeerNo);
        shellPeer3.execute(killPeerCmd);
        String resp3 = mgToolCmd.addPeer("join",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp3.contains("success"));
        shellExeCmd(PEER3IP,startPeerCmd);//此步骤应该启动不成功，因节点当前配置文件中Type=1，但是使用addConsensusPeer 即join加入，两者不一致时无法启动成功
        Thread.sleep(STARTSLEEPTIME);
//        queryPeerListNo(peer1IPPort,2);
        //shellPeer3.execute(killPeerCmd);


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
        //所有非必须配置采用默认配置
        chkPeerDetailsOK(peer1IPPort,"60030",version,consType,"2019-",
                        "info","peerdb","sm2","sm3","Raft","tjfoc/tjfoc-ccenv 2.0","500","1024");
    }

    public void chkPeerSimInfoOK(String queryIPPort,String tcpPort,String version,String Type)throws Exception{

        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        String[] temp = queryIP.split("\\.");
        String tcpIP=queryIP+"/tcp/"+tcpPort; //10.1.3.240:60030
        String peerID=getPeerId(queryIP,USERNAME,PASSWD);  //取IP的最后一位点分十进制作为节点ID,ex. 240
        String peerName="peer"+temp[3];//peer240

        String response = mgToolCmd.getPeerSimpleInfo(queryIPPort);

        //assertEquals(response.contains("失败"), false);
        assertEquals(response.contains(version), true);
        assertEquals(response.contains(tcpIP), true);
        assertEquals(response.contains(peerID), true);
        assertEquals(response.contains(peerName), true);
        assertEquals(response.contains(Type), true);
        assertEquals(response.contains(rpcPort), true);
    }

    public void chkPeerSimInfoErr(String queryIPPort,String ErrMsg)throws Exception{
        String response = mgToolCmd.getPeerSimpleInfo(queryIPPort);
        assertEquals(response.contains(ErrMsg), true);
    }


//    /**
//     *
//     * @param queryIPPort  查看节点地址
//     * @param tcpPort 节点tcpport  //checkinfo[0]
//     * @param version 节点版本 //checkinfo[1]
//     * @param Type 节点类型共识or数据节点 //checkinfo[2]
//     * @param launchTime 启动时间 //checkinfo[3]
//     * @param log 日志级别 //checkinfo[4]
//     * @param dbPath db数据库目录 //checkinfo[5]
//     * @param Crypt 加密算法 //checkinfo[6]
//     * @param Hash hash算法 //checkinfo[7]
//     * @param Consensus 共识算法 //checkinfo[8]
//     * @param dockerImage 合约版本 //checkinfo[9]
//     * @param blockPackTime 打包时间 //checkinfo[10]
//     * @param blockSize 区块大小 //checkinfo[11]
//     * @throws Exception
//     */
    public void chkPeerDetailsOK(String queryIPPort,String...checkinfo)throws Exception{

        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        String[] temp = queryIP.split("\\.");
        String tcpIP=queryIP+"/tcp/"+checkinfo[0]; //10.1.3.240:60030
        String peerID=getPeerId(queryIP,USERNAME,PASSWD);  //根据证书生成
        String peerName="peer"+temp[3];//peer240

        String response = mgToolCmd.getPeerDetails(queryIPPort);

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



    public void queryPeerListNo(String queryIPPort,int peerNo) throws Exception{
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
            if(str.contains("shownName"))
                No++;
        }
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        assertEquals(peerNo,No);
        //assertEquals(response.contains("isLeader"), true);

    }

   @Test
   public void chkHealth()throws Exception{
       checkPeerHealth(PEER1IP+":"+rpcPort);
   }

    public void checkPeerHealth(String queryIPPort)throws Exception{
        String response = mgToolCmd.checkPeerHealth(queryIPPort);
        assertEquals(response.contains("DiskUsedPercent"), true);
        assertEquals(response.contains("DiskTotal"), true);
        assertEquals(response.contains("MemUsed"), true);
        assertEquals(response.contains("MemAvailable"), true);
        assertEquals(response.contains("MemTotal"), true);
        assertEquals(response.contains("SingleCPUUsedPercent"), true);
        assertEquals(response.contains("TotalCPUUsedPercent"), true);
//        assertEquals(response.contains("WsRateTotal"), true);
//        assertEquals(response.contains("WsRateFromLast"), true);
//        assertEquals(response.contains("WsRateEverySec10"), true);
        assertEquals(response.contains("Uploadspeed"), true);
        assertEquals(response.contains("Downloadspeed"), true);


        //-p参数中带IP
        response = shExeAndReturn(PEER1IP,toolPath+"./toolkit health -p " + queryIPPort);
        assertEquals(response.contains("too many colons in address"), true);


        //无-p参数
        response = shExeAndReturn(PEER1IP,toolPath+"./toolkit health -p " + queryIPPort);
        assertEquals(response.contains("management"), true);


        //查询不是一个区块链系统中的节点
        response = shExeAndReturn(PEER1IP,toolPath+"./toolkit health -p " + queryIPPort);
        assertEquals(response.contains("connection refused"), true);
    }

    @Test
    public  void testTX()throws Exception{
        int blockHeight=0;
        String rsp="";
        blockHeight=Integer.parseInt(mgToolCmd.queryBlockHeight((peer1IPPort)));

        //确认管理工具获取的高度与sdk获取高度一致，需要注意sdk是否有权限执行获取高度
        rsp = store.GetHeight();
        assertEquals(JSONObject.fromObject(rsp).getString("Data"), mgToolCmd.queryBlockHeight((peer1IPPort)));

        //发送单笔交易
        rsp=mgToolCmd.sendNewTx(peer1IPPort,"","");
        assertEquals(rsp.contains("tjfoc"), true);
        Thread.sleep(6000);
        assertEquals(blockHeight+1, Integer.parseInt(mgToolCmd.queryBlockHeight((peer1IPPort))));

        rsp = mgToolCmd.queryBlockByHeight(peer1IPPort,String.valueOf(blockHeight));
        assertEquals(rsp.contains(String.valueOf(blockHeight)), true);

    }


    //管理工具不再支持未上链交易查询
    //@Test
    public void UnconfirmedTxCheck()throws Exception{
        String rsp=mgToolCmd.queryPeerUnconfirmedTx(peer1IPPort);
        assertEquals(rsp.contains("Count:\t0"),true);
        //log.info("Current Unconfirmed Tx Count:"+rsp.substring(rsp.lastIndexOf("Count:")+1).trim());

        setAndRestartPeerList("cp "+ PeerPATH + "conf/basePkTm20s.toml "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
        setAndRestartSDK(resetSDKConfig);

//        Shell shellPeer1=new Shell(PEER1IP,USERNAME,PASSWD);
//        shellPeer1.execute(killPeerCmd);
//        shellPeer1.execute("sed -i \"s/PackTime = 1000/PackTime = 10000/g\" "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
//
//        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
//        shellPeer2.execute(killPeerCmd);
//        shellPeer2.execute("sed -i \"s/PackTime = 1000/PackTime = 10000/g\" "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
//
//        startPeer(PEER1IP);
//        startPeer(PEER2IP);
//
//        Thread.sleep(STARTSLEEPTIME);

        rsp = mgToolCmd.sendNewTx(peer1IPPort,"3","1");
        assertEquals(rsp.contains("HashData"),true);

        rsp=mgToolCmd.queryPeerUnconfirmedTx(peer1IPPort);
        assertEquals(rsp.contains("Count:\t3"),true);

        //double check
//        rsp=mgToolCmd.queryPeerUnconfirmedTx(peer1IPPort);
//        assertEquals(rsp.contains("Count:\t3"),true);
        for(int i=0;i<txHashList.size();i++){
            log.info(txHashList.get(i));
            assertEquals(rsp.contains(txHashList.get(i)),true);
        }



        //恢复原始配置
        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);


        //需要补充以上交易上链后的交易查询
        for(int i=0;i<txHashList.size();i++){
            log.info(txHashList.get(i));
            String rsp1= store.GetTxDetail(txHashList.get(i));
            assertEquals(JSONObject.fromObject(rsp1).getString("State"),"200");
        }

    }


    @Test
    public void testPeerPerm()throws Exception{
        String rsp="";
        String sdkID1="01de8e335282e28a6bae4081cf8b7ef61ee45d19b0503f3180a343355226b305a21bd6e714c5455becf28f83463a67ee771209b93c75d76c586b168a96666d26";
        String sdkID2="23de9d435282e28a6bae4081cf8b7ef61ee45d19b0503f3180a343355226b305a21bd6e714c5455becf28f83463a67ee771209b93c75d76c586b168a96666d26";
        String sdkID3="45ff9d435282e28a6bae4081cf8b7ef61ee45d19b0503f3180a343355226b305a21bd6e714c5455becf28f83463a67ee771209b93c75d76c586b168a96666d26";
        String sdkID4="sdk56";

        String succRsp="success";
        String errRsp="FuncUpdatePeerPermission err";
        //分别给sdkID1~3 赋值权限，之后再做查询
        rsp = mgToolCmd.setPeerPerm(peer1IPPort,sdkID1,"1,2,3");
        assertEquals(rsp.contains(succRsp), true);

        rsp = mgToolCmd.setPeerPerm(peer1IPPort,sdkID2,"0");
        assertEquals(rsp.contains(succRsp), true);

        rsp = mgToolCmd.setPeerPerm(peer1IPPort,sdkID3,"999");
        assertEquals(rsp.contains(succRsp), true);

        rsp = mgToolCmd.setPeerPerm(peer1IPPort,sdkID4,"1,2,3");
        assertEquals(rsp.contains(errRsp), true);

        Thread.sleep(3000);
        rsp=mgToolCmd.getPeerPerm(peer1IPPort,"");
        assertEquals(rsp.contains(sdkID1), true);
        assertEquals(rsp.contains("peermission:[1 2 3]"), true);
        assertEquals(rsp.contains(sdkID2), true);
        assertEquals(rsp.contains("peermission:[0]"), true);
        assertEquals(rsp.contains(sdkID3), true);
        assertEquals(rsp.contains("peermission:[1 2 3 4 5 6 7 8 9 10 21 22 23 24 25 211 212 221 222 223 224 231 232 233 235 236 251 252 253 254 255 256]"), true);
        assertEquals(rsp.contains(sdkID4), false);

        rsp=mgToolCmd.getPeerPerm(peer1IPPort,sdkID1);
        assertEquals(rsp.contains("peermission:[1 2 3]"), true);

    }

    @Test
    public void testGetIDInterface()throws Exception{

        assertEquals(mgToolCmd.getID(PEER1IP,"tls/key.pem","sm2").contains("id"),true);
        assertEquals(mgToolCmd.getID(PEER1IP,"tls/key.pem","").contains("id"),true);
        assertEquals(mgToolCmd.getID(PEER1IP,"tls/key.pem","ecc").contains("failed to parse EC private key embedded in PKCS#8: x509"),true);
        assertEquals(mgToolCmd.getID(PEER1IP,"tls/key.pem","eee").contains("unsupport sign algorithm"),true);


        assertEquals(mgToolCmd.getID(PEER1IP,"ecdsa/key.pem","ecc").contains("id"),true);
        assertEquals(mgToolCmd.getID(PEER1IP,"ecdsa/key.pem","").contains("id"),true);
        assertEquals(mgToolCmd.getID(PEER1IP,"ecdsa/key.pem","sm2").contains("id"),true);
        assertEquals(mgToolCmd.getID(PEER1IP,"ecdsa/key.pem","eee").contains("unsupport sign algorithm"),true);

        assertEquals(mgToolCmd.getID(PEER1IP,"","sm2").contains("management"),true);
        assertEquals(mgToolCmd.getID(PEER1IP,"key11.pem","ecc").contains(" no such file or directory"),true);
    }



}
