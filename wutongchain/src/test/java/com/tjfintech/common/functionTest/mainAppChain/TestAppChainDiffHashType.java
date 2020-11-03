package com.tjfintech.common.functionTest.mainAppChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetHashTypeSHA256;
import com.tjfintech.common.functionTest.Conditions.SetHashTypeSM3;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestAppChainDiffHashType {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    SoloSign soloSign = testBuilder.getSoloSign();
    BeforeCondition beforeCondition = new BeforeCondition();

    MgToolCmd mgToolCmd =new MgToolCmd();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    String ids = " -m "+ id1+","+ id2+","+ id3;

    @Test
    public void TC1649_1650_HashChange()throws Exception{
        //设置主链sm3 sdk使用sm3 （子链sha256） 设置前清空节点及sdk数据
        SetHashTypeSM3 setSM3part= new SetHashTypeSM3();
        setSM3part.setHashsm3();
        subLedger = "";
        beforeCondition.setPermission999();
        beforeCondition.updatePubPriKey();
        Thread.sleep(SLEEPTIME);
        //创建子链，包含三个节点 hashtype 使用sha256 主链使用sm3
        String chainName="tc1649_01";
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sha256"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
//        assertEquals(resp.contains(glbChain01), true);

        //设置主链sm3 sdk使用sha256 （子链sha256）
//        utilsClass.setAndRestartPeerList("sed -i 's/sm3/sha256/g " + PTPATH + "conf/base.toml");
        commonFunc.setSDKCryptHashType(utilsClass.getIPFromStr(SDKADD),"sha256");//将hashtype设置为sha256
        utilsClass.setAndRestartSDK();//重启sdk

        sleepAndSaveInfo(SLEEPTIME);
        //检查子链可以成功发送，主链无法成功发送
        subLedger="";
        String response2 = store.CreateStore("tc1649 data");
        assertThat(response2,containsString("hash error want"));

        subLedger=chainName;
        beforeCondition.collAddressTest();
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));  //确认可以发送成功

        //发送一笔utxo发行，并查询，检查数据库无问题
        String tokenType = "subTT" + Random(5);
        String issueInfo = soloSign.issueToken(PRIKEY1,tokenType,"1000","PRIKEY1 发行",PRIKEY1);
        assertEquals("200",JSONObject.fromObject(issueInfo).getString("state"));

        sleepAndSaveInfo(SLEEPTIME*2);
        //确认交易上链无异常
        String txHash1 =commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功
        //确认数据库同步无异常
        String query = soloSign.BalanceByAddr(ADDRESS1,tokenType);
        assertEquals("1000", JSONObject.fromObject(query).getJSONObject("data").getString("total"));


        //设置主链sm3 sdk使用sm3 （子链sha256）
        //utilsClass.setAndRestartPeerList(resetPeerBase);
        commonFunc.setSDKCryptHashType(utilsClass.getIPFromStr(SDKADD),"sm3");//将hashtype设置为sm3
        utilsClass.setAndRestartSDK();

        //检查主/子链可以成功发送
        subLedger="";
        String response3 = store.CreateStore("tc1650 data");
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash2 =commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));  //确认可以c查询成功

        //SDK兼容子链所有类型hashtype
        subLedger=chainName;
        String response4 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response4).getString("state"));  //确认可以发送成功

        String tokenType2 = "subTT" + Random(5);
        String issueInfo2 = soloSign.issueToken(PRIKEY1,tokenType2,"1000","PRIKEY1 发行",PRIKEY1);
        assertEquals("200",JSONObject.fromObject(issueInfo2).getString("state"));

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash3 =commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));  //确认可以c查询成功
        //确认数据库同步无异常
        String query2 = soloSign.BalanceByAddr(ADDRESS1,tokenType2);
        assertEquals("1000", JSONObject.fromObject(query2).getJSONObject("data").getString("total"));
    }

    @Test
    public void TC1651_1652_HashChange()throws Exception{
        //设置主链sha256 sdk使用sha256 （子链sm3） 设置前清空节点及sdk数据
        SetHashTypeSHA256 setSHA256 = new SetHashTypeSHA256();
        setSHA256.setHashSHA256();
        subLedger = "";
        beforeCondition.setPermission999();
        beforeCondition.updatePubPriKey();
        sleepAndSaveInfo(SLEEPTIME);

        //创建子链，包含三个节点 hashtype 子链sm3 主链使用sha256
        String chainName="tc1651_01";
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        sleepAndSaveInfo(SLEEPTIME);
        mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");

        subLedger=chainName;
        beforeCondition.collAddressTest();
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String response2 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));  //确认可以发送成功

        //发送一笔utxo发行，并查询，检查数据库无问题
        String tokenType = "subTT" + Random(5);
        String issueInfo = soloSign.issueToken(PRIKEY1,tokenType,"1000","PRIKEY1 发行",PRIKEY1);
        assertEquals("200",JSONObject.fromObject(issueInfo).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        //确认数据库同步无异常
        String query = soloSign.BalanceByAddr(ADDRESS1,tokenType);
        assertEquals("1000", JSONObject.fromObject(query).getJSONObject("data").getString("total"));


        subLedger="";
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        String txHash1 =commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功


        //设置主链sha256 sdk使用sm3 （子链sha256）
        utilsClass.setAndRestartSDK(resetSDKConfig);

        sleepAndSaveInfo(SLEEPTIME);
        //检查子链可以成功发送，主链无法成功发送
        subLedger=chainName;
        String response3 = store.CreateStore("tc1650 data");
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));  //确认可以发送成功

        String tokenType2 = "subTT" + Random(5);
        String issueInfo2 = soloSign.issueToken(PRIKEY1,tokenType2,"1000","PRIKEY1 发行",PRIKEY1);
        assertEquals("200",JSONObject.fromObject(issueInfo2).getString("state"));

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash3 =commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));  //确认可以c查询成功
        //确认数据库同步无异常
        String query2 = soloSign.BalanceByAddr(ADDRESS1,tokenType2);
        assertEquals("1000", JSONObject.fromObject(query2).getJSONObject("data").getString("total"));

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
