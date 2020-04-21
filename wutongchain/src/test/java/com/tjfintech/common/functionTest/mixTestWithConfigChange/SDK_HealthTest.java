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

    public void startMongo()throws Exception{
        databaseIP=utilsClass.getSDKWalletDBConfig().split(",")[1];;
        Shell shellMongo=new Shell(databaseIP,USERNAME,PASSWD);
        shellMongo.execute("docker ps -a");
        ArrayList<String> stdout = shellMongo.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        if(response.contains(mongoID)==false)
        {
            if(response.contains("mongo"))
            {
                shellMongo.execute("docker ps -a |grep mongo|awk '{print $1}'");
                ArrayList<String> stdout1 = shellMongo.getStandardOutput();
                mongoID=stdout1.get(0).trim();
            }
            else
            {
                shellMongo.execute("docker run --name mongo -p 27017:27017 -v /data/database/mongotest:/data -d mongo:3.6");
               sleepAndSaveInfo(6000);
                shellMongo.execute("docker ps -a |grep mongo|awk '{print $1}");
                ArrayList<String> stdout1 = shellMongo.getStandardOutput();
                mongoID=stdout1.get(0).trim();
                log.info("get mongo id "+mongoID);
            }
        }

    }

    //@Test
    public void TestErrMsg() throws Exception {
        assertThat(store.GetApiHealth(),containsString("503"));
    }


//    @Test
    public void asetConfigMongoAndStop() throws Exception {
        bResult=false;
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute(killSDKCmd);
        commonFunc.setSDKWalletAddrDBMongo(utilsClass.getIPFromStr(SDKADD));
        startMongo();
        databaseIP=utilsClass.getSDKWalletDBConfig().split(",")[1];
        Shell shellMongo=new Shell(databaseIP,USERNAME,PASSWD);
        log.info(mongoID);
        shellMongo.execute("docker restart "+mongoID);
        sleepAndSaveInfo(6000);
        shellSDK.execute(startSDKCmd);
        sleepAndSaveInfo(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));

        //停止mongo数据库进程
        shellMongo.execute("docker stop "+mongoID);
        sleepAndSaveInfo(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mgodb err;[addressDB]:ping mgodb err;"));

//        log.info("******************please getapihealth manual with configMongo stop momgo************");
//       sleepAndSaveInfo(15000);


        shellMongo.execute("docker restart "+mongoID);
        sleepAndSaveInfo(6000);

        assertThat(store.GetApiHealth(),containsString("success"));

        shellSDK.execute(killSDKCmd);
        shellSDK.execute(startSDKCmd);
        sleepAndSaveInfo(6000);
        assertThat(store.GetApiHealth(),containsString("success"));
    }


    @Test
    public void csetConfigMysqlAndStop() throws Exception {
        bResult=false;

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        //系统配置数据库为mysql时，且数据库正常进行检查
        shellSDK.execute(killSDKCmd);
        commonFunc.setSDKWalletAddrDBMysql(utilsClass.getIPFromStr(SDKADD));
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


    //非常规应用场景 暂不测试
//    @Test
    public void esetConfigMongoMysqlAndStop() throws Exception {
        bResult=false;
        String mongoIP = "10.1.3.246";
        String mysqlIP = "10.1.3.246";
        startMongo();

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");

        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute(killSDKCmd);
        commonFunc.setSDKWalletDBMongoAddrDBMysql(utilsClass.getIPFromStr(SDKADD));
        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
       sleepAndSaveInfo(5 * 1000);
        shellSDK.execute(startSDKCmd);
       sleepAndSaveInfo(10 * 1000);


        String response4= store.GetApiHealth();
        assertThat(response4, containsString("success"));
        assertThat(response4,containsString("200"));

        //停止mongo数据库进程
        shellMongo.execute("docker stop "+mongoID);
       sleepAndSaveInfo(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mgodb err;"));
//        log.info("******************please getapihealth manual with configMongoMysql stop mongo************");
//       sleepAndSaveInfo(15000);
        //停止mysql进程
        shellMysql.execute("service mysql stop");
       sleepAndSaveInfo(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mgodb err;[addressDB]:ping mysql err;"));
//        log.info("******************please getapihealth manual with configMongoMysql stop mysql************");
//       sleepAndSaveInfo(15000);

        shellMongo.execute("docker start "+mongoID);
       sleepAndSaveInfo(5000);
        assertThat(store.GetApiHealth(),containsString("[addressDB]:ping mysql err;"));
//        log.info("******************please getapihealth manual with configMongoMysql restart mongo stop mysql************");
//       sleepAndSaveInfo(15000);


        shellMysql.execute("service mysql start");
       sleepAndSaveInfo(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));

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

    //非常规应用场景 暂不测试
//    @Test
    public void fsetConfigMysqlMongoAndStop() throws Exception {
        String mongoIP = "10.1.3.246";
        String mysqlIP = "10.1.3.246";
        bResult=false;

        startMongo();

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");

        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute(killSDKCmd);
        commonFunc.setSDKWalletDBMysqlAddrDBMongo(utilsClass.getIPFromStr(SDKADD));

        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
       sleepAndSaveInfo(6000);
        shellSDK.execute(startSDKCmd);
       sleepAndSaveInfo(6000);

        String response4= store.GetApiHealth();
        assertThat(response4, containsString("success"));
        assertThat(response4,containsString("200"));

        //停止mongo数据库进程
        shellMongo.execute("docker stop "+mongoID);
       sleepAndSaveInfo(5000);
        assertThat(store.GetApiHealth(),containsString("[addressDB]:ping mgodb err;"));
//        log.info("******************please getapihealth manual with configMysqlMongo stop mongo************");
//       sleepAndSaveInfo(15000);
        //停止mysql进程
        shellMysql.execute("service mysql stop");
       sleepAndSaveInfo(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mysql err;[addressDB]:ping mgodb err;"));
//       sleepAndSaveInfo(15000);
//        log.info("******************please getapihealth manual with configMysqlMongo stop mysql************");

        shellMongo.execute("docker start "+mongoID);
       sleepAndSaveInfo(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mysql err;"));
//        log.info("******************please getapihealth manual with configMysqlMongo restart docker************");
//       sleepAndSaveInfo(15000);

        shellMysql.execute("service mysql start");
       sleepAndSaveInfo(10 * 1000);


        assertThat(store.GetApiHealth(),containsString("success"));

        shellSDK.execute(killSDKCmd);
        shellSDK.execute(startSDKCmd);
       sleepAndSaveInfo(10 * 1000);
        assertThat(store.GetApiHealth(),containsString("success"));
        //assertThat(response, containsString("success"));
        //assertThat(response,containsString("200"));
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
