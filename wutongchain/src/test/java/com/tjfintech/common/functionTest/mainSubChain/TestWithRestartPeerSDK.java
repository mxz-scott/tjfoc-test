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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestWithRestartPeerSDK {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
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
            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -n " + glbChain01,
                    " -t sm3", " -w first", " -c raft", ids);
            Thread.sleep(SLEEPTIME*2);
            assertEquals(testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -n " + glbChain02,
                    " -t sm3", " -w first", " -c raft", ids);
            Thread.sleep(SLEEPTIME*2);
            assertEquals(testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
        }
    }

    @Test
    public void TC1608_1620_restartPeer()throws Exception{
        setAndRestartPeerList();
        testMainSubChain.sendTxToMainActiveChain("tc1608 data");
    }

    @Test
    public void TC1649_1650_restartPeer()throws Exception{
        //创建子链，包含三个节点 hashtype 使用sha256 主链使用sm3
        String chainName="tc1649_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -n "+chainName,
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

        subLedger=chainName;
        String response4 = store.CreateStore("tc1649 data");
        assertThat(response4,containsString("hash error want"));


        testMainSubChain.sendTxToMainActiveChain("tc1649 data");

    }

    @Test
    public void TC1651_1652_restartPeer()throws Exception{
        //创建子链，包含三个节点 hashtype 子链sm3 主链使用sha256
        String chainName="tc1651_01";
        String res = testMainSubChain.createSubChain(PEER1IP,PEER1RPCPort," -n "+chainName," -t sm3"," -w first"," -c raft",ids);
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

        //检查主链可以成功发送，子链无法成功发送
        subLedger=chainName;
        String response2 = store.CreateStore("tc1649 data");
        assertThat(response2,containsString("hash error want"));

        subLedger="";
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));  //确认可以发送成功

        Thread.sleep(SLEEPTIME);
        String txHash1 =JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));  //确认可以c查询成功


        //设置主链sm3 sdk使用sha256 （子链sha256）
        //setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseSHA256.toml "+ PTPATH +"peer/conf/base.toml");
        setAndRestartSDK("cp "+PTPATH+"sdk/conf/configOK.toml "+PTPATH+"sdk/conf/config.toml");

        //检查子链可以成功发送，主链无法成功发送
        subLedger=chainName;
        String response3 = store.CreateStore("tc1650 data");
        assertEquals("200",JSONObject.fromObject(response3).getString("State"));  //确认可以发送成功

        Thread.sleep(SLEEPTIME);
        String txHash2 =JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));  //确认可以c查询成功

        subLedger="";
        String response4 = store.CreateStore("tc1651 data");
        assertThat(response4,containsString("hash error want"));

    }

    @AfterClass
    public static void resetPeerAndSDK()throws  Exception {
        setAndRestartPeerList("cp " + PTPATH + "peer/conf/baseOK.toml " + PTPATH + "peer/conf/base.toml");
        setAndRestartSDK("cp " + PTPATH + "sdk/conf/configOK.toml " + PTPATH + "sdk/conf/config.toml");
    }
}
