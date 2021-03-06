package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
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

import static com.tjfintech.common.CommonFunc.*;
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
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();


    /***
     * 此用例仅为修改配置项为使用ECDSA证书 需要在测试环境中自行备份ECDSA证书文件
     * 该用例属于半自动化用例
     * @throws Exception
     */
    @Test
    public void setConfigECDSACert()throws Exception{
        BeforeCondition bf =new BeforeCondition();
        bf.setPermission999();
        bf.updatePubPriKey();
        bf.collAddressTest();

        //配置节点TLS证书使用ECDSA
        commonFunc.setPeerTLSCertECDSA(PEER1IP);
        commonFunc.setPeerTLSCertECDSA(PEER2IP);
        commonFunc.setPeerTLSCertECDSA(PEER4IP);
        //配置SDK TLS证书使用ECDSA
        commonFunc.setSDKTLSCertECDSA(utilsClass.getIPFromStr(SDKADD));

        //重启节点和SDK
        utilsClass.setAndRestartPeerList();
        utilsClass.setAndRestartSDK();
        Thread.sleep(6000);



        //发送存证交易
        String Data="ECDSA store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        String response1=store.CreateStore(Data);
        String txHash1 = JSONObject.fromObject(response1).getString("data");

        String amount="305";
        String tokenTypeS = "ECDSASOLOTC-"+ UtilsClass.Random(6);
        String response2= soloSign.issueToken(PRIKEY1,tokenTypeS,amount,"发行token "+tokenTypeS,ADDRESS1);
        String txHash2 = JSONObject.fromObject(response2).getString("data");


        Thread.sleep(SLEEPTIME);
        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));//确认交易上链
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));//确认交易上链

    }

    //用例待补充
//    @Test
    public void expiredTLSCertTest()throws Exception{
        //设置节点使用过期tls 证书
        commonFunc.setPeerTLSCertExpired(PEER1IP);
        commonFunc.setPeerTLSCertExpired(PEER2IP);
        commonFunc.setPeerTLSCertExpired(PEER4IP);
        utilsClass.setAndRestartPeerList();
        assertEquals(false,true);


        //设置sdk使用过期tls证书

        commonFunc.setSDKTLSCertExpired(utilsClass.getIPFromStr(SDKADD));
        utilsClass.setAndRestartSDK();

    }

    @After
    public void recoverConfigSt()throws Exception{
        utilsClass.setAndRestartPeerList(resetPeerBase);
        utilsClass.setAndRestartSDK(resetSDKConfig);
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));


    }

}
