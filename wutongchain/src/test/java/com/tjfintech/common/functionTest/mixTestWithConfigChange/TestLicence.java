package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class TestLicence {
    public static final int STARTSLEEPTIME=40000;
    TestBuilder testBuilder = TestBuilder.getInstance();
    MgToolCmd mgTool = new MgToolCmd();
    TestMgTool testMgTool = new TestMgTool();

    String rpcPort = PEER3RPCPort;
    String tcpPort = PEER3TCPPort;
    int basePeerNo = 3;
    String ipType="/ip4/";
    String tcpType="/tcp/";


    String peer1IPPort=PEER1IP+":"+PEER1RPCPort;
    String peer2IPPort=PEER2IP+":"+PEER2RPCPort;

    long timeStamp = 0;


    @Before
    public void resetPeerEnv()throws Exception{
        BeforeCondition bf =new BeforeCondition();
        bf.setPermission999();
        PEER1MAC=getMACAddr(PEER1IP,USERNAME,PASSWD).trim();
        PEER2MAC=getMACAddr(PEER2IP,USERNAME,PASSWD).trim();
        PEER3MAC=getMACAddr(PEER3IP,USERNAME,PASSWD).trim();
        PEER4MAC=getMACAddr(PEER4IP,USERNAME,PASSWD).trim();

        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);

        mgTool.quitPeer(peer1IPPort,PEER3IP);

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute(killPeerCmd);
        shellPeer3.execute(resetPeerBase);
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
    }

    @Test
    public void testLicGenAndDec() throws Exception{
        String rsp="";
        String dayTime="36500";
        String PeerNo="6";
        //生成证书
        rsp = mgTool.genLicence(PEER1IP,PEER1MAC,PEER1IP,dayTime,PeerNo,version.substring(0,3));
        Date date = new Date();
        timeStamp = date.getTime(); //记录证书生成时间时间戳

        log.info(PEER1MAC);
        log.info(rsp);
        assertEquals(rsp.contains(PEER1MAC),true);
        assertEquals(rsp.contains(PEER1IP),true);
        assertEquals(rsp.contains("DayTime:"+dayTime),true);
        assertEquals(rsp.contains("PeerNum:"+PeerNo),true);
        assertEquals(rsp.contains("version:"+version.substring(0,3)),true);

        //解析证书 确认参数一致
        rsp = mgTool.deLicence(PEER1IP,"peer.lic");
        assertEquals(rsp.contains("PeerNum:" + PeerNo),true);
        assertEquals(rsp.contains("PeerVersion:" + version.substring(0,3)),true);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String outTime = df.format(new Date(timeStamp + Long.parseLong(dayTime)*24*60*60*1000));
        log.info("OutTime:" + outTime);
        assertEquals(rsp.contains(outTime.split(" ")[0]),true); //确认有效期时间正确，因虚拟机与测试机可能不是同一台可能存在时间差，因此目前仅校验年月日是否正确

        Shell shellPeer1 = new Shell(PEER1IP,USERNAME,PASSWD);
        shellPeer1.execute("rm -f "+ ToolPATH + "peer.lic");
        assertEquals(mgTool.deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);


        //生成使用无效的参数验证:无效的mac地址、无效IP地址、无效时间、无效节点数
        ExeToolCmdAndChk(PEER1IP,"./license create -m 12:11 -p 10.1.3.240 -d 100 -n 6 -v 2.0","invalid MAC address");
        assertEquals(mgTool.deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);

        ExeToolCmdAndChk(PEER1IP,"./license create -m 02:42:fc:a2:5b:1b -p 10.1 -d 100 -n 6 -v 2.0","invalid IP address");
        assertEquals(mgTool.deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);

        ExeToolCmdAndChk(PEER1IP,"./license create -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 0.5 -n 6 -v 2.0","invalid argument");
        assertEquals(mgTool.deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);

        ExeToolCmdAndChk(PEER1IP,"./license create -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 5 -n 0.5 -v 2.0","invalid argument");
        assertEquals(mgTool.deLicence(PEER1IP,"peer.lic").contains("open peer.lic: no such file or directory"),true);

        ExeToolCmdAndChk(PEER1IP,"./license create -m 02:42:fc:a2:5b:1b -p 10.1.3.240 -d 5 -n 0 -v 2.0","success");

        rsp = mgTool.deLicence(PEER1IP,"peer.lic");
        assertEquals(rsp.contains("PeerNum:0"),true);

        //解析证书使用无效参数
        ExeToolCmdAndChk(PEER1IP,"./license decode -p ./crypt/key.pem","data Illegal");

    }

    @Test
    public void testLicValidForPeer()throws Exception{

        //验证已过期证书，此证书需要提前准备 246已有过期证书peer246d1n2.lic
        log.info("********************Test for licence timeout********************");
        //ToolIP= PEER2IP;
        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
        shellPeer2.execute(killPeerCmd);
        //替换配置licence文件为过期文件
        shellPeer2.execute("cp "+ PeerPATH + "conf/based1.toml "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
        shellPeer2.execute(startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        assertEquals(true,mgTool.checkPeerHealth(PEER2IP+":"+rpcPort).contains("connection error"));

        log.info("********************Test for dismatch license********************");
        //ToolIP= PEER2IP;
        shellPeer2.execute(killPeerCmd);
        mgTool.genLicence(PEER2IP,PEER2MAC,PEER2IP,"200","3","test");
        shellPeer2.execute("cp "+ ToolPATH + "peer.lic "+ PeerPATH + "peerDisMatch.lic");
        //替换配置licence文件为过期文件
        shellPeer2.execute("cp "+ PeerPATH + "conf/baseDisMatch.toml "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
        shellPeer2.execute(startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        assertEquals(true,mgTool.checkPeerHealth(PEER2IP+":"+rpcPort).contains("connection error"));

        log.info("********************Test for invalid peer count********************");
        //验证节点数据小于配置文件中节点数场景
        shellPeer2.execute(killPeerCmd);
        mgTool.genLicence(PEER2IP,PEER2MAC,PEER2IP,"200","1",version.substring(0,3));
        shellPeer2.execute("cp "+ ToolPATH + "peer.lic "+ PeerPATH + "peer246n1.lic");
        //替换配置licence文件为n=1文件
        shellPeer2.execute("cp "+ PeerPATH + "conf/basen1.toml "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
        shellPeer2.execute(startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        assertEquals(true,mgTool.checkPeerHealth(PEER2IP+":"+rpcPort).contains("connection error"));

        log.info("********************Test for invalid MAC addr ********************");
        //证书IP正确，MAC不正确 替换配置licence文件为Mac1文件
        shellPeer2.execute("cp "+ PeerPATH + "conf/baseMac1.toml "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
        shellPeer2.execute(killPeerCmd);
        mgTool.genLicence(PEER2IP,PEER1MAC,PEER2IP,"200","3",version.substring(0,3));
        shellPeer2.execute("cp "+ ToolPATH + "peer.lic "+ PeerPATH + "peerMac1.lic");
        shellPeer2.execute(startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        assertEquals(true,mgTool.checkPeerHealth(PEER2IP+":"+rpcPort).contains("connection error"));

        log.info("********************Test for invalid IP addr ********************");
        //证书MAC正确，IP不正确 替换配置licence文件为IP1文件
        shellPeer2.execute("cp "+ PeerPATH + "conf/baseIP1.toml "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
        shellPeer2.execute(killPeerCmd);
        mgTool.genLicence(PEER2IP,PEER2MAC,PEER1IP,"200","3",version.substring(0,3));
        shellPeer2.execute("cp "+ ToolPATH + "peer.lic "+ PeerPATH + "peerIP1.lic");
        shellPeer2.execute(startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        assertEquals(true,mgTool.checkPeerHealth(PEER2IP+":"+rpcPort).contains("connection error"));

        log.info("********************Test for invalid IP&MAC addr ********************");
        //使用其他节点licence
        shellPeer2.execute("cp "+ PeerPATH + "conf/baseIPMac1.toml "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
        shellPeer2.execute(killPeerCmd);
        mgTool.genLicence(PEER1IP,PEER1MAC,PEER1IP,"200","3",version.substring(0,3));
        shellPeer2.execute("cp "+ ToolPATH + "peer.lic "+ PeerPATH + "peerIPMac1.lic");
        shellPeer2.execute(startPeerCmd);
         Thread.sleep(STARTSLEEPTIME);
        assertEquals(true,mgTool.checkPeerHealth(PEER3IP+":"+rpcPort).contains("connection error"));

        log.info("********************Test for old license(no version check) ********************");
        //使用旧版本工具生成的不带version检查的licence 需要提前准备好旧版本的license
        shellPeer2.execute("cp "+ PeerPATH + "conf/baseNoVer.toml "+ PeerPATH + "conf/"+PeerInfoConfig+".toml");
        shellPeer2.execute(killPeerCmd);
        shellPeer2.execute(startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        assertEquals(true,mgTool.checkPeerHealth(PEER3IP+":"+rpcPort).contains("connection error"));

        log.info("********************Test for valid licence********************");
        //恢复配置并重启，使用有效证书验证
        shellPeer2.execute(resetPeerBase);
        shellPeer2.execute(killPeerCmd);
        shellPeer2.execute(startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        //ToolIP= PEER1IP;
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
    }

//    @Test
//    public void testLicence()throws Exception{
//        testLicGenAndDec();
//        testLicValidForPeer();
//        testLicForAddPeer();
//    }
    //此用例需要保证各个节点上都存在管理工具
    //目前规划目录：10.1.3.240/246/247  "+ ToolPATH

    @Test
    public void testLicForAddPeer()throws Exception{

        //重新生成节点个数为3的240证书并拷贝至节点目录
        mgTool.genLicence(PEER1IP,PEER1MAC,PEER1IP,"20",String.valueOf(basePeerNo),version.substring(0,3));

        //重新生成节点个数为3的246证书并拷贝至节点目录
        mgTool.genLicence(PEER2IP,PEER2MAC,PEER2IP,"20",String.valueOf(basePeerNo),version.substring(0,3));

        //重新生成节点个数为3的168证书并拷贝至节点目录
        mgTool.genLicence(PEER4IP,PEER4MAC,PEER4IP,"20",String.valueOf(basePeerNo),version.substring(0,3));

        //确认系统中无247节点
        mgTool.quitPeer(peer1IPPort,PEER3IP);
        //停止节点，修改配置文件使用节点licence名称为peerTest.lic
        String peerTestlicConf = "sed -i \"s/peer.lic/peerTest.lic/g\" " + PeerPATH + "conf/" + PeerInfoConfig + ".toml";
        String cpLic="cp " + ToolPATH + "peer.lic " + PeerPATH + "peerTest.lic";

        setAndRestartPeerList(resetPeerBase,peerTestlicConf,cpLic);
        setAndRestartSDK(resetSDKConfig);


        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute(killPeerCmd);
        shellPeer3.execute(resetPeerBase);
        shellPeer3.execute("sed -i \"s/peer.lic/peerTest.lic/g\" " + PeerPATH + "conf/" + PeerInfoConfig + ".toml");

//        //重新生成节点个数为2的240证书并拷贝至节点目录
//        mgTool.genLicence(PEER1IP,PEER1MAC,PEER1IP,"20",String.valueOf(basePeerNo),version.substring(0,3));
//        shellPeer1.execute("cp "+ ToolPATH + "peer.lic "+ PeerPATH + "peerTest.lic");
//
//        //重新生成节点个数为2的246证书并拷贝至节点目录
//        mgTool.genLicence(PEER2IP,PEER2MAC,PEER2IP,"20",String.valueOf(basePeerNo),version.substring(0,3));
//        shellPeer2.execute("cp "+ ToolPATH + "peer.lic "+ PeerPATH + "peerTest.lic");

        //重新生成节点个数为4的247证书并拷贝至节点目录
        mgTool.genLicence(PEER3IP,PEER3MAC,PEER3IP,"20","4",version.substring(0,3));
        shellPeer3.execute("cp " + ToolPATH + "peer.lic " + PeerPATH + "peerTest.lic");

        //启动节点240/246
//        startPeer(PEER1IP);
//        startPeer(PEER2IP);

//        Thread.sleep(STARTSLEEPTIME);
        //检查当前节点列表个数basePeerNo,成功则证明节点启动无异常
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);

        //动态加入节点247
        String resp = mgTool.addPeer("join",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("update failed"));
        shellExeCmd(PEER3IP,startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo); //检查节点247已经启动成功

        mgTool.quitPeer(peer1IPPort,PEER3IP);
        Thread.sleep(2000);
        shellPeer3.execute(killPeerCmd);

        //重新生成节点个数为3的247证书并拷贝至节点目录
        //ToolIP=PEER3IP;
        mgTool.genLicence(PEER3IP,PEER3MAC,PEER3IP,"20","3",version.substring(0,3));
        shellPeer3.execute("cp "+ ToolPATH + "peer.lic "+ PeerPATH + "peerTest.lic");

        //动态加入节点247
        String resp2 = mgTool.addPeer("join",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("update failed"));
        shellExeCmd(PEER3IP,startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        assertEquals(true,mgTool.checkPeerHealth(PEER3IP+":"+rpcPort).contains("connection error"));

        mgTool.quitPeer(peer1IPPort,PEER3IP);
        Thread.sleep(2000);

        //恢复原始配置重新启动节点
        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
    }



}