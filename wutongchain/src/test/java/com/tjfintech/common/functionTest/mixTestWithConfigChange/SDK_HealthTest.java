package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetSDKWalletDisabled;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SDK_HealthTest {

    public   final static int   SLEEPTIME=10*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String mongoID="e5b4023db787";
    String databaseIP="";

    boolean bResult=false;
    int iCount=0;


    @Test
    public void csetConfigMysqlAndStop() throws Exception {
        bResult=false;
        //确保sdk为钱包开启 当前默认数据库使用mysql
        commonFunc.setSDKWalletEnabled(utilsClass.getIPFromStr(SDKADD),"true");

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);
        shellSDK.execute(killSDKCmd);

        databaseIP=utilsClass.getSDKWalletDBConfig().split(",")[1];
        Shell shellMysql=new Shell(databaseIP,USERNAME,PASSWD);

        shellMysql.execute("service mysql restart");
        sleepAndSaveInfo(10000);
        
        shellSDK.execute(startSDKCmd);
        sleepAndSaveInfo(10000);

        String response4= store.GetApiHealth();
        assertThat(response4, containsString("success"));
        assertThat(response4,containsString("200"));

        //停止mysql进程
        shellMysql.execute("service mysql stop");
        sleepAndSaveInfo(10000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mysql err;[addressDB]:ping mysql err;"));
//        log.info("******************please getapihealth manual with configMysql stop mysql************");
//       sleepAndSaveInfo(15000);


        shellMysql.execute("service mysql restart");
        sleepAndSaveInfo(10000);
        String response6= store.GetApiHealth();
        assertThat(response6, containsString("success"));
        assertThat(response6,containsString("200"));
    }


    @Test
    public void peerHealthChk()throws Exception{
        assertThat(store.GetApiHealth(),containsString("success"));

        //停止其中两个节点
        shellExeCmd(PEER1IP,killPeerCmd);
        shellExeCmd(PEER2IP,killPeerCmd);
        sleepAndSaveInfo(1000);

        assertThat(store.GetApiHealth(),containsString("success"));

        //停止所有节点
        shellExeCmd(PEER4IP,killPeerCmd);
        sleepAndSaveInfo(1000);
        assertThat(store.GetApiHealth(),containsString("rpc error"));

        //重启所有节点
        shellExeCmd(PEER1IP,startPeerCmd);
        shellExeCmd(PEER2IP,startPeerCmd);
        shellExeCmd(PEER4IP,startPeerCmd);

        sleepAndSaveInfo(SLEEPTIME*4);
        assertThat(store.GetApiHealth(),containsString("success"));
    }

    @Test
    public void TC2008_WalletDisabled()throws Exception{
        SetSDKWalletDisabled setSDKWalletDisabled = new SetSDKWalletDisabled();
        //关闭wallet
        setSDKWalletDisabled.setWalletDisabled();

        assertThat(store.GetApiHealth(),containsString("success"));

        peerHealthChk();
    }

    @After
    public void recoverConfigSt()throws Exception{

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        shellSDK.execute(killSDKCmd);
        shellSDK.execute(resetSDKConfig);

        shellSDK.execute(startSDKCmd);
        sleepAndSaveInfo(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));


    }

}
