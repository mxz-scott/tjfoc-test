package com.tjfintech.common.functionTest.PermissionTest;

import com.sun.deploy.util.StringUtils;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.TestBuilder;

import com.tjfintech.common.utils.MysqlOperation;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class TestPermission_Docker {
    public static final String ToolIP=PEER1IP;
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    TestBuilder testBuilder=TestBuilder.getInstance();
    APermfuncDocker pFunCt =new APermfuncDocker();
    TestPermission testPermission = new TestPermission();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    String glbCtName="t888";

    String def="Def:111";
    //"+ def+ Sys0+ Store0+ Docker0+ WVM0+ Mg0+ UTXO0+ "
    String Sys0="Sys:00000000"; //移除tx/search接口测试标记
    String Store0="Store:00";
    String Docker0 ="00000";
    String Docker1 ="11111";
    String WVM0="WVM:000";
    String Mg0="Mg:000000";
    String UTXO0="UTXO:0000000000";
    String full = "Sys:11111111Store:11Docker:11111WVM:111Mg:111111UTXO:1111111111";


    String toolPath="cd " + ToolPATH + ";";
    String ledger = (subLedger!="")?" -z " + subLedger:"";
    String exeCmd="./" + ToolTPName + " permission " + ledger;

    String peerIP=PEER1RPCPort;
    String sdkID= utilsClass.getSDKID();
    String preCmd=toolPath + exeCmd + " -p " + peerIP + " -d " + sdkID + " -m ";
    ArrayList<String> dockerList =new ArrayList();


    @Before
    public void beforeTest() throws Exception {
        //将管理工具id权限设置为999
        String resp = "";
        resp = shExeAndReturn(PEER1IP,
                toolPath + exeCmd + " -p " + peerIP + " -d " + utilsClass.getToolID(PEER1IP) + " -m 999");
        resp = shExeAndReturn(PEER1IP,
                toolPath + exeCmd + " -p " + peerIP + " -d " + utilsClass.getSDKID() + " -m 999");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(resp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        if(IMPPUTIONADD.isEmpty() ||
                !commonFunc.checkDataInMysqlDB(rSDKADD, "acc_multiaddr", "muladdr", IMPPUTIONADD)) {
            log.info("数据库中未查询到多签地址，重新读取并创建添加");
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.updatePubPriKey();
            beforeCondition.collAddressTest();
        }
    }

    @Test
    public void CheckDocker999or0()throws Exception{
        //权限设置为0 仅检查docker合约
        shExeAndReturn(PEER1IP,toolPath + exeCmd + " -p " + peerIP + " -d " + utilsClass.getSDKID() + " -m 0");
        sleepAndSaveInfo(SLEEPTIME,"权限0后等待sdk同步时间");

        assertEquals(Docker0,dockerPermCheck());


        //权限设置为999 仅检查docker合约
        String res = shExeAndReturn(PEER1IP,
                toolPath + exeCmd + " -p " + peerIP + " -d " + utilsClass.getSDKID() + " -m 999");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(res,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertEquals(Docker1,dockerPermCheck());
    }

    @Test
    public void ChkDocker1by1() throws Exception{

        String response = shExeAndReturn(ToolIP,preCmd + "999");//添加所有权限
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(response,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(3000); //等待sdk同步

        pFunCt.name="0220" + RandomUtils.nextInt(100000);
        glbCtName = pFunCt.name;
        pFunCt.version = "2.1";
        pFunCt.installContract();
        Thread.sleep(ContractInstallSleep);
        log.info("Contract install sleep time(ms): " + ContractInstallSleep);
        dockerList.add(pFunCt.name);
        log.info("docker list size: " + dockerList.size());

        pFunCt.initMobileTest();

        checkAllInterface("22",def + Sys0 + Store0 + Docker0 + WVM0 + Mg0 + UTXO0);

        //合约安装权限
        checkAllInterface("221",def + Sys0 + Store0 + "Docker:10000" + WVM0 + Mg0 + UTXO0);

        //合约交易权限
        checkAllInterface("223",def + Sys0 + Store0 + "Docker:01000" + WVM0 + Mg0 + UTXO0);

        //合约销毁权限
        checkAllInterface("222",def + Sys0 + Store0 + "Docker:00100" + WVM0 + Mg0 + UTXO0);

        //合约搜索
        checkAllInterface("224",def + Sys0 + Store0 + "Docker:00011" + WVM0 + Mg0 + UTXO0);

        //合约安装及合约交易
        checkAllInterface("221,223",def + Sys0 + Store0 + "Docker:11000" + WVM0 + Mg0 + UTXO0);


        for (String str : dockerList) {
            log.info("Destroy Docker:" + str);
            pFunCt.name=str;
            pFunCt.destroyContract();
            Thread.sleep(SLEEPTIME);
        }
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
    }

    public String dockerPermCheck()throws Exception{
        String permStr= "";
        pFunCt.name = "0215" + RandomUtils.nextInt(100000);
        pFunCt.version="2.0";
        permStr = permStr + pFunCt.installContract();//SDK发送合约安装交易请求
        log.info("docker permission:" + permStr);

        //perStr 若为"1"则表示存在合约安装权限，则等待合约安装
        if(permStr.contains("1")) {
            Thread.sleep(ContractInstallSleep);
            log.info("Contract install sleep time(ms): " + ContractInstallSleep);
            dockerList.add(pFunCt.name);
        }

        permStr = permStr + pFunCt.initMobileTest();//SDK发送合约交易请求

        permStr = permStr + pFunCt.destroyContract();//SDK发送合约删除交易请求

        log.info(glbCtName);
        permStr=permStr + pFunCt.searchByKey("Mobile0",glbCtName);//SDK发送按key查询请求
        permStr=permStr + pFunCt.searchByPrefix("Mo",glbCtName);//SDK发送按prefix查询请求
        return permStr; //should be a string with a length of 5
    }


    public void checkAllInterface(String right,String chkStr)throws Exception{

        String tempResp = shExeAndReturn(ToolIP,preCmd + right);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(tempResp,"mg"),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        //权限更新后查询检查生效与否
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        //String cmd1="cd zll;ls";//替换为权限命令
        shell1.execute(toolPath + "./" + ToolTPName + " getpermission -p " + PEER1RPCPort + ledger);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String resp = StringUtils.join(stdout,"\n");
        log.info(" +  +  +  +  +  +  + :" + resp);
        if(right=="999")
        {
            assertEquals(resp.contains(fullPerm),true);
//            assertThat(resp,anyOf(containsString(fullPerm),containsString(fullPerm2)));
        }else
            assertEquals(resp.contains("[" + right.replace(","," ") + "]"),true);

        if(right.equals("0"))
        {
            Thread.sleep(3000);
        }
        //确认获取权限无异常后 增加sleep时间 此时间最好大于sdk从链上拉取权限列表时间
        Thread.sleep(5000);
        String permList="";
        permList=permList +  "Def:";
        //默认开启接口检查
        permList=permList +  testPermission.defaultSup(); //must be def +  "" a length of 4

        permList=permList +  "Sys:";
        //系统类交易检查
        permList=permList +  testPermission.sysPermCheck(); //Eg. "Sys:111111111" a length of 9

        permList=permList +  "Store:";
        //存证类交易检查
        permList=permList +  testPermission.storePermCheck();//Eg. "Store:11" a length of 2

        permList=permList +  "Docker:";
        //合约交易类检查
        permList=permList +  dockerPermCheck();//Eg. "Docker:11111" a length of 5

        permList = permList +  "WVM:";
        //wvm合约交易类检查
        permList = permList +  testPermission.wvmPermCheck();//Eg. "WVM:111" a length of 3

        permList=permList +  "Mg:";
        //管理类接口
        permList=permList +  testPermission.collManageCheck();//Eg. "Mg:111111" a length of 6

        permList=permList +  "UTXO:";
        //UTXO类交易权限检查
        permList=permList +  testPermission.utxoPermCheck();//Eg. "UTXO:1111111111" a length of 10
        //return permList; //Eg. def +  "Sys:111111111Store:11Docker:11111Mg:111111UTXO:1111111111" a length of 33
        log.info("Right:" +  right +  " with Check Str:" +  permList);
        assertThat(permList, containsString(chkStr));


    }

    @After
    public void resetPermission() throws Exception{
        BeforeCondition bf=new BeforeCondition();
        bf.setPermission999();
    }
}
