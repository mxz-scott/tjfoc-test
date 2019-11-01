package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.SubLedgerCmd;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMultiSubChain_02 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    MgToolCmd mgToolCmd = new MgToolCmd();


    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";
    String noPerm="not found";

    @BeforeClass
    public static void clearData()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.clearDataSetPerm999();
//        beforeCondition.createAdd();
        sleepAndSaveInfo(SLEEPTIME);
    }

    @Before
    public void beforeConfig() throws Exception {
        subLedger="";
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"999");
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01+"\"")) {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01,
                    " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02,
                    " -t sm3", " -w first", " -c raft", ids);
            sleepAndSaveInfo(SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
        }
    }

    @Test
    public void TC1555_sendTxToMultiChains()throws Exception{
        String Data ="tc1555 ledger tx data";
        String chainName="tc1555";

        ArrayList<String> hashList = new ArrayList<>();

        for(int i=1;i<10;i++)
        {
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + chainName+i,
                    " -t sm3", " -w first", " -c raft", ids);
        }

        sleepAndSaveInfo(SLEEPTIME*3);
        //确认所有子链均存在
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        for(int i=1;i<10;i++){
            assertEquals(resp.contains("\"name\": \""+chainName+i+"\""), true);
        }

        //向新创建的所有子链发送交易
        for(int i=1;i<10;i++)
        {
            subLedger=chainName+i;
            String response = store.CreateStore(Data);
            String txHash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
            hashList.add(txHash);
        }


        subLedger="";
        String response10 = store.CreateStore(Data);
        String txHash10 = JSONObject.fromObject(response10).getJSONObject("Data").getString("Figure");

        sleepAndSaveInfo(SLEEPTIME*3);

        //检查向新创建的所有子链发送交易均可以查询到
        for(int i=1;i<10;i++)
        {
            subLedger=chainName+i;
            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hashList.get(i-1))).getString("State"));
        }
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash10)).getString("State"));

    }

    @Test
    public void TC1660_1666_1667_createSubChain()throws Exception{

        //设置主链上sdk权限为1,2,3,4,5,6,7,8,9,10
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"1,2,3,4,5,6,7,8,9,10");
        sleepAndSaveInfo(SLEEPTIME);
        subLedger="";
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[1 2 3 4 5 6 7 8 9 10]");
        assertEquals(true,store.GetHeight().contains("\"State\":200"));

        //创建子链01 包含节点A、B、C
        String chainName1="tc1666_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName1,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName1;
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        assertThat(store.CreateStore("tc1666 no permission tx data").toLowerCase(),containsString(noPerm));


        subLedger="";
        //设置主链上sdk权限为0
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"0");
        sleepAndSaveInfo(SLEEPTIME);

        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        assertThat(store.CreateStore("tc1660 no permission tx data").toLowerCase(),containsString(noPerm));

        //创建子链01 包含节点A、B、C
        String chainName2="tc1660_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = mgToolCmd.createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName2,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName2), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName2;
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        assertThat(store.CreateStore("tc1660 no permission tx data").toLowerCase(),containsString(noPerm));


        subLedger="";
        //设置主链上sdk权限为999
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,getSDKID(),"999");
        sleepAndSaveInfo(SLEEPTIME*2);
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), fullPerm);
        assertThat(store.CreateStore("tc1660 no permission tx data"),containsString("\"State\":200"));

        //创建子链01 包含节点A、B、C
        String chainName3="tc1660_01"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        res = mgToolCmd.createSubChainNoPerm(PEER1IP,PEER1RPCPort," -z "+chainName3,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName3), true);

        //获取子链权限列表指定sdk为空 测试发送交易无权限
        subLedger=chainName3;
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), "[0]");
        sleepAndSaveInfo(SLEEPTIME);
        assertThat(store.CreateStore("tc1666 no permission tx data").toLowerCase(),containsString(noPerm));

        subLedger="";
        //设置主链权限为999,确认主链sdk权限恢复
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(getCertainPermissionList(PEER1IP,PEER1RPCPort,getSDKID()), fullPerm);
        //向子链glbChain01/glbChain02和主链发送交易
        subLedgerCmd.sendTxToMainActiveChain(glbChain01,glbChain02,"tc1656 tx data2");
    }


    @Test
    public void TC1534_sendTxTest01()throws Exception{
        //存在主/子链*2
        //1.先向主链发送交易并可以成功查询到 再向子链发送交易并可以成功查询到
        String Data = "tc1534 01 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);


        //向主链发送交易
        subLedger="";
        int height =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response1 = store.CreateStore(Data);
        sleepAndSaveInfo(SLEEPTIME);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));

        //向子链glbChain01发送交易
        subLedger=glbChain01;
        int height1 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response2 = store.CreateStore(Data);
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height1+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));

        //向子链glbChain02发送交易
        subLedger=glbChain02;
        int height2 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response3 = store.CreateStore(Data);
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height2+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));


    }

    @Test
    public void TC1534_sendTxTest02()throws Exception{
        //存在主/子链*2
        //1.先向子链发送交易并可以成功查询到 再向主链发送交易并可以成功查询到 最后向另一条子链发送交易并查询
        String Data = "tc1534 02 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);


        //向子链glbChain01发送交易
        subLedger=glbChain01;
        int height1 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response2 = store.CreateStore(Data);
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height1+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));


        //向主链发送交易
        subLedger="";
        int height =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response1 = store.CreateStore(Data);
        sleepAndSaveInfo(SLEEPTIME);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        int height2 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response3 = store.CreateStore(Data);
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME*2);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height2+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));

    }

    @Test
    public void TC1534_sendTxTest03()throws Exception{
        //存在主/子链*2
        //1.先向两个子链发送交易并可以成功查询到 再向主链发送交易并可以成功查询到
        String Data = "tc1534 03 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);


        //向子链glbChain01发送交易
        subLedger=glbChain01;
        int height1 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response2 = store.CreateStore(Data);
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height1+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        int height2 =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response3 = store.CreateStore(Data);
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height2+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));

        //向主链发送交易
        subLedger="";
        int height =Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String response1 = store.CreateStore(Data);
        sleepAndSaveInfo(SLEEPTIME);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
        assertEquals(height+1,Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data")));

    }
    @Test
    public void TC1534_sendTxTest04()throws Exception{
        //存在主/子链*2
        //1.先主链发送交易、再向子链发送交易，先查询主链交易成功查询到，再查询子链交易成功查询
        String Data = "tc1534 04 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向主链发送交易
        subLedger="";
        String response1 = store.CreateStore(Data);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();


        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response2 = store.CreateStore(Data);
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        String response3 = store.CreateStore(Data);
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();

        sleepAndSaveInfo(SLEEPTIME);
        //查询主链交易
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));


        //查询子链glbChain01交易
        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));


        //查询子链glbChain02交易
        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

    }

    @Test
    public void TC1534_sendTxTest05()throws Exception{
        //存在主/子链*2
        //1.先主链发送交易、再向子链发送交易，先查询子链交易成功查询到，再查询主链交易成功查询
        String Data = "tc1534 04 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向主链发送交易
        subLedger="";
        String response1 = store.CreateStore(Data);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();


        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response2 = store.CreateStore(Data);
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        String response3 = store.CreateStore(Data);
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();

        sleepAndSaveInfo(SLEEPTIME*2);

        //查询子链glbChain01交易
        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));


        //查询子链glbChain02交易
        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

        //查询主链交易
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

    }


    @Test
    public void TC1534_sendTxTest06()throws Exception{
        //存在主/子链*2
        //1.先向子链发送交易、再主子链发送交易，先查询主链交易成功查询到，再查询子链交易成功查询
        String Data = "tc1534 04 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response2 = store.CreateStore(Data);
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        String response3 = store.CreateStore(Data);
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();

        //向主链发送交易
        subLedger="";
        String response1 = store.CreateStore(Data);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();


        sleepAndSaveInfo(SLEEPTIME);
        //查询主链交易
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));


        //查询子链glbChain01交易
        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));


        //查询子链glbChain02交易
        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

    }


    @Test
    public void TC1534_sendTxTest07()throws Exception{
        //存在主/子链*2
        //1.先子链发送交易、再向主链发送交易，先查询子链交易成功查询到，再查询主链交易成功查询
        String Data = "tc1534 04 tx test";
        //检查可以执行获取所有子链信息命令
        assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("name"), true);

        //向子链glbChain01发送交易
        subLedger=glbChain01;
        String response2 = store.CreateStore(Data);
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();


        //向子链glbChain02发送交易
        subLedger=glbChain02;
        String response3 = store.CreateStore(Data);
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").get("Figure").toString();

        //向主链发送交易
        subLedger="";
        String response1 = store.CreateStore(Data);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();

        sleepAndSaveInfo(SLEEPTIME*2);

        //查询子链glbChain01交易
        subLedger=glbChain01;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));


        //查询子链glbChain02交易
        subLedger=glbChain02;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

        //查询主链交易
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));

    }
}
