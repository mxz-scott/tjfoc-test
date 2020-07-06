package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.utils.SubLedgerCmd;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMainSubChain_Perm {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    MgToolCmd mgToolCmd= new MgToolCmd();
    UtilsClass utilsClass = new UtilsClass();

    String noPerm ="not found";
    String noPerm2 ="no permission";
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();

    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";

    @BeforeClass
    public static void clearData()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.clearDataSetPerm999();
        beforeCondition.updatePubPriKey();
        beforeCondition.createAddresses();
        sleepAndSaveInfo(SLEEPTIME);
    }


    @Before
    public void beforeConfig() throws Exception {
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01.toLowerCase()+"\"")) {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01, " -t sm3",
                    " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01.toLowerCase()+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02.toLowerCase()+"\"")) {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02, " -t sm3",
                    " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02.toLowerCase()+"\""), true);
        }
    }


    @Test
    public void TC1661_1668_MainSubPermTest()throws Exception{

        //设置主链上sdk权限为0
        subLedger="";
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"0");
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        String tempResp = store.CreateStore("tc1660 no permission tx data").toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));

        //创建子链01 包含节点A、B、C
        String chainName="tc1661_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName;
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        tempResp = store.CreateStore("tc1661 no permission tx data").toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));


        //设置子链权限为1 即只允许发送存证 不允许其他操作
        subLedger=chainName;
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"211");
        sleepAndSaveInfo(SLEEPTIME);
        //获取子链权限列表检查是否为211
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[211]");

        //子链发送权限对应的接口以及检查无权限接口
        assertThat(store.CreateStore("tc1661 ledger with permission 211 tx data"),containsString("\"state\":200"));
        tempResp = store.GetHeight().toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));

        subLedger="";
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        tempResp = store.CreateStore("tc1661 no permission tx data").toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));


        //设置主链权限为999,确认主链sdk权限恢复
        subLedger="";
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), fullPerm);
        assertThat(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()),
                anyOf(containsString(fullPerm), containsString(fullPerm2)));
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,"tc1661 with permission 999 tx data2");
    }
    

    @Test
    public void TC1662_MainSubPermTest()throws Exception{

        //设置主链上sdk权限为236/251;余额查询以及注册归集地址权限
        subLedger="";
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"236,253");
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[236 253]");
        assertThat(multiSign.QueryZero(""),containsString("\"state\":200"));
        assertThat(multiSign.addIssueAddrs(ADDRESS1),
                anyOf(containsString("\"state\":200"),containsString("exist")));

        //创建子链01 包含节点A、B、C
        String chainName="tc1662_01"+ sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName;
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        String tempResp = store.CreateStore("tc1662 no permission tx data").toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));

        //设置子链权限为3,211 即只允许发送存证 不允许其他操作
        subLedger=chainName;
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"3,211");
        sleepAndSaveInfo(SLEEPTIME);
        //获取子链权限列表检查是否为3,211
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[3 211]");
        //子链发送权限对应的接口以及检查无权限接口
        assertThat(store.CreateStore("tc1662 ledger with permission3,211 tx data"),containsString("\"state\":200"));
        tempResp = store.GetHeight().toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));

        assertThat(store.GetBlockByHeight(0),containsString("\"state\":200"));

        tempResp = multiSign.QueryZero("").toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));
        tempResp = multiSign.addIssueAddrs(ADDRESS1).toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));
        //获取主链权限列表检查无变更
        log.info("Current subledger: "+subLedger);
        subLedger="";
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[236 253]");
        tempResp = store.CreateStore("tc1662 main no permission tx data").toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));
        assertThat(multiSign.QueryZero(""),containsString("\"state\":200"));
        assertThat(multiSign.addIssueAddrs(ADDRESS1),
                anyOf(containsString("\"state\":200"),containsString("exist")));


        //设置主链权限为999,确认主链sdk权限恢复
        subLedger="";
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), fullPerm);
        assertThat(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()),
                anyOf(containsString(fullPerm), containsString(fullPerm2)));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,"tc1662 with permission 999 tx data2");
    }

    @Test
    public void TC1663_MainSubPermTest()throws Exception{

        //设置主链上sdk权限为236/251;余额查询以及注册归集地址权限
        subLedger="";
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME);
        //检查主链权限列表
//        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), fullPerm);
        assertThat(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()),
                anyOf(containsString(fullPerm), containsString(fullPerm2)));
        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data"),containsString("\"state\":200"));
        assertThat(multiSign.QueryZero(""),containsString("\"state\":200"));
        assertThat(multiSign.addIssueAddrs(ADDRESS1),
                anyOf(containsString("\"state\":200"),containsString("exist")));

        //创建子链01 包含节点A、B、C
        String chainName="tc1663_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName;
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        String tempResp = store.CreateStore("tc1663 no permission tx data").toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));


        //设置子链权限为3,211
        subLedger=chainName;
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"3,211");
        sleepAndSaveInfo(SLEEPTIME*3/2);
        //获取子链权限列表检查是否为3,211
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[3 211]");
        //子链发送权限对应的接口以及检查无权限接口
        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data"),containsString("\"state\":200"));
        assertThat(store.GetBlockByHeight(0),containsString("\"state\":200"));
        tempResp = store.GetHeight().toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));

        tempResp = multiSign.QueryZero("").toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));

        tempResp = multiSign.addIssueAddrs(ADDRESS1).toLowerCase();
        assertThat(tempResp,anyOf(containsString(noPerm),containsString(noPerm2)));
        //获取主链权限列表检查无变更
        subLedger="";
//        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), fullPerm);
        assertThat(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()),
                anyOf(containsString(fullPerm), containsString(fullPerm2)));
        assertEquals(store.CreateStore("tc1663 main no permission tx data").contains("\"state\":200"),true);
        assertThat(multiSign.QueryZero(""),containsString("\"state\":200"));
        assertThat(multiSign.addIssueAddrs(ADDRESS1),
                anyOf(containsString("\"state\":200"),containsString("exist")));


        //设置主链权限为999,确认主链sdk权限恢复
        subLedger="";
        //mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999");
        //sleepAndSaveInfo(SLEEPTIME);
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,"tc1663 with permission 999 tx data2");
    }
}
