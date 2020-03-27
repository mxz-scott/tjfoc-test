package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.FileOperation.getSDKConfigValueByShell;
import static com.tjfintech.common.utils.FileOperation.setSDKConfigByShell;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SDKCmdTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    /**
     * 当前支持命令
     root@ubuntu:~/zll/chain2.0.1/sdk# ./sdk -h
     tjfoc sdk

     Usage:
     sdk [flags]
     sdk [command]

     Available Commands:
     encrypt     SDK encrypt
     getid       SDK pid
     help        Help about any command
     start       SDK startup
     stop        SDK stop
     version     Current Version

     Flags:
     -h, --help   help for sdk

     Use "sdk [command] --help" for more information about a command.
     */

    @Before
    public void setSDKconfig()throws Exception{
        shellExeCmd(utilsClass.getIPFromStr(SDKADD),killSDKCmd,resetSDKConfig,startSDKCmd);
    }


    String exePre = "cd "+ SDKPATH + ";./" + SDKTPName;
    String remoteIP = utilsClass.getIPFromStr(SDKADD);

    @Test
    public void testencrypt()throws Exception{

        shExeAndReturn(remoteIP,killSDKCmd);
        sleepAndSaveInfo(100,"停止SDK");
        String dbInfo = getSDKConfigValueByShell(remoteIP,"Wallet","DBPath");
        log.info("Test DB info: " + dbInfo);
        String encryptInfo = shExeAndReturn(remoteIP,"cd " + SDKPATH + ";./" + SDKTPName + " encrypt -p " + dbInfo);
        log.info("Test DB Encrypt Info: " + "\"\\\""+ encryptInfo.trim() + "\"\\\"");
        sleepAndSaveInfo(100,"加密数据库DB信息");
        setSDKConfigByShell(remoteIP,"Wallet","DBPath", "\"\\\""+ encryptInfo.trim() + "\"\\\"");

        shExeAndReturn(remoteIP,startSDKCmd);
        sleepAndSaveInfo(SLEEPTIME/2,"等待sdk启动");
        assertEquals("200", JSONObject.fromObject(store.CreateStore("test")).getString("State"));

        //结束后重新设置回明文配置
        shellExeCmd(remoteIP,killSDKCmd,resetSDKConfig,startSDKCmd);
        sleepAndSaveInfo(SLEEPTIME/2,"等待sdk启动");
        assertEquals("200", JSONObject.fromObject(store.CreateStore("test")).getString("State"));
    }

    @Test
    public void testGetID()throws Exception{
        String resp1 = shExeAndReturn(remoteIP,exePre + " getid");
        assertEquals(true,resp1.toLowerCase().contains("sdk_id:"));
    }


    @Test
    public void testStart()throws Exception{
        String resp = shExeAndReturn(remoteIP,killSDKCmd);
        String resp2 = shExeAndReturn(remoteIP,"ps -ef |grep " + SDKTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(true,resp2.trim().isEmpty()); //确认进程未启动

        String resp1 = shExeAndReturn(remoteIP,tmuxSessionSDK + "'./" + SDKTPName + " start -d' ENTER"); //使用start -d方式启动
        sleepAndSaveInfo(SLEEPTIME,"start -d 启动节点进程");

        resp2 = shExeAndReturn(remoteIP,"ps -ef |grep " + SDKTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(false,resp2.trim().isEmpty()); //确认进程启动
        String resp3 = shExeAndReturn(remoteIP,tmuxSessionSDK + "'./" + SDKTPName + " stop' ENTER");//使用stop命令停止节点（20190909目前仅支持停止采用start -d启动命令）
        sleepAndSaveInfo(100,"stop进程");

        resp2 = shExeAndReturn(remoteIP,"ps -ef |grep " + SDKTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(true,resp2.trim().isEmpty());

        resp = shExeAndReturn(remoteIP,startSDKCmd);
        sleepAndSaveInfo(SLEEPTIME/2,"等待sdk启动");
        assertEquals("200", JSONObject.fromObject(store.CreateStore("test")).getString("State"));
    }

    //@Test
    public void testPeerStop()throws Exception{
        String resp2 = shExeAndReturn(remoteIP,"ps -ef |grep " + SDKTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(false,resp2.trim().isEmpty());
        String resp1 = shExeAndReturn(remoteIP,exePre + " stop");
        resp2 = shExeAndReturn(remoteIP,"ps -ef |grep " + SDKTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(true,resp2.trim().isEmpty());
    }

    @Test
    public void testVersion()throws Exception{
        String resp1 = shExeAndReturn(remoteIP,exePre + " version");
        assertEquals(true,resp1.toLowerCase().contains("sdk version"));
        assertEquals(true,resp1.toLowerCase().contains("go version"));
        assertEquals(true,resp1.toLowerCase().contains("build time"));
        assertEquals(true,resp1.toLowerCase().contains("decimals"));
        assertEquals(true,resp1.toLowerCase().contains("run  mode"));
    }


    @Test
    public void testHelp()throws Exception{
        Shell shell1=new Shell(remoteIP,USERNAME,PASSWD);
        shell1.execute(exePre + " help");
        ArrayList<String> stdout = shell1.getStandardOutput();
        //String resp1 = shExeAndReturn(remoteIP,exePre + " help");
        //log.info(resp1);
        boolean bCount = false;
        int CmdCount = 0;
        for (String str:stdout) {
            if (str.trim().isEmpty()) continue;

            log.info(str);
            if (str.contains("Available")) {
                bCount = true;
                continue;
            }
            if (str.contains("Flags:")) break;
            if(bCount) CmdCount ++;
        }
        assertEquals(6,CmdCount);
        }


//        @AfterClass
        public static void resetSDKConfig()throws Exception{
            UtilsClass utilsClassTemp = new UtilsClass();
            shellExeCmd(utilsClassTemp.getIPFromStr(SDKADD),killSDKCmd,resetSDKConfig,startSDKCmd);
        }
}
