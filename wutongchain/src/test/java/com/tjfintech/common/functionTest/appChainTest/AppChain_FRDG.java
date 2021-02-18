package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.SubLedgerCmd;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_FRDG {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    String hash = "";
    String hashTime = "";


    @Before
    public void beforeConfig() throws Exception {
        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);
        urlAddr = "";
    }

    @Test
    public void TC1515_1514_1648_recoverFreezeChain03()throws Exception{

        //创建应用链，包含三个节点
        String chainName = "tc1515_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+ chainName,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

//        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //冻结应用链
        res = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(2000,"等待同步时间");
        //检查应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateFreeze), true);


        //解除应用链
        res = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(2000,"等待同步时间");
        //检查应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        if(resp.contains("state"))  assertEquals(resp.contains(ledgerStateNormal), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取应用链列表
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data = "1521 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向新建应用链chainName发送交易
        String response1 = store.CreateStore(Data);

        hash = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType00);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String txHash1 = JSONObject.fromObject(response1).get("data").toString();
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

        storeTypeSupportCheck("normal");
        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2,subLedger);
    }


    @Test
    public void TC1516_recoverNormChain02()throws Exception{

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
//        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);


        //解除应用链
        String res = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + globalAppId1);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(2000,"等待同步时间");
        //检查应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + globalAppId1);
        if(resp.contains("state"))  assertEquals(resp.contains(ledgerStateNormal), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取应用链列表
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data = "1521 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);

    }

    @Test
    public void TC1517_recoverDestroyChain01()throws Exception{

        //创建应用链，包含三个节点
        String chainName = "tc1517_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
//        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //销毁应用链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(2000,"等待同步时间");
        //检查被销毁应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        //解除销毁应用链
        res = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("off-time not support service"), true);

        //检查应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取应用链列表
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);

        //发送交易测试
        String Data = "1517 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向新建应用链chainName发送交易
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestroyed));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }


    @Test
    public void TC1522_destroyChain04()throws Exception{

        //创建应用链，包含三个节点
        String chainName = "tc1522_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(globalAppId1), true);

        //冻结应用链
        res = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        //检查被冻结应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateFreeze), true);

        //解除冻结应用链
        res = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        if(resp.contains("state"))  assertEquals(resp.contains(ledgerStateNormal), true);

        //销毁一个被冻结应用链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查被销毁应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取应用链列表
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data = "1522 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向新建应用链chainName发送交易
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestroyed));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }

    @Test
    public void TC1521_destroyChain03()throws Exception{

        //创建应用链，包含三个节点
        String chainName = "tc1521_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(globalAppId1), true);

        //冻结应用链
        res = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查被销毁应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateFreeze), true);

        //销毁一个被冻结应用链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查被销毁应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取应用链列表
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data = "1521 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向新建应用链chainName发送交易
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestroyed));


        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }

    @Test
    public void TC1622_destroyChain02()throws Exception{

        //创建应用链，包含三个节点
        String chainName = "tc1622_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(globalAppId1), true);

        //第一次销毁一个已存在的活跃应用链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查被销毁应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        //再次销毁已被销毁的应用链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("off-time not support service"), true);

        //检查被销毁应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取应用链列表
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data = "1622 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向新建应用链chainName发送交易
        String response1 = store.CreateStore(Data);
        assertThat(response1,containsString(stateDestroyed));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }


    @Test
    public void TC1518_1519_1496_1646_destroyChain01()throws Exception{

        //创建应用链，包含三个节点
        String chainName = "tc1518_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //销毁一个已存在的活跃应用链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查被销毁应用链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);
        assertEquals(resp.contains("\"name\": \"" + chainName + "\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);

        String Data = "1518 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向新建应用链chainName发送交易
        storeTypeSupportCheck("destroy");

        Data = "1518 ledger2 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }

    public void storeTypeSupportCheck(String type)throws Exception{
        PRIKEY1 = utilsClass.getKeyPairsFromFile(certPath + "/keys1/key.pem");
        PUBKEY1 = utilsClass.getKeyPairsFromFile(certPath + "/keys1/pubkey.pem");

        String txHash1 = "de1600ecb828b8c524a7d650cc9498b35e3b78414fc4ae8677ae165d2ef029a6";
        String txHash2 = "de1600ecb828b8c524a7d650cc9498b35e3b78414fc4ae8677ae165d2ef029a6";
        String blockHash = "24bf6ca3cabf7963f356480bf3f3397d21a647a17c18d164a8d6c848ec6f266a";
        //String notSupport=notSupport;
        //String notSupport="doesn't exist";
        boolean bCheck1 = false;//上链类交易返回校验字符串
        boolean bCheck2 = false;//查询类交易返回校验字符串

        Map<String,Object> map = new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        //map.put("pubkeys",PUBKEY6);

        if(type=="normal"){
            txHash1 = JSONObject.fromObject(store.CreateStore("test")).getString("data");
            txHash2 = JSONObject.fromObject(store.CreatePrivateStore("test",map)).getString("data");
            bCheck1 = false;
            bCheck2 = false;
            blockHash = JSONObject.fromObject(store.GetBlockByHeight(1)).getJSONObject("data").getJSONObject("header").getString("blockId");
            sleepAndSaveInfo(SLEEPTIME);
        }
        else if(type == "freeze"){
            notSupport = stateFreezed;
            bCheck1 = true;
            bCheck2 = false;
            blockHash = JSONObject.fromObject(store.GetBlockByHeight(1)).getJSONObject("data").getJSONObject("header").getString("blockId");
        }
        else if(type == "destroy"){
            notSupport = stateDestroyed;
            bCheck1 = true;
            bCheck2 = true;
        }
        else {
            log.info("type string is illegal,please check");
            assertEquals(false, true);
        }
        log.info("start check");
        assertEquals(bCheck2,store.GetTxDetail(txHash1).contains(notSupport));
        assertEquals(bCheck2,store.GetHeight().contains(notSupport));
        assertEquals(bCheck2,store.GetStore(txHash1).contains(notSupport));
        assertEquals(bCheck2,store.GetBlockByHeight(1).contains(notSupport));
//        assertEquals(bCheck2,store.GetTransactionBlock(txHash1).contains(notSupport));
        assertEquals(bCheck2,store.GetTransactionIndex(txHash1).contains(notSupport));
        assertEquals(false,store.GetInlocal(txHash1).contains(notSupport));//此接口与开发确认不管控
        assertEquals(bCheck2,store.GetStore(txHash1).contains(notSupport));
        assertEquals(bCheck2,store.GetApiHealth().contains(notSupport));
        assertEquals(bCheck2,store.GetStorePost(txHash2,PRIKEY1).contains(notSupport));
        assertEquals(bCheck2,store.GetBlockByHash(blockHash).contains(notSupport));
//        assertEquals(false,store.GetPeerList().contains(notSupport));
        assertEquals(bCheck1,store.CreatePrivateStore("test1",map).contains(notSupport));

        assertEquals(bCheck1,store.CreateStore("test2").contains(notSupport));

//        sleepAndSaveInfo(SLEEPTIME);
        //20200401 与先先确认 移除sync接口测试
//        assertEquals(bCheck1,store.SynCreateStore(SHORTMEOUT,"test3").contains(notSupport));
//        assertEquals(bCheck1,store.SynCreateStore(SHORTMEOUT,"test4",PUBKEY1).contains(notSupport));

    }

    @Test
    public void TC1497_1536_getNoExistChain()throws Exception{

        //应用链
        String chainName = "tc1497_" + sdf.format(dt) + RandomUtils.nextInt(1000);

//        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(globalAppId1), true);

        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + chainName);
        assertEquals(resp.contains("{}"), true);


        //检查可以获取应用链列表 存在其他应用链 确认异常操作后系统无异常
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);

        String Data = "1475 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向不存在的应用链chainName发送交易
        subLedger = "notexist";
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString("ledger not found:"));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }




    @Test
    public void TC1494_1495_getFreezeChain()throws Exception{
        String chainName = "tc1494_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        //创建一个应用链
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
;
        tempLedgerId = subLedger;
        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(globalAppId1), true);

        res = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateFreeze), true);
        assertEquals(resp.contains("\"name\": \"" + chainName + "\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);

        String Data = "1475 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向新建应用链chainName发送交易
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateFreezed));
        
        //应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);

        subLedger = tempLedgerId;

        //查看恢复应用链后的应用链状态并向新建应用链发送交易
        res = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        if(resp.contains("state"))  assertEquals(resp.contains(ledgerStateNormal), true);
        assertEquals(resp.contains("\"name\": \"" + chainName + "\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);


        //向新建应用链chainName发送交易
        String txHash2 = JSONObject.fromObject(store.CreateStore(Data)).get("data").toString();

        hashTime = commonFunc.sdkCheckTxOrSleep(txHash2,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        
        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }


    @Test
    public void TC1609_recoverRecoverChains()throws Exception{

        //创建应用链
        String chainName = "tc1609_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName," -t sm3",
                " -w first"," -c raft"," -m "+ id1 + "," + id2);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查可以获取应用链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName), true);

        //冻结应用链
        String respon = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        hash = commonFunc.getTxHash(respon,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //应用链信息检查
        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res3.contains(ledgerStateFreeze), true);


        //恢复冻结应用链 连续两次恢复
        String respon1 = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        hash = commonFunc.getTxHash(respon1,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        respon1 = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        hash = commonFunc.getTxHash(respon1,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //应用链信息检查
        String res4 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        if(res4.contains("state"))  assertEquals(res4.contains(ledgerStateNormal), true);

        //确认恢复后再次恢复
        respon1 = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        hash = commonFunc.getTxHash(respon1,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        res4 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        if(res4.contains("state"))  assertEquals(res4.contains(ledgerStateNormal), true);

        String Data = "1609 ledger1 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向新建应用链chainName发送交易
        String txHash1 = JSONObject.fromObject(store.CreateStore(Data)).get("data").toString();
        hashTime = commonFunc.sdkCheckTxOrSleep(txHash1,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查可以获取应用链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }


    @Test
    public void TC1510_1647_freezeAppChains()throws Exception{
        //创建应用链，包含两个节点
        String chainName = "tc1510_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first",
                " -c raft"," -m " + id1 + "," + id2);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        sleepAndSaveInfo(4000);

        //检查可以获取应用链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName), true);

        urlAddr = PEER1IP+  ":" + PEER1RPCPort;
        //冻结前发送一笔交易
        String response = store.CreateStore("tttttt");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(SLEEPTIME);

        //冻结应用链
        String respon = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        hash = commonFunc.getTxHash(respon,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查可以获取应用链列表
        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res3.contains(ledgerStateFreeze), true);

        //向冻结应用链chainName2发送交易
        String Data = "1510 ledger1 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        String response1 = store.CreateStore(Data);
        assertEquals(true,response1.contains(stateFreezed));


        //检查可以获取应用链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(globalAppId1), true);

        //检查向冻结应用链发送交易 结果检查
        storeTypeSupportCheck("freeze");

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }

    @Test
    public void TC1509_freezeMultiAppChains()throws Exception{
        //创建应用链，包含两个节点
        String chainName2 = "tc1509_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = "";
        mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName2,
                " -t sm3"," -w first"," -c raft"," -m "+id1+","+id2);
        String tempLedgerId1 = subLedger;

        //创建应用链，包含三个节点
        String chainName3 = "tc1509_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName3,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String tempLedgerId2 = subLedger;

        //检查可以获取应用链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2), true);
        assertEquals(res2.contains(chainName3), true);

        //冻结两条应用链
        String respon = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + tempLedgerId1);
        String respon2 = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + tempLedgerId2);

        hash = commonFunc.getTxHash(respon2,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        sleepAndSaveInfo(4000);

        //检查可以获取应用链列表
        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + tempLedgerId1);
        assertEquals(res3.contains(ledgerStateFreeze), true);
        String res4 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + tempLedgerId2);
        assertEquals(res4.contains(ledgerStateFreeze), true);


        String Data = "1509 ledger1 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向新建应用链chainName2发送交易
        subLedger = tempLedgerId1;
        urlAddr = PEER1IP +  ":" + PEER1RPCPort;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateFreezed));

        Data = "1509 ledger2 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向新建应用链chainName3发送交易
        subLedger = tempLedgerId2;
        String response2 = store.CreateStore(Data);
        assertThat(response2, containsString(stateFreezed));


        //检查可以获取应用链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);


        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);

    }

    @Test
    public void TC1511_freezeFrozenChain()throws Exception{
        //创建应用链
        String chainName = "tc1511_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        hash = commonFunc.getTxHash(res,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(globalAppId1), true);

        String Data = "1511 ledger2 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //1.冻结一个应用链chainName
        String respon = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertThat(respon, containsString("send transaction success"));
        hash = commonFunc.getTxHash(respon,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //2.查询应用链状态为冻结
        String resp2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp2.contains(ledgerStateFreeze), true);

        //3.冻结后发送一笔存证交易 应该无法上链
        String response10 = store.CreateStore(Data);
        assertThat(response10, containsString(stateFreezed));

        //5.再次冻结已冻结的应用链
        String respon1 = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertThat(respon1, containsString("send transaction success"));
        hash = commonFunc.getTxHash(respon1,utilsClass.mgGetTxHashType);
        hashTime = commonFunc.sdkCheckTxOrSleep(hash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //6.查询应用链状态为冻结
        String resp21 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp21.contains(ledgerStateFreeze), true);

        //7.冻结后发送一笔存证交易 应该无法上链
        String response11 = store.CreateStore(Data);
        assertThat(response11, containsString(stateFreezed));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }

    @Test
    public void TC1512_freezeNoExistChain()throws Exception{

        //待发送应用链名
        String chainName = "tc1512_" + sdf.format(dt) + RandomUtils.nextInt(1000);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(globalAppId1), true);

        String Data = "1512 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //解除一个不存在的应用链chainName
        String respon = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + chainName);
        assertThat(respon, containsString("error"));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }

    @Test
    public void TC1520_destroyNoExistChain()throws Exception{

        //待发送应用链名
        String chainName = "tc1520_" + sdf.format(dt) + RandomUtils.nextInt(1000);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(globalAppId1), true);

        String Data = "1520 ledger tx store " + sdf.format(dt)+ RandomUtils.nextInt(100000);

        //销毁一个不存在的应用链chainName
        String respon = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + chainName);
        assertThat(respon, containsString("error"));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }


    @Test
    public void TC1585_recoverNoExistChain()throws Exception{

        //待发送应用链名
        String chainName = "tc1585_" + sdf.format(dt) + RandomUtils.nextInt(1000);

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(globalAppId1), true);

        String Data = "1585 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //恢复一个不存在的应用链chainName
        String respon = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + chainName);
        assertThat(respon, containsString("error"));

        //向应用链glbChain01/glbChain02发送交易
        subLedgerCmd.sendTxToMultiActiveChain(Data,globalAppId1,globalAppId2);
    }

    @Test
    public void TC1513_freezeEmptyName()throws Exception{
        assertEquals(mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort,"").contains("subLedgerName is null"), true);

        //检查可以获取应用链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }
}
