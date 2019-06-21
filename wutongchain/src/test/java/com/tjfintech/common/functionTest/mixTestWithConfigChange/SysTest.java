package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SysTest {

    public   final static int   SLEEPTIME=10*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    String mongoIP="10.1.3.246";
    String mongoID="e5b4023db787";
    String mysqlIP="10.1.3.164";

    boolean bResult=false;
    int iCount=0;

    public void startMongo()throws Exception{
        Shell shellMongo=new Shell(mongoIP,USERNAME,PASSWD);
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
                Thread.sleep(6000);
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


    @Test
    public void asetConfigMongoAndStop() throws Exception {
        bResult=false;

        startMongo();

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);
        Shell shellMongo=new Shell(mongoIP,USERNAME,PASSWD);


        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+PTPATH+"sdk/conf/configMongo.toml "+PTPATH+"sdk/conf/config.toml");
        log.info(mongoID);
        shellMongo.execute("docker restart "+mongoID);
        Thread.sleep(6000);
        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));

        //停止mongo数据库进程
        shellMongo.execute("docker stop "+mongoID);
        Thread.sleep(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mgodb err;[addressDB]:ping mgodb err;"));

//        log.info("******************please getapihealth manual with configMongo stop momgo************");
//        Thread.sleep(15000);


        shellMongo.execute("docker restart "+mongoID);
        Thread.sleep(6000);

        assertThat(store.GetApiHealth(),containsString("success"));

        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
        Thread.sleep(6000);
        assertThat(store.GetApiHealth(),containsString("success"));
    }


    @Test
    public void csetConfigMysqlAndStop() throws Exception {
        bResult=false;

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);
        Shell shellMysql=new Shell(mysqlIP,USERNAME,PASSWD);


        //系统配置数据库为mysql时，且数据库正常进行检查
        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+PTPATH+"sdk/conf/configMysql.toml "+PTPATH+"sdk/conf/config.toml");

        shellMysql.execute("service mysql restart");
        Thread.sleep(4000);
        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
        Thread.sleep(6000);

        String response4= store.GetApiHealth();
        assertThat(response4, containsString("success"));
        assertThat(response4,containsString("200"));

        //停止mysql进程
        shellMysql.execute("service mysql stop");
        Thread.sleep(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mysql err;[addressDB]:ping mysql err;"));
//        log.info("******************please getapihealth manual with configMysql stop mysql************");
//        Thread.sleep(15000);


        shellMysql.execute("service mysql restart");
        Thread.sleep(3000);
        String response6= store.GetApiHealth();
        assertThat(response6, containsString("success"));
        assertThat(response6,containsString("200"));
    }


    @Test
    public void esetConfigMongoMysqlAndStop() throws Exception {
        bResult=false;

        startMongo();

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");

        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+ PTPATH +"sdk/conf/configMongoMysql.toml "+ PTPATH +"sdk/conf/config.toml");
        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
        Thread.sleep(6000);
        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
        Thread.sleep(6000);


        String response4= store.GetApiHealth();
        assertThat(response4, containsString("success"));
        assertThat(response4,containsString("200"));

        //停止mongo数据库进程
        shellMongo.execute("docker stop "+mongoID);
        Thread.sleep(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mgodb err;"));
//        log.info("******************please getapihealth manual with configMongoMysql stop mongo************");
//        Thread.sleep(15000);
        //停止mysql进程
        shellMysql.execute("service mysql stop");
        Thread.sleep(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mgodb err;[addressDB]:ping mysql err;"));
//        log.info("******************please getapihealth manual with configMongoMysql stop mysql************");
//        Thread.sleep(15000);

        shellMongo.execute("docker start "+mongoID);
        Thread.sleep(5000);
        assertThat(store.GetApiHealth(),containsString("[addressDB]:ping mysql err;"));
//        log.info("******************please getapihealth manual with configMongoMysql restart mongo stop mysql************");
//        Thread.sleep(15000);


        shellMysql.execute("service mysql start");
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));

    }


    @Test
    public void fsetConfigMysqlMongoAndStop() throws Exception {
        bResult=false;

        startMongo();

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");

        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+PTPATH+"sdk/conf/configMysqlMongo.toml "+PTPATH+"sdk/conf/config.toml");

        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
        Thread.sleep(6000);
        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
        Thread.sleep(6000);

        String response4= store.GetApiHealth();
        assertThat(response4, containsString("success"));
        assertThat(response4,containsString("200"));

        //停止mongo数据库进程
        shellMongo.execute("docker stop "+mongoID);
        Thread.sleep(5000);
        assertThat(store.GetApiHealth(),containsString("[addressDB]:ping mgodb err;"));
//        log.info("******************please getapihealth manual with configMysqlMongo stop mongo************");
//        Thread.sleep(15000);
        //停止mysql进程
        shellMysql.execute("service mysql stop");
        Thread.sleep(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mysql err;[addressDB]:ping mgodb err;"));
//        Thread.sleep(15000);
//        log.info("******************please getapihealth manual with configMysqlMongo stop mysql************");

        shellMongo.execute("docker start "+mongoID);
        Thread.sleep(5000);
        assertThat(store.GetApiHealth(),containsString("[walletDB]:ping mysql err;"));
//        log.info("******************please getapihealth manual with configMysqlMongo restart docker************");
//        Thread.sleep(15000);

        shellMysql.execute("service mysql start");
        Thread.sleep(6000);


        assertThat(store.GetApiHealth(),containsString("success"));

        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
        Thread.sleep(6000);
        assertThat(store.GetApiHealth(),containsString("success"));
        //assertThat(response, containsString("success"));
        //assertThat(response,containsString("200"));
    }

    @After
    public void recoverConfigSt()throws Exception{

        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);
        Shell shellSDK=new Shell(sdkIP,"root","root");

        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+PTPATH+"sdk/conf/configOK.toml "+PTPATH+"sdk/conf/config.toml");

        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));


    }

}
