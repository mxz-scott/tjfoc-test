package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.TestMgTool;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMainSubChain {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    TestMgTool testMgTool=new TestMgTool();
    Date dt=new Date();
    SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
    String noPerm="not found";
    String notSupport="not support service";
    String stateDestoryed ="has been destroyed";
    String stateFreezed ="not support service";

    boolean bExe=false;

    //String glbChain01= "glbCh1_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
    //String glbChain02= "glbCh2_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";

    @Before
    public void beforeConfig() throws Exception {
//        if(certPath!=""&& bReg==false) {
//            BeforeCondition bf = new BeforeCondition();
//            bf.updatePubPriKey();
//            bf.collAddressTest();
//
//            bReg=true;
//        }

        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01+"\"")) {
            createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01, " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
            createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02, " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
        }
    }


    @Test
    public void TC1661_1668MainSubPermTest()throws Exception{

        //设置主链上sdk权限为0
        subLedger="";
        testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"0");
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        assertEquals(store.CreateStore("tc1660 no permission tx data").toLowerCase().contains(noPerm),true);

        //创建子链01 包含节点A、B、C
        String chainName="tc1661_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName;
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(store.CreateStore("tc1661 no permission tx data").toLowerCase().contains(noPerm),true);


        //设置子链权限为1 即只允许发送存证 不允许其他操作
        subLedger=chainName;
        testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"211");
        sleepAndSaveInfo(SLEEPTIME);
        //获取子链权限列表检查是否为211
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[211]");

        //子链发送权限对应的接口以及检查无权限接口
        assertThat(store.CreateStore("tc1661 ledger with permission 211 tx data"),containsString("\"State\":200"));
        assertEquals(store.GetHeight().contains(noPerm),true);

        subLedger="";
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        assertEquals(store.CreateStore("tc1661 main no permission tx data").contains(noPerm),true);


        //设置主链权限为999,确认主链sdk权限恢复
        subLedger="";
        testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), fullPerm);
        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain("tc1661 with permission 999 tx data2");
    }

    //@Test
    public void test()throws Exception{
        for(int i=0;i < 50; i++){
            log.info("Current index " + i);
            String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
            assertEquals(resp.contains("name"), true);
            sleepAndSaveInfo(1000);
        }
    }

    @Test
    public void TC1662_MainSubPermTest()throws Exception{

        //设置主链上sdk权限为236/251;余额查询以及注册归集地址权限
        subLedger="";
        testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"236,253");
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[236 253]");
        assertThat(multiSign.QueryZero(""),containsString("\"State\":200"));
        assertThat(multiSign.addissueaddressRemovePri(ADDRESS1),containsString("\"State\":200"));

        //创建子链01 包含节点A、B、C
        String chainName="tc1662_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName;
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        assertEquals(store.CreateStore("tc1662 no permission tx data").toLowerCase().contains(noPerm),true);


        //设置子链权限为3,211 即只允许发送存证 不允许其他操作
        subLedger=chainName;
        testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"3,211");
        sleepAndSaveInfo(SLEEPTIME);
        //获取子链权限列表检查是否为3,211
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[3 211]");
        //子链发送权限对应的接口以及检查无权限接口
        assertThat(store.CreateStore("tc1662 ledger with permission3,211 tx data"),containsString("\"State\":200"));
        assertEquals(store.GetHeight().contains(noPerm),true);
        assertThat(store.GetBlockByHeight(0),containsString("\"State\":200"));
        assertEquals(multiSign.QueryZero("").toLowerCase().contains(noPerm),true);
        assertEquals(multiSign.addissueaddressRemovePri(ADDRESS1).toLowerCase().contains(noPerm),true);
        //获取主链权限列表检查无变更
        log.info("Current subledger: "+subLedger);
        subLedger="";
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[236 253]");
        assertEquals(store.CreateStore("tc1662 main no permission tx data").contains(noPerm),true);
        assertThat(multiSign.QueryZero(""),containsString("\"State\":200"));
        assertThat(multiSign.addissueaddressRemovePri(ADDRESS1),containsString("\"State\":200"));


        //设置主链权限为999,确认主链sdk权限恢复
        subLedger="";
        testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), fullPerm);
        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain("tc1662 with permission 999 tx data2");
    }

    @Test
    public void TC1663_MainSubPermTest()throws Exception{

        //设置主链上sdk权限为236/251;余额查询以及注册归集地址权限
        subLedger="";
        testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME);
        //检查主链权限列表
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), fullPerm);

        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data"),containsString("\"State\":200"));
        assertThat(multiSign.QueryZero(""),containsString("\"State\":200"));
        assertThat(multiSign.addissueaddressRemovePri(ADDRESS1),containsString("\"State\":200"));

        //创建子链01 包含节点A、B、C
        String chainName="tc1663_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName;
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(store.CreateStore("tc1663 no permission tx data").toLowerCase().contains(noPerm),true);


        //设置子链权限为3,211
        subLedger=chainName;
        testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"3,211");
        sleepAndSaveInfo(SLEEPTIME*3/2);
        //获取子链权限列表检查是否为3,211
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[3 211]");
        //子链发送权限对应的接口以及检查无权限接口
        assertThat(store.CreateStore("tc1663 ledger with permission3,211 tx data"),containsString("\"State\":200"));
        assertThat(store.GetBlockByHeight(0),containsString("\"State\":200"));
        assertThat(store.GetHeight(),containsString(noPerm));
        //assertEquals(store.GetHeight().contains(noPerm),true);
        assertThat(multiSign.QueryZero(""),containsString(noPerm));
        //assertEquals(multiSign.QueryZero("").toLowerCase().contains(noPerm),true);
        assertEquals(multiSign.addissueaddressRemovePri(ADDRESS1).toLowerCase().contains(noPerm),true);

        //获取主链权限列表检查无变更
        subLedger="";
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), fullPerm);
        assertEquals(store.CreateStore("tc1663 main no permission tx data").contains("\"State\":200"),true);
        assertThat(multiSign.QueryZero(""),containsString("\"State\":200"));
        assertThat(multiSign.addissueaddressRemovePri(ADDRESS1),containsString("\"State\":200"));


        //设置主链权限为999,确认主链sdk权限恢复
        subLedger="";
        //testMgTool.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"999");
        //sleepAndSaveInfo(SLEEPTIME);
        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain("tc1663 with permission 999 tx data2");
    }

    @Test
    public void TC1658_createAfterFreezeDestorySubChain()throws Exception{

        //创建子链01 包含节点A、B、C
        String chainName1="tc1658_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        //冻结子链
        res = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        assertEquals(resp.contains("\"state\": \"Freeze\""), true);


        //解除子链
        res = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1);
        assertEquals(resp.contains("\"state\": \"Destory\""), true);

        String chainName2="tc1658_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2,
                " -t sm3"," -w first"," -c raft",ids);
        sleepAndSaveInfo(SLEEPTIME);
        String chainName3="tc1658_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain("tc1656 tx data2");
    }


    @Test
    public void TC1515_1514_1648_recoverFreezeChain03()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1515_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //冻结子链
        res = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Freeze\""), true);


        //解除子链
        res = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Normal\""), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
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
        sendTxToMainActiveChain(Data);
    }


    @Test
    public void TC1516_recoverNormChain02()throws Exception{

//        //创建子链，包含三个节点
//        String chainName="tc1516_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
//        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
//        assertEquals(res.contains("send transaction success"), true);
//
//        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
//        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);


        //解除子链
        String res = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+glbChain01);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+glbChain01);
        assertEquals(resp.contains("\"state\": \"Normal\""), true);


        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1521 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
