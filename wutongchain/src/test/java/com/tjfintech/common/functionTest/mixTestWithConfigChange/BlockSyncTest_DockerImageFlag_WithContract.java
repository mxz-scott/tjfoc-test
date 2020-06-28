package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
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
public class BlockSyncTest_DockerImageFlag_WithContract {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    DockerContractTest ct =new DockerContractTest();

    public long OnChainSleep = 5000;
    ArrayList<String> hashList = new ArrayList<>();
    TestTxType testTxType = new TestTxType();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    MgToolCmd mgToolCmd = new MgToolCmd();

    String wvmHash = "";

    @Before
    public void beforeConfig() throws Exception {
        utilsClass.setAndRestartPeerList(clearPeerDB,clearPeerWVMsrc,clearPeerWVMbin,resetPeerBase,resetPeerConfig);
        utilsClass.delDataBase();//清空sdk当前使用数据库数据
        utilsClass.setAndRestartSDK();
        hashList.clear();
        testTxType.initSetting();
        log.info("*********************before config end*********************");
    }

    @Test
    public void TC832_SyncWithContractTxEnableCtFlag()throws Exception{
        String syncPeer=PEER2IP;
        //停止节点PEER2
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute(killPeerCmd);

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        WVMTx();
        Contract();
        //停止其中一个节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        utilsClass.setAndRestartPeer(syncPeer,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc);

        //等待同步时间
        mgToolCmd.mgCheckHeightOrSleep(PEER1IP + ":" + PEER1RPCPort,
                PEER2IP + ":" + PEER2RPCPort,80*1000);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        sleepAndSaveInfo(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }

    @Test
    public void TC986_SyncDataPeerWithTxEnableCtFlag()throws Exception{
        //设置PEER4为数据节点
        commonFunc.setPeerClusterWithOneDataPeer();
        utilsClass.setAndRestartPeerList(clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc);
        utilsClass.delDataBase();//清空sdk当前使用数据库数据
        utilsClass.setAndRestartSDK();
        testTxType.initSetting();

        String syncPeer=PEER4IP; //247为非共识节点

        //停止节点PEER2
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute(killPeerCmd);

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        WVMTx();
        Contract();
        //停止其中一个节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        utilsClass.setAndRestartPeer(syncPeer,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc);

        //等待同步时间
        mgToolCmd.mgCheckHeightOrSleep(PEER1IP + ":" + PEER1RPCPort,
                PEER2IP + ":" + PEER2RPCPort,80*1000);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        sleepAndSaveInfo(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }

    @Test
    public void TC831_SyncWithContractTxDisableCtFlag()throws Exception{
        String syncPeer=PEER2IP;
        //停止节点PEER2
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute(killPeerCmd);

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        WVMTx();
        Contract();

        //节点清除db数据，并将Contract Enabled设置为false 例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        commonFunc.setPeerContractEnabled(PEER2IP,"false");
        utilsClass.setAndRestartPeer(syncPeer,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc);

        //等待同步时间
        sleepAndSaveInfo(10*1000,"等待节点启动");

        //检查Peer2同步异常节点会停止
        ExeToolCmdAndChk(syncPeer,"./" + ToolTPName + " height -p "+ PEER2RPCPort,"rpc error");


        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        //恢复PEER2配置 检查可以正常同步
        utilsClass.setAndRestartPeer(syncPeer,resetPeerBase);

        //等待同步时间
        mgToolCmd.mgCheckHeightOrSleep(PEER1IP + ":" + PEER1RPCPort,
                PEER2IP + ":" + PEER2RPCPort,80*1000);

        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }

    //989  操作节点PEER4
    @Test
    public void TC989_SyncNoBaseImage()throws Exception{
        //停止节点PEER4,删除节点4上的基础镜像
        String syncPeer=PEER4IP;
        String synvPeerPort = PEER4RPCPort;
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute(killPeerCmd);
        shellPeer.execute("docker rm -f `docker ps -aq`");
        sleepAndSaveInfo(1500);
        shellPeer.execute("docker rmi `docker images`");
        sleepAndSaveInfo(SLEEPTIME);

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        WVMTx();
        Contract();
        //无基础镜像时同步包含合约交易的区块交易
        utilsClass.setAndRestartPeer(syncPeer,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc,resetPeerBase);
        //同步失败节点异常 停止运行
        sleepAndSaveInfo(10*1000,"等待节点启动");
        //检查同步异常节点会停止
        ExeToolCmdAndChk(syncPeer,"./" + ToolTPName + " height -p "+ synvPeerPort,"rpc error");

        //安装合约镜像
        utilsClass.setAndRestartPeer(syncPeer,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc,ccenvPull);

        //等待镜像安装时间
        sleepAndSaveInfo(ContractInstallSleep,"docker images installation waiting......");

        //等待同步时间+合约安装
        //等待同步时间
        mgToolCmd.mgCheckHeightOrSleep(PEER1IP + ":" + PEER1RPCPort,
                PEER2IP + ":" + PEER2RPCPort,80*1000);

        log.info("Check peer height after reloading base images");
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        //同步成功后检查新交易后是否同步
        sleepAndSaveInfo(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }


    @Test
    public void TC982_OnePeerContract()throws Exception{
        //SDK配置文件中仅配置PEER1节点

        //停止节点PEER2 和PEER4
        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
        shellPeer2.execute(killPeerCmd);

        Shell shellPeer4=new Shell(PEER4IP,USERNAME,PASSWD);
        shellPeer4.execute(killPeerCmd);

        ContractNoCheck();
        //清空剩下两个节点db数据 并重启
        utilsClass.setAndRestartPeer(PEER2IP,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc);
        utilsClass.setAndRestartPeer(PEER4IP,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc);

        //等待同步时间
        mgToolCmd.mgCheckHeightOrSleep(PEER1IP + ":" + PEER1RPCPort,
                PEER2IP + ":" + PEER2RPCPort,100*1000);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        sleepAndSaveInfo(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        for(String hash : hashList){
            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hash)).getString("State"));
        }
    }

    @Test
    public void TC981_OnePeerAll()throws Exception{
        WVMContractTest wvmContractTest = new WVMContractTest();
        wvmHash = JSONObject.fromObject(wvmContractTest.intallUpdateName("testWVM",PRIKEY1)).getJSONObject("data").getString("name");
        //SDK配置文件中仅配置PEER1节点
        sleepAndSaveInfo(SLEEPTIME,"等待wvm合约安装交易上链");
        //停止节点PEER2 和PEER4
        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
        shellPeer2.execute(killPeerCmd);

        Shell shellPeer4=new Shell(PEER4IP,USERNAME,PASSWD);
        shellPeer4.execute(killPeerCmd);

        //配置sdk节点集群仅为Peer1并重启sdk
        commonFunc.setSDKOnePeer(utilsClass.getIPFromStr(SDKADD),PEER1IP + ":" + PEER1RPCPort,"true",peerTLSServerName);
        utilsClass.setAndRestartSDK();

        StoreUTXONoCheck();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        ContractNoCheck();
        WVMTxNoCheck(wvmHash);
        //清空剩下两个节点db数据 并重启
        utilsClass.setAndRestartPeer(PEER2IP,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc);
        utilsClass.setAndRestartPeer(PEER4IP,clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc);

        //等待同步时间
        mgToolCmd.mgCheckHeightOrSleep(PEER1IP + ":" + PEER1RPCPort,
                PEER2IP + ":" + PEER2RPCPort,100*1000);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        sleepAndSaveInfo(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        for(String hash : hashList){
            log.info("checking hash on chain: "+ hash);
            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(hash)).getString("State"));
        }
    }

