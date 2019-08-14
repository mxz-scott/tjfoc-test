package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static  com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain.*;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.TestMgTool;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestWithConfigChange {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    TestMgTool testMgTool=new TestMgTool();

    Date dt=new Date();
    SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
    TestMainSubChain testMainSubChain=new TestMainSubChain();


    //String glbChain01= "glbCh1_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
    //String glbChain02= "glbCh2_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
    public static String glbChain01= "glbCh1";
    public static String glbChain02= "glbCh2";

    @Before
    public void beforeConfig() throws Exception {
        TestMainSubChain testMainSubChain=new TestMainSubChain();

        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01+"\"")) {
            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01,
                    " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02,
                    " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
        }
    }

    //此用例需要调整
    //@Test
    public void TC1538_quitMainJoinPeer()throws Exception{
        setAndRestartSDK("cp "+ SDKPATH + "conf/configOnePeer240.toml "+ SDKPATH + "conf/config.toml");
        //创建子链01 包含节点A、B、C
        String chainName1="tc1538_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        //创建子链02 包含节点A、C
        String chainName2="tc1538_02";
        String res2 = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2,
                " -t sm3"," -w first"," -c raft"," -m "+id1+","+id3);
        assertEquals(res2.contains("send transaction success"), true);


        //创建子链03 包含节点A、B
        String chainName3="tc1538_03";
        String res3 = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3,
                " -t sm3"," -w first"," -c raft"," -m "+id1+","+id2);
        assertEquals(res3.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);


        String Data = "tc1538 tx1 test";
        //动态删除节点B，向主链和子链01/02/03发交易 子链1、2的交易可以成功上链，3无法上链（恢复后可以）
        testMgTool.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER2IP);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);


        resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        assertEquals(resp.contains(PEER2IP), false);
        resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3);
        assertEquals(resp.contains(PEER2IP), false);

        subLedger=chainName1;
        String response1=store.CreateStore(Data);
        String txHash1= JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");

        subLedger=chainName2;
        Data = "tc1538 tx2 test";
        String response2=store.CreateStore(Data);
        String txHash2= JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");

        subLedger=chainName3;
        Data = "tc1538 tx3 test";
        String response3=store.CreateStore(Data);
        String txHash3= JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");


        subLedger="";
        Data = "tc1538 tx4 test";
        String response4=store.CreateStore(Data);
        String txHash4= JSONObject.fromObject(response4).getJSONObject("Data").getString("Figure");

        sleepAndSaveInfo(SLEEPTIME*2);
        subLedger=chainName1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功
        subLedger=chainName2;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认不可以c查询成功
        subLedger=chainName3;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));  //确认不可以c查询成功
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash4)).getString("State"));  //确认不可以c查询成功


        //恢复节点
        testMgTool.addPeer("join",PEER1IP+":"+PEER1RPCPort,
                "/ip4/"+PEER2IP,"/tcp/60011",PEER2RPCPort,"success");
        sleepAndSaveInfo(SLEEPTIME*2);
        subLedger=chainName1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功
        subLedger=chainName2;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认可以c查询成功
        subLedger=chainName3;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));  //确认可以c查询成功
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash4)).getString("State"));  //确认可以c查询成功

        //setAndRestartSDK(resetSDKConfig);
    }

    @Test
    public void TC1537_createChainWithJoinPeer()throws Exception{
        setAndRestartPeer(PEER3IP,"cp "+ PeerPATH + "configjoin.toml " + PeerPATH + PeerMemConfig + ".toml");
        //动态加入节点168
        testMgTool.addPeer("join",PEER1IP+":"+PEER1RPCPort,
                "/ip4/"+PEER3IP,"/tcp/60011",PEER3RPCPort,"success");
        sleepAndSaveInfo(SLEEPTIME);

        //创建子链01 包含节点A、B、C
        String chainName1="tc1537_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1,
                " -t sm3"," -w first",
                " -c raft",ids+","+getPeerId(PEER3IP,USERNAME,PASSWD));
        assertEquals(res.contains("send transaction success"), true);



        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);


        String Data = "tc1537 tx1 test";

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        subLedger=chainName1;
        String response1=store.CreateStore(Data);
        String txHash1= JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");


        subLedger="";
        Data = "tc1538 tx4 test";
        String response4=store.CreateStore(Data);
        String txHash4= JSONObject.fromObject(response4).getJSONObject("Data").getString("Figure");

        sleepAndSaveInfo(SLEEPTIME*2);
        subLedger=chainName1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash4)).getString("State"));  //确认不可以c查询成功

        //销毁子链 以便恢复集群（退出动态加入的节点）
        testMainSubChain.destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        sleepAndSaveInfo(SLEEPTIME*3/2);
        resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        assertEquals(resp.contains("Destory"), true);

        //恢复节点
        testMgTool.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER3IP);
        //停止节点id3
        Shell shell=new Shell(PEER3IP,USERNAME,PASSWD);
        shell.execute(killPeerCmd);
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));

    }

    @Test  //数据节点异常
    public void TC1659_1655_createChainWithDataPeer()throws Exception{
        setAndRestartPeer(PEER3IP,"cp "+ PeerPATH + "configobs.toml "+ PeerPATH + PeerMemConfig + ".toml");
        //动态加入节点168
        testMgTool.addPeer("observer",PEER1IP+":"+PEER1RPCPort,
                "/ip4/"+PEER3IP,"/tcp/60011",PEER3RPCPort,"success");
        sleepAndSaveInfo(SLEEPTIME);
        //创建子链01 包含节点A、B、C
        String chainName1="tc1659_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1,
                " -t sm3"," -w first",
                " -c raft",ids+","+getPeerId(PEER3IP,USERNAME,PASSWD));
        assertEquals(res.contains("is not Consensus Node"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), false);


        //恢复节点
        testMgTool.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER3IP);
        //停止节点id3
        Shell shell=new Shell(PEER3IP,USERNAME,PASSWD);
        shell.execute(killPeerCmd);
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));

        testMainSubChain.sendTxToMainActiveChain("1659 data");

    }


    @Test
    public void TC1523_subChainStatus()throws Exception{
//        setAndRestartSDK("cp "+ SDKPATH + "conf/configOnePeer240.toml "+ SDKPATH + "conf/config.toml");
        //创建子链，包含两个节点
        String chainName="tc1523_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft"," -m "+id1+","+id2);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*3);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        subLedger=chainName;
        String response1 = store.CreateStore("tc1523 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash1 =JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功

        //停止节点id2
        Shell shell=new Shell(PEER2IP,USERNAME,PASSWD);
        shell.execute(killPeerCmd);
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
//        return StringUtils.join(stdout,"\n");

        sleepAndSaveInfo(SLEEPTIME);
        String response2 = store.CreateStore("tc1523 data2");
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));  //确认可以发送成功
        sleepAndSaveInfo(SLEEPTIME);
        String txHash2 =JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认不可以c查询成功

        shell.execute(startPeerCmd);
    }

    @Test
    public void TC1608_1620_restartPeer()throws Exception{
        setAndRestartPeerList();
        testMainSubChain.sendTxToMainActiveChain("tc1608 data");
    }


    //@After
    public void resetPeerAndSDK()throws  Exception {
        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);
    }
}
