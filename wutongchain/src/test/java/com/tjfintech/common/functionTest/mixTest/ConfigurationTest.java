package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import sun.print.PeekGraphics;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class ConfigurationTest {
    public static final String PEER1IP="10.1.3.240";
    public static final String PEER2IP = "10.1.3.246";
    public static final String PEER3IP="10.1.3.247";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    public static String tokenType;
    public static String tokenType2;
    TestBuilder testBuilder=TestBuilder.getInstance();
    SoloSign soloSign=testBuilder.getSoloSign();
    MultiSign multiSign=testBuilder.getMultiSign();

    /**
     * 设置SDK数据库为boltdb。节点与sdk开启TLS
     */
    @Test
    public void setBoltdb_TLS(){
        Shell shell1=new Shell(PEER1IP,USERNAME,PASSWORD);
        Shell shell2=new Shell(PEER2IP,USERNAME,PASSWORD);
        Shell shell3=new Shell(PEER3IP,USERNAME,PASSWORD);
        shell1.execute("/bin/sh /root/chenxu/script/setTlsTrue_peer.sh");
        shell2.execute("/bin/sh /root/chenxu/script/setTlsTrue_peer.sh");
        shell3.execute("/bin/sh /root/chenxu/script/setTlsTrue_peer.sh");
        shell2.execute("/bin/sh /root/chenxu/script/setBoltdb_sdk.sh");
        ArrayList<String> stdout = shell1.getStandardOutput();
        stdout.addAll(shell2.getStandardOutput());
        stdout.addAll(shell3.getStandardOutput());
        for (String str : stdout) {
            log.info(str);
            assertEquals(str.contains("失败"), false);
        }

    }
    /**
     * Default默认设置TLSEnable=false Raft共识 toml配置类型 SDK数据库默认mongodb
     */
    @Test
    public void setDefault(){
        Shell shell1=new Shell(PEER1IP,USERNAME,PASSWORD);
        Shell shell2=new Shell(PEER2IP,USERNAME,PASSWORD);
        Shell shell3=new Shell(PEER3IP,USERNAME,PASSWORD);
//        shell1.execute("/bin/sh /root/chenxu/script/default_peer.sh");
//        shell2.execute("/bin/sh /root/chenxu/script/default_peer.sh");
//        shell3.execute("/bin/sh /root/chenxu/script/default_peer.sh");
        shell2.execute("/bin/sh /root/chenxu/script/default_sdk.sh");
        ArrayList<String> stdout = shell1.getStandardOutput();
        stdout.addAll(shell2.getStandardOutput());
        stdout.addAll(shell3.getStandardOutput());
        for (String str : stdout) {
            log.info(str);
            assertEquals(str.contains("失败"), false);
        }

    }
    @Test
    public void Tc675_Check() {
        Shell shell = new Shell(PEER2IP, USERNAME, PASSWORD);  //连接节点

        shell.execute("/bin/sh /root/basechain/sdk/script/tc675_1.sh");    //执行的命令
        shell.execute("/bin/sh /root/bashchain/sdk/script/judge.sh");    //执行的命令
        shell.execute("/bin/sh /root/basechain/sdk/script/tc675_2.sh");  //执行的命令
        shell.execute("/bin/sh /root/bashchain/sdk/script/judge.sh");    //执行的命令
        ArrayList<String> stdout = shell.getStandardOutput();
        for (String str : stdout) {
            //log.info(str);
            assertEquals(str.contains("失败"), false);
        }
    }
    @Test
    public void Tc676_check(){
        Shell shell = new Shell(PEER2IP, USERNAME, PASSWORD);  //连接节点
        shell.execute("/bin/sh /root/basechain/sdk/script/tc676.sh");    //执行的命令
        ArrayList<String> stdout = shell.getStandardOutput();
        for (String str : stdout) {
            //log.info(str);
            assertEquals(str.contains("失败"), false);
        }
    }
    @Test
    public void Tc677_boltDB()throws  Exception{
        Shell shell = new Shell(PEER2IP, USERNAME, PASSWORD);  //连接节点
        shell.execute("/bin/sh /root/basechain/sdk/script/tc677.sh");    //执行的命令
        shell.execute("/bin/sh /root/bashchain/sdk/script/judge.sh");    //执行的命令
        ArrayList<String> stdout = shell.getStandardOutput();
        for (String str : stdout) {
            //log.info(str);
            assertEquals(str.contains("失败"), false);
        }
        issueToken();

    }
    @Test
    public  void Tc678_mongoDB()throws  Exception{
        Shell shell = new Shell(PEER2IP, USERNAME, PASSWORD);  //连接节点
        shell.execute("/bin/sh /root/basechain/sdk/script/tc678.sh");    //执行的命令
        shell.execute("/bin/sh /root/bashchain/sdk/script/judge.sh");    //执行的命令
        ArrayList<String> stdout = shell.getStandardOutput();
        for (String str : stdout) {
            //log.info(str);
            assertEquals(str.contains("失败"), false);
        }
        issueToken();
    }

    /**
     * 脚本尚未上传至节点上
     */
    @Test
    public void Tc783_CertNotFound(){
        Shell shell = new Shell(PEER1IP, USERNAME, PASSWORD);  //连接节点
        shell.execute("/bin/sh /root/basechain/script/tc783.sh");    //执行的命令
        ArrayList<String> stdout = shell.getStandardOutput();
        for (String str : stdout) {
            //log.info(str);
            assertEquals(str.contains("成功"),true);
            assertEquals(str.contains("失败"), false);
        }
    }

    public void issueToken()throws  Exception{
        log.info("发行两种token1000个");
        tokenType = "SOLOTC-"+ UtilsClass.Random(6);
        String isResult= soloSign.issueToken(PRIKEY1,tokenType,"1000.123456789","发行token",ADDRESS1);

        tokenType2 = "SOLOTC-"+UtilsClass.Random(6);
        String isResult2= soloSign.issueToken(PRIKEY1,tokenType2,"2000.87654321","发行token",ADDRESS1);
        assertThat(tokenType+"发行token错误",isResult, containsString("200"));
        assertThat(tokenType+"发行token错误",isResult2, containsString("200"));
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString("1000.123456789"));
        assertThat(tokenType+"查询余额不正确",response2, containsString("2000.87654321"));
    }
}
