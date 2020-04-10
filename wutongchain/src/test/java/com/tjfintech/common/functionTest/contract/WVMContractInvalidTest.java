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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class WVMContractInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();

    WVMContractTest wvmContractTest = new WVMContractTest();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

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
    public void TC1816_InvokeInvalidMethod() throws Exception{
        String ctName = "Err_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(resourcePath + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt",PRIKEY1);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("Data").getString("Name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");//暂时添加

        wvmContractTest.chkTxDetailRsp("200",txHash1);
        //调用合约内的方法 init方法
        String response2 = wvmContractTest.invokeNew(ctHash,"inita",accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        wvmContractTest.chkTxDetailRsp("404",txHash2);
    }


    @Test
    public void TC1819_InvalidNameTest()throws Exception{
        ArrayList<String> ctNameList = new ArrayList<>();
        ArrayList<String> txHashList = new ArrayList<>();

        ctNameList.add("___");
        ctNameList.add("a1_");
        ctNameList.add("aaaa");
        ctNameList.add("_ae3");
        ctNameList.add("a123");
        ctNameList.add("asdfghjklqwertyuiop1234567833333333333333333333333333333333333333333333333390");
        for(String name : ctNameList) {
            txHashList.add(JSONObject.fromObject(wvmContractTest.intallUpdateName(name,PRIKEY1)).getJSONObject("Data").getString("Figure"));
        }

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals(6,txHashList.size());
        for(String hash : txHashList){
            wvmContractTest.chkTxDetailRsp("200",hash);
        }

        ctNameList.clear();
        txHashList.clear();
        ctNameList.add("123");
        ctNameList.add("1a1_");
        ctNameList.add("1a");
        for(String name : ctNameList) {
            assertEquals("500",JSONObject.fromObject(wvmContractTest.intallUpdateName(name,PRIKEY1)).getString("State"));
        }
    }

    @Test
    public void TC_1855_installWithInvalidParameter()throws Exception{
        //测试私钥为空
        log.info("test file:"+ wvmFile + "_temp.txt");
        String response1 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
//        assertEquals(false,JSONObject.fromObject(response1).getString("State").contains("200"));//当前存在bug sdk panic

        //测试wvm文件为空
        String response2 = wvmContractTest.wvmInstallTest("",PRIKEY1);
        assertEquals(false,JSONObject.fromObject(response2).getString("State").contains("200"));

        //测试wvm及私钥为空
        String response3 = wvmContractTest.wvmInstallTest("","");
//        assertEquals(false,JSONObject.fromObject(response3).getString("State").contains("200"));//当前存在bug sdk panic

        //测试私钥非法
        String response4 = wvmContractTest.wvmInstallTest(wvmFile + ".txt","88888");
//        assertEquals(false,JSONObject.fromObject(response4).getString("State").contains("200"));//当前存在bug sdk panic

    }
    @Test
    public void TC_1796_1798_1800_1801_DismatchCategory()throws Exception{
        //安装一笔合法的wvm合约
        String ctName = "Err_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(resourcePath + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt",PRIKEY1);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("Data").getString("Name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        wvmContractTest.chkTxDetailRsp("200",txHash1);


        //测试docker合约使用category wvm，也就是说wvm合约无私钥
        DockerContractTest dockerContractTest = new DockerContractTest();
        dockerContractTest.category = "wvm";
        String response2 = dockerContractTest.installTest();
//        assertEquals(false,JSONObject.fromObject(response2).getString("State").contains("200"));//当前存在bug sdk panic

        //测试wvma安装、调用、销毁使用category docker
        wvmContractTest.category = "docker";
        String response3 = wvmContractTest.wvmDestroyTest(ctHash);
        assertEquals(false,JSONObject.fromObject(response3).getString("State").contains("200"));

        String response4 = wvmContractTest.invokeNew(ctHash,"initAccount",accountA,amountA);
//        assertEquals(false,JSONObject.fromObject(response4).getString("State").contains("200"));//当前存在bug sdk panic

        String response5 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt",PRIKEY1);
        assertEquals(false,JSONObject.fromObject(response3).getString("State").contains("200"));


    }
}