        public void StoreUTXO()throws Exception{

        sleepAndSaveInfo(OnChainSleep);

        String resp = store.GetHeight();
        //发送存证交易
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        //构造错误交易
        BeforeCondition bf = new BeforeCondition();
        bf.collAddressTest();//添加归集地址和发行地址的注册
        sleepAndSaveInfo(OnChainSleep);

        assertThat(multiSign.delCollAddressRemovePri(ADDRESS6), containsString("200"));
        assertThat(multiSign.delCollAddressRemovePri(ADDRESS1), containsString("200"));
        assertThat(multiSign.delissueaddressRemovePri(ADDRESS6), containsString("200"));
        assertThat(multiSign.delissueaddressRemovePri(ADDRESS1), containsString("200"));

        sleepAndSaveInfo(OnChainSleep);
        String response2= multiSign.collAddressRemovePri(ADDRESS6);
        String response3= multiSign.collAddressRemovePri(ADDRESS1);
        String response4= multiSign.addissueaddressRemovePri(ADDRESS6);
        String response5= multiSign.addissueaddressRemovePri(ADDRESS1);
        sleepAndSaveInfo(OnChainSleep);


        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000","发行token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);
        String data = IMPPUTIONADD + "发行" + tokenTypeM + " token，数量为：" + amount;
        log.info(data);
        String response7 = multiSign.issueToken(IMPPUTIONADD, tokenTypeM, amount, data);
        log.info("99999  "+IMPPUTIONADD);
        assertEquals("200", JSONObject.fromObject(response7).getString("State"));
        String Tx1 = JSONObject.fromObject(response7).getJSONObject("data").getString("tx");
        log.info("第一次签名");
        String response8 = multiSign.Sign(Tx1, PRIKEY5);

