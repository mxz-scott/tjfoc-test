package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MixTxTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    WVMContractTest wvmContractTest = new WVMContractTest();
    FileOperation fileOper = new FileOperation();
    ArrayList<String> txHashList = new ArrayList<>();
    ArrayList<String> txHashNo = new ArrayList<>();
    @Before
    public void beforeConfig() throws Exception {
        if(MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            Thread.sleep(8000);
        }
    }

    @Test
    public void TestMultiTypeTx()throws Exception{
        //TC1845
        txHashList.clear();
        String response12 = multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response13 = multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response14 = multiSign.addissueaddress(PRIKEY1,ADDRESS6);
        String response15 = multiSign.addissueaddress(PRIKEY1,ADDRESS1);

        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS5), CoreMatchers.containsString("200"));
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS2), CoreMatchers.containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS5), CoreMatchers.containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS2), CoreMatchers.containsString("200"));

        Thread.sleep(6000);
        //设置打包时间为20s 使得各种类型的交易同时打包
        setPeerPackTime(PEER1IP,"20000");
        setPeerPackTime(PEER2IP,"20000");
        setPeerPackTime(PEER4IP,"20000");
        setAndRestartPeerList();
        setAndRestartSDK(resetSDKConfig);
        String resp = store.GetHeight();

        //发送存证交易
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        String response2= multiSign.collAddress(PRIKEY1,ADDRESS5);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS2);
        String response4= multiSign.addissueaddress(PRIKEY1,ADDRESS5);
        String response5= multiSign.addissueaddress(PRIKEY1,ADDRESS2);

        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        log.info(ADDRESS1+"发行token "+tokenTypeS);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000",ADDRESS1+"发行token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);

        String data = IMPPUTIONADD + "发行" + tokenTypeM + " token，数量为：" + amount;
        log.info(data);
        String response7 = multiSign.issueToken(IMPPUTIONADD, tokenTypeM, amount, data);
        assertEquals("200",JSONObject.fromObject(response7).getString("State"));
        String Tx1 = JSONObject.fromObject(response7).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response8 = multiSign.Sign(Tx1, PRIKEY5);


        //执行wvm合约测试
        wvm_install_invoke_destory();


        assertThat(response1, CoreMatchers.containsString("200"));
        assertThat(response2, CoreMatchers.containsString("200"));
        assertThat(response3, CoreMatchers.containsString("200"));
        assertThat(response4, CoreMatchers.containsString("200"));
        assertThat(response5, CoreMatchers.containsString("200"));
        assertThat(response6, CoreMatchers.containsString("200"));
        assertThat(response7, CoreMatchers.containsString("200"));
        assertThat(response8, CoreMatchers.containsString("200"));


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
        sleepAndSaveInfo(20000,"等待一个打包周期");

        response1=store.GetTxDetail(StoreHash1);
        response2=store.GetTxDetail(StoreHash2);
        response3=store.GetTxDetail(StoreHash3);
        response4=store.GetTxDetail(StoreHash4);
        response5=store.GetTxDetail(StoreHash5);
        response6=store.GetTxDetail(StoreHash6);
        response8=store.GetTxDetail(StoreHash8);


        txHashList.add(StoreHash1);
        txHashList.add(StoreHash2);
        txHashList.add(StoreHash3);
        txHashList.add(StoreHash4);
        txHashList.add(StoreHash5);
        txHashList.add(StoreHash6);
        txHashList.add(StoreHash8);


        String resp1 = store.GetHeight();

        int height =Integer.parseInt(JSONObject.fromObject(resp).getString("Data"));
        int height1=Integer.parseInt(JSONObject.fromObject(resp1).getString("Data"));
//        assertEquals(height,height1-1);
        boolean bright = height1 > height ? true: false;
        assertEquals(true,bright);

        log.info("list no.:" + txHashList.size());

        for(String hash : txHashList){
            wvmContractTest.chkTxDetailRsp("200",hash );
        }

        for(String hash : txHashNo){
            wvmContractTest.chkTxDetailRsp("404",hash );
        }

    }


    public void wvm_install_invoke_destory() throws Exception{

        String ctName="MIX_" + sdf.format(dt)+ RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(resourcePath + wvmContractTest.wvmFile + ".txt", wvmContractTest.orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmContractTest.wvmInstallTest(wvmContractTest.wvmFile +"_temp.txt",PRIKEY1);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("Data").getString("Name");

        //调用合约内的交易
        String response2 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountA,wvmContractTest.amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");


        //销毁wvm合约
        String response9 = wvmContractTest.wvmDestroyTest(ctHash);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("Data").getString("Figure");

        txHashList.add(txHash1);
        txHashList.add(txHash9);

        txHashNo.add(txHash2);
    }

    @After
    public void  reset()throws Exception{
        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("State"));
    }

}
