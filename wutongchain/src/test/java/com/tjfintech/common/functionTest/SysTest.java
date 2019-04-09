package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
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
                shellMongo.execute("docker ps -a |grep mongo|awk '{print $1}");
                ArrayList<String> stdout1 = shellMongo.getStandardOutput();
                mongoID=stdout1.get(0).trim();
            }
        }

    }

    //@Test
    public void TestErrMsg() throws Exception {

        String response3= store.GetApiHealth();
    }


    @Test
    public void asetConfigMongoAndStop() throws Exception {
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
//        Thread.sleep(3000);
//        store.GetApiHealth();
        //assertThat(response2,containsString("503"));

        log.info("******************please getapihealth manual with configMongo stop momgo************");
        Thread.sleep(15000);


        shellMongo.execute("docker restart "+mongoID);
        Thread.sleep(6000);

        String response3= store.GetApiHealth();
        //assertThat(response3, containsString("success"));
        //assertThat(response3,containsString("200"));

    }


    @Test
    public void csetConfigMysqlAndStop() throws Exception {
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
        Thread.sleep(3000);
//        store.GetApiHealth();
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
//        Thread.sleep(3000);
//        store.GetApiHealth();
        log.info("******************please getapihealth manual with configMongoMysql stop mongo************");
        Thread.sleep(15000);
        //停止mysql进程
        shellMysql.execute("service mysql stop");
//        Thread.sleep(3000);
//        store.GetApiHealth();
        log.info("******************please getapihealth manual with configMongoMysql stop mysql************");
        Thread.sleep(15000);

        shellMysql.execute("service mysql restart");
//        Thread.sleep(4000);
//        store.GetApiHealth();
        log.info("******************please getapihealth manual with configMongoMysql restart mysql stop mongo************");
        Thread.sleep(15000);


        shellMongo.execute("docker restart "+mongoID);
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));
    }


    @Test
    public void fsetConfigMysqlMongoAndStop() throws Exception {
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
//        Thread.sleep(3000);
//        store.GetApiHealth();
        log.info("******************please getapihealth manual with configMysqlMongo stop mongo************");
        Thread.sleep(15000);
        //停止mysql进程
        shellMysql.execute("service mysql stop");
//        Thread.sleep(3000);
//        store.GetApiHealth();
        Thread.sleep(15000);
        log.info("******************please getapihealth manual with configMysqlMongo stop mysql************");

        shellMysql.execute("service mysql restart");
//        Thread.sleep(3000);
//        store.GetApiHealth();
        log.info("******************please getapihealth manual with configMysqlMongo restart mysql************");
        Thread.sleep(15000);

        shellMongo.execute("docker restart "+mongoID);
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        //assertThat(response, containsString("success"));
        //assertThat(response,containsString("200"));
    }

    //@Before
    public void recoverConfigSt()throws Exception{
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);
        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");

        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+PTPATH+"sdk/conf/configOK.toml "+PTPATH+"sdk/conf/config.toml");

        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
        Thread.sleep(1000);
        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));


    }

}