        assertThat(response1, containsString("200"));
        assertThat(response2, containsString("200"));
        assertThat(response3, containsString("200"));
        assertThat(response4, containsString("200"));
        assertThat(response5, containsString("200"));
        assertThat(response6, containsString("200"));
        assertThat(response7, containsString("200"));
        assertThat(response8, containsString("200"));

        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHash1 = jsonObject.getJSONObject("data").get("figure").toString();
        jsonObject=JSONObject.fromObject(response2);
        String StoreHash2 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response3);
        String StoreHash3 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response4);
        String StoreHash4 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response5);
        String StoreHash5 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response6);
        String StoreHash6 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response8);
        String StoreHash8 = jsonObject.getJSONObject("data").get("txId").toString();


        //等待一个打包周期
        sleepAndSaveInfo(OnChainSleep);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(StoreHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(StoreHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(StoreHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(StoreHash4)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(StoreHash5)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(StoreHash6)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(StoreHash8)).getString("State"));

    }

    public void StoreUTXONoCheck()throws Exception{

        sleepAndSaveInfo(OnChainSleep);

        String resp = store.GetHeight();
        //发送存证交易
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        //构造错误交易
        BeforeCondition bf = new BeforeCondition();
        bf.collAddressTest();//添加归集地址和发行地址的注册
        sleepAndSaveInfo(OnChainSleep);

        assertThat(multiSign.delCollAddressRemovePri(ADDRESS6), containsString("200"));
        assertThat(multiSign.delCollAddressRemovePri(ADDRESS1), containsString("200"));
        assertThat(multiSign.delissueaddressRemovePri(ADDRESS6), containsString("200"));
        assertThat(multiSign.delissueaddressRemovePri(PRIKEY1,ADDRESS1), containsString("200"));

        sleepAndSaveInfo(OnChainSleep);
        String response2= multiSign.collAddressRemovePri(ADDRESS6);
        String response3= multiSign.collAddressRemovePri(ADDRESS1);
        String response4= multiSign.addissueaddressRemovePri(ADDRESS6);
        String response5= multiSign.addissueaddressRemovePri(ADDRESS1);
        sleepAndSaveInfo(OnChainSleep);


        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000","发行token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);
        String data = IMPPUTIONADD + "发行" + tokenTypeM + " token，数量为：" + amount;
        log.info(data);
        String response7 = multiSign.issueToken(IMPPUTIONADD, tokenTypeM, amount, data);
        assertEquals("200", JSONObject.fromObject(response7).getString("State"));
        String Tx1 = JSONObject.fromObject(response7).getJSONObject("data").getString("tx");
        log.info("第一次签名");
        String response8 = multiSign.Sign(Tx1, PRIKEY5);

        assertThat(response1, containsString("200"));
        assertThat(response2, containsString("200"));
        assertThat(response3, containsString("200"));
        assertThat(response4, containsString("200"));
        assertThat(response5, containsString("200"));
        assertThat(response6, containsString("200"));
        assertThat(response7, containsString("200"));
        assertThat(response8, containsString("200"));

        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHash1 = jsonObject.getJSONObject("data").get("figure").toString();
        jsonObject=JSONObject.fromObject(response2);
        String StoreHash2 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response3);
        String StoreHash3 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response4);
        String StoreHash4 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response5);
        String StoreHash5 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response6);
        String StoreHash6 = jsonObject.getString("data").toString();
        jsonObject=JSONObject.fromObject(response8);
        String StoreHash8 = jsonObject.getJSONObject("data").get("txId").toString();
        hashList.add(StoreHash1);
        hashList.add(StoreHash2);
        hashList.add(StoreHash6);
        hashList.add(StoreHash8);
    }

    public void MgToolStore()throws Exception{
        ExeToolCmdAndChk(PEER1IP,"./" + ToolTPName + " newtx -p " + PEER1RPCPort + " -n 50 -t 1","HashData");
    }

    public void WVMTx()throws Exception{
        WVMContractTest wvmContractTest = new WVMContractTest();
        wvmContractTest.TC1774_1784_1786_testContract();
    }

    public void WVMTxNoCheck(String existHash)throws Exception{
        FileOperation fileOper = new FileOperation();
        String ctName="UI_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        WVMContractTest wvm = new WVMContractTest();
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(resourcePath + wvm.wvmFile + ".txt", wvm.orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvm.wvmInstallTest( wvm.wvmFile +"_temp.txt",PRIKEY1);
        log.info("Install Pri:"+PRIKEY1);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("figure");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        sleepAndSaveInfo(SLEEPTIME);
        //调用合约内的交易  调用已存在的合约wHash中的交易
        String response2 = wvm.invokeNew(existHash,"initAccount",wvm.accountA,wvm.amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("figure");

        String response3 = wvm.invokeNew(existHash,"initAccount",wvm.accountB,wvm.amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("figure");

        sleepAndSaveInfo(SLEEPTIME);

        String response4 = wvm.invokeNew(existHash,"transfer",wvm.accountA,wvm.accountB,wvm.transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("figure");

        sleepAndSaveInfo(SLEEPTIME);

        //查询余额invoke接口
        String response5 = wvm.invokeNew(existHash,"getBalance",wvm.accountA);//获取账户A账户余额
        String txHash5 = JSONObject.fromObject(response5).getJSONObject("data").getString("figure");

        sleepAndSaveInfo(SLEEPTIME/2);

        //销毁wvm合约
        String response9 = wvm.wvmDestroyTest(ctHash);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("data").getString("figure");
        sleepAndSaveInfo(SLEEPTIME);

        hashList.add(txHash1);
        hashList.add(txHash2);
        hashList.add(txHash3);
        hashList.add(txHash4);
        hashList.add(txHash5);
        hashList.add(txHash9);
    }

    public void Contract()throws Exception{
        //创建合约
        dockerFileName="simple.go";
        log.info("创建合约"+ct.name);
        String response7 = ct.installTest();
        sleepAndSaveInfo(ContractInstallSleep);
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("data").get("figure").toString();

        //发送合约交易initMobile
        log.info("发送合约交易initMobile");
        String response81 = ct.initMobileTest();
        sleepAndSaveInfo(OnChainSleep);
        String txHash81 = JSONObject.fromObject(response81).getJSONObject("data").get("figure").toString();
        //发送合约交易querymobile
        log.info("发送合约交易querymobile");
        String response8 = ct.queryMobileTest("Mobile1");
        sleepAndSaveInfo(OnChainSleep);
        String txHash8 = JSONObject.fromObject(response8).getJSONObject("data").get("figure").toString();

        //销毁合约
        log.info("销毁合约"+ct.name);
        String response9 = ct.destroyTest();
        sleepAndSaveInfo(OnChainSleep);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("data").get("figure").toString();

        //等待一个打包周期
        sleepAndSaveInfo(OnChainSleep);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash7)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash81)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash8)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash9)).getString("State"));
    }

    public void ContractNoCheck()throws Exception{
        //创建合约
        dockerFileName="simple.go";
        log.info("创建合约"+ct.name);
        String response7 = ct.installTest();
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("data").get("figure").toString();
        sleepAndSaveInfo(ContractInstallSleep);
        //发送合约交易initMobile
        log.info("发送合约交易initMobile");
        String response81 = ct.initMobileTest();
        sleepAndSaveInfo(OnChainSleep);
        String txHash81 = JSONObject.fromObject(response81).getJSONObject("data").get("figure").toString();
        //发送合约交易querymobile
        log.info("发送合约交易querymobile");
        String response8 = ct.queryMobileTest("Mobile1");
        sleepAndSaveInfo(OnChainSleep);
        String txHash8 = JSONObject.fromObject(response8).getJSONObject("data").get("figure").toString();

        //销毁合约
        log.info("销毁合约"+ct.name);
        String response9 = ct.destroyTest();
        sleepAndSaveInfo(OnChainSleep);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("data").get("figure").toString();
        hashList.add(txHash7);
        hashList.add(txHash81);
        hashList.add(txHash9);
    }

    public String getPeerHeight(String shellIP,String rpcPort)throws Exception{
        String height="";

        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);

        String cmd1="cd "+ ToolPATH + ";./" + ToolTPName + " height -p "+ rpcPort;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        for(String line : stdout){
            if(line.contains("BlockHeight:"))  height=line.substring(line.indexOf(":")+1).trim();
        }
        assertEquals(false,height.isEmpty());
        return height;
    }

    //@AfterClass
    public static void resetEnv()throws Exception{
        UtilsClass utilsClassTemp = new UtilsClass();
        utilsClassTemp.setAndRestartPeerList(clearPeerDB,clearPeerWVMbin,clearPeerWVMsrc,resetPeerBase,resetPeerConfig);
        utilsClassTemp.delDataBase();//清空sdk当前使用数据库数据
        utilsClassTemp.setAndRestartSDK(resetSDKConfig);
    }

}
