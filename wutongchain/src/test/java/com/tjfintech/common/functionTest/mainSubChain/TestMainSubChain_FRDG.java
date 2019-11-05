package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.ManageTool;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.SubLedgerCmd;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMainSubChain_FRDG {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();

    String notSupport="not support service";
    String stateDestroyed ="has been destroyed";
    String stateFreezed ="has been freezed";
//    String stateFreezed ="not support service";
//    String ledgerStateDestroy = "\"state\": \"Destory\"";
//    String ledgerStateFreeze = "\"state\": \"Freeze\"";


    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";

    @BeforeClass
    public static void clearData()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.clearDataSetPerm999();
        sleepAndSaveInfo(SLEEPTIME);
    }


    @Before
    public void beforeConfig() throws Exception {
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01+"\"")) {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01, " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02, " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
        }
    }



    @Test
    public void TC1515_1514_1648_recoverFreezeChain03()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1515_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //冻结子链
        res = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateFreeze), true);


        //解除子链
        res = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateNormal), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1521 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

        storeTypeSupportCheck("normal");
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }


    @Test
    public void TC1516_recoverNormChain02()throws Exception{

        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
//        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);


        //解除子链
        String res = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+glbChain01);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+glbChain01);
        assertEquals(resp.contains(ledgerStateNormal), true);


        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1521 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);

    }

    @Test
    public void TC1517_recoverDestroyChain01()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1517_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //销毁子链
        res = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        //解除销毁子链
        res = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1517 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestroyed));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }


    @Test
    public void TC1522_destroyChain04()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1522_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //冻结子链
        res = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateFreeze), true);


        //解除冻结子链
        res = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateNormal), true);

        //销毁一个被冻结子链
        res = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1522 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestroyed));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }

    @Test
    public void TC1521_destroyChain03()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1521_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //冻结子链
        res = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateFreeze), true);

        //销毁一个被冻结子链
        res = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1521 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestroyed));


        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }

    @Test
    public void TC1622_destroyChain02()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1622_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //第一次销毁一个已存在的活跃子链
        res = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateDestroy), true);


        //再次销毁已被销毁的子链
        res = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateDestroy), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1622 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1,containsString(stateDestroyed));




        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }


    @Test
    public void TC1518_1519_1496_1646_destroyChain01()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1518_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //销毁一个已存在的活跃子链
        res = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateDestroy), true);
        assertEquals(resp.contains("\"name\": \""+chainName+"\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);

        String Data="1518 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
        storeTypeSupportCheck("destroy");

        Data="1518 ledger2 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }

    public void storeTypeSupportCheck(String type)throws Exception{
        String txHash1 ="HMO5gFTZ2swdDp2BQmIWS/ZBNeEZLo/TakixYhSRy3U=";
        String txHash2 ="HMO5gFTZ2swdDp2BQmIWS/ZBNeEZLo/TakixYhSRy3U=";
        String blockHash ="i1XhwBUvL1alXVhd0GH3Z/Uaxe+1wFBw+OZ8yOaBWig=";
        //String notSupport=notSupport;
        //String notSupport="doesn't exist";
        boolean bCheck1 = false;//上链类交易返回校验字符串
        boolean bCheck2 = false;//查询类交易返回校验字符串

        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        //map.put("pubkeys",PUBKEY6);

        if(type.toLowerCase()=="normal"){
            txHash1=JSONObject.fromObject(store.CreateStore("test")).getJSONObject("Data").getString("Figure");
            txHash2=JSONObject.fromObject(store.CreatePrivateStore("test",map)).getJSONObject("Data").getString("Figure");
            bCheck1=false;
            bCheck2=false;
            blockHash=JSONObject.fromObject(store.GetBlockByHeight(1)).getJSONObject("Data").getJSONObject("header").getString("blockHash");
            sleepAndSaveInfo(SLEEPTIME);
        }
        else if(type.toLowerCase()=="freeze"){
            notSupport=stateFreezed;
            bCheck1=true;
            bCheck2=false;
            blockHash=JSONObject.fromObject(store.GetBlockByHeight(1)).getJSONObject("Data").getJSONObject("header").getString("blockHash");
        }
        else if(type.toLowerCase()=="destroy"){
            notSupport=stateDestroyed;
            bCheck1=true;
            bCheck2=true;
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
        assertEquals(bCheck2,store.GetTransactionBlock(txHash1).contains(notSupport));
        assertEquals(bCheck2,store.GetTransactionIndex(txHash1).contains(notSupport));
        assertEquals(false,store.GetInlocal(txHash1).contains(notSupport));//此接口与开发确认不管控
        assertEquals(bCheck2,store.GetStore(txHash1).contains(notSupport));
        assertEquals(bCheck2,store.GetApiHealth().contains(notSupport));
        assertEquals(bCheck2,store.GetStorePost(txHash2,PRIKEY1).contains(notSupport));
        assertEquals(bCheck2,store.GetBlockByHash(blockHash).contains(notSupport));
        assertEquals(false,store.GetPeerList().contains(notSupport));
        assertEquals(bCheck1,store.CreatePrivateStore("test1",map).contains(notSupport));

        assertEquals(bCheck1,store.CreateStore("test2").contains(notSupport));
//        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(bCheck1,store.SynCreateStore(SHORTMEOUT,"test3").contains(notSupport));
        assertEquals(bCheck1,store.SynCreateStore(SHORTMEOUT,"test4",PUBKEY1).contains(notSupport));


    }

    @Test
    public void TC1497_1536_getNoExistChain()throws Exception{

        //子链
        String chainName="tc1497_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(glbChain01), true);

        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("subledger not exist"), true);


        //检查可以获取子链列表 存在其他子链 确认异常操作后系统无异常
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");

        assertEquals(resp.contains("name"), true);

        String Data="1475 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向不存在的子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString("doesn't exist"));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }




    @Test
    public void TC1494_1495_getFreezeChain()throws Exception{
        String chainName="tc1494_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建一个子链
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        res = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);

        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateFreeze), true);
        assertEquals(resp.contains("\"name\": \""+chainName+"\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);

        String Data="1475 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
//        sleepAndSaveInfo(SLEEPTIME);
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateFreezed));
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);

        //查看恢复子链后的子链状态并向子链发送交易
        res = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);

        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains(ledgerStateNormal), true);
        assertEquals(resp.contains("\"name\": \""+chainName+"\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);


        //向子链chainName发送交易
        String txHash2 = JSONObject.fromObject(store.CreateStore(Data)).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }


    @Test
    public void TC1609_recoverRecoverChains()throws Exception{

        //创建子链
        String chainName="tc1609_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z " + chainName," -t sm3",
                " -w first"," -c raft"," -m "+ id1 + "," + id2);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName), true);

        //冻结子链
        String respon = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);

        //子链信息检查
        String res3 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res3.contains(ledgerStateFreeze), true);


        //恢复冻结子链 连续两次恢复
        String respon1 = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);
        respon1 = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);

        //子链信息检查
        String res4 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res4.contains(ledgerStateNormal), true);

        //确认恢复后再次恢复
        respon1 = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);
        res4 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res4.contains(ledgerStateNormal), true);

        String Data="1609 ledger1 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
        String txHash1 = JSONObject.fromObject(store.CreateStore(Data)).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));


        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }



    @Test
    public void TC1510_1647_freezeSubChains()throws Exception{
        //创建子链，包含两个节点
        String chainName="tc1510_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft"," -m "+id1+","+id2);
        assertEquals(res.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName), true);

        //冻结前发送一笔交易
        subLedger=chainName;
        String response = store.CreateStore("tttttt");
        sleepAndSaveInfo(SLEEPTIME);

        //冻结子链
        String respon = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);

        sleepAndSaveInfo(SLEEPTIME,"冻结子链交易上链等待");
        //检查可以获取子链列表
        String res3 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res3.contains(ledgerStateFreeze), true);


        String Data="1510 ledger1 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName2发送交易
        subLedger=chainName;
