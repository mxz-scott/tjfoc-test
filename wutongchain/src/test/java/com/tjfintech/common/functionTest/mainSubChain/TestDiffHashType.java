package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetHashTypeSHA256;
import com.tjfintech.common.functionTest.Conditions.SetHashTypeSM3;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.TestMgTool;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestDiffHashType {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    TestMgTool testMgTool=new TestMgTool();
    BeforeCondition beforeCondition = new BeforeCondition();

    Date dt=new Date();
    SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
    TestMainSubChain testMainSubChain=new TestMainSubChain();

    //String glbChain01= "glbCh1_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
    //String glbChain02= "glbCh2_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
//    public static String glbChain01= "glbCh1";
//    public static String glbChain02= "glbCh2";

    //修改节点hash后重启如果之前的hashtype存在交易则节点无法启动崩溃
    //@Before
//    public void beforeConfig() throws Exception {
//        TestMainSubChain testMainSubChain=new TestMainSubChain();
//
//        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
//        assertEquals(false,resp.contains("errot"));
//        if(! resp.contains("\"name\": \""+glbChain01+"\"")) {
//            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01,
//                    " -t sm3", " -w first", " -c raft", ids);
//            sleepAndSaveInfo(SLEEPTIME*2);
//            assertEquals(testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
//        }
//
//        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
//            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02,
//                    " -t sm3", " -w first", " -c raft", ids);
//            sleepAndSaveInfo(SLEEPTIME*2);
//            assertEquals(testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
//        }
//    }



    @Test
    public void TC1649_1650_HashChange()throws Exception{
        //设置主链sm3 sdk使用sm3 （子链sha256） 设置前清空节点及sdk数据
        SetHashTypeSM3 setSM3part= new SetHashTypeSM3();
        setSM3part.setHashsm3();
        subLedger = "";
        beforeCondition.setPermission999();
        //创建子链，包含三个节点 hashtype 使用sha256 主链使用sm3
        String chainName="tc1649_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sha256"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
//        assertEquals(resp.contains(glbChain01), true);

        //设置主链sm3 sdk使用sha256 （子链sha256）
        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK("cp "+ PTPATH + "sdk/conf/configSHA256.toml "+ PTPATH + "sdk/conf/config.toml");

        sleepAndSaveInfo(SLEEPTIME);
        //检查子链可以成功发送，主链无法成功发送
        subLedger="";
        String response2 = store.CreateStore("tc1649 data");
        assertThat(response2,containsString("hash error want"));

        subLedger=chainName;
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash1 =JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功


        //设置主链sm3 sdk使用sm3 （子链sha256）
        //setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);

        //检查主链可以成功发送，子链无法成功发送
        subLedger="";
        String response3 = store.CreateStore("tc1650 data");
        assertEquals("200",JSONObject.fromObject(response3).getString("State"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash2 =JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认可以c查询成功

        //SDK兼容子链所有类型hashtype
        subLedger=chainName;
        String response4 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response4).getString("State"));  //确认可以发送成功

    }

    @Test
    public void TC1651_1652_HashChange()throws Exception{
        //设置主链sha256 sdk使用sha256 （子链sm3） 设置前清空节点及sdk数据
        SetHashTypeSHA256 setSHA256 = new SetHashTypeSHA256();
        setSHA256.setHashSHA256();
        subLedger = "";
        beforeCondition.setPermission999();
        sleepAndSaveInfo(SLEEPTIME);

        //创建子链，包含三个节点 hashtype 子链sm3 主链使用sha256
        String chainName="tc1651_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
//        assertEquals(resp.contains(glbChain01), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"");
        subLedger=chainName;
        String response2 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));  //确认可以发送成功

        subLedger="";
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME);
        String txHash1 =JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功


        //设置主链sm3 sdk使用sha256 （子链sha256）
        //setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseSHA256.toml "+ PTPATH +"peer/conf/base.toml");
        setAndRestartSDK(resetSDKConfig);

        sleepAndSaveInfo(SLEEPTIME);
        //检查子链可以成功发送，主链无法成功发送
        subLedger=chainName;
        String response3 = store.CreateStore("tc1650 data");
        assertEquals("200",JSONObject.fromObject(response3).getString("State"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash2 =JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认可以c查询成功

        subLedger="";
        String response4 = store.CreateStore("tc1651 data");
        assertThat(response4,containsString("hash error want"));

    }

    @AfterClass
    public static void resetPeerAndSDK()throws  Exception {
        subLedger="";
        //设置节点、sdk、管理工具hashtype为SM3
        SetHashTypeSM3 setSM3 = new SetHashTypeSM3();
        setSM3.setHashsm3();

        BeforeCondition beforeCond = new BeforeCondition();
        beforeCond.setPermission999();
    }
}
