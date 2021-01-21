package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.SubLedgerCmd;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_Perm {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    MgToolCmd mgToolCmd= new MgToolCmd();
    UtilsClass utilsClass = new UtilsClass();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();



    @Before
    public void beforeConfig() throws Exception {
        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);
    }



    @Test
    public void PermTest01()throws Exception{

        //设置globalAppId1上sdk权限为999
        subLedger = globalAppId1;
        mgToolCmd.setPeerPerm(PEER1IP + ":" + PEER1RPCPort,utilsClass.getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME/2);

        //检查globalAppId1权限列表
        assertThat(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()),
                anyOf(containsString(fullPerm), containsString(fullPerm2)));
        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data"),containsString("\"state\":200"));


        //创建应用链01 包含节点A、B、C
        String chainName = "tc1663_01" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChainNoPerm(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        sleepAndSaveInfo(SLEEPTIME/2);
        tempLedgerId = subLedger;

        //检查可以获取应用链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //获取新建应用链权限列表指定sdk为空 测试发送交易无权限
        subLedger = tempLedgerId;
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        assertEquals(true,store.CreateStore("tc1663 no permission tx data").toLowerCase().contains(noPerm));

        //设置新建应用链权限为3,211
        mgToolCmd.setPeerPerm(PEER1IP + ":" + PEER1RPCPort,utilsClass.getSDKID(),"3,211");
        sleepAndSaveInfo(SLEEPTIME/2);
        //获取新建应用链权限列表检查是否为3,211
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[3 211]");
        //新建应用链发送权限对应的接口以及检查无权限接口
        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data"),containsString("\"state\":200"));
        assertThat(store.GetBlockByHeight(0),containsString("\"state\":200"));
        assertEquals(true,store.GetHeight().toLowerCase().contains(noPerm));


        //获取globalAppId1链权限列表检查无变更
        subLedger = globalAppId1;
        assertThat(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()),
                anyOf(containsString(fullPerm), containsString(fullPerm2)));

        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data2"),containsString("\"state\":200"));
        assertThat(store.GetBlockByHeight(0),containsString("\"state\":200"));
        assertThat(store.GetHeight(),containsString("\"state\":200"));

        subLedgerCmd.sendTxToMultiActiveChain("tc1663 with permission 999 tx data2",globalAppId1,globalAppId2);
    }


    @Test
    public void PermTest02()throws Exception{

        //设置globalAppId1上sdk权限为999
        subLedger = globalAppId1;
        mgToolCmd.setPeerPerm(PEER1IP + ":" + PEER1RPCPort,utilsClass.getSDKID(),"0");
        sleepAndSaveInfo(SLEEPTIME/2);

        //检查globalAppId1权限列表
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        assertEquals(true,store.CreateStore("tc1663 ledger with permission3,211 tx data").toLowerCase().contains(noPerm));
        assertEquals(true,store.GetHeight().toLowerCase().contains(noPerm));


        //创建应用链01 包含节点A、B、C
        String chainName = "tc1663_01" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",ids);

        sleepAndSaveInfo(SLEEPTIME/2);
        tempLedgerId = subLedger;

        //检查可以获取应用链列表
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //获取新建应用链权限列表
        subLedger = tempLedgerId;
        assertThat(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()),
                anyOf(containsString(fullPerm), containsString(fullPerm2)));
        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data"),containsString("\"state\":200"));

        //设置新建应用链权限为3,211
        mgToolCmd.setPeerPerm(PEER1IP + ":" + PEER1RPCPort,utilsClass.getSDKID(),"3,211");
        sleepAndSaveInfo(SLEEPTIME/2);
        //获取新建应用链权限列表检查是否为3,211
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[3 211]");
        //新建应用链发送权限对应的接口以及检查无权限接口
        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data"),containsString("\"state\":200"));
        assertThat(store.GetBlockByHeight(0),containsString("\"state\":200"));
        assertEquals(true,store.GetHeight().toLowerCase().contains(noPerm));


        //获取globalAppId1链权限列表检查无变更
        subLedger = globalAppId1;
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");

        assertEquals(true,store.CreateStore("tc1663 ledger with permission3,211 tx data2").toLowerCase().contains(noPerm));
        assertEquals(true,store.GetBlockByHeight(0).toLowerCase().contains(noPerm));
        assertEquals(true,store.GetHeight().toLowerCase().contains(noPerm));

        //设置回999全部权限
        mgToolCmd.setPeerPerm(PEER1IP + ":" + PEER1RPCPort,utilsClass.getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME/2);
        subLedgerCmd.sendTxToMultiActiveChain("tc1663 with permission 999 tx data2",globalAppId1,globalAppId2);
    }
}
