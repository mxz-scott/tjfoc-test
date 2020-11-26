package com.tjfintech.common.functionTest.mainAppChain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
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
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMainAppChain_Create_01 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();

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
        beforeCondition.clearDataSetPerm999();
    }


    /***
     * 链内节点*3 链外节点*1
     * @throws Exception
     */
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
//        peer.put("RpcPort",Integer.valueOf(PEER3RPCPort));

        listPeer.add(JSON.toJSONString(peer).replace("\"","\\\""));

        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \"" + glbChain01.toLowerCase() + "\"")) {
            mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01,
                    " -t sm3", " -w first", " -c raft",
                    ids, " -n \"" + listPeer.toString() + "\"");
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(
                    mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01.toLowerCase()+"\""),
                    true);
        }

        if(! resp.contains("\"name\": \""+glbChain02.toLowerCase()+"\"")) {
            mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02,
                    " -t sm3", " -w first", " -c raft",
                    ids," -n \"" + listPeer.toString() + "\"");
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(
                    mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02.toLowerCase()+"\""),
                    true);
        }
    }

    @Test
    public void TC1613_createWordMultiStr()throws Exception{
        //创建子链，-w "first word"
        String chainName = "tc1613_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String word =chainName+" first word";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z "+chainName," -t sm3",
                " -w \""+word+"\""," -c raft",ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName), true);

        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(res3.contains(word), true);
    }


    @Test
    public void TC1491_createNoCons()throws Exception{
        String chainName1="tc1491_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        //创建子链，共识算法不填写
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z "+chainName1,
                " -t sm3"," -w first ","",
                ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }
    @Test
    public void TC1489_createWordWithSpecial()throws Exception{
        String chainName1="tc1489_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="\"!@#~$%^&*()-=+/?><中文{}[]|\"";
        //创建子链，共识算法不填写
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w "+wordValue,"",ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }

    @Test
    public void TC1488_createWordWith79len()throws Exception{
        String chainName1="tc1488_"+sdf.format(dt)+ RandomUtils.nextInt(1000);

        //先检查系统中不存在以上待创建的子链
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res1.contains(chainName1), false);

        String wordValue ="1234567890123456789012345678901234567890123456789012345678901234567890123456789";
        //创建子链，共识算法不填写
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z "+chainName1," -t sm3",
                " -w "+wordValue,"",ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表中存在刚创建的两条子链 以及各个子链的共识算法为默认raft
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName1), true);

        String res3 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -z "+ chainName1);
        assertEquals(res3.contains("raft"), true);
    }

   //此用例在执行后进检查可以使用 不检查是否成功创建
    //移除支持 . 不再支持 . 20200420
   //20191106 规则测试有变更 首字母仅允许数字字母（大小写）
    @Test
    public void TC1472_CreateNameValid()throws Exception{

        //创建子链，名称为"1"
        String chainName1 = "1";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z " + chainName1,
                " -t sm3"," -w first word"," -c raft",
                ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res.contains("send transaction success"), true);

        //创建子链，名称为"A"
        String chainName2 = "A";
        String res1 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z " + chainName2,
                " -t sm3"," -w first word"," -c raft",
                ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res1.contains("send transaction success"), true);

        //创建子链，名称为"test"
        String chainName3 = "test";
        String res2 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z " + chainName3,
                " -t sm3"," -w first word"," -c raft",
                ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res2.contains("send transaction success"), true);

        //创建子链，名称为"a_Q123"
        String chainName4 = "a_Q123";
        String res3 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z " + chainName4,
                " -t sm3"," -w first word"," -c raft",
                ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res3.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);

        //检查子链列表
        String res6 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res6.contains("name"), true);
        assertEquals(res6.contains("\"name\": \""+chainName1+"\""), true);
        assertEquals(res6.contains("\"name\": \""+chainName2.toLowerCase()+"\""), true);
        assertEquals(res6.contains("\"name\": \""+chainName3.toLowerCase()+"\""), true);
        assertEquals(res6.contains("\"name\": \""+chainName4.toLowerCase()+"\""), true);
    }

    @Test
    public void TC1471_CreateAndCheck()throws Exception{
        //获取单个子链信息
        String res1 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -z "+glbChain01);
        assertEquals(res1.contains(glbChain01.toLowerCase()), true);
        assertEquals(res1.contains(id1), true);
        assertEquals(res1.contains(id2), true);
        assertEquals(res1.contains(id3), true);

        //获取系统所有子链信息
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(glbChain01.toLowerCase()), true);
        assertEquals(res2.contains(id1), true);
        assertEquals(res2.contains(id2), true);
        assertEquals(res2.contains(id3), true);
    }

    @Test
    public void TC1621_testTXforMainAppChains()throws Exception{
        //创建子链，包含两个节点
        String chainName2="05";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -z "+chainName2,
                " -t sm3"," -w first"," -c raft",
                ids, " -n \"" + listPeer.toString() + "\"");
        assertEquals(res.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2.toLowerCase()), true);

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
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2.toLowerCase()), true);

        String txHash1 = JSONObject.fromObject(response1).get("data").toString();
        String txHash3 = JSONObject.fromObject(response3).get("data").toString();

        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
    }

}