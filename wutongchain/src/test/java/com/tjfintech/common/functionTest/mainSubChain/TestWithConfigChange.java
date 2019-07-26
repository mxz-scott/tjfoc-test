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

    public static String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    public static String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    public static String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    public static String ids = " -m "+ id1+","+ id2+","+ id3;

    boolean bExe=false;

    //String glbChain01= "glbCh1_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
    //String glbChain02= "glbCh2_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
    public static String glbChain01= "glbCh1";
    public static String glbChain02= "glbCh2";

    @Before
    public void beforeConfig() throws Exception {
        TestMainSubChain testMainSubChain=new TestMainSubChain();

//        if(certPath!=""&& bReg==false) {
//            BeforeCondition bf = new BeforeCondition();
//            bf.updatePubPriKey();
//            bf.collAddressTest();
//
//            bReg=true;
//        }

        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01+"\"")) {
            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01,
                    " -t sm3", " -w first", " -c raft", ids);
            Thread.sleep(SLEEPTIME*2);
            assertEquals(testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02,
                    " -t sm3", " -w first", " -c raft", ids);
            Thread.sleep(SLEEPTIME*2);
            assertEquals(testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
        }
    }

    //此用例需要调整
    @Test
    public void TC1538_quitMainJoinPeer()throws Exception{
        setAndRestartSDK("cp "+PTPATH+"sdk/conf/configOnePeer240.toml "+PTPATH+"sdk/conf/config.toml");
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


        Thread.sleep(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);


        String Data = "tc1538 tx1 test";
        //动态删除节点B，向主链和子链01/02/03发交易 子链1、2的交易可以成功上链，3无法上链（恢复后可以）
        testMgTool.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER2IP);

        Thread.sleep(SLEEPTIME*2);
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

        Thread.sleep(SLEEPTIME*2);
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
        Thread.sleep(SLEEPTIME*2);
        subLedger=chainName1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功
        subLedger=chainName2;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认可以c查询成功
        subLedger=chainName3;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));  //确认可以c查询成功
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash4)).getString("State"));  //确认可以c查询成功

        //setAndRestartSDK("cp "+PTPATH+"sdk/conf/configOK.toml "+PTPATH+"sdk/conf/config.toml");
    }

    @Test
    public void TC1537_createChainWithJoinPeer()throws Exception{
        setAndRestartPeer(PEER3IP,"cp "+PTPATH+"peer/configjoin.toml "+PTPATH+"peer/"+PeerMemConfig+".toml");
        //动态加入节点168
        testMgTool.addPeer("join",PEER1IP+":"+PEER1RPCPort,
                "/ip4/"+PEER3IP,"/tcp/60011",PEER3RPCPort,"success");
        Thread.sleep(SLEEPTIME);

        //创建子链01 包含节点A、B、C
        String chainName1="tc1537_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1,
                " -t sm3"," -w first",
                " -c raft",ids+","+getPeerId(PEER3IP,USERNAME,PASSWD));
        assertEquals(res.contains("send transaction success"), true);



        Thread.sleep(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);


        String Data = "tc1537 tx1 test";

        Thread.sleep(SLEEPTIME/2);
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

        Thread.sleep(SLEEPTIME*2);
        subLedger=chainName1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash4)).getString("State"));  //确认不可以c查询成功

        //销毁子链 以便恢复集群（退出动态加入的节点）
        testMainSubChain.destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        Thread.sleep(SLEEPTIME*3/2);
        resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        assertEquals(resp.contains("Destory"), true);

        //恢复节点
        testMgTool.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER3IP);
        //停止节点id3
        Shell shell=new Shell(PEER3IP,USERNAME,PASSWD);
        shell.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));

    }

    @Test  //数据节点异常
    public void TC1659_1655_createChainWithDataPeer()throws Exception{
        setAndRestartPeer(PEER3IP,"cp "+PTPATH+"peer/configobs.toml "+PTPATH+"peer/"+PeerMemConfig+".toml");
        //动态加入节点168
        testMgTool.addPeer("observer",PEER1IP+":"+PEER1RPCPort,
                "/ip4/"+PEER3IP,"/tcp/60011",PEER3RPCPort,"success");
        Thread.sleep(SLEEPTIME);
        //创建子链01 包含节点A、B、C
        String chainName1="tc1659_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1,
                " -t sm3"," -w first",
                " -c raft",ids+","+getPeerId(PEER3IP,USERNAME,PASSWD));
        assertEquals(res.contains("is not Consensus Node"), true);

        Thread.sleep(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), false);


        //恢复节点
        testMgTool.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER3IP);
        //停止节点id3
        Shell shell=new Shell(PEER3IP,USERNAME,PASSWD);
        shell.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));

        testMainSubChain.sendTxToMainActiveChain("1659 data");

    }


    @Test
    public void TC1523_subChainStatus()throws Exception{
//        setAndRestartSDK("cp "+PTPATH+"sdk/conf/configOnePeer240.toml "+PTPATH+"sdk/conf/config.toml");
        //创建子链，包含两个节点
        String chainName="tc1523_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft"," -m "+id1+","+id2);
        assertEquals(res.contains("send transaction success"), true);

        Thread.sleep(SLEEPTIME*3);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        subLedger=chainName;
        String response1 = store.CreateStore("tc1523 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));  //确认可以发送成功

        Thread.sleep(SLEEPTIME*2);
        String txHash1 =JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功

        //停止节点id2
        Shell shell=new Shell(PEER2IP,USERNAME,PASSWD);
        shell.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
//        return StringUtils.join(stdout,"\n");

        Thread.sleep(SLEEPTIME);
        String response2 = store.CreateStore("tc1523 data2");
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));  //确认可以发送成功
        Thread.sleep(SLEEPTIME);
        String txHash2 =JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认不可以c查询成功

        shell.execute("sh "+PTPATH+"peer/start.sh");
    }

    @Test
    public void TC1608_1620_restartPeer()throws Exception{
        setAndRestartPeerList();
        testMainSubChain.sendTxToMainActiveChain("tc1608 data");
    }

    @Test
    public void TC1649_1650_HashChange()throws Exception{
        //创建子链，包含三个节点 hashtype 使用sha256 主链使用sm3
        String chainName="tc1649_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sha256"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        Thread.sleep(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //设置主链sm3 sdk使用sha256 （子链sha256）
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/base.toml");
        setAndRestartSDK("cp "+PTPATH+"sdk/conf/configSHA256.toml "+PTPATH+"sdk/conf/config.toml");

        Thread.sleep(SLEEPTIME);
        //检查子链可以成功发送，主链无法成功发送
        subLedger="";
        String response2 = store.CreateStore("tc1649 data");
        assertThat(response2,containsString("hash error want"));

        subLedger=chainName;
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));  //确认可以发送成功

        Thread.sleep(SLEEPTIME*2);
        String txHash1 =JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功


        //设置主链sm3 sdk使用sm3 （子链sha256）
        //setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/base.toml");
        setAndRestartSDK("cp "+PTPATH+"sdk/conf/configOK.toml "+PTPATH+"sdk/conf/config.toml");

        //检查主链可以成功发送，子链无法成功发送
        subLedger="";
        String response3 = store.CreateStore("tc1650 data");
        assertEquals("200",JSONObject.fromObject(response3).getString("State"));  //确认可以发送成功

        Thread.sleep(SLEEPTIME*2);
        String txHash2 =JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认可以c查询成功

        //SDK兼容子链所有类型hashtype
        subLedger=chainName;
        String response4 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response4).getString("State"));  //确认可以发送成功


        testMainSubChain.sendTxToMainActiveChain("tc1649 data");

    }

    @Test
    public void TC1651_1652_HashChange()throws Exception{
        //创建子链，包含三个节点 hashtype 子链sm3 主链使用sha256
        String chainName="tc1651_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        Thread.sleep(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //设置主链sha256 sdk使用sha256 （子链sm3）
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseSHA256.toml "+ PTPATH +"peer/conf/base.toml");
        setAndRestartSDK("cp " + PTPATH + "sdk/conf/configSHA256.toml "+PTPATH+"sdk/conf/config.toml");

        Thread.sleep(SLEEPTIME*2);
        //检查主链可以成功发送，子链无法成功发送
        subLedger=chainName;
        String response2 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));  //确认可以发送成功

        subLedger="";
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));  //确认可以发送成功

        Thread.sleep(SLEEPTIME);
        String txHash1 =JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功


        //设置主链sm3 sdk使用sha256 （子链sha256）
        //setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseSHA256.toml "+ PTPATH +"peer/conf/base.toml");
        setAndRestartSDK("cp "+PTPATH+"sdk/conf/configOK.toml "+PTPATH+"sdk/conf/config.toml");

        Thread.sleep(SLEEPTIME);
        //检查子链可以成功发送，主链无法成功发送
        subLedger=chainName;
        String response3 = store.CreateStore("tc1650 data");
        assertEquals("200",JSONObject.fromObject(response3).getString("State"));  //确认可以发送成功

        Thread.sleep(SLEEPTIME*2);
        String txHash2 =JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认可以c查询成功

        subLedger="";
        String response4 = store.CreateStore("tc1651 data");
        assertThat(response4,containsString("hash error want"));

    }



    //@After
    public void resetPeerAndSDK()throws  Exception {
        setAndRestartPeerList("cp " + PTPATH + "peer/conf/baseOK.toml " + PTPATH + "peer/conf/base.toml");
        setAndRestartSDK("cp " + PTPATH + "sdk/conf/configOK.toml " + PTPATH + "sdk/conf/config.toml");
    }
}
