package com.tjfintech.common.functionTest.blockSync;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.ContractTest.ContractTest;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hamcrest.CoreMatchers;
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

    public long OnChainSleep = 6000;

    boolean bRe=false;
    //@Before
    public void beforeConfig() throws Exception {
        if(certPath!=""&& bReg==false) {
            //String newDB="newDB"+System.currentTimeMillis();
            //初始清空节点数据库及使用新的sdk数据库
            setAndRestartPeerList("rm -rf "+ PTPATH + "peer/*.db ","cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml");
            setAndRestartSDK("sed -i \"s/newDB/newDB1/g\" "+ PTPATH+"sdk/conf/configNewDB.toml ","cp "+PTPATH+"sdk/conf/configNewDB.toml "+PTPATH+"sdk/conf/"+SDKConfig+".toml");
            BeforeCondition bf = new BeforeCondition();
            bf.initTest();//赋值权限999
            bf.updatePubPriKey();//更新全局pub、prikey
            bf.collAddressTest();//添加归集地址和发行地址的注册
            Thread.sleep(OnChainSleep);
            bRe=true;
        }
    }

    @Test
    public void SyncNoContractTxEnableCtFlag()throws Exception{
        StoreUTXO();
        //停止其中一个节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        setAndRestartPeer(PEER2IP,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(10000);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

    }

    @Test
    public void SyncNoContractTxDisableCtFlag()throws Exception{
        StoreUTXO();
        //停止其中一个节点清除db数据，例如Peer2 --》10.1.3.246，重启节点 开始同步数据
        setAndRestartPeer(PEER2IP,"rm -rf "+ PTPATH + "peer/*.db ");

        //等待同步时间
        Thread.sleep(10000);

        //检查Peer2数据高度是否与其他节点一致
        assertEquals(getPeerHeight(PEER1IP,PEER1RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));
        assertEquals(getPeerHeight(PEER4IP,PEER4RPCPort),getPeerHeight(PEER2IP,PEER2RPCPort));

    }

        public void StoreUTXO()throws Exception{
        //setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/"+PeerInfoConfig+".toml");
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS6), containsString("200"));
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS1), containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS6), containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS1), containsString("200"));

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

        String response2= multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response4= multiSign.addissueaddress(PRIKEY1,ADDRESS6);
        String response5= multiSign.addissueaddress(PRIKEY1,ADDRESS1);
        Thread.sleep(OnChainSleep);


        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000","发行token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);
        log.info(MULITADD3+ "发行" + tokenTypeM + " token，数量为：" + amount);
        String data = "MULITADD3" + "发行" + tokenTypeM + " token，数量为：" + amount;
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
        jsonObject=JSONObject.fromObject(response7);
        String StoreHash7 = jsonObject.getString("Data").toString();
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
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash7)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTransaction(StoreHash8)).getString("State"));

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
    }

    public String getPeerHeight(String shellIP,String rpcPort)throws Exception{
        String height="";

        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);

        String cmd1="cd "+PTPATH+"./toolkit height -p "+ rpcPort;
        shell1.execute(cmd1);
        ArrayList<String> stdout = shell1.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
        for(String line : stdout){
            if(line.contains("BlockHeight:"))  height=line.substring(line.indexOf(":")+1).trim();
        }
        assertEquals(false,height.isEmpty());
        return height;
    }

}
