package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetSDKWalletDisabled;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.FileOperation.getSDKConfigValueByShell;
import static com.tjfintech.common.utils.FileOperation.getTokenApiConfigValueByShell;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TokenApi_HealthTest {

    public   final static int   SLEEPTIME=10*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String databaseIP="";

    boolean bResult=false;

    Token tokenModule = testBuilder.getToken();


    @Test
    public void csetConfigMysqlAndStop() throws Exception {
        SDKADD = TOKENADD;
        bResult=false;

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);
        shellSDK.execute(killTokenApiCmd);
        String dbConfig = getTokenApiConfigValueByShell(utilsClass.getIPFromStr(SDKADD),"DB","Connection");//token api db
        databaseIP = utilsClass.getIPFromStr(dbConfig);
        log.info(databaseIP);

        Shell shellMysql=new Shell(databaseIP,USERNAME,PASSWD);

        shellMysql.execute("service mysql restart");
        sleepAndSaveInfo(10000);
        
        shellSDK.execute(startTokenApiCmd);
        sleepAndSaveInfo(10000);

        String response4= tokenModule.tokenGetApiHealth();
        assertThat(response4, containsString("success"));
        assertThat(response4,containsString("200"));
        
        
        //停止mysql进程
        shellMysql.execute("service mysql stop");
        sleepAndSaveInfo(10000);
        assertThat(tokenModule.tokenGetApiHealth(),containsString("connect: connection refused;"));
//        log.info("******************please getapihealth manual with configMysql stop mysql************");
//       sleepAndSaveInfo(15000);


        shellMysql.execute("service mysql restart");
        sleepAndSaveInfo(10000);
        String response6= tokenModule.tokenGetApiHealth();
        assertThat(response6, containsString("success"));
        assertThat(response6,containsString("200"));
    }


    @Test
    public void peerHealthChk()throws Exception{
        SDKADD = TOKENADD;
        assertThat(tokenModule.tokenGetApiHealth(),containsString("success"));

        //停止其中两个节点
        shellExeCmd(PEER1IP,killPeerCmd);
        shellExeCmd(PEER2IP,killPeerCmd);
        sleepAndSaveInfo(1000);

        assertThat(tokenModule.tokenGetApiHealth(),containsString("success"));

        //停止所有节点
        shellExeCmd(PEER4IP,killPeerCmd);
        sleepAndSaveInfo(2000);
        assertThat(tokenModule.tokenGetApiHealth(),containsString("rpc error"));

        //重启所有节点
        shellExeCmd(PEER1IP,startPeerCmd);
        shellExeCmd(PEER2IP,startPeerCmd);
        shellExeCmd(PEER4IP,startPeerCmd);

        sleepAndSaveInfo(SLEEPTIME*4);
        assertThat(tokenModule.tokenGetApiHealth(),containsString("success"));
    }



    @After
    public void recoverConfigSt()throws Exception{
        SDKADD = rSDKADD;
        shExeAndReturn(databaseIP,"service mysql restart");
        sleepAndSaveInfo(10*1000);
    }

}
