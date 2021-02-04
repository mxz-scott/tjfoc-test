package com.tjfintech.common.functionTest.contract;

import com.tjfintech.common.BeforeCondition;
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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class WVMContractTest_UpgradeTestOnly {
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
    public int transfer = 30;
    public String wvmFile = "wvm";

    @BeforeClass
    public  static void setPermFull()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.updatePubPriKey();
    }

    @Test
    public void TC1774_1784_1786_testContract() throws Exception{

        String ctName="A_" + sdf.format(dt)+ RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile +"_temp.txt",PRIKEY1);
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

        if (state == 500 || state == 400){
            assertThat("500 or 400, both ok", containsString("500 or 400, both ok"));
        } else{
            assertThat("wrong state", containsString("500 or 400, both ok"));
        }

        if (wvmVersion != ""){
            assertThat(JSONObject.fromObject(response10).getString("message"),containsString("This version[1.0.0] of the smart contract is destroyed"));
        }else{
            assertThat(JSONObject.fromObject(response10).getString("message"),containsString("This smart contract is destroyed"));
        }

        // 销毁后会提示找不到合约文件 500 error code

        chkTxDetailRsp("200",txHash1,txHash2,txHash3,txHash4,txHash9);
        chkTxDetailRsp("404",txHash5,txHash6);  //因合约实际实现并未返回success 20200907 开发确认针对此规则交易不再上链，故做此检查

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
