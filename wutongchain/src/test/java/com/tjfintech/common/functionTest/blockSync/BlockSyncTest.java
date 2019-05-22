package com.tjfintech.common.functionTest.blockSync;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.ContractTest.ContractTest;
import com.tjfintech.common.functionTest.TestMgTool;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Before;
import static org.hamcrest.Matchers.containsString;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import java.util.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class BlockSyncTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    ContractTest ct =new ContractTest();

    public long OnChainSleep = 3000;
    ArrayList<String> hashList = new ArrayList<>();

    boolean bRe=false;
    @Before
    public void beforeConfig() throws Exception {
        setAndRestartPeerList("rm -rf "+ PTPATH + "peer/*.db "
                ,"cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml"
                ,"cp "+ PTPATH + "peer/conf/configOK.toml "+ PTPATH +"peer/conf/"+PeerMemConfig+".toml");
        setAndRestartSDK("sed -i \"s/newDB/newDB1/g\" "+ PTPATH+"sdk/conf/configNewDB.toml "
                ,"cp "+PTPATH+"sdk/conf/configNewDB.toml "+PTPATH+"sdk/conf/"+SDKConfig+".toml");
        hashList.clear();

        if(certPath!=""&& bRe==false) {
            //String newDB="newDB"+System.currentTimeMillis();
            //初始清空节点数据库及使用新的sdk数据库

            BeforeCondition bf = new BeforeCondition();
            bf.initTest();//赋值权限999
            bf.updatePubPriKey();//更新全局pub、prikey
            bf.collAddressTest();//添加归集地址和发行地址的注册
            Thread.sleep(OnChainSleep);
            bRe=true;
        }
        log.info("*********************before config end*********************");
    }

    @Test
    public void TC992_SyncNoContractTxEnableCtFlag()throws Exception{
        String syncPeer=PEER2IP;
        //停止节点PEER2
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //停止其中一个节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        setAndRestartPeer(syncPeer,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(OnChainSleep*3);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }

    @Test
    public void TC969_SyncNoContractTxDisableCtFlag()throws Exception{
        String syncPeer=PEER2IP;
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseContractfalse.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml");
        //停止节点PEER2
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易

        //个节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        setAndRestartPeer(syncPeer,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(OnChainSleep*3);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }

    //test case 923
    @Test
    public void TC923_SyncNoContractTxCtFlagChange1()throws Exception{
        String syncPeer=PEER2IP;
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseContractfalse.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml");
        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易

        //节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        setAndRestartPeer(syncPeer,"rm -rf "+ PTPATH + "peer/*.db "
                ,"cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml");

        //等待同步时间
        Thread.sleep(OnChainSleep*3);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }


    @Test
    public void TC832_SyncWithContractTxEnableCtFlag()throws Exception{
        String syncPeer=PEER2IP;
        //停止节点PEER2
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        Contract();
        //停止其中一个节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        setAndRestartPeer(syncPeer,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(OnChainSleep*3+ContractInstallSleep);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }

    @Test
    public void TC986_SyncDataPeerWithTxEnableCtFlag()throws Exception{
        setAndRestartPeerList("rm -rf "+ PTPATH + "peer/*.db "
                ,"cp "+ PTPATH + "peer/conf/configData.toml "+ PTPATH +"peer/conf/"+PeerMemConfig+".toml");
        setAndRestartSDK("sed -i \"s/newDB/newDB1/g\" "+ PTPATH+"sdk/conf/configNewDB.toml "
                ,"cp "+PTPATH+"sdk/conf/configNewDB.toml "+PTPATH+"sdk/conf/"+SDKConfig+".toml");
        BeforeCondition bf = new BeforeCondition();
        bf.initTest();//赋值权限999
        bf.updatePubPriKey();//更新全局pub、prikey
        bf.collAddressTest();//添加归集地址和发行地址的注册
        Thread.sleep(OnChainSleep);

        String syncPeer=PEER4IP; //247为非共识节点

        //停止节点PEER2
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        Contract();
        //停止其中一个节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        setAndRestartPeer(syncPeer,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(OnChainSleep*3+ContractInstallSleep);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }


    //831
    @Test
    public void TC831_SyncWithContractTxDisableCtFlag()throws Exception{
        String syncPeer=PEER2IP;
        //停止节点PEER2
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        Contract();

        //节点清除db数据，并将Contract Enabled设置为false 例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        setAndRestartPeer(syncPeer,"rm -rf "+ PTPATH + "peer/*.db "
                ,"cp "+ PTPATH + "peer/conf/baseContractfalse.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml");

        //等待同步时间
        Thread.sleep(OnChainSleep*3);

        //检查Peer2同步异常节点会停止
        TestMgTool mgTool = new TestMgTool();
        mgTool.checkParam(syncPeer,"./toolkit height -p 9300","rpc error");


        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        //恢复PEER2配置 检查可以正常同步
        setAndRestartPeer(syncPeer,"cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml");
        Thread.sleep(OnChainSleep*3 +ContractInstallSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }

    //989  操作节点PEER4
    @Test
    public void TC989_SyncNoBaseImage()throws Exception{
        //停止节点PEER2,删除节点2上的基础镜像
        String syncPeer=PEER4IP;
        Shell shellPeer=new Shell(syncPeer,USERNAME,PASSWD);
        shellPeer.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellPeer.execute("docker rm -f `docker ps -aq`");
        Thread.sleep(1500);
        shellPeer.execute("docker rmi `docker images`");
        Thread.sleep(1500);
        StoreUTXO();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        Contract();
        //无基础镜像时同步包含合约交易的区块交易
        setAndRestartPeer(syncPeer,"rm -rf "+ PTPATH + "peer/*.db "
                ,"cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml");
        //同步失败节点异常 停止运行

        //检查Peer2同步异常节点会停止
        TestMgTool mgTool = new TestMgTool();
        mgTool.checkParam(syncPeer,"./toolkit height -p 9300","rpc error");

        //安装合约镜像
        setAndRestartPeer(syncPeer,"rm -rf "+ PTPATH + "peer/*.db ","docker load < /root/ccenv.docker");

        //等待同步时间+合约安装
        Thread.sleep(OnChainSleep*3+ContractInstallSleep);
        log.info("Check peer height after reloading base images");
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        //同步成功后检查新交易后是否同步
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
    }


    @Test
    public void TC983_OnePeerStoreUTXO()throws Exception{
        //SDK配置文件中仅配置PEER1节点

        //停止节点PEER2 和PEER4
        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        Shell shellPeer4=new Shell(PEER4IP,USERNAME,PASSWD);
        shellPeer4.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");


        StoreUTXONoCheck();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易

        //清空剩下两个节点db数据 并重启
        setAndRestartPeer(PEER2IP,"rm -rf "+ PTPATH + "peer/*.db ");
        setAndRestartPeer(PEER4IP,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(OnChainSleep*3);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        for(String hash : hashList){
            assertEquals("200",JSONObject.fromObject(store.GetTransaction(hash)).getString("State"));
        }

    }

    @Test
    public void TC982_OnePeerContract()throws Exception{
        //SDK配置文件中仅配置PEER1节点

        //停止节点PEER2 和PEER4
        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        Shell shellPeer4=new Shell(PEER4IP,USERNAME,PASSWD);
        shellPeer4.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        ContractNoCheck();
        //清空剩下两个节点db数据 并重启
        setAndRestartPeer(PEER2IP,"rm -rf "+ PTPATH + "peer/*.db ");
        setAndRestartPeer(PEER4IP,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(OnChainSleep+ContractInstallSleep);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        for(String hash : hashList){
            assertEquals("200",JSONObject.fromObject(store.GetTransaction(hash)).getString("State"));
        }
    }

    @Test
    public void TC981_OnePeerAll()throws Exception{
        //SDK配置文件中仅配置PEER1节点

        //停止节点PEER2 和PEER4
        Shell shellPeer2=new Shell(PEER2IP,USERNAME,PASSWD);
        shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

        Shell shellPeer4=new Shell(PEER4IP,USERNAME,PASSWD);
        shellPeer4.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");


        StoreUTXONoCheck();
        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        ContractNoCheck();
        //清空剩下两个节点db数据 并重启
        setAndRestartPeer(PEER2IP,"rm -rf "+ PTPATH + "peer/*.db ");
        setAndRestartPeer(PEER4IP,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(OnChainSleep*3+ContractInstallSleep);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        MgToolStore();//使用管理工具短时间内发送多笔存证交易
        //等待交易上链
        Thread.sleep(OnChainSleep);
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

        for(String hash : hashList){
            log.info("checking hash on chain: "+ hash);
            assertEquals("200",JSONObject.fromObject(store.GetTransaction(hash)).getString("State"));
        }
    }

        public void StoreUTXO()throws Exception{

        Thread.sleep(OnChainSleep);

        String resp = store.GetHeight();
        //发送存证交易
        Date dt=new Date();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        //构造错误交易
        BeforeCondition bf = new BeforeCondition();
        bf.collAddressTest();//添加归集地址和发行地址的注册
        Thread.sleep(OnChainSleep);

        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS6), containsString("200"));
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS1), containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS6), containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS1), containsString("200"));

        Thread.sleep(OnChainSleep);
        String response2= multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response4= multiSign.addissueaddress(PRIKEY1,ADDRESS6);
        String response5= multiSign.addissueaddress(PRIKEY1,ADDRESS1);
        Thread.sleep(OnChainSleep);


        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000","发行token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);
        String data = IMPPUTIONADD + "发行" + tokenTypeM + " token，数量为：" + amount;
        log.info(data);
        String response7 = multiSign.issueToken(IMPPUTIONADD, tokenTypeM, amount, data);
        assertEquals("200", JSONObject.fromObject(response7).getString("State"));
        String Tx1 = JSONObject.fromObject(response7).getJSONObject("Data").getString("Tx");
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
        String StoreHash1 = jsonObject.getJSONObject("Data").get("Figure").toString();
        jsonObject=JSONObject.fromObject(response2);
        String StoreHash2 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response3);
        String StoreHash3 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response4);
        String StoreHash4 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response5);
        String StoreHash5 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response6);
        String StoreHash6 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response8);
        String StoreHash8 = jsonObject.getJSONObject("Data").get("TxId").toString();


        //等待一个打包周期
        Thread.sleep(OnChainSleep);

        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash3)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash4)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash5)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash6)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash8)).getString("State"));

    }

    public void StoreUTXONoCheck()throws Exception{

        Thread.sleep(OnChainSleep);

        String resp = store.GetHeight();
        //发送存证交易
        Date dt=new Date();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        //构造错误交易
        BeforeCondition bf = new BeforeCondition();
        bf.collAddressTest();//添加归集地址和发行地址的注册
        Thread.sleep(OnChainSleep);

        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS6), containsString("200"));
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS1), containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS6), containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS1), containsString("200"));

        Thread.sleep(OnChainSleep);
        String response2= multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response4= multiSign.addissueaddress(PRIKEY1,ADDRESS6);
        String response5= multiSign.addissueaddress(PRIKEY1,ADDRESS1);
        Thread.sleep(OnChainSleep);


        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000","发行token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);
        String data = IMPPUTIONADD + "发行" + tokenTypeM + " token，数量为：" + amount;
        log.info(data);
        String response7 = multiSign.issueToken(IMPPUTIONADD, tokenTypeM, amount, data);
        assertEquals("200", JSONObject.fromObject(response7).getString("State"));
        String Tx1 = JSONObject.fromObject(response7).getJSONObject("Data").getString("Tx");
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
        String StoreHash1 = jsonObject.getJSONObject("Data").get("Figure").toString();
        jsonObject=JSONObject.fromObject(response2);
        String StoreHash2 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response3);
        String StoreHash3 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response4);
        String StoreHash4 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response5);
        String StoreHash5 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response6);
        String StoreHash6 = jsonObject.getString("Data").toString();
        jsonObject=JSONObject.fromObject(response8);
        String StoreHash8 = jsonObject.getJSONObject("Data").get("TxId").toString();
        hashList.add(StoreHash1);
        hashList.add(StoreHash2);
        hashList.add(StoreHash6);
        hashList.add(StoreHash8);
    }

    public void MgToolStore()throws Exception{
        TestMgTool mgTool = new TestMgTool();
        mgTool.checkParam(PEER1IP,"./toolkit newtx -p 9300 -n 50 -t 1","HashData");
    }
    public void Contract()throws Exception{
        //创建合约
        dockerFileName="simple.go";
        log.info("创建合约"+ct.name);
        String response7 = ct.installTest();
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("Data").get("Figure").toString();
        Thread.sleep(ContractInstallSleep);
        //发送合约交易initMobile
        log.info("发送合约交易initMobile");
        String response81 = ct.initMobileTest();
        Thread.sleep(OnChainSleep);
        String txHash81 = JSONObject.fromObject(response81).getJSONObject("Data").get("Figure").toString();
        //发送合约交易querymobile
        log.info("发送合约交易querymobile");
        String response8 = ct.queryMobileTest("Mobile1");
        Thread.sleep(OnChainSleep);
        String txHash8 = JSONObject.fromObject(response8).getJSONObject("Data").get("Figure").toString();

        //销毁合约
        log.info("销毁合约"+ct.name);
        String response9 = ct.destroyTest();
        Thread.sleep(OnChainSleep);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("Data").get("Figure").toString();

        //等待一个打包周期
        Thread.sleep(OnChainSleep);

        assertEquals("200",JSONObject.fromObject(store.GetTransaction(txHash7)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(txHash81)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(txHash9)).getString("State"));
    }

    public void ContractNoCheck()throws Exception{
        //创建合约
        dockerFileName="simple.go";
        log.info("创建合约"+ct.name);
        String response7 = ct.installTest();
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("Data").get("Figure").toString();
        Thread.sleep(ContractInstallSleep);
        //发送合约交易initMobile
        log.info("发送合约交易initMobile");
        String response81 = ct.initMobileTest();
        Thread.sleep(OnChainSleep);
        String txHash81 = JSONObject.fromObject(response81).getJSONObject("Data").get("Figure").toString();
        //发送合约交易querymobile
        log.info("发送合约交易querymobile");
        String response8 = ct.queryMobileTest("Mobile1");
        Thread.sleep(OnChainSleep);
        String txHash8 = JSONObject.fromObject(response8).getJSONObject("Data").get("Figure").toString();

        //销毁合约
        log.info("销毁合约"+ct.name);
        String response9 = ct.destroyTest();
        Thread.sleep(OnChainSleep);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("Data").get("Figure").toString();
        hashList.add(txHash7);
        hashList.add(txHash81);
        hashList.add(txHash9);
    }

    public String getPeerHeight(String shellIP,String rpcPort)throws Exception{
        String height="";

        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);

        String cmd1="cd "+PTPATH+"toolkit/;./toolkit height -p "+ rpcPort;
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
        setAndRestartPeerList("rm -rf "+ PTPATH + "peer/*.db "
                ,"cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml"
                ,"cp "+ PTPATH + "peer/conf/configOK.toml "+ PTPATH +"peer/conf/"+PeerMemConfig+".toml");
        setAndRestartSDK("cp "+PTPATH+"sdk/conf/configOK.toml "+PTPATH+"sdk/conf/"+SDKConfig+".toml");
    }

}