//        sleepAndSaveInfo(SLEEPTIME);
        String response1 = store.CreateStore(Data);


        //检查可以获取子链列表
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        assertThat(response1, containsString(stateFreezed));
        storeTypeSupportCheck("freeze");
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }

    @Test
    public void TC1509_freezeMultiSubChains()throws Exception{
        //创建子链，包含两个节点
        String chainName2="tc1509_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2,
                " -t sm3"," -w first"," -c raft"," -m "+id1+","+id2);
        assertEquals(res.contains("send transaction success"), true);

        //创建子链，包含三个节点
        String chainName3="tc1509_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);

        //检查可以获取子链列表
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2), true);
        assertEquals(res2.contains(chainName3), true);

        //冻结两条子链
        String respon = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2);
        String respon2 = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3);
        sleepAndSaveInfo(SLEEPTIME);


        //检查可以获取子链列表
        String res3 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2);
        assertEquals(res3.contains(ledgerStateFreeze), true);
        String res4 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3);
        assertEquals(res4.contains(ledgerStateFreeze), true);


        String Data="1509 ledger1 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName2发送交易
        subLedger=chainName2;
        String response1 = store.CreateStore(Data);

        Data="1509 ledger2 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链chainName3发送交易
        subLedger=chainName3;
        String response2 = store.CreateStore(Data);

        sleepAndSaveInfo(SLEEPTIME*2);

        //检查可以获取子链列表
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);

        subLedger=chainName2;
