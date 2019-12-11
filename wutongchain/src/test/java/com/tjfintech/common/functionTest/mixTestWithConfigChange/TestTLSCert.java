package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tjfintech.common.CommonFunc.setPeerTLSCertECDSA;
import static com.tjfintech.common.CommonFunc.setSDKTLSCertECDSA;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestTLSCert {

    public   final static int   SLEEPTIME=10*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    SoloSign soloSign =testBuilder.getSoloSign();
    MultiSign multiSign =testBuilder.getMultiSign();


    @Test
    public void setConfigECDSACert()throws Exception{
        BeforeCondition bf =new BeforeCondition();
        bf.setPermission999();
        bf.updatePubPriKey();
        bf.collAddressTest();

        //配置节点TLS证书使用ECDSA
        setPeerTLSCertECDSA(PEER1IP);
        setPeerTLSCertECDSA(PEER2IP);
        setPeerTLSCertECDSA(PEER4IP);
        //配置SDK TLS证书使用ECDSA
        setSDKTLSCertECDSA();

        //重启节点和SDK
        setAndRestartPeerList();
        setAndRestartSDK();
        Thread.sleep(6000);



        //发送存证交易
        String Data="ECDSA store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();

        String amount="305";
        String tokenTypeS = "ECDSASOLOTC-"+ UtilsClass.Random(6);
        String response2= soloSign.issueToken(PRIKEY1,tokenTypeS,amount,"发行token "+tokenTypeS,ADDRESS1);
        String txHash2 = JSONObject.fromObject(response2).getString("Data");


        Thread.sleep(SLEEPTIME);
        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));//确认交易上链
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("State"));//确认交易上链

    }


    @After
    public void recoverConfigSt()throws Exception{
        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));


    }

}