//        //向子链chainName发送交易
//        subLedger=chainName;
//        String response1 = store.CreateStore(Data);
//        sleepAndSaveInfo(SLEEPTIME*2);
//        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
//        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);

    }

    @Test
    public void TC1517_recoverDestoryChain01()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1517_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //销毁子链
        res = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Destory\""), true);


        //解除销毁子链
        res = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Destory\""), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1517 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestoryed));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }


    @Test
    public void TC1522_destoryChain04()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1522_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //冻结子链
        res = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Freeze\""), true);


        //解除冻结子链
        res = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Normal\""), true);

        //销毁一个被冻结子链
        res = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Destory\""), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1522 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestoryed));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }

    @Test
    public void TC1521_destoryChain03()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1521_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //冻结子链
        res = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Freeze\""), true);

        //销毁一个被冻结子链
        res = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Destory\""), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1521 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString(stateDestoryed));


        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }

    @Test
    public void TC1622_destoryChain02()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1622_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        //第一次销毁一个已存在的活跃子链
        res = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Destory\""), true);


        //再次销毁已被销毁的子链
        res = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Destory\""), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);


        //发送交易测试
        String Data="1622 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1,containsString(stateDestoryed));




        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }


    @Test
    public void TC1518_1519_1496_1646_destoryChain01()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1518_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //销毁一个已存在的活跃子链
        res = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查被销毁子链状态正确
        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Destory\""), true);
        assertEquals(resp.contains("\"name\": \""+chainName+"\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);

        String Data="1518 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
        storeTypeSupportCheck("destory");

        Data="1518 ledger2 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
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
        else if(type.toLowerCase()=="destory"){
            notSupport=stateDestoryed;
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
        assertEquals(false,store.GetApiHealth().contains(notSupport));
        assertEquals(bCheck2,store.GetStorePost(txHash2,PRIKEY1).contains(notSupport));
        assertEquals(bCheck2,store.GetBlockByHash(blockHash).contains(notSupport));
        assertEquals(false,store.GetPeerList().contains(notSupport));
        assertEquals(bCheck1,store.CreatePrivateStore("test1",map).contains(notSupport));

        assertEquals(bCheck1,store.CreateStore("test2").contains(notSupport));
//        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(bCheck1,store.SynCreateStore(SHORTMEOUT,"test3").contains(notSupport));
        assertEquals(bCheck1,store.SynCreateStore(SHORTMEOUT,"test4",PUBKEY1).contains(notSupport));




    }

    //    //@Test
//    public void test1() throws Exception{
//        subLedger="tc1510_20190704776";
//        storeTypeSupportCheck("freeze");
//        subLedger="tc1518_20190704162";
//        storeTypeSupportCheck("destory");
//    }
    @Test
    public void TC1702_TC1703_createSpecMainNameChain()throws Exception{

        //创建一个名称为main的子链（与链上主链标识一致）
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+"main"," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("exist"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 中无main子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains("\"name\": \"main\""), false);

        assertEquals(resp.contains("\"name\": \"wtchain\""), false);//wtchain 为节点配置文件中ledger字段后的内容

        resp = getSubChain(PEER1IP,PEER1RPCPort," -z wtchain");
        assertEquals(resp.contains("not exist"), true);


    }

    @Test
    public void TC1475_1493_createExistChain()throws Exception{

        String resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+ glbChain01);
        assertEquals(resp.contains("\"state\": \"Normal\""), true);
        assertEquals(resp.contains("\"name\": \""+ glbChain01 +"\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);


        //创建一个已存在的活跃子链glbChain01
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+glbChain01," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链 确认异常操作后系统无异常
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain("tc1475 data");
    }

    @Test
    public void TC1476_createExistDestoryChain()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1476_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);


        //销毁一个已存在的活跃子链
        res = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表 存在其他子链 确认异常操作后系统无异常
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);
        String txHash = res.substring(res.lastIndexOf(":")+1).trim();
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash)).getString("State"));

        String Data="1475 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        Data="1476 main tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        assertThat(response1, containsString(stateDestoryed));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }

    @Test
    public void TC1497_1536_getNoExistChain()throws Exception{

        //子链
        String chainName="tc1497_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(glbChain01), true);

        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("subledger not exist"), true);


        //检查可以获取子链列表 存在其他子链 确认异常操作后系统无异常
        resp = getSubChain(PEER1IP,PEER1RPCPort,"");

        assertEquals(resp.contains("name"), true);

        String Data="1475 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向不存在的子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        assertThat(response1, containsString("doesn't exist"));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }

    public void sendTxToMainActiveChain(String data)throws Exception{
        //检查可以执行获取所有子链信息命令
        assertEquals(getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response1 = store.CreateStore(data);

        subLedger=glbChain02;
        String response2 = store.CreateStore(data);

        //向主链发送交易
        subLedger="";
        String response3 = store.CreateStore(data);

        sleepAndSaveInfo(SLEEPTIME*2);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();

        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

    }


    @Test
    public void TC1494_1495_getFreezeChain()throws Exception{
        String chainName="tc1494_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建一个子链
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        res = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);

        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Freeze\""), true);
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
        sendTxToMainActiveChain(Data);

        //查看恢复子链后的子链状态并向子链发送交易
        res = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);

        resp = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp.contains("\"state\": \"Normal\""), true);
        assertEquals(resp.contains("\"name\": \""+chainName+"\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);


        //向子链chainName发送交易
        String txHash2 = JSONObject.fromObject(store.CreateStore(Data)).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }


    @Test
    public void TC1609_recoverRecoverChains()throws Exception{

        //创建子链
        String chainName="tc1609_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3",
                " -w first"," -c raft"," -m "+id1+","+id2);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName), true);

        //冻结子链
        String respon = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);

        //子链信息检查
        String res3 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res3.contains("\"state\": \"Freeze\""), true);


        //恢复冻结子链 连续两次恢复
        String respon1 = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);
        respon1 = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);

        //子链信息检查
        String res4 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res4.contains("\"state\": \"Normal\""), true);

        //确认恢复后再次恢复
        respon1 = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);
        res4 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res4.contains("\"state\": \"Normal\""), true);

        String Data="1609 ledger1 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
        String txHash1 = JSONObject.fromObject(store.CreateStore(Data)).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));


        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }



    @Test
    public void TC1510_1647_freezeSubChains()throws Exception{
        //创建子链，包含两个节点
        String chainName="tc1510_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft"," -m "+id1+","+id2);
        assertEquals(res.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName), true);

        //冻结前发送一笔交易
        subLedger=chainName;
        String response = store.CreateStore("tttttt");
        sleepAndSaveInfo(SLEEPTIME);

        //冻结子链
        String respon = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        sleepAndSaveInfo(SLEEPTIME);


        //检查可以获取子链列表
        String res3 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res3.contains("\"state\": \"Freeze\""), true);


        String Data="1510 ledger1 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName2发送交易
        subLedger=chainName;