//        sleepAndSaveInfo(SLEEPTIME);
        assertThat(response1, containsString(stateFreezed));

        subLedger=chainName3;
        assertThat(response2, containsString(stateFreezed));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);

    }

    @Test
    public void TC1511_freezeFrozenChain()throws Exception{

        //待发送子链名
        String chainName="tc1511_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        String Data="1511 ledger2 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //1.冻结一个子链chainName
        String respon = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon, containsString("send transaction success"));
        sleepAndSaveInfo(SLEEPTIME);

        //2.查询子链状态为冻结
        String resp2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp2.contains(ledgerStateFreeze), true);

        subLedger=chainName;
        //3.冻结后发送一笔存证交易 应该无法上链
//        sleepAndSaveInfo(SLEEPTIME);
        String response10 = store.CreateStore(Data);
        assertThat(response10, containsString(stateFreezed));


        //5.再次冻结已冻结的子链
        String respon1 = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon1, containsString("send transaction success"));
        sleepAndSaveInfo(SLEEPTIME);
        //6.查询子链状态为冻结
        String resp21 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp21.contains(ledgerStateFreeze), true);

        subLedger=chainName;
        //7.冻结后发送一笔存证交易 应该无法上链
        String response11 = store.CreateStore(Data);
        assertThat(response11, containsString(stateFreezed));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }

    @Test
    public void TC1512_freezeNoExistChain()throws Exception{

        //待发送子链名
        String chainName="tc1512_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(glbChain01), true);

        String Data="1512 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //解除一个不存在的子链chainName
        String respon = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon, containsString(" subledger not exist"));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }

    @Test
    public void TC1520_destroyNoExistChain()throws Exception{

        //待发送子链名
        String chainName="tc1520_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(glbChain01), true);

        String Data="1520 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //销毁一个不存在的子链chainName
        String respon = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon, containsString(" subledger not exist"));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }


    @Test
    public void TC1585_recoverNoExistChain()throws Exception{

        //待发送子链名
        String chainName="tc1585_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(glbChain01), true);

        String Data="1585 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //恢复一个不存在的子链chainName
        String respon = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon, containsString(" subledger not exist"));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }
    
    @Test
    public void TC1513_freezeEmptyName()throws Exception{
        assertEquals(mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort,"").contains("management freezeledger"), true);

        //检查可以获取子链列表
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }
}
