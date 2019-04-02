package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
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
    String mongoID="5adda58c4778";
    String mysqlIP="10.1.3.164";


    @Test
    public void asetConfigMongoAndStop() throws Exception {
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");


        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp /root/zll/chain2.0.1/sdk/conf/configMongo.toml /root/zll/chain2.0.1/sdk/conf/config.toml");

        shellMongo.execute("docker restart "+mongoID);
        Thread.sleep(6000);
        shellSDK.execute("sh /root/zll/chain2.0.1/sdk/start.sh");
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

        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");


        //系统配置数据库为mysql时，且数据库正常进行检查
        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp /root/zll/chain2.0.1/sdk/conf/configMysql.toml /root/zll/chain2.0.1/sdk/conf/config.toml");

        shellMysql.execute("service mysql restart");
        Thread.sleep(4000);
        shellSDK.execute("sh /root/zll/chain2.0.1/sdk/start.sh");
        Thread.sleep(6000);

        String response4= store.GetApiHealth();
        assertThat(response4, containsString("success"));
        assertThat(response4,containsString("200"));

        //停止mysql进程
        shellMysql.execute("service mysql stop");
//        Thread.sleep(3000);
//        store.GetApiHealth();
        log.info("******************please getapihealth manual with configMysql stop mysql************");
        Thread.sleep(15000);


        shellMysql.execute("service mysql restart");
        Thread.sleep(3000);
        String response6= store.GetApiHealth();
        assertThat(response6, containsString("success"));
        assertThat(response6,containsString("200"));
    }


    @Test
    public void esetConfigMongoMysqlAndStop() throws Exception {
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");

        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp /root/zll/chain2.0.1/sdk/conf/configMongoMysql.toml /root/zll/chain2.0.1/sdk/conf/config.toml");
        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
        Thread.sleep(6000);
        shellSDK.execute("sh /root/zll/chain2.0.1/sdk/start.sh");
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
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);

        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");

        //系统配置数据库为mongodb时，且数据库正常进行检查
        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp /root/zll/chain2.0.1/sdk/conf/configMysqlMongo.toml /root/zll/chain2.0.1/sdk/conf/config.toml");

        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
        Thread.sleep(6000);
        shellSDK.execute("sh /root/zll/chain2.0.1/sdk/start.sh");
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
        shellSDK.execute("cp /root/zll/chain2.0.1/sdk/conf/configOK.toml /root/zll/chain2.0.1/sdk/conf/config.toml");

        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
        Thread.sleep(1000);
        shellSDK.execute("sh /root/zll/chain2.0.1/sdk/start.sh");
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));


    }

  // @After
   //@Test
    public void recoverConfigEn()throws Exception{
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        log.info(sdkIP);
        Shell shellSDK=new Shell(sdkIP,"root","root");
        Shell shellMongo=new Shell(mongoIP,"root","root");
        Shell shellMysql=new Shell(mysqlIP,"root","root");

        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp /root/zll/chain2.0.1/sdk/conf/configOK.toml /root/zll/chain2.0.1/sdk/conf/config.toml");

        shellMongo.execute("docker restart "+mongoID);
        shellMysql.execute("service mysql restart");
        Thread.sleep(1000);
        shellSDK.execute("sh /root/zll/chain2.0.1/sdk/start.sh");
        Thread.sleep(6000);

        String response= store.GetApiHealth();
        assertThat(response, containsString("success"));
        assertThat(response,containsString("200"));


    }


}
