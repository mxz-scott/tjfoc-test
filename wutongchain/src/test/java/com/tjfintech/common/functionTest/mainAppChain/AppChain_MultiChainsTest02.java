package com.tjfintech.common.functionTest.mainAppChain;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
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
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.globalAppId1;
import static com.tjfintech.common.utils.UtilsClassApp.globalAppId2;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_MultiChainsTest02 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    MgToolCmd mgToolCmd = new MgToolCmd();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();


    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";
    String noPerm="no permission";

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
//        beforeCondition.createAdd();
        sleepAndSaveInfo(SLEEPTIME);
    }

    @Before
    public void beforeConfig() throws Exception {
        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);
    }

    @Test
    public void TC1555_sendTxToMultiChains()throws Exception{
        String Data ="tc1555 ledger tx data";
        String chainName="tc1555";

        ArrayList<String> hashList = new ArrayList<>();

        String mgResp = "";
        for(int i=1;i<10;i++)
        {
            mgResp = mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chainName+i,
                    " -t sm3", " -w first", " -c raft", ids);
        }
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(mgResp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);
        sleepAndSaveInfo(SLEEPTIME);
        //确认所有子链均存在
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        for(int i=1;i<10;i++){
            assertEquals(resp.contains("\"name\": \""+chainName+i+"\""), true);
        }

        //向新创建的所有子链发送交易
        for(int i=1;i<10;i++)
        {
            subLedger=chainName+i;
            String response = store.CreateStore(Data);
            String txHash = commonFunc.getTxHash(response,utilsClass.sdkGetTxHashType21);
            hashList.add(txHash);
        }


        subLedger="";
        String response10 = store.CreateStore(Data);
        String txHash10 = commonFunc.getTxHash(response10,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME*3);

        //检查向新创建的所有子链发送交易均可以查询到
        for(int i=1;i<10;i++)
        {
            subLedger=chainName+i;
            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hashList.get(i-1))).getString("state"));
        }
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash10)).getString("state"));

    }

    @Test
    public void TC1660_1666_1667_createAppChain()throws Exception{

        //设置主链上sdk权限为1,2,3,4,5,6,7,8,9,10
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"1,2,3,4,5,6,7,8,9,10");
        sleepAndSaveInfo(SLEEPTIME);
        subLedger="";
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[1 2 3 4 5 6 7 8 9 10]");
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

        //创建子链01 包含节点A、B、C
        String chainName1="tc1666_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChainNoPerm(PEER1IP,PEER1RPCPort," -n "+chainName1,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(res,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);

        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName1;
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        assertThat(store.CreateStore("tc1666 no permission tx data").toLowerCase(),containsString(noPerm));


        subLedger="";
        //设置主链上sdk权限为0
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"0");
        sleepAndSaveInfo(SLEEPTIME);

        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        assertThat(store.CreateStore("tc1660 no permission tx data").toLowerCase(),containsString(noPerm));

        //创建子链01 包含节点A、B、C
        String chainName2="tc1660_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChainNoPerm(PEER1IP,PEER1RPCPort," -n "+chainName2,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(res,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);

        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName2;
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        assertThat(store.CreateStore("tc1660 no permission tx data").toLowerCase(),containsString(noPerm));


        subLedger="";
        //设置主链上sdk权限为999
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME*2);
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), fullPerm);
        assertThat(store.CreateStore("tc1660 no permission tx data"),containsString("\"state\":200"));

        //创建子链01 包含节点A、B、C
        String chainName3="tc1660_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChainNoPerm(PEER1IP,PEER1RPCPort," -n "+chainName3,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(res,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);

        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName3), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName3;
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        assertThat(store.CreateStore("tc1666 no permission tx data").toLowerCase(),containsString(noPerm));

        subLedger="";
        //设置主链权限为999,确认主链sdk权限恢复
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(utilsClass.getCertainPermissionList(PEER1IP,PEER1RPCPort,utilsClass.getSDKID()), fullPerm);
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,"tc1656 tx data2");
    }


    @Test
    public void TC1534_sendTxTest01()throws Exception{
        //存在主/子链*2
        //1.先向主链发送交易并可以成功查询到 再向子链发送交易并可以成功查询到
        String Data = "tc1534 01 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);


        //向主链发送交易
        subLedger="";
        int height =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response1 = store.CreateStore(Data);
        sleepAndSaveInfo(SLEEPTIME);
        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));

        //向子链glbChain01发送交易
        subLedger=glbChain01;
        int height1 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response2 = store.CreateStore(Data);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height1+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));

        //向子链glbChain02发送交易
        subLedger=glbChain02;
        int height2 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response3 = store.CreateStore(Data);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height2+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));


    }

    @Test
    public void TC1534_sendTxTest02()throws Exception{
        //存在主/子链*2
        //1.先向子链发送交易并可以成功查询到 再向主链发送交易并可以成功查询到 最后向另一条子链发送交易并查询
        String Data = "tc1534 02 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);


        //向子链glbChain01发送交易
        subLedger=glbChain01;
        int height1 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response2 = store.CreateStore(Data);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);
        commonFunc.sdkCheckTxOrSleep(txHash2,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height1+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));


        //向主链发送交易
        subLedger="";
        int height =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response1 = store.CreateStore(Data);
        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);
        commonFunc.sdkCheckTxOrSleep(txHash1,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        int height2 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response3 = store.CreateStore(Data);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);
        commonFunc.sdkCheckTxOrSleep(txHash3,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME * 2);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height2+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));

    }

    @Test
    public void TC1534_sendTxTest03()throws Exception{
        //存在主/子链*2
        //1.先向两个子链发送交易并可以成功查询到 再向主链发送交易并可以成功查询到
        String Data = "tc1534 03 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);


        //向子链glbChain01发送交易
        subLedger=glbChain01;
        int height1 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response2 = store.CreateStore(Data);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height1+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        int height2 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response3 = store.CreateStore(Data);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height2+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));

        //向主链发送交易
        subLedger="";
        int height =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String response1 = store.CreateStore(Data);
        sleepAndSaveInfo(SLEEPTIME);
        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
        assertEquals(height+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")));

    }
    @Test
    public void TC1534_sendTxTest04()throws Exception{
        //存在主/子链*2
        //1.先主链发送交易、再向子链发送交易，先查询主链交易成功查询到，再查询子链交易成功查询
        String Data = "tc1534 04 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向主链发送交易
        subLedger="";
        String response1 = store.CreateStore(Data);
        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);


        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response2 = store.CreateStore(Data);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        String response3 = store.CreateStore(Data);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME);
        //查询主链交易
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));


        //查询子链glbChain01交易
        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));


        //查询子链glbChain02交易
        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

    }

    @Test
    public void TC1534_sendTxTest05()throws Exception{
        //存在主/子链*2
        //1.先主链发送交易、再向子链发送交易，先查询子链交易成功查询到，再查询主链交易成功查询
        String Data = "tc1534 04 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向主链发送交易
        subLedger="";
        String response1 = store.CreateStore(Data);
        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);


        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response2 = store.CreateStore(Data);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        String response3 = store.CreateStore(Data);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME*2);

        //查询子链glbChain01交易
        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));


        //查询子链glbChain02交易
        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

        //查询主链交易
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

    }


    @Test
    public void TC1534_sendTxTest06()throws Exception{
        //存在主/子链*2
        //1.先向子链发送交易、再主子链发送交易，先查询主链交易成功查询到，再查询子链交易成功查询
        String Data = "tc1534 04 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response2 = store.CreateStore(Data);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        String response3 = store.CreateStore(Data);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);

        //向主链发送交易
        subLedger="";
        String response1 = store.CreateStore(Data);
        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);


        sleepAndSaveInfo(SLEEPTIME);
        //查询主链交易
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));


        //查询子链glbChain01交易
        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));


        //查询子链glbChain02交易
        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

    }


    @Test
    public void TC1534_sendTxTest07()throws Exception{
        //存在主/子链*2
        //1.先子链发送交易、再向主链发送交易，先查询子链交易成功查询到，再查询主链交易成功查询
        String Data = "tc1534 04 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response2 = store.CreateStore(Data);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        String response3 = store.CreateStore(Data);
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);

        //向主链发送交易
        subLedger="";
        String response1 = store.CreateStore(Data);
        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME*2);

        //查询子链glbChain01交易
        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));


        //查询子链glbChain02交易
        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

        //查询主链交易
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));

    }
}
