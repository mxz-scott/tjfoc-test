package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_Y_UpgradeTestOnly {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    String id4 = getPeerId(PEER3IP,USERNAME,PASSWD);
    String ids = " -m "+ id1+","+ id2+","+ id3;
    List<String> listPeer = new ArrayList<>();

//    @Before
    public void clearData()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.clearDataSetPerm999();
        sleepAndSaveInfo(SLEEPTIME);
    }

//    @BeforeClass
    public static void clearPeerDB()throws Exception{
        UtilsClass utilsClass = new UtilsClass();
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB);
        //重启SDK
        utilsClass.setAndRestartSDK();
    }

    @Test
    public void CreaterecoverFreezeDestoryChain()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1515_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = "";
        mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",ids);

//        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName.toLowerCase()), true);

        //冻结子链
        res = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
//        assertEquals(resp.contains(ledgerStateFreeze), true);
        assertThat(resp,anyOf(containsString(ledgerStateFreeze),containsString(ledgerStateFreeze2)));


        //解除子链
        res = mgToolCmd.recoverAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateFreeze), false);//子链可能默认不显示state


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);

        assertEquals("判断subLedger不为空", false,subLedger.isEmpty());

        //发送交易测试
        String Data="1521 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1 = store.CreateStore(Data);

        sleepAndSaveInfo(SLEEPTIME);

        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

        //销毁子链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);
    }
}
