package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
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
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    String tempWVMHash = "";

    @Before
    public void beforeConfig() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        bf.collAddressTest();
        Thread.sleep(8000);

        WVMContractTest wvmContractTest = new WVMContractTest();
        String respInstall = wvmContractTest.intallUpdateName("testWVM",PRIKEY1);
        tempWVMHash = JSONObject.fromObject(respInstall).getJSONObject("data").getString("name");
        String tempHash = JSONObject.fromObject(respInstall).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(tempHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
    }

    @Test
    public void TestMultiTypeTx()throws Exception{
        //TC1845
        txHashList.clear();
        String response12 = multiSign.addCollAddrs(ADDRESS6);
        String response13 = multiSign.addCollAddrs(ADDRESS1);
        String response14 = multiSign.addIssueAddrs(ADDRESS6);
        String response15 = multiSign.addIssueAddrs(ADDRESS1);

        assertThat(multiSign.delCollAddrs(ADDRESS5), CoreMatchers.containsString("200"));
        assertThat(multiSign.delCollAddrs(ADDRESS2), CoreMatchers.containsString("200"));
        assertThat(multiSign.delIssueaddrs(ADDRESS5), CoreMatchers.containsString("200"));
        assertThat(multiSign.delIssueaddrs(ADDRESS2), CoreMatchers.containsString("200"));

        Thread.sleep(6000);
        //?????????????????????20s ???????????????????????????????????????
        commonFunc.setPeerPackTime(PEER1IP,"20000");
        commonFunc.setPeerPackTime(PEER2IP,"20000");
        commonFunc.setPeerPackTime(PEER4IP,"20000");
        utilsClass.setAndRestartPeerList();
        utilsClass.setAndRestartSDK(resetSDKConfig);
        String resp = store.GetHeight();

        //??????????????????
        String Data="Mix tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        String response2= multiSign.addCollAddrs(ADDRESS5);
        String response3= multiSign.addCollAddrs(ADDRESS2);
        String response4= multiSign.addIssueAddrs(ADDRESS5);
        String response5= multiSign.addIssueAddrs(ADDRESS2);

        String tokenTypeS = "MixSOLOTC-"+ UtilsClass.Random(6);
        log.info(ADDRESS1+"??????token "+tokenTypeS);
        String response6= soloSign.issueToken(PRIKEY1,tokenTypeS,"10000",ADDRESS1+"??????token "+tokenTypeS,ADDRESS1);

        String amount="3000";
        String tokenTypeM = "MixMultiTC-"+ UtilsClass.Random(6);

        String data = IMPPUTIONADD + "??????" + tokenTypeM + " token???????????????" + amount;
        log.info(data);
        String response7 = multiSign.issueToken(IMPPUTIONADD, tokenTypeM, amount, data);
        assertEquals("200",JSONObject.fromObject(response7).getString("state"));
        String Tx1 = JSONObject.fromObject(response7).getJSONObject("data").getString("tx");
        log.info("???????????????");
        String response8 = multiSign.Sign(Tx1, PRIKEY5);


        //??????wvm????????????
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
        String StoreHash1 = jsonObject.getString("data");
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


        //????????????????????????
        sleepAndSaveInfo(20000,"????????????????????????");

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

        int height =Integer.parseInt(JSONObject.fromObject(resp).getString("data"));
        int height1=Integer.parseInt(JSONObject.fromObject(resp1).getString("data"));
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

        // ?????????wvm??????????????????????????????????????????????????????????????????
        // ?????????????????????????????????????????????"_temp"????????????????????????????????????????????????
        fileOper.replace(tempWVMDir + wvmContractTest.wvmFile + ".txt", wvmContractTest.orgName, ctName);

        //??????????????????????????????hash??????Prikey???ctName??????????????????
        String response1 = wvmContractTest.wvmInstallTest(wvmContractTest.wvmFile +"_temp.txt",PRIKEY1);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //????????????????????????
        String response2 = wvmContractTest.invokeNew(tempWVMHash,"initAccount",wvmContractTest.accountA,wvmContractTest.amountA);//???????????????A ????????????50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");


        //??????wvm??????
        String response9 = wvmContractTest.wvmDestroyTest(ctHash);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("data").getString("txId");

        txHashList.add(txHash1);
        txHashList.add(txHash9);

        //20200320 ??????????????????????????????????????? ???????????????????????????????????????????????????????????????
//        txHashNo.add(txHash2);
        txHashList.add(txHash2);
    }

    @After
    public void  reset()throws Exception{
        UtilsClass utilsClassTemp = new UtilsClass();
        utilsClassTemp.setAndRestartPeerList(resetPeerBase);
        utilsClass.setAndRestartSDK(resetSDKConfig);
        assertEquals("200",JSONObject.fromObject(store.GetHeight()).getString("state"));
    }

}
