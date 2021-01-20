package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_Create_01 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();



//    @Test
    public void testTemp()throws Exception{
        log.info(getStrByReg("\"id\" :\"qgnuxg7eej\"","\\\"id\\\"\\s+:\\\"(\\w+)\\\""));

        log.info(getStrByReg(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c qgnuxg7eej"),"\"id\":\\s+\"(\\w{10})\""));
    }

    /***
     * 链内节点*3
     * @throws Exception
     */
    @Before
    public void beforeConfig() throws Exception {
        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);
    }

    @Test
    public void TC1613_createWordMultiStr()throws Exception{
        //创建子链，-w "first word"
        String chainName = "tc1613_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String word = chainName + " first word";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName," -t sm3",
                " -w \"" + word + "\""," -c raft",ids);

        sleepAndSaveInfo(4000);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName), true);

//        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n " + chainName);
        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"  ");
        assertEquals(res3.contains(word), true);
    }


    @Test
    public void TC1491_createNoCons()throws Exception{
        String chainName1 = "tc1491_" + sdf.format(dt) + RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        //创建子链，共识算法不填写
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w first ","",ids);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res3.contains("raft"), true);
    }
    @Test
    public void TC1489_createWordWithSpecial()throws Exception{
        String chainName1 = "tc1489_" + sdf.format(dt) + RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="\"!@#~$%^&*()-=+/?><中文{}[]|\"";
        //创建子链，共识算法不填写
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w " + wordValue,"",ids);
        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res3.contains("raft"), true);
    }

    @Test
    public void TC1488_createWordWith79len()throws Exception{
        String chainName1 = "tc1488_" + sdf.format(dt) + RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="1234567890123456789012345678901234567890123456789012345678901234567890123456789";
        //创建子链，共识算法不填写
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1," -t sm3",
                " -w " + wordValue,"",ids);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res3.contains("raft"), true);
    }

   //此用例在执行后进检查可以使用 不检查是否成功创建
    //移除支持 . 不再支持 . 20200420
   //20191106 规则测试有变更 首字母仅允许数字字母（大小写）
    @Test
    public void TC1472_CreateNameValid()throws Exception{

        //创建子链，名称为"1"
        String chainName1 = "1";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w first word"," -c raft",ids);

        //创建子链，名称为"A"
        String chainName2 = "A";
        String res1 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName2,
                " -t sm3"," -w first word"," -c raft",ids);

        //创建子链，名称为"test"
        String chainName3 = "test";
        String res2 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName3,
                " -t sm3"," -w first word"," -c raft",ids);

        //创建子链，名称为"a_Q123"
        String chainName4 = "a_Q123";
        String res3 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName4,
                " -t sm3"," -w first word"," -c raft",ids);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表
        String res6 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res6.contains("name"), true);
        assertEquals(res6.contains("\"name\": \"" + chainName1 + "\""), true);
        assertEquals(res6.contains("\"name\": \"" + chainName2 + "\""), true);
        assertEquals(res6.contains("\"name\": \"" + chainName3 + "\""), true);
        assertEquals(res6.contains("\"name\": \"" + chainName4 + "\""), true);
    }

    @Test
    public void TC1471_CreateAndCheck()throws Exception{
        //获取单个子链信息
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + globalAppId1);
        assertEquals(res1.contains(id1), true);
        assertEquals(res1.contains(id2), true);
        assertEquals(res1.contains(id3), true);

        //获取系统所有子链信息
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(globalAppId1), true);
        assertEquals(res2.contains(id1), true);
        assertEquals(res2.contains(id2), true);
        assertEquals(res2.contains(id3), true);
    }

    @Test
    public void TC1621_testTXforMainAppChains()throws Exception{
        //创建子链，包含两个节点
        String chainName2 = "05";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+ chainName2,
                " -t sm3"," -w first"," -c raft",ids);

        String ledgerId2 = subLedger;
        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId2);
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2), true);

        String Data = "1621 ledger1 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向子链chainName2发送交易
        String response1 = store.CreateStore(Data);
        sleepAndSaveInfo(SLEEPTIME);

        Data = "1621 main tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向globalAppId1链发送交易
        subLedger = globalAppId1;
        String response3 = store.CreateStore(Data);

        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);

        String txHash1 = JSONObject.fromObject(response1).get("data").toString();
        String txHash3 = JSONObject.fromObject(response3).get("data").toString();
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
    }

}