//        sleepAndSaveInfo(SLEEPTIME);
        String response1 = store.CreateStore(Data);


        //检查可以获取子链列表
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        assertThat(response1, containsString(stateFreezed));
        storeTypeSupportCheck("freeze");
        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }

    @Test
    public void TC1509_freezeMultiSubChains()throws Exception{
        //创建子链，包含两个节点
        String chainName2="tc1509_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2," -t sm3"," -w first"," -c raft"," -m "+id1+","+id2);
        assertEquals(res.contains("send transaction success"), true);

        //创建子链，包含三个节点
        String chainName3="tc1509_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);

        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2), true);
        assertEquals(res2.contains(chainName3), true);

        //冻结两条子链
        String respon = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2);
        String respon2 = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3);
        sleepAndSaveInfo(SLEEPTIME);


        //检查可以获取子链列表
        String res3 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2);
        assertEquals(res3.contains("\"state\": \"Freeze\""), true);
        String res4 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3);
        assertEquals(res4.contains("\"state\": \"Freeze\""), true);


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
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);

        subLedger=chainName2;
//        sleepAndSaveInfo(SLEEPTIME);
        assertThat(response1, containsString(stateFreezed));

        subLedger=chainName3;
        assertThat(response2, containsString(stateFreezed));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);

    }

    @Test
    public void TC1511_freezeFrozenChain()throws Exception{

        //待发送子链名
        String chainName="tc1511_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        String Data="1511 ledger2 tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //1.冻结一个子链chainName
        String respon = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon, containsString("send transaction success"));
        sleepAndSaveInfo(SLEEPTIME);

        //2.查询子链状态为冻结
        String resp2 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp2.contains("\"state\": \"Freeze\""), true);

        subLedger=chainName;
        //3.冻结后发送一笔存证交易 应该无法上链
