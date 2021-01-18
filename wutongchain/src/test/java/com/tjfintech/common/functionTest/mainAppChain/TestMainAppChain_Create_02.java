package com.tjfintech.common.functionTest.mainAppChain;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.SubLedgerCmd;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMainAppChain_Create_02 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    MgToolCmd mgToolCmd = new MgToolCmd();

    String stateDestroyed ="has been destroyed";

    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";

    String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    String id4 = getPeerId(PEER3IP,USERNAME,PASSWD);
    String ids = " -m "+ id1+","+ id2+","+ id3;
    List<String> listPeer = new ArrayList<>();

    @BeforeClass
    public static void clearData()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.clearDataSetPerm999(); //清空数据库 目的是让当前系统存在的子链少一些
    }

    @Before
    public void beforeConfig() throws Exception {
        HashMap peer = new HashMap();
        peer.put("ID",id4);
        peer.put("ShownName","testName");
        List inAddr = new ArrayList();
        List outAddr = new ArrayList();
        inAddr.add(ipv4 + PEER3IP + tcpProtocol + PEER3TCPPort);
        outAddr.add("");
        peer.put("InAddrs",inAddr);
        peer.put("OutAddrs",outAddr);
        peer.put("PeerType",2);
        //        peer.put("RpcPort",Integer.valueOf(PEER3RPCPort));;

        listPeer.add(JSON.toJSONString(peer).replace("\"","\\\""));

        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \"" + glbChain01.toLowerCase() + "\"")) {
            mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + glbChain01,
                    " -t sm3", " -w first", " -c raft",
                    ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(
                    mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01.toLowerCase()+"\""),
                    true);
        }

        if(! resp.contains("\"name\": \""+glbChain02.toLowerCase()+"\"")) {
            mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + glbChain02,
                    " -t sm3", " -w first", " -c raft",
                    ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(
                    mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02.toLowerCase()+"\""),
                    true);
        }
    }


    @Test
    public void TC1658_createAfterFreezedestroyAppChain()throws Exception{

        //创建子链01 包含节点A、B、C
        String chainName1="tc1658_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1.toLowerCase()), true);

        //冻结子链
        res = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1);
        assertEquals(resp.contains(ledgerStateFreeze), true);


        //解除子链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1);
        assertEquals(res.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1);
        assertEquals(resp.contains(ledgerStateDestroy), true);

        String chainName2="tc1658_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName2,
                " -t sm3"," -w first"," -c raft",ids);
        sleepAndSaveInfo(SLEEPTIME);
        String chainName3="tc1658_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName3,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2.toLowerCase()), true);
        assertEquals(resp.contains(chainName3.toLowerCase()), true);

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,"tc1656 tx data2");
    }

    @Test
    public void TC1702_TC1703_createSpecMainNameChain()throws Exception{

        //创建一个名称为main的子链（与链上主链标识一致）
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+"main"," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("exist"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 中无main子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains("\"name\": \"main\""), false);

        assertEquals(resp.contains("\"name\": \"wtchain\""), false);//wtchain 为节点配置文件中ledger字段后的内容

        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n wtchain");
        assertEquals(resp.contains("not exist"), true);


    }

    @Test
    public void TC1475_1493_createExistChain()throws Exception{

        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n "+ glbChain01);
        if(resp.contains("state"))  assertEquals(resp.contains(ledgerStateNormal), true);
        assertEquals(resp.contains("\"name\": \""+ glbChain01.toLowerCase() +"\""), true);
        assertEquals(resp.contains("\"hashType\": \"sm3\""), true);
        assertEquals(resp.contains("\"cons\": \"raft\""), true);
        assertEquals(resp.contains("\"word\": \"first\""), true);


        //创建一个已存在的活跃子链glbChain01
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+glbChain01,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("has exist"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链 确认异常操作后系统无异常
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,"1475 data");
    }

    @Test
    public void TC1476_createExistDestroyChain()throws Exception{

        //创建子链，包含三个节点
        String chainName="tc1476_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);


        //销毁一个已存在的活跃子链
        res = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -n "+chainName);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查可以获取子链列表 存在其他子链 确认异常操作后系统无异常
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("has exist"), true);
//        String txHash = res.substring(res.lastIndexOf(":")+1).trim();
//        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash)).getString("state"));

        String Data="1475 ledger tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);

        //向子链chainName发送交易
        subLedger=chainName;
        String response1 = store.CreateStore(Data);
        Data="1476 main tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        assertThat(response1, containsString(stateDestroyed));

        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,Data);
    }


    @Test
    public void TC1492_createUnsupportCons()throws Exception{
        //创建子链，共识算法为不支持的算法
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n 1",
                " -t sm3"," -w first"," -c test",ids);
        assertEquals(res.contains("unsupported"), true);

        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1614_createUnsupportHash()throws Exception{
        //创建子链，hash不支持的hash算法
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n 1"," -t s3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("unsupported"), true);

        //创建子链，hash不支持的hash算法
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n 1",
                " -t sh"," -w first"," -c raft",ids);
        assertEquals(res.contains("unsupported"), true);

        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1483_createSameId()throws Exception{
        String chainName="tc1483_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建子链，id重复
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName," -t sm3",
                " -w first"," -c raft"," -m "+id1+","+id1+","+id1);
        assertEquals(res.contains("id repeat"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(" \"name\": \""+chainName+"\""), false);
    }

    @Test
    public void TC1482_createInvalidSpiltor()throws Exception{
        String chainName1="tc1482_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建子链，id重复
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+";"+id2+";"+id3);
        assertEquals(res.contains("send transaction success"), false);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName1), false);
    }

    @Test
    public void TC1481_createErrorPeerid()throws Exception{
        String chainName1="tc1481_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建子链，id格式错误 非集群中的id
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft"," -m 1,"+id2+","+id3);
        assertEquals(res.contains("not found peerId"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+",1,"+id3);
        assertEquals(res.contains("not found peerId"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+","+id2+",1");
        assertEquals(res.contains("not found peerId"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1480_createNoPeerid()throws Exception{
        String chainName1="tc1480_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        //创建子链，id格式错误 非集群中的id
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft"," -m 1,"+id2+","+id3);
        assertEquals(res.contains("not exist"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+",1,"+id3);
        assertEquals(res.contains("not exist"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft"," -m "+id1+","+id2+",1");
        assertEquals(res.contains("not found peerId"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1611_createLongName()throws Exception{
        //长度24
        String chainName1="tc1611_12345678901234567";
        //创建子链子链名长度24
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        //长度25
        String chainName2="tc1611_123456789012345678";
        //创建子链子链子链名长度25
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName2," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);


        //长度26
        String chainName3="tc1611_1234567890123456789";
        //创建子链子链子链名长度25
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName3," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), false);

        //长度128
        String chainName4="tc1611_1234567890123456789011234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        //创建子链子链子链名长度25
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName4," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), false);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }

    @Test
    public void TC1486_TC1480_createParamCheck()throws Exception{

        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n 1",
                " -t sm3"," -w first"," -c ",ids);
        assertEquals(res.contains("unsupported"), true);

        //testcase 1486
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n 1",
                " -t sm3"," -w "," -c raft",ids);
        assertEquals(res.contains("err:"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n 1",
                " -t "," -w 566"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), false);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n ",
                " -t sm3"," -w 566"," -c raft",ids);
        assertEquals(res.contains("Invalid"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," ",
                " -t sm3"," -w 566"," -c raft",ids);
        assertEquals(res.contains("management addledger"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n test",
                " -t sm3"," -w 566"," -c raft"," -m ");
        assertEquals(res.contains("management addledger"), true);

        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n test",
                " -t sm3"," -w 566"," -c raft"," ");
        assertEquals(res.contains("requires at least two ids"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
    }


    @Test
    public void TC1478_creatInvalidName()throws Exception{
        //创建子链，子链名称非法
        String chainName1="#123";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("management addappledger"), true);


        String chainName2="a$";
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName2," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName3="1*";
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName3," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName4="Q-";
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName4," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName5=" ";
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName5," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName6="。";
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName6," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName7="、";
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName7," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        String chainName8="?";
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName8," -t sm3",
                " -w first"," -c raft",ids);
        assertEquals(res.contains("Invalid ledger name"), true);

        //创建子链，名称为"_" 不支持仅包含特殊字符的子链名
        String chainName9="_";
        String res3 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName9,
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res3.contains("Invalid ledger name"), true);

        //创建子链，名称为"." 不支持仅包含特殊字符的子链名
        String chainName10=".";
        String res4 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName10,
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res4.contains("Invalid ledger name"), true);

        //创建子链，名称为".a" 不支持以非字母数字开头的子链名
        String chainName11=".a";
        String res5 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName11,
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res5.contains("Invalid ledger name"), true);

        String chainName12="a_";
        String res6 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName12,
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res6.contains("success"), true);

        //创建子链，名称为"1.a" 不支持包含 .
        String chainName13 = "1.a";
        String res7 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName13,
                " -t sm3"," -w first word"," -c raft",ids);
        assertEquals(res7.contains("Invalid ledger name"), true);

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(" \"name\": \""+chainName12+"\""), false);
        assertEquals(res2.contains(" \"name\": \""+chainName11+"\""), false);
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
    public void TC1487_createWordWith81len()throws Exception{
        String chainName1="tc1487_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="123456789012345678901234567890123456789012345678901234567890123456789012345678901";
        //创建子链，word名称超过80
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1," -t sm3",
                " -w "+wordValue,"",ids);
        assertEquals(res.contains("Character Length Over 80"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), false);

    }

}
