package com.tjfintech.common.functionTest.contract;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetSDKPerm999;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.utils.FileOperation.setSDKConfigByShell;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class WVMContractWithVersionTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    public String category="wvm";
    public String caller=""; // 这个字段旧版本中不能为空
    FileOperation fileOper = new FileOperation();
    public String orgName = "TestExample";
    public String accountA = "A";
    public String accountB = "B";
    public int amountA = 50;
    public int amountB = 60;
    public int transfer = 30;
    public String wvmFile = "wvm";

    @BeforeClass
    public static void setVersion(){
        if(wvmVersion.isEmpty()){
            wvmVersion = "2.0.0";
        }
    }

    /***
     * 跨合约调用 权限赋值时仅赋给SDK和另一个调用的合约
     * 管理工具命令 contract modifyfunc 来进程调用权限修改
     * @throws Exception
     */
//    @Test
    public void TestCrossInvoke_PermitSDKAndContract() throws Exception{
        if(!wvmVersion.isEmpty())  {
            log.info("20210310 20200805当前不支持带版本号的跨合约调用，问题单1003270");
            return;}
        String ctName = "L_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);

        //安装需要跨合约调用的合约 wvm_cross.txt
        String response2 = wvmInstallTest("wvm_cross.txt","");
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
        String ctHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("name");

                commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //使用管理工具给第一个合约的initAccount方法添加第二个合约名，赋予其及SDK调用权限。
        MgToolCmd mgToolCmd = new MgToolCmd();
        String permitStr = utilsClass.getSDKID().trim() + "," + ctHash2;
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,ctHash,"","initAccount",permitStr);
        sleepAndSaveInfo(SLEEPTIME/2);

        //跨合约调用合约内的正确的方法 initAccount方法
        String response3 = invokeNew(ctHash2,"CrossInitAccount",
                ctHash,"initAccount","\"[\"C\",123]\"");//初始化账户A 账户余额50
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");


        commonFunc.sdkCheckTxOrSleep(txHash3,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        chkTxDetailRsp("200",txHash3);
        String response7 = query(ctHash,"BalanceTest","C");//获取账户A账户余额
        assertEquals("123",JSONObject.fromObject(response7).getJSONObject("data").getString("result"));


        //跨合约调用，被调用合约内的不存在的方法，正确的参数格式
        String response4 = invokeNew(ctHash2,"CrossInitAccount",
                ctHash,"initAnt","\"[\"C\",123]\"");//初始化账户A 账户余额50
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");


        commonFunc.sdkCheckTxOrSleep(txHash4,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        chkTxDetailRsp("404",txHash4);

        //跨合约调用合约内的存在的方法，错误的参数格式
        String response5 = invokeNew(ctHash2,"CrossInitAccount",
                ctHash,"initAccount","[\"C\",123]");//初始化账户A 账户余额50

        //pp分支 v2 会直接返回错误提示
        assertEquals(true,response5.contains("不支持的合约参数格式"));
//        String txHash5 = JSONObject.fromObject(response5).getJSONObject("data").getString("txId");
//
//
//        commonFunc.sdkCheckTxOrSleep(txHash5,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        chkTxDetailRsp("404",txHash5);
    }

//    @Test
    public void TestCrossInvoke_PermitEveryone() throws Exception{
        if(!wvmVersion.isEmpty())  {
            log.info("20210310 20200805当前不支持带版本号的跨合约调用，问题单1003270");
            return;}
        String ctName = "L_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);

        //安装需要跨合约调用的合约 wvm_cross.txt
        String response2 = wvmInstallTest("wvm_cross.txt","");
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
        String ctHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //使用管理工具给第一个合约的initAccount方法添加第二个合约名，赋予everyone调用权限。
        MgToolCmd mgToolCmd = new MgToolCmd();
        String permitStr = "";
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,ctHash,"","initAccount",permitStr);
        sleepAndSaveInfo(SLEEPTIME/2);

        //跨合约调用合约内的正确的方法 initAccount方法
        String response3 = invokeNew(ctHash2,"CrossInitAccount",
                ctHash,"initAccount","\"[\"C\",123]\"");//初始化账户A 账户余额50
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");


        commonFunc.sdkCheckTxOrSleep(txHash3,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        chkTxDetailRsp("200",txHash3);
        String response7 = query(ctHash,"BalanceTest","C");//获取账户A账户余额
        assertEquals("123",JSONObject.fromObject(response7).getJSONObject("data").getString("result"));


        //跨合约调用，被调用合约内的不存在的方法，正确的参数格式
        String response4 = invokeNew(ctHash2,"CrossInitAccount",
                ctHash,"initAnt","\"[\"C\",123]\"");//初始化账户A 账户余额50
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");


        commonFunc.sdkCheckTxOrSleep(txHash4,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        chkTxDetailRsp("404",txHash4);

        //跨合约调用合约内的存在的方法，错误的参数格式
        String response5 = invokeNew(ctHash2,"CrossInitAccount",
                ctHash,"initAccount","[\"C\",123]");//初始化账户A 账户余额50

        //pp分支 v2 会直接返回错误提示
        assertEquals(true,response5.contains("不支持的合约参数格式"));
    }

    /**
     * 默认合约仅给sdk有调用权限 合约无权限
     * @throws Exception
     */
//    @Test
    public void TestCrossInvoke_PermitSDKOnly() throws Exception{
        if(!wvmVersion.isEmpty())  {
            log.info("20210310 20200805当前不支持带版本号的跨合约调用，问题单1003270");
            return;}
        String ctName = "L_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);

        //安装需要跨合约调用的合约 wvm_cross.txt
        String response2 = wvmInstallTest("wvm_cross.txt","");
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
        String ctHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //跨合约调用合约内的正确的方法 initAccount方法 合约无调用权限故交易失败不会上链
        String response3 = invokeNew(ctHash2,"CrossInitAccount",
                ctHash,"initAccount","\"[\"C\",123]\"");//初始化账户A 账户余额50
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txHash3,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        chkTxDetailRsp("404",txHash3);
    }

    /**
     * 默认合约仅给合约调用权限 sdk无权限
     * @throws Exception
     */
//    @Test
    public void TestCrossInvoke_PermitContractOnly() throws Exception{
        if(!wvmVersion.isEmpty())  {
            log.info("20210310 20200805当前不支持带版本号的跨合约调用，问题单1003270");
            return;}
        String ctName = "L_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        wvmVersion = "1.0.1." + Random(3);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);

        //安装需要跨合约调用的合约 wvm_cross.txt
        String response2 = wvmInstallTest("wvm_cross.txt","");
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
        String ctHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //使用管理工具给第一个合约的initAccount方法添加第二个合约名，赋予其调用权限。
        MgToolCmd mgToolCmd = new MgToolCmd();
        String permitStr = ctHash2;
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,ctHash,wvmVersion,"initAccount",permitStr);
        sleepAndSaveInfo(SLEEPTIME/2);

        //跨合约调用合约内的正确的方法 initAccount方法 合约无调用权限故交易失败不会上链
        String response3 = invokeNew(ctHash2,"CrossInitAccount",
                ctHash,"initAccount","\"[\"C\",123]\"");//初始化账户A 账户余额50
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txHash3,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        chkTxDetailRsp("200",txHash3);

        //原合约SDK调用合约内的交易
        String response4 = invokeNew(ctHash,"initAccount",accountA,amountA);//初始化账户A 账户余额50
        assertEquals("400", JSONObject.fromObject(response4).getString("state"));
        assertEquals(true, JSONObject.fromObject(response4).getString("message").contains("You don't have permission to call this method"));
    }

    @Test
    public void TC1774_1784_1786_testContract() throws Exception{

        String ctName="A_" + sdf.format(dt)+ RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile +"_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        commonFunc.verifyTxDetailField(txHash1,"wvm_install", "2", "3", "40");
        commonFunc.verifyTxRawField(txHash1, "2", "3", "40");
        commonFunc.verifyRawFieldMatch(txHash1);

        //调用合约内的交易
        String response2 = invokeNew(ctHash,"initAccount",accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        String response3 = invokeNew(ctHash,"initAccount",accountB,amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        commonFunc.verifyTxDetailField(txHash2,"wvm_invoke", "2", "3", "42");
        commonFunc.verifyTxRawField(txHash2, "2", "3", "42");
        commonFunc.verifyRawFieldMatch(txHash2);

        String response4 = invokeNew(ctHash,"transfer",accountA,accountB,transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //查询余额invoke接口
        String response5 = invokeNew(ctHash,"BalanceTest",accountA);//获取账户A账户余额
        String txHash5 = JSONObject.fromObject(response5).getJSONObject("data").getString("txId");

        String response6 = invokeNew(ctHash,"BalanceTest",accountB);//获取账户A账户余额
        String txHash6 = JSONObject.fromObject(response6).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //查询余额query接口 交易不上链 //query接口不再显示交易hash
        String response7 = query(ctHash,"BalanceTest",accountA);//获取转账后账户A账户余额
//        String txHash7 = JSONObject.fromObject(response7).getJSONObject("data").getString("txId");
        assertEquals(Integer.toString(amountA-transfer),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = query(ctHash,"BalanceTest",accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(amountB+transfer),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        //销毁wvm合约
        String response9 = wvmDestroyTest(ctHash);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        commonFunc.verifyTxDetailField(txHash9,"wvm_destroy", "2", "3", "41");
        commonFunc.verifyTxRawField(txHash9, "2", "3", "41");
        commonFunc.verifyRawFieldMatch(txHash9);

        String response10 = query(ctHash,"BalanceTest",accountB);//获取账户B账户余额 报错

        int state = JSONObject.fromObject(response10).getInt("state");

        assertEquals("400",JSONObject.fromObject(response10).getString("state"));
        assertThat(JSONObject.fromObject(response10).getString("message"),
                containsString("This version[" + wvmVersion +  "] of the smart contract is destroyed"));

        // 销毁后会提示找不到合约文件 500 error code

        chkTxDetailRsp("200",txHash1,txHash2,txHash3,txHash4,txHash9);
        chkTxDetailRsp("404",txHash5,txHash6);  //因合约实际实现并未返回success 20200907 开发确认针对此规则交易不再上链，故做此检查
    }

    @Test
    public void TC1779_DiffInternalContractName() throws Exception{
        String ctName = "B_" + sdf.format(dt)+ RandomUtils.nextInt(100000);

        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");

        //再安装一个不同合约名 相同合约内容的合约
        ctName = "B_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String ctHash2 =installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");

        assertEquals(false,ctHash1.equals(ctHash2)); //确认两个hash不相同
    }

    @Test
    public void TC1775_InstallContractWithSameVersion() throws Exception{
        String ctName = "F_" + sdf.format(dt)+ RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);
        //第一次安装
        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1); //确认安装合约交易上链
        //重复使用同一个版本号进行合约安装 会安装失败
        String response2 = wvmInstallTest(wvmFile + "_temp.txt","");
        assertEquals(true,JSONObject.fromObject(response2).getString("message").contains(
                "This contract verion[" + wvmVersion  + "] is exist"));
    }

    @Test
    public void TC1776_InstallSameVersionAfterInvokeTransaction() throws Exception{
        String ctName = "G_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        //第一次安装 并调用init transfer接口
        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");

        //再次安装合约
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        assertEquals(true,JSONObject.fromObject(response1).getString("message").contains(
                "This contract verion[" + wvmVersion  + "] is exist"));
    }

    @Test
    public void TC1777_InstallDestroyedContract() throws Exception{
        String ctName = "H_" + sdf.format(dt)+ RandomUtils.nextInt(100000);

        //安装及销毁合约 未执行过合约内交易
        String ctHash1 = installDestroy(ctName,"");

        //销毁后再次使用相同的版本号进行合约安装 执行失败
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        assertEquals(true,JSONObject.fromObject(response1).getString("message").contains(
                "This contract verion[" + wvmVersion  + "] is exist"));

        //使用新的版本进行合约安装
        wvmVersion = "1.0.2." + Random(3);
        installDestroy(ctName,"");
    }

    @Test
    public void TC1778_MultiInstallDestroyedContract() throws Exception {
        String ctName = "I_" + sdf.format(dt) + RandomUtils.nextInt(100000);
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        for (int i = 0; i < 6; i++) {
            wvmVersion = "1.0.3." + i;
            installDestroy(ctName, "");
        }

        //多次安装销毁后再次多次多版本安装
        for (int i = 0; i < 6; i++) {
            wvmVersion = "1.0.3." + i;
            //再次安装合约后会得到合约hash：由Prikey和ctName进行运算得到
            String response1 = wvmInstallTest(wvmFile + "_temp.txt", "");
            assertEquals(true, JSONObject.fromObject(response1).getString("message").contains(
                    "This contract verion[" + wvmVersion  + "] is exist"));
        }

        wvmVersion = "1.0.4";
        //使用新版本进行安装合约
        String response1 = wvmInstallTest(wvmFile + "_temp.txt", "");
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
    }

    @Test
    public void TC1787_DestroyNotExistContract() throws Exception{
        String ctHash = "1234567890";
        //销毁不存在的合约
        String response1 = wvmDestroyTest(ctHash);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        chkTxDetailRsp("404",txHash1);
    }

    @Test
    public void TestDestroyVersionNotExistContract() throws Exception{
        String ctName = "J_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        chkTxDetailRsp("200",txHash1);

        //版本号设置为不存在的版本号
        wvmVersion  = wvmVersion + ".1";

        //销毁合约
        response1 = wvmDestroyTest(ctHash);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        chkTxDetailRsp("200",txHash1);
    }

    @Test
    public void TC1788_DestroyAndTransfer() throws Exception{
        String ctName = "J_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        chkTxDetailRsp("200",txHash1);
        //调用合约内的方法 init方法
        String response2 = invokeNew(ctHash,"initAccount",accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        String response3 = invokeNew(ctHash,"initAccount",accountB,amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //调用transfer和销毁一起
        String response4 = invokeNew(ctHash,"transfer",accountA,accountB,transfer);//A向B转30
        String response5 = wvmDestroyTest(ctHash);//销毁
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");
        String txHash5 = JSONObject.fromObject(response5).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

//        String response7 = query(ctHash,"BalanceTest",accountA);//获取账户A账户余额
//        String response7 = query(ctHash,"BalanceTest",accountA);//获取账户A账户余额
        //两笔交易可能打在两个区块中
        assertThat(JSONObject.fromObject(store.GetTxDetail(txHash4)).getString("state"),
                anyOf(containsString("200"),containsString("404")));
//        chkTxDetailRsp("404",txHash4);
        chkTxDetailRsp("200",txHash5);
    }

    @Test
    public void upgradeWVMContract()throws Exception{
        String ctName = "K_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");

        String wvmVerOld = wvmVersion;

        //升级合约 版本号变更
        wvmFile ="wvm_update";


        wvmVersion = wvmVersion.substring(0,wvmVersion.lastIndexOf("."))
                + (Integer.valueOf(wvmVersion.substring(wvmVersion.lastIndexOf(".") + 1)) + 1);
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash2 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");
        assertEquals(ctHash1,ctHash2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);
        //调用升级后合约内的方法 transfer方法 A->B转transfer/2
        String response4 = invokeNew(ctHash2,"transfer",accountA,accountB,transfer);//A向B转15

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        String response7 = query(ctHash1,"BalanceTest",accountA);//获取账户A账户余额
        assertEquals(Integer.toString(amountA-transfer-transfer/2),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = query(ctHash1,"BalanceTest",accountB);//获取账户A账户余额
        assertEquals(Integer.toString(amountB+transfer+transfer/2),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        wvmVersion = wvmVerOld;
        response7 = query(ctHash1,"BalanceTest",accountA);//获取账户A账户余额
        assertEquals(Integer.toString(amountA-transfer-transfer/2),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

    }
    @Test
    public void TC1815_1847_ConcurrentTransfer() throws Exception{
        String ctName = "L_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);
        //调用合约内的方法 init方法
        String response2 = invokeNew(ctHash,"initAccount",accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        String response3 = invokeNew(ctHash,"initAccount",accountB,amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //调用合约内的方法 transfer方法
        String response4 = invokeNew(ctHash,"transfer",accountA,accountB,transfer);//A向B转30
        String response5 = invokeNew(ctHash,"transfer",accountA,accountB,transfer+1);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String response7 = query(ctHash,"BalanceTest",accountA);//获取账户A账户余额
        assertEquals(Integer.toString(amountA-transfer),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));
    }


    @Test
    public void TC1783_1848_1846_InstallMultiCtConcurrent() throws Exception{
        ArrayList<String> ctHashList = new ArrayList<>();
        //安装及销毁合约 未执行过合约内交易
        for(int i = 0; i < 30; i++) {
            String ctName = "M_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
            // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
            fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);
            ctHashList.add(JSONObject.fromObject(wvmInstallTest(wvmFile + "_temp.txt","")).getJSONObject("data").getString("name"));
        }

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        for(String ctHash : ctHashList){
            String response7 = query(ctHash,"BalanceTest",accountA);//获取账户A账户余额
            assertEquals(Integer.toString(0),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));
        }

        for(String ctHash : ctHashList){
            String response8 = wvmDestroyTest(ctHash);//销毁
        }

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        for(String ctHash : ctHashList){
            String response1 = query(ctHash,"BalanceTest",accountA);//获取账户A账户余额
            assertEquals("400",JSONObject.fromObject(response1).getString("state"));

            assertThat(JSONObject.fromObject(response1).getString("message"),
                    containsString("This version[" + wvmVersion + "] of the smart contract is destroyed"));

        }
    }

//    @Test
    public void TC1780_1781_1782_DiffPriSameCt() throws Exception{
        String sdkIP = utilsClass.getIPFromStr(SDKADD);
        //替换SDK证书及auth目录下私钥 重启并给SDK赋权限
        shellExeCmd(sdkIP,killSDKCmd);  //停止sdk进程

        //修改SDK配置文件 第二个身份的SDK
        setSDKConfigByShell(sdkIP,"Rpc","TLSCaPath","\"\\\".\\/cert\\/ca.pem\"\\\"");
        setSDKConfigByShell(sdkIP,"Rpc","TLSCertPath","\"\\\".\\/cert\\/cert.pem\"\\\"");
        setSDKConfigByShell(sdkIP,"Rpc","TLSKeyPath","\"\\\".\\/cert\\/key.pem\"\\\"");
        setSDKConfigByShell(sdkIP,"Auth","KeyPath","\"\\\".\\/cert\\/key.pem\"\\\"");
        //替换sdk auth及tls目录下的证书 私钥文件
        //启动sdk 并赋权限
        shellExeCmd(sdkIP,startSDKCmd);
        SetSDKPerm999 setSDKPerm999 = new SetSDKPerm999();
        setSDKPerm999.test();

        //第一个身份的SDK

        String ctName = "E_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");


        //恢复SDK证书及auth目录下私钥 重启并给SDK赋权限
        shellExeCmd(sdkIP,killSDKCmd);  //停止sdk进程

        //修改SDK配置文件 第二个身份的SDK
        setSDKConfigByShell(sdkIP,"Rpc","TLSCaPath","\"\\\".\\/tls\\/ca.pem\"\\\"");
        setSDKConfigByShell(sdkIP,"Rpc","TLSCertPath","\"\\\".\\/tls\\/cert.pem\"\\\"");
        setSDKConfigByShell(sdkIP,"Rpc","TLSKeyPath","\"\\\".\\/tls\\/key.pem\"\\\"");
        setSDKConfigByShell(sdkIP,"Auth","KeyPath","\"\\\".\\/tls\\/key.pem\"\\\"");
        //替换sdk auth及tls目录下的证书 私钥文件
        //启动sdk 并赋权限
        shellExeCmd(sdkIP,startSDKCmd);
        setSDKPerm999.test();


        //使用不同私钥安装相同合约及内容的合约
        String ctHash2 = installInitTransfer(ctName,PRIKEY2,"initAccount","transfer","BalanceTest");

        assertEquals(false,ctHash1.equals(ctHash2)); //确认两个hash不相同

        //再安装不同私钥 相同合约名 不同合约内容的合约
        wvmFile = "wvm_update";
        String ctHash3 = installInitTransferUpdate(ctName,PRIKEY2,"initAccount","transfer","BalanceTest");
        assertEquals(false,ctHash1.equals(ctHash3)); //确认两个hash不相同


        //再安装一个不同合约名 相同合约内容的合约
        ctName = "C_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        wvmFile = "wvm2";
        orgName = "Test";
        String ctHash4 = installInitTransfer(ctName,PRIKEY2,"initBAccount","transferB","BalanceTestB");
        assertEquals(false,ctHash1.equals(ctHash4)); //确认两个hash不相同

//        shellExeCmd(sdkIP,killSDKCmd,resetSDKConfig,startSDKCmd);
//        sleepAndSaveInfo(5000,"等待SDK重新启动");
    }

//    @Test
//    public void TC1782_DiffPriSameCtDiffContent() throws Exception{
//        String ctName = "N_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
//
//        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");
//
//        //再安装不同私钥 相同合约名 不同合约内容的合约
//        wvmFile = "wvm_update";
//        String ctHash2 =installInitTransferUpdate(ctName,PRIKEY2,"initAccount","transfer","BalanceTest");
//        assertEquals(false,ctHash1.equals(ctHash2)); //确认两个hash不相同
//    }

    //@Test 当前实现方案有问题 未调整
    public void TC1790_upgradeWithDismatchPriKey() throws Exception{

        String ctName="A_" + sdf.format(dt)+ RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile +"_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //调用合约内的交易
        String response2 = invokeNew(ctHash,"initAccount",accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        String response3 = invokeNew(ctHash,"initAccount",accountB,amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String response4 = invokeNew(ctHash,"transfer",accountA,accountB,transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //查询余额invoke接口
        String response5 = invokeNew(ctHash,"BalanceTest",accountA);//获取账户A账户余额
        String txHash5 = JSONObject.fromObject(response5).getJSONObject("data").getString("txId");

        String response6 = invokeNew(ctHash,"BalanceTest",accountB);//获取账户A账户余额
        String txHash6 = JSONObject.fromObject(response6).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //查询余额query接口 交易不上链 //query接口不再显示交易hash
        String response7 = query(ctHash,"BalanceTest",accountA);//获取转账后账户A账户余额
//        String txHash7 = JSONObject.fromObject(response7).getJSONObject("data").getString("txId");
        assertEquals(Integer.toString(amountA-transfer),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = query(ctHash,"BalanceTest",accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(amountB+transfer),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        //使用不匹配的私钥进行合约升级
        wvmFile = "wvm_update";
        response1 = intallUpdateName(ctName,PRIKEY2);
        assertEquals(true,response1.contains("invalid prikey"));

        //非法操作后 不影响现有数据
        response7 = query(ctHash,"BalanceTest",accountA);//获取转账后账户A账户余额
        assertEquals(Integer.toString(amountA-transfer),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        response8 = query(ctHash,"BalanceTest",accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(amountB+transfer),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

    }

    public String installDestroy(String ctName,String Prikey)throws Exception{
        //当前示例合约仅存在三个方法：init ->method[0] transfer->method[1],getBalance->method[2]
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt",Prikey);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        chkTxDetailRsp("200",txHash1);

        //销毁wvm合约
        String response9 = wvmDestroyTest(ctHash1);
        String txHash2 = JSONObject.fromObject(response9).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash2);

        String response10 = query(ctHash1,"BalanceTest",accountB);//获取转账后账户B账户余额 报错

        // 销毁后会提示找不到合约文件 500 error code
        int state = JSONObject.fromObject(response10).getInt("state");

        if (state == 500 || state == 400){
            assertThat("500 or 400, both ok", containsString("500 or 400, both ok"));
        } else{
            assertThat("wrong state", containsString("500 or 400, both ok"));
        }
        assertThat(JSONObject.fromObject(response10).getString("message"),
                containsString("This version[" + wvmVersion + "] of the smart contract is destroyed"));
        return ctHash1;
    }

    public String installInitTransfer(String ctName,String Prikey,String...method)throws Exception{
        //当前示例合约仅存在三个方法：init ->method[0] transfer->method[1],getBalance->method[2]
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约名：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt",Prikey);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctName1 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        chkTxDetailRsp("200",txHash1);
        //调用合约内的方法 init方法
        String response2 = invokeNew(ctName1,method[0],accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        String response3 = invokeNew(ctName1,method[0],accountB,amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //调用合约内的方法 transfer方法
        String response4 = invokeNew(ctName1,method[1],accountA,accountB,transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        //调用合约内的方法 getBalance方法查询余额query接口 交易不上链
        String response7 = query(ctName1,method[2],accountA);//获取转账后账户A账户余额
        assertEquals(Integer.toString(amountA-transfer),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = query(ctName1,method[2],accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(amountB+transfer),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        return ctName1;
    }

    //此方法针对wvm_update.txt中的合约
    public String installInitTransferUpdate(String ctName,String Prikey,String...method)throws Exception{
        //当前示例合约仅存在三个方法：init ->method[0] transfer->method[1],getBalance->method[2]
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt",Prikey);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);
        //调用合约内的方法 init方法
        String response2 = invokeNew(ctHash,method[0],accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        String response3 = invokeNew(ctHash,method[0],accountB,amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //调用合约内的方法 transfer方法
        String response4 = invokeNew(ctHash,method[1],accountA,accountB,transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //调用合约内的方法 getBalance方法查询余额query接口 交易不上链
        String response7 = query(ctHash,method[2],accountA);//获取转账后账户A账户余额
        assertEquals(Integer.toString(amountA-transfer/2),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = query(ctHash,method[2],accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(amountB+transfer/2),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        return ctHash;
    }

    public void chkTxDetailRsp(String retCode,String...hashList){
        for(String hash : hashList) {
            log.info("Check Hash: " + hash);
            assertEquals(retCode,JSONObject.fromObject(store.GetTxDetail(hash)).getString("state"));
        }
    }


    public String intallUpdateName(String name,String priKey)throws Exception{
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, name);
        sleepAndSaveInfo(100,"文件操作后等待时间");

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        return wvmInstallTest(wvmFile + "_temp.txt",priKey);
    }

    @After
    public void resetParam()throws Exception{
        wvmFile = "wvm";
        orgName = "TestExample";
    }


    public String wvmInstallTest(String wvmfile,String Prikey) throws Exception {
        if(wvmfile == "") return contract.InstallWVM("",category,Prikey);

        String filePath = testDataPath + "wvm/" + wvmfile;
        log.info("filepath "+ filePath);
        String file = utilsClass.readInput(filePath).toString().trim();
        String data = utilsClass.encryptBASE64(file.getBytes()).replaceAll("\r\n", "");//BASE64编码
        log.info("base64 data: " + data);
        String response=contract.InstallWVM(data,category,Prikey);
        return response;
    }


    public String wvmDestroyTest(String cthash) throws Exception {
        String response = contract.DestroyWVM(cthash,category);
        return response;

    }


    public String invokeNew(String cthash, String method, Object... arg) throws Exception {
        List<Object> args = new LinkedList<>();
        for (Object obj:arg){
            args.add(obj);
        }
        String response = contract.Invoke(cthash, wvmVersion, category,method,caller, args);
        return response;
    }

    public String query(String cthash, String method, String... arg) throws Exception {
        List<Object> args = new LinkedList<>();
        for (Object obj:arg){
            args.add(obj);
        }
        String response = contract.QueryWVM(cthash, wvmVersion, category,method,caller,args);
        return response;
    }


}
