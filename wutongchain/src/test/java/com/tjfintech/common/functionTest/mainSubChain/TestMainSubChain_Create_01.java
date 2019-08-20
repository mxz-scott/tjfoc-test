package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;

import com.tjfintech.common.functionTest.Conditions.SetDatabaseMongo;
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
public class TestMainSubChain_Create_01 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    MgToolCmd mgToolCmd = new MgToolCmd();

    String noPerm="not found";
    String notSupport="not support service";
    String stateDestroyed ="has been destroyed";
    String stateFreezed ="not support service";
    String ledgerStateDestroy = "\"state\": \"Destory\"";
    String ledgerStateFreeze = "\"state\": \"Freeze\"";

    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";

    @BeforeClass
    public static void clearData()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.clearDataSetPerm999();
    }

    @Before
    public void beforeConfig() throws Exception {
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01+"\"")) {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01,
                    " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02,
                    " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
        }
    }

    @Test
    public void TC1613_createWordMultiStr()throws Exception{
        //创建子链，-w "first word"
        String chainName = "tc1613_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String word =chainName+" first word";
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3",
                " -w \""+word+"\""," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName), true);

        String res3 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res3.contains(word), true);
    }


    @Test
    public void TC1491_createNoCons()throws Exception{
        String chainName1="tc1491_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        //创建子链，共识算法不填写
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3"," -w first ","",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }
    @Test
    public void TC1489_createWordWithSpecial()throws Exception{
        String chainName1="tc1489_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="\"!@#~$%^&*()-=+/?><中文{}[]|\"";
        //创建子链，共识算法不填写
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w "+wordValue,"",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }

    @Test
    public void TC1488_createWordWith79len()throws Exception{
        String chainName1="tc1488_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="1234567890123456789012345678901234567890123456789012345678901234567890123456789";
        //创建子链，共识算法不填写
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w "+wordValue,"",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }

   //此用例在执行后进检查可以使用 不检查是否成功创建
    @Test
    public void TC1472_CreateNameValid()throws Exception{

        //创建子链，名称为"1"
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z 1",
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);
        String txHash = res.substring(res.lastIndexOf(":")+1).trim();

        //创建子链，名称为"A"
        String res1 = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z A",
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res1.contains("send transaction success"), true);
        String txHash1 = res.substring(res1.lastIndexOf(":")+1).trim();

        //创建子链，名称为"test"
        String res2 = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z test",
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res2.contains("send transaction success"), true);
        String txHash2 = res.substring(res2.lastIndexOf(":")+1).trim();


        //创建子链，名称为"_a"
        String res3 = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z _a",
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res3.contains("send transaction success"), true);
        String txHash3 = res.substring(res3.lastIndexOf(":")+1).trim();

        //创建子链，名称为"."
        String res4 = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z .a",
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res4.contains("send transaction success"), true);
        String txHash4 = res.substring(res4.lastIndexOf(":")+1).trim();

        //创建子链，名称为"_1aZ."
        String res5 = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z _1aZ.",
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res5.contains("send transaction success"), true);
        String txHash5 = res.substring(res5.lastIndexOf(":")+1).trim();

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft,确认可以查到数据
        String res6 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res6.contains("name"), true);
    }

    @Test
    public void TC1471_CreateAndCheck()throws Exception{
        //获取单个子链信息
        String res1 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort," -z "+glbChain01);
        assertEquals(res1.contains(glbChain01), true);
        assertEquals(res1.contains(id1), true);
        assertEquals(res1.contains(id2), true);
        assertEquals(res1.contains(id3), true);

        //获取系统所有子链信息
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(glbChain01), true);
        assertEquals(res2.contains(id1), true);
        assertEquals(res2.contains(id2), true);
        assertEquals(res2.contains(id3), true);
    }

    @Test
    public void TC1621_testTXforMainSubChains()throws Exception{
        //创建子链，包含两个节点
        String chainName2="05";
        String res = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2), true);

        String Data="1621 ledger1 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName2发送交易
        subLedger=chainName2;
        String response1 = store.CreateStore(Data);
        sleepAndSaveInfo(SLEEPTIME);

        Data="1621 main tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向主链发送交易
        subLedger="";
        String response3 = store.CreateStore(Data);

        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);

        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();

        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
    }

}
