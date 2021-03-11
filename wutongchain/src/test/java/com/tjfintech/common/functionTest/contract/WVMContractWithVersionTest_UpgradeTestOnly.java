package com.tjfintech.common.functionTest.contract;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class WVMContractWithVersionTest_UpgradeTestOnly {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    public String category="wvm";
    public String caller="test";
    FileOperation fileOper = new FileOperation();
    public String orgName = "TestExample";
    public String accountA = "A";
    public String accountB = "B";
    public int amountA = 50;
    public int amountB = 60;
    public int transfer = 10;
    public String wvmFile = "wvm";

    @BeforeClass
    public static void setVersion(){
        if(wvmVersion.isEmpty()){
            wvmVersion = "2.0.0";
        }
    }

    //调用不存在的版本合约
    @Test
    public void invokeNotExistVerWVMTest() throws Exception{
        wvmVersion = "1.0";
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

        String response2 = invokeNew(ctHash,"initAccount",accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1,txHash2);  //确认交易会上链

        //调用不存在的版本号
        wvmVersion = "2.0";
        String response3 = invokeNew(ctHash,"initAccount",accountB,amountB);//初始化账户B 账户余额60

        int state = JSONObject.fromObject(response3).getInt("state");

        if (state == 500 || state == 400){
            assertThat("500 or 400, both ok", containsString("500 or 400, both ok"));
        } else{
            assertThat("wrong state", containsString("500 or 400, both ok"));
        }

        assertEquals(true,response3.contains("This version[2.0] does not exist for smart contract"));

        String response5 = wvmDestroyTest(ctHash);//销毁

    }

    //与下面的用例case重复

//    @Test
    public void upgradeWVMContractWithVersion()throws Exception{
        wvmVersion = "1.1";
        String ctName = "K_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");

        //升级合约
        wvmVersion = "1.2";
        wvmFile ="wvm_update";
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
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
        assertEquals(Integer.toString(amountA - transfer - transfer/2),
                JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = query(ctHash1,"BalanceTest",accountB);//获取账户A账户余额
        assertEquals(Integer.toString(amountB + transfer + transfer/2),
                JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        String response5 = wvmDestroyTest(ctHash1);//销毁
    }

    @Test
    public void upgradeWVMContractWithVersion02()throws Exception{
        //20210311 与开发确认 带版本的合约 如果升级后 则不允许使用非最新版本的合约 进行任何上链和非上链操作
        wvmVersion = "2.1";
        String ctName = "K_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");

        //升级合约
        wvmVersion = "2.2";
        wvmFile ="wvm_update";
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash2 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");
        assertEquals(ctHash1,ctHash2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);


        wvmVersion = "2.2";
        //调用升级后合约内的方法 transfer方法 A->B转transferAmount/2
        String response5 = invokeNew(ctHash2,"transfer",accountA,accountB,transfer);//A向B转15

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        String response = query(ctHash1,"BalanceTest",accountA);//获取账户A账户余额
        assertEquals(Integer.toString(amountA - transfer - transfer/2),
                JSONObject.fromObject(response).getJSONObject("data").getString("result"));

        String response8 = query(ctHash1,"BalanceTest",accountB);//获取账户A账户余额
        assertEquals(Integer.toString(amountB + transfer + transfer/2),
                JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        wvmVersion = "2.1";
        //调用升级前合约内的方法 transfer方法 A->B转transfer
        String response4 = invokeNew(ctHash2,"transfer",accountA,accountB,transfer);//A向B转15
        assertEquals("400",JSONObject.fromObject(response4).getString("state"));
        assertEquals(true,JSONObject.fromObject(response4).getString("message").contains(
                "This version[" + wvmVersion + "] is not newest"));

        String response7 = query(ctHash1,"BalanceTest",accountA);//获取账户A账户余额
        assertEquals("400",JSONObject.fromObject(response7).getString("state"));
        assertEquals(true,JSONObject.fromObject(response7).getString("message").contains(
                "This version[" + wvmVersion + "] is not newest"));



        wvmVersion = "2.2";
        
        response = query(ctHash1,"BalanceTest",accountA);//获取账户A账户余额
        assertEquals(Integer.toString(amountA - transfer - transfer/2),
                JSONObject.fromObject(response).getJSONObject("data").getString("result"));

        response8 = query(ctHash1,"BalanceTest",accountB);//获取账户A账户余额
        assertEquals(Integer.toString(amountB + transfer + transfer/2),
                JSONObject.fromObject(response8).getJSONObject("data").getString("result"));
        String response10 = wvmDestroyTest(ctHash1);//销毁
    }

    /***
     * //20210311 与开发确认 带版本的合约 如果升级后 则不允许使用非最新版本的合约 进行任何上链和非上链操作
     * 不孕多版本并发
     * @throws Exception
     */
    @Test
    public void ConcurrentTransferWithDiffVersion() throws Exception{
        wvmVersion = "3.1";
        String ctName = "K_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        //A 50 B 60   A->B 10 A 40 B 70
        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");

        //升级合约
        wvmVersion = "3.2";
        wvmFile ="wvm_update";
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash2 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");
        assertEquals(ctHash1,ctHash2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);

        wvmVersion = "3.1";
        //调用升级前合约内的方法 transfer方法 A->B转transfer
        String response7 = query(ctHash1,"BalanceTest",accountA);//获取账户A账户余额
        String response4 = invokeNew(ctHash2,"transfer",accountA,accountB,transfer);//A向B转15

        wvmVersion = "3.2";
        //调用升级后合约内的方法 transfer方法 A->B转transferAmount/2
        String response5 = invokeNew(ctHash2,"transfer",accountA,accountB,transfer);//A向B转15

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        //检查旧版本不支持调用
        assertEquals("400",JSONObject.fromObject(response7).getString("state"));
        assertEquals(true,JSONObject.fromObject(response7).getString("message").contains(
                "This version[" + wvmVersion + "] is not newest"));

        assertEquals("400",JSONObject.fromObject(response4).getString("state"));
        assertEquals(true,JSONObject.fromObject(response4).getString("message").contains(
                "This version[" + wvmVersion + "] is not newest"));


        String response = query(ctHash1,"BalanceTest",accountA);//获取账户A账户余额
        assertEquals(Integer.toString(amountA - transfer - transfer/2),
                JSONObject.fromObject(response).getJSONObject("data").getString("result"));

        String response8 = query(ctHash1,"BalanceTest",accountB);//获取账户A账户余额
        assertEquals(Integer.toString(amountB + transfer + transfer/2),
                JSONObject.fromObject(response8).getJSONObject("data").getString("result"));
        String response10 = wvmDestroyTest(ctHash1);//销毁
    }

    @Test
    public void MultiVersionCtDestory() throws Exception{
        wvmVersion = "4.1";
        String ctName = "K_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String ctHash1 = installInitTransfer(ctName,"","initAccount","transfer","BalanceTest");

        //升级合约
        wvmVersion = "4.2";
        wvmFile ="wvm_update";
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash2 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");
        assertEquals(ctHash1,ctHash2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        chkTxDetailRsp("200",txHash1);

        String response10 = wvmDestroyTest(ctHash1);//销毁

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        //调用升级前合约内的方法 transfer方法 A->B转transfer
        String response4 = invokeNew(ctHash2,"transfer",accountA,accountB,transfer);//A向B转15
        if(response4.contains("\"state\":200")) {
            String txHash2 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");
            sleepAndSaveInfo(SLEEPTIME);
            chkTxDetailRsp("404",txHash2);
        }
        else {
            int state = JSONObject.fromObject(response4).getInt("state");

            if (state == 500 || state == 400){
                assertThat("500 or 400, both ok", containsString("500 or 400, both ok"));
            } else{
                assertThat("wrong state", containsString("500 or 400, both ok"));
            }
            assertEquals(true, response4.contains("of the smart contract is destroyed"));
        }
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
        assertThat(JSONObject.fromObject(response10).getString("message"),containsString("no such file or directory")); //销毁后会提示找不到合约文件 500 error code

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