//        sleepAndSaveInfo(SLEEPTIME);
        String response10 = store.CreateStore(Data);
        assertThat(response10, containsString(stateFreezed));


        //5.再次冻结已冻结的子链
        String respon1 = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon1, containsString("send transaction success"));
        sleepAndSaveInfo(SLEEPTIME);
        //6.查询子链状态为冻结
        String resp21 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(resp21.contains("\"state\": \"Freeze\""), true);

        subLedger=chainName;
        //7.冻结后发送一笔存证交易 应该无法上链
        String response11 = store.CreateStore(Data);
        assertThat(response11, containsString(stateFreezed));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }

    @Test
    public void TC1512_freezeNoExistChain()throws Exception{

        //待发送子链名
        String chainName="tc1512_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(glbChain01), true);

        String Data="1512 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //解除一个不存在的子链chainName
        String respon = freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon, containsString(" subledger not exist"));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }

    @Test
    public void TC1520_destoryNoExistChain()throws Exception{

        //待发送子链名
        String chainName="tc1520_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(glbChain01), true);

        String Data="1520 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //销毁一个不存在的子链chainName
        String respon = destorySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon, containsString(" subledger not exist"));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }


    @Test
    public void TC1585_recoverNoExistChain()throws Exception{

        //待发送子链名
        String chainName="tc1585_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), false);
        assertEquals(resp.contains(glbChain01), true);

        String Data="1585 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //恢复一个不存在的子链chainName
        String respon = recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertThat(respon, containsString(" subledger not exist"));

        //向子链glbChain01/glbChain02和主链发送交易
        sendTxToMainActiveChain(Data);
    }

    @Test
    public void TC1621_testTXforMainSubChains()throws Exception{
        //创建子链，包含两个节点
        String chainName2="05";
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2," -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
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
        String resp = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);

        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();

        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
    }




    //------------------------------------------------------------------------------------------------------------------------
    @Test
    public void TC1513_freezeEmptyName()throws Exception{
        assertEquals(freezeSubChain(PEER1IP,PEER1RPCPort,"").contains("management freezeledger"), true);

        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1613_createWordMultiStr()throws Exception{
        //创建子链，-w "first word"
        String chainName = "tc1613_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String word =chainName+" first word";
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3",
                " -w \""+word+"\""," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName), true);

        String res3 = getSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res3.contains(word), true);
    }

    @Test
    public void TC1492_createUnsupportCons()throws Exception{
        //创建子链，共识算法为不支持的算法
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z 1"," -t sm3"," -w first"," -c test",ids);
        assertEquals(res.contains("unsupported"), true);

        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1614_createUnsupportHash()throws Exception{
        //创建子链，hash不支持的hash算法
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z 1"," -t s3"," -w first"," -c raft",ids);
        assertEquals(res.contains("unsupported"), true);

        //创建子链，hash不支持的hash算法
        res = createSubChain(PEER1IP,PEER1RPCPort," -z 1"," -t sh"," -w first"," -c raft",ids);
        assertEquals(res.contains("unsupported"), true);

        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1483_createSameId()throws Exception{
        String chainName="tc1483_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建子链，id重复
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3",
                " -w first"," -c raft"," -m "+id1+","+id1+","+id1);
        assertEquals(res.contains("id repeat"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(" \"name\": \""+chainName+"\""), false);
    }

    @Test
    public void TC1482_createInvalidSpiltor()throws Exception{
        String chainName1="tc1482_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建子链，id重复
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+";"+id2+";"+id3);
        assertEquals(res.contains("send transaction success"), false);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName1), false);
    }

    @Test
    public void TC1481_createErrorPeerid()throws Exception{
        String chainName1="tc1481_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建子链，id格式错误 非集群中的id
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft"," -m 1,"+id2+","+id3);
        assertEquals(res.contains("not found peerId"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+",1,"+id3);
        assertEquals(res.contains("not found peerId"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+","+id2+",1");
        assertEquals(res.contains("not found peerId"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1480_createNoPeerid()throws Exception{
        String chainName1="tc1480_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建子链，id格式错误 非集群中的id
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft"," -m 1,"+id2+","+id3);
        assertEquals(res.contains("not found peerId"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+",1,"+id3);
        assertEquals(res.contains("not found peerId"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+","+id2+",1");
        assertEquals(res.contains("not found peerId"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1611_creatLongName()throws Exception{
        //长度24
        String chainName1="tc1611_12345678901234567";
        //创建子链子链名长度24
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        //长度25
        String chainName2="tc1611_123456789012345678";
        //创建子链子链子链名长度25
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);


        //长度26
        String chainName3="tc1611_1234567890123456789";
        //创建子链子链子链名长度25
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), false);

        //长度128
        String chainName4="tc1611_1234567890123456789011234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        //创建子链子链子链名长度25
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName4," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), false);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }


    @Test
    public void TC1478_creatInvalidName()throws Exception{
        //创建子链，子链名称非法
        String chainName1="#123";
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("management addledger"), true);


        String chainName2="a$";
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName2," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName3="1*";
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName3," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName4="Q-";
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName4," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName5=" ";
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName5," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName6="。";
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName6," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName7="、";
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName7," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName8="?";
        res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName8," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        //创建子链，名称为"_" 不支持仅包含特殊字符的子链名
        String chainName9="_";
        String res3 = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName9," -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res3.contains("Invalid ledger name"), true);

        //创建子链，名称为"." 不支持仅包含特殊字符的子链名
        String chainName10=".";
        String res4 = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName10," -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res4.contains("Invalid ledger name"), true);


        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(" \"name\": \""+chainName10+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName9+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName8+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName7+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName6+"\""), false);
        assertEquals(res2.contains("\"name\": \""+chainName5+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName4+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName3+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName2+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName1+"\""), false);

    }

    @Test
    public void TC1486_TC1480_createParamCheck()throws Exception{

        String res = createSubChain(PEER1IP,PEER1RPCPort," -z 1"," -t sm3"," -w first"," -c ",ids);
        assertEquals(res.contains("unsupported"), true);

        //testcase 1486
        res = createSubChain(PEER1IP,PEER1RPCPort," -z 1"," -t sm3"," -w "," -c raft",ids);
        assertEquals(res.contains("err:"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z 1"," -t "," -w 566"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), false);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z "," -t sm3"," -w 566"," -c raft",ids);
        assertEquals(res.contains("Invalid"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," "," -t sm3"," -w 566"," -c raft",ids);
        assertEquals(res.contains("management addledger"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z test"," -t sm3"," -w 566"," -c raft"," -m ");
        assertEquals(res.contains("management addledger"), true);

        res = createSubChain(PEER1IP,PEER1RPCPort," -z test"," -t sm3"," -w 566"," -c raft"," ");
        assertEquals(res.contains("requires at least two ids"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1491_createNoCons()throws Exception{
        String chainName1="tc1491_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        //创建子链，共识算法不填写
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3"," -w first ","",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = getSubChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }
    @Test
    public void TC1489_createWordWithSpecial()throws Exception{
        String chainName1="tc1489_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="\"!@#~$%^&*()-=+/?><中文{}[]|\"";
        //创建子链，共识算法不填写
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w "+wordValue,"",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = getSubChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }

    @Test
    public void TC1488_createWordWith79len()throws Exception{
        String chainName1="tc1488_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="1234567890123456789012345678901234567890123456789012345678901234567890123456789";
        //创建子链，共识算法不填写
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w "+wordValue,"",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = getSubChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }

    @Test
    public void TC1487_createWordWith81len()throws Exception{
        String chainName1="tc1487_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="123456789012345678901234567890123456789012345678901234567890123456789012345678901";
        //创建子链，word名称超过80
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w "+wordValue,"",ids);
        assertEquals(res.contains("Character Length Over 80"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), false);

    }


    //此用例在执行后进检查可以使用 不检查是否成功创建
    //@Test
    public void TC1472_CreateNameValid()throws Exception{

        //创建子链，名称为"1"
        String res = createSubChain(PEER1IP,PEER1RPCPort," -z 1"," -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);
        String txHash = res.substring(res.lastIndexOf(":")+1).trim();

        //创建子链，名称为"A"
        String res1 = createSubChain(PEER1IP,PEER1RPCPort," -z A"," -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res1.contains("send transaction success"), true);
        String txHash1 = res.substring(res1.lastIndexOf(":")+1).trim();

        //创建子链，名称为"test"
        String res2 = createSubChain(PEER1IP,PEER1RPCPort," -z test"," -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res2.contains("send transaction success"), true);
        String txHash2 = res.substring(res2.lastIndexOf(":")+1).trim();


        //创建子链，名称为"_a"
        String res3 = createSubChain(PEER1IP,PEER1RPCPort," -z _a"," -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res3.contains("send transaction success"), true);
        String txHash3 = res.substring(res3.lastIndexOf(":")+1).trim();

        //创建子链，名称为"."
        String res4 = createSubChain(PEER1IP,PEER1RPCPort," -z .a"," -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res4.contains("send transaction success"), true);
        String txHash4 = res.substring(res4.lastIndexOf(":")+1).trim();

        //创建子链，名称为"_1aZ."
        String res5 = createSubChain(PEER1IP,PEER1RPCPort," -z _1aZ."," -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res5.contains("send transaction success"), true);
        String txHash5 = res.substring(res5.lastIndexOf(":")+1).trim();

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft,确认可以查到数据
        String res6 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res6.contains("name"), true);
    }

    @Test
    public void TC1471_CreateAndCheck()throws Exception{
        //获取单个子链信息
        String res1 = getSubChain(PEER1IP,PEER1RPCPort," -z "+glbChain01);
        assertEquals(res1.contains(glbChain01), true);
        assertEquals(res1.contains(id1), true);
        assertEquals(res1.contains(id2), true);
        assertEquals(res1.contains(id3), true);

        //获取系统所有子链信息
        String res2 = getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(glbChain01), true);
        assertEquals(res2.contains(id1), true);
        assertEquals(res2.contains(id2), true);
        assertEquals(res2.contains(id3), true);
    }

    /*
     * 创建子链
     * */
    public String createSubChain(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                 String firstBlockInfoParam,String consensusParam,String peeridsParam)throws Exception{
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String mainCmd =" addledger ";
        String cmd1="cd "+ ToolPATH + ";./" + ToolTPName
                + mainCmd + " -p "+ rpcPort + chainNameParam + hashTypeParam + firstBlockInfoParam + consensusParam + peeridsParam;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));

        if(StringUtils.join(stdout,"\n").contains("transaction success")) {
            sleepAndSaveInfo(SLEEPTIME*2);
            subLedger = chainNameParam.trim().split(" ")[1];
            log.info("**************  set permission 999 for " + subLedger);
            String resp = testMgTool.setPeerPerm(PEER1IP + ":" + PEER1RPCPort, getSDKID(), "999");
            //assertEquals(false,resp.contains("err"));
            subLedger = "";
        }
        return StringUtils.join(stdout,"\n");
    }

    public String createSubChainNoPerm(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                       String firstBlockInfoParam,String consensusParam,String peeridsParam)throws Exception{
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String mainCmd =" addledger ";
        String cmd1="cd "+ ToolPATH + ";./" + ToolTPName
                + mainCmd + " -p "+ rpcPort + chainNameParam + hashTypeParam + firstBlockInfoParam + consensusParam + peeridsParam;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    /*
     * 获取子链信息 包括所有子链及单个子链
     * */
    public String getSubChain(String shellIP,String rpcPort,String chainNameParam){
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String mainCmd =" getledger ";
        String cmd1="cd "+ ToolPATH + ";./" + ToolTPName + mainCmd + " -p "+ rpcPort + chainNameParam;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    /*
     * 冻结子链
     * */
    public String freezeSubChain(String shellIP,String rpcPort,String chainNameParam){
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String mainCmd =" freezeledger ";
        String cmd1="cd "+ ToolPATH + ";./" + ToolTPName + mainCmd + " -p "+ rpcPort + chainNameParam;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    /*
     * 恢复子链
     * */
    public String recoverSubChain(String shellIP,String rpcPort,String chainNameParam){
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String mainCmd =" recoverledger ";
        String cmd1="cd "+ ToolPATH + ";./" + ToolTPName + mainCmd + " -p "+ rpcPort + chainNameParam;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }

    /*
     * 销毁子链
     * */
    public String destorySubChain(String shellIP,String rpcPort,String chainNameParam){
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String mainCmd =" destoryledger ";
        String cmd1="cd "+ ToolPATH + ";./" + ToolTPName + mainCmd + " -p "+ rpcPort + chainNameParam;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        return StringUtils.join(stdout,"\n");
    }
//    @Test
//    public void test()throws Exception{
//        getSubChainInfoFromAll(PEER1IP,PEER1RPCPort,"1","state");
//    }

    public String getSubChainInfoFromAll(String shellIP,String rpcPort,String chainName,String Keyword){
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String mainCmd =" getledger ";
        String cmd1="cd "+ ToolPATH + ";./" + ToolTPName + mainCmd + " -p "+ rpcPort;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        stdout.remove(0);//删除版本号信息行
        stdout.remove(0);//删除=================行
        String response =StringUtils.join(stdout,"");
//        response=response.replaceAll("\t","");
        JSONObject jsonObject = JSONObject.fromObject(response);
        String ledgerNo = jsonObject.getString("number");
        JSONArray ledMemList = jsonObject.getJSONArray("subLedgers");

        boolean bExist=false;
        String value="";
        for(int i =0;i<ledMemList.size();i++)
        {
            //log.info(ledMemList.getString(i));
            if(ledMemList.getString(i).contains("\"name\":\""+ chainName +"\""))
            {
                bExist=true;
                log.info("Get subChain:"+chainName+" OK");
                log.info(ledMemList.getString(i));
                value=JSONObject.fromObject(ledMemList.getString(i)).getString(Keyword);
                log.info("Keyword " + Keyword + ": " + value);
            }
        }
        return value;

    }

}
