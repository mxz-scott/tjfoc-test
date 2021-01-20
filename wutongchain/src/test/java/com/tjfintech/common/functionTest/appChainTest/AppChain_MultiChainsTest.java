package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
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

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_MultiChainsTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    MgToolCmd mgToolCmd = new MgToolCmd();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    @Before
    public void beforeConfig() throws Exception {
        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);
    }


    @Test
    public void TC1589_createMultiChains()throws Exception{
        //创建子链，包含两个节点
        String chainName2 = "tc1589_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName2,
                " -t sm3"," -w first",
                " -c raft"," -m " + id1 + "," + id2);

        tempLedgerId1 = subLedger;

        //创建子链，包含三个节点
        String chainName3 = "tc1589_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName3,
                " -t sm3"," -w first"," -c raft",ids);

        tempLedgerId2 = subLedger;

        sleepAndSaveInfo(2000);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2), true);
        assertEquals(res2.contains(chainName3), true);

        String Data = "1589 ledger1 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向子链chainName2发送交易
        subLedger = tempLedgerId1;
        String response1 = store.CreateStore(Data);

        Data = "1589 ledger2 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向子链chainName3发送交易
        subLedger = tempLedgerId2;
        String response2 = store.CreateStore(Data);
        sleepAndSaveInfo(SLEEPTIME);

        Data = "1589 main tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        subLedger = globalAppId1;
        String response3 = store.CreateStore(Data);

        sleepAndSaveInfo(SLEEPTIME);

        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);

        subLedger = tempLedgerId1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));

        subLedger = tempLedgerId2;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));

        subLedger = globalAppId1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));

        //检查可以获取子链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);
    }

    @Test
    public void TC1556_TC1557_createSameStoreInMainAppChains()throws Exception{
        //创建子链，包含两个节点
        String chainName2 = "tc1589_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName2,
                " -t sm3"," -w first",
                " -c raft"," -m " + id1 + "," + id2);

        tempLedgerId1 = subLedger;

        //创建子链，包含三个节点
        String chainName3="tc1589_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName3,
                " -t sm3"," -w first"," -c raft",ids);

        tempLedgerId2 = subLedger;

        sleepAndSaveInfo(4000);

        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2), true);
        assertEquals(res2.contains(chainName3), true);

        String Data = "1589 ledger1 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);

        //向子链chainName2发送交易
        subLedger = tempLedgerId1;
        String response1 = store.CreateStore(Data);

        //向子链chainName3发送交易
        subLedger = tempLedgerId2;
        String response2 = store.CreateStore(Data);

        //向主链发送交易
        subLedger = globalAppId1;
        String response3 = store.CreateStore(Data);

        sleepAndSaveInfo(SLEEPTIME);

        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);

        subLedger = tempLedgerId1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));

        subLedger = tempLedgerId2;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));

        subLedger = globalAppId1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));

        //检查可以获取子链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);
    }


    @Test
    public void TC1592_1484_1477_1525_1528_1531_1524_createMultiChains()throws Exception{
        //创建子链，包含一个节点
        String chainName1="tc1592_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w first"," -c raft"," -m " + id1);
        assertEquals(res.contains("2 nodes is required for the memberList"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //创建子链，包含两个节点 为主链中的一个共识节点和一个非共识节点
        String chainName2="tc1592_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName2,
                " -t sm3"," -w first",
                " -c raft"," -m " + id1 + "," + id3);
        tempLedgerId1 = subLedger;

        sleepAndSaveInfo(2000);
        //创建子链，包含三个节点
        String chainName3="tc1592_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName3,
                " -t sm3"," -w first"," -c raft",ids);
        tempLedgerId2 = subLedger;

        sleepAndSaveInfo(2000);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName1), false);
        assertEquals(res2.contains(chainName2), true);
        assertEquals(res2.contains(chainName3), true);
        assertEquals(chainName2.contains(chainName3), false);


        String Data = "1592 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向子链chainName2发送交易
        subLedger = tempLedgerId1;
        String response1 = store.CreateStore(Data);

        Data = "1592 ledger2 tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向子链chainName3发送交易
        subLedger = tempLedgerId2;
        String response2 = store.CreateStore(Data);

        Data = "1592 main tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
        //向主链发送交易
        subLedger = globalAppId1;
        String response3 = store.CreateStore(Data);

        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);

        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);

        subLedger = tempLedgerId1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));

        subLedger = tempLedgerId2;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));

        subLedger = globalAppId1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));


    }

    @Test
    public void TC1593_createMultiChains()throws Exception{

        //创建子链，包含三个节点
        String chainName3 = "tc1593_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = "";
        mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName3,
                " -t sm3"," -w first"," -c raft",ids);

        tempLedgerId1 = subLedger;

        //创建子链，包含两个节点
        String chainName2="tc1593_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName2,
                " -t sm3"," -w first",
                " -c raft"," -m " + id1 + "," + id2);

        tempLedgerId2 = subLedger;

        //创建子链，包含一个节点
        String chainName1="tc1593_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w first",
                " -c raft"," -m " + id1);
        assertEquals(res.contains("2 nodes is required for the memberList"), true);

        sleepAndSaveInfo(2000);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName1), false);
        assertEquals(res2.contains(chainName2), true);
        assertEquals(res2.contains(chainName3), true);

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMultiActiveChain("1593 tx",globalAppId1,globalAppId2,tempLedgerId1,tempLedgerId2);
    }

}
