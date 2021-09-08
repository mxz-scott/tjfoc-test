package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetHashTypeSHA256;
import com.tjfintech.common.functionTest.Conditions.SetHashTypeSM3;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.ids;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_Z_DiffHashType {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    SoloSign soloSign = testBuilder.getSoloSign();
    BeforeCondition beforeCondition = new BeforeCondition();

    MgToolCmd mgToolCmd =new MgToolCmd();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

//    @BeforeClass
    public static void clearPeerDB()throws Exception{
        UtilsClass utilsClass = new UtilsClass();
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB);
        //重启SDK
        utilsClass.setAndRestartSDK();
    }

    @Before
    public void clearData()throws Exception{
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB);
        //重启SDK
        utilsClass.setAndRestartSDK();
    }

    @Test
    public void HashType_sdkSM3_AppChainSHA256()throws Exception{
        //sdk使用sm3 （应用链sha256） 设置前清空节点及sdk数据
        SetHashTypeSM3 setSM3part= new SetHashTypeSM3();
        setSM3part.setHashsm3();
        Thread.sleep(SLEEPTIME);
        //创建应用链使用 sha256
        String chainName = "tc1649_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sha256"," -w first"," -c raft",
                ids);
        sleepAndSaveInfo(SLEEPTIME/2);

        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //向应用链发送交易
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("500",JSONObject.fromObject(response1).getString("state"));  //确认可以发送成功

        //创建新的应用链 hash与sdk一致
        chainName = "testHash" + Random(6);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",
                ids);

        sleepAndSaveInfo(SLEEPTIME/2);

        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //向应用链发送交易
        response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));  //确认可以发送成功
    }

    @Test
    public void HashType_sdkSHA256_AppChainSM3()throws Exception{
        //应用链 sm3 sdk sha256 设置前清空节点及sdk数据
        SetHashTypeSHA256 setHashTypeSHA256= new SetHashTypeSHA256();
        setHashTypeSHA256.setHashSHA256();

        Thread.sleep(SLEEPTIME);
        //创建应用链使用 sm3
        String chainName = "tc1649_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sm3"," -w first"," -c raft",
                ids);
        sleepAndSaveInfo(SLEEPTIME/2);

        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //向应用链发送交易
        String response1 = store.CreateStore("tc1649 data");
        assertEquals("500",JSONObject.fromObject(response1).getString("state"));  //确认可以发送成功

        //创建新的应用链 hash与sdk一致
        chainName = "testHash2" + Random(6);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName,
                " -t sha256"," -w first"," -c raft",
                ids);

        sleepAndSaveInfo(SLEEPTIME/2);

        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);

        //向应用链发送交易
        response1 = store.CreateStore("tc1649 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));  //确认可以发送成功
    }



    @AfterClass
    public static void resetPeerAndSDK()throws  Exception {
        //sdk、管理工具hashtype为SM3
        SetHashTypeSM3 setSM3 = new SetHashTypeSM3();
        setSM3.setHashsm3();
    }
}
